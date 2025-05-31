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

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final UserClient userClient;


    @Override
    public CompanyResponseDTO createCompany(CompanyRequestDTO dto) {
        log.info("[CompanyService] Creating company with name: {}", dto.getName());

        checkCompanyNameConflict(dto.getName());

        Company company = companyRepository.save(convertDtoToEntity(dto));
        log.info("[CompanyService] Company created with id: {}", company.getId());

        List<UserDTO> users = fetchUsersSafe(company.getEmployeeIds());

        return convertEntityToDto(company, users);
    }

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

    @Override
    public CompanyResponseDTO getCompanyById(Long id, boolean includeEmployees) {
        log.info("[CompanyService] Fetching company with id: {}", id);

        Company company = getCompanyOrThrow(id);
        List<UserDTO> users = includeEmployees ? fetchUsersSafe(company.getEmployeeIds()) : List.of();

        return convertEntityToDto(company, users);
    }

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

    @Override
    public List<UserDTO> fetchUsersByIds(List<Long> ids) {
        log.info("[CompanyService] Fetching users from user-service by ids: {}", ids);

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return userClient.getUsersByIds(ids);
    }

    // --- Private methods ---

    private Company getCompanyOrThrow(Long id) {
        return companyRepository.getCompanyById(id)
                .orElseThrow(() -> new CompanyNotFoundException(id));
    }

    private void checkCompanyNameConflict(String name) {
        Company existing = companyRepository.getCompanyByName(name);
        if (existing != null) {
            log.warn("[CompanyService] Company with name '{}' already exists", name);
            throw new ConflictException("Company with such name: " + name + " already exists in the system");
        }
    }

    private void checkCompanyNameConflict(String name, Long excludeCompanyId) {
        if (name.isEmpty() || name.isBlank())  throw new ConflictException("Company cannot have such name: " + name);
        Company existing = companyRepository.getCompanyByName(name);
        if (existing != null && !existing.getId().equals(excludeCompanyId)) {
            log.warn("[CompanyService] Name conflict during update: {}", name);
            throw new ConflictException("Company with such name: " + name + " already exists");
        }
    }

    private void callUserServiceRemoveUserFromCompany(Long userId, Long companyId) {
        log.debug("[CompanyService] Calling user-service to remove user");
        userClient.removeUserFromCompany(userId, companyId);
    }

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

    private List<Long> safeCopy(List<Long> list) {
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }

    private void patchCompany(Company company, CompanyRequestDTO dto) {
        log.debug("[CompanyService] Patching company {} with data: {}", company.getId(), dto);

        Optional.ofNullable(dto.getName())
                .ifPresent(company::setName);

        Optional.ofNullable(dto.getBudget())
                .ifPresent(company::setBudget);
    }
}
