package com.avbinvest.company.service;

import com.avbinvest.company.dto.CompanyRequestDTO;
import com.avbinvest.company.dto.CompanyResponseDTO;
import com.avbinvest.company.dto.UserDTO;
import com.avbinvest.company.exceptions.CompanyNotFoundException;
import com.avbinvest.company.exceptions.ConflictException;
import com.avbinvest.company.exceptions.RestRequestFailedException;
import com.avbinvest.company.feignClient.UserClient;
import com.avbinvest.company.module.Company;
import com.avbinvest.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.avbinvest.company.util.CompanyConverter.*;

/**
 * Service implementation for managing companies.
 * <p>
 * Provides CRUD operations for companies and handles business logic related to
 * company creation, updates, deletion, and employee management.
 * Integrates with user-service via Feign client to fetch and update employee data.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserClient userClient;

    /**
     * Creates a new company based on the provided DTO.
     * Validates uniqueness of company name before saving.
     *
     * @param dto the company data transfer object containing creation details
     * @return the created company details along with its employees
     * @throws ConflictException if a company with the same name already exists
     */
    @Override
    public CompanyResponseDTO createCompany(CompanyRequestDTO dto) {
        log.info("[CompanyService] Creating company with name: {}", dto.getName());

        checkCompanyNameConflict(dto.getName());

        Company company = companyRepository.save(convertDtoToEntity(dto));
        log.info("[CompanyService] Company created with id: {}", company.getId());

        List<UserDTO> users = fetchUsersSafe(company.getEmployeeIds());

        return convertEntityToDto(company, users);
    }

    /**
     * Updates an existing company identified by its ID.
     * Performs name conflict check if the name is changed.
     *
     * @param id  the company ID to update
     * @param dto the company data transfer object containing update details
     * @return the updated company details along with its employees
     * @throws CompanyNotFoundException if company with given ID does not exist
     * @throws ConflictException        if the new company name conflicts with another company
     */
    @Override
    public CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO dto) {
        log.info("[CompanyService] Updating company with id: {}", id);
        Company company = getCompanyOrThrow(id);

        if (dto.getName() != null) {
            checkCompanyNameConflict(dto.getName(), id);
        }

        patchCompany(company, dto);

        Company updatedCompany = companyRepository.save(company);
        log.info("[CompanyService] Company updated: {}", updatedCompany.getId());

        List<UserDTO> users = fetchUsersSafe(company.getEmployeeIds());
        return convertEntityToDto(updatedCompany, users);
    }

    /**
     * Retrieves a company by its ID.
     * Optionally includes detailed employee information.
     *
     * @param id               the company ID to fetch
     * @param includeEmployees if true, includes employee details; otherwise, returns empty list for employees
     * @return the company details with optional employees
     * @throws CompanyNotFoundException if company with given ID does not exist
     */
    @Override
    public CompanyResponseDTO getCompanyById(Long id, boolean includeEmployees) {
        log.info("[CompanyService] Fetching company with id: {}", id);

        Company company = getCompanyOrThrow(id);
        List<UserDTO> users = includeEmployees ? fetchUsersSafe(company.getEmployeeIds()) : List.of();

        return convertEntityToDto(company, users);
    }

    /**
     * Retrieves all companies.
     * Optionally includes detailed employee information for each company.
     *
     * @param includeEmployees if true, includes employee details; otherwise, employees list will be empty
     * @return list of all companies with optional employees
     */
    @Override
    public List<CompanyResponseDTO> getAllCompanies(boolean includeEmployees) {
        log.info("[CompanyService] Fetching all companies with includeEmployees={}", includeEmployees);

        List<Company> companies = companyRepository.findAll();

        List<CompanyResponseDTO> result = new ArrayList<>(companies.size());

        for (Company company : companies) {
            List<UserDTO> users = includeEmployees ? fetchUsersSafe(company.getEmployeeIds()) : List.of();
            result.add(convertEntityToDto(company, users));
        }
        return result;
    }

    /**
     * Deletes a company by its ID.
     * Before deletion, notifies the user-service to remove all employees from the company.
     *
     * @param companyId the ID of the company to delete
     * @throws CompanyNotFoundException     if company with given ID does not exist
     * @throws RestRequestFailedException   if notification to user-service fails for any employee
     */
    @Override
    public void deleteCompany(Long companyId) {
        log.info("[CompanyService] Deleting company with id: {}", companyId);

        Company company = getCompanyOrThrow(companyId);

        List<Long> employeeIds = safeCopy(company.getEmployeeIds());

        for (Long userId : employeeIds) {
            try {
                callUserServiceRemoveUserFromCompany(userId, companyId);
            } catch (RestRequestFailedException ex) {
                log.error("[CompanyService] Failed to notify user-service to remove user {}: {}", userId, ex.getMessage());
                throw ex;
            }
        }

        companyRepository.deleteById(companyId);
        log.info("[CompanyService] Company deleted: {}", companyId);
    }

    /**
     * Adds an employee to the specified company.
     * If the employee already exists in the company, no changes are made.
     *
     * @param companyId the ID of the company
     * @param userId    the ID of the user to add as an employee
     * @throws CompanyNotFoundException if company with given ID does not exist
     */
    @Override
    public void addEmployee(Long companyId, Long userId) {
        log.info("[CompanyService] Adding employee {} to company {}", userId, companyId);

        Company company = getCompanyOrThrow(companyId);

        if (company.getEmployeeIds() == null) {
            company.setEmployeeIds(new ArrayList<>());
        }

        if (!company.getEmployeeIds().contains(userId)) {
            company.getEmployeeIds().add(userId);
            companyRepository.save(company);
            log.info("[CompanyService] Employee {} added to company {}", userId, companyId);
        } else {
            log.info("[CompanyService] Employee {} is already in company {}", userId, companyId);
        }
    }

    /**
     * Removes an employee from the specified company.
     *
     * @param companyId the ID of the company
     * @param userId    the ID of the user to remove
     * @throws CompanyNotFoundException if company with given ID does not exist
     * @throws RuntimeException         if company has no employees or user not found in company
     */
    @Override
    public void removeEmployee(Long companyId, Long userId) {
        log.info("[CompanyService] Removing employee {} from company {}", userId, companyId);

        Company company = getCompanyOrThrow(companyId);

        List<Long> employeeIds = company.getEmployeeIds();

        if (employeeIds == null || employeeIds.isEmpty()) {
            log.warn("[CompanyService] Attempted to remove user from company {} with no employees", companyId);
            throw new RuntimeException("[CompanyService] Company has no employees");
        }

        if (!employeeIds.remove(userId)) {
            log.warn("[CompanyService] User {} not found in company {}", userId, companyId);
            throw new RuntimeException("User not found in the company");
        }

        companyRepository.save(company);
        log.info("[CompanyService] User {} removed from company {}", userId, companyId);
    }

    /**
     * Fetches user details from user-service by their IDs.
     *
     * @param ids list of user IDs to fetch
     * @return list of UserDTOs corresponding to the provided IDs
     */
    @Override
    public List<UserDTO> fetchUsersByIds(List<Long> ids) {
        log.info("[CompanyService] Fetching users from user-service by ids: {}", ids);

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return userClient.getUsersByIds(ids);
    }

    // --- Private helper methods ---

    /**
     * Retrieves a company entity by ID or throws exception if not found.
     *
     * @param id company ID
     * @return Company entity
     * @throws CompanyNotFoundException if company not found
     */
    private Company getCompanyOrThrow(Long id) {
        return companyRepository.getCompanyById(id).orElseThrow(() -> new CompanyNotFoundException(id));
    }

    /**
     * Checks if a company name already exists in the system.
     *
     * @param name company name to check
     * @throws ConflictException if name conflict exists
     */
    private void checkCompanyNameConflict(String name) {
        Company existing = companyRepository.getCompanyByName(name);
        if (existing != null) {
            log.warn("[CompanyService] Company with name '{}' already exists", name);
            throw new ConflictException("Company with such name: " + name + " already exists in the system");
        }
    }

    /**
     * Checks for company name conflict excluding a specific company ID (used during updates).
     *
     * @param name             new company name to check
     * @param excludeCompanyId company ID to exclude from check
     * @throws ConflictException if name conflict exists
     */
    private void checkCompanyNameConflict(String name, Long excludeCompanyId) {
        if (name.isEmpty() || name.isBlank()) throw new ConflictException("Company cannot have such name: " + name);
        Company existing = companyRepository.getCompanyByName(name);
        if (existing != null && !existing.getId().equals(excludeCompanyId)) {
            log.warn("[CompanyService] Name conflict during update: {}", name);
            throw new ConflictException("Company with such name: " + name + " already exists");
        }
    }

    /**
     * Notifies the user-service to remove a user from a company.
     *
     * @param userId    ID of the user to remove
     * @param companyId ID of the company
     */
    private void callUserServiceRemoveUserFromCompany(Long userId, Long companyId) {
        log.debug("[CompanyService] Calling user-service to remove user");
        userClient.removeUserFromCompany(userId, companyId);
    }

    /**
     * Safely fetches users by their IDs, returning an empty list on failure.
     *
     * @param ids list of user IDs
     * @return list of UserDTOs or empty list if fetch fails
     */
    private List<UserDTO> fetchUsersSafe(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        try {
            return fetchUsersByIds(ids);
        } catch (Exception e) {
            log.error("[CompanyService] Failed to fetch users for ids {}: {}", ids, e.getMessage());
            return List.of();
        }
    }

    /**
     * Creates a safe copy of the given list or returns an empty list if null.
     *
     * @param list original list
     * @return new list copy or empty list
     */
    private List<Long> safeCopy(List<Long> list) {
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }

    /**
     * Applies partial updates from the DTO to the Company entity.
     *
     * @param company entity to patch
     * @param dto     DTO with update data
     */
    private void patchCompany(Company company, CompanyRequestDTO dto) {
        if (dto.getName() != null) company.setName(dto.getName());
        if (dto.getEmployeeIds() != null) company.setEmployeeIds(dto.getEmployeeIds());
    }
}
