package com.avbinvest.company.service;

import com.avbinvest.company.dto.*;
import com.avbinvest.company.exceptions.CompanyNotFoundException;
import com.avbinvest.company.exceptions.ConflictException;
import com.avbinvest.company.exceptions.EmployeeNotFoundException;
import com.avbinvest.company.exceptions.RestRequestFailedException;
import com.avbinvest.company.feignClient.UserClient;
import com.avbinvest.company.module.Company;
import com.avbinvest.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public CompanyResponseDTO createCompany(CompanyCreateDTO dto) {
        validateCompanyNameNotBlank(dto.getName());
        validateCompanyNameUnique(dto.getName());

        Company company = companyRepository.save(convertDtoToEntity(dto));
        List<UserDTO> users = fetchUsersSafe(company.getEmployeeIds());

        CompanyResponseDTO result = convertEntityToDto(company, users);
        log.info("Created company with id: {}", company.getId());
        return result;
    }

    @Override
    public CompanyResponseDTO updateCompany(Long id, CompanyUpdateDTO dto) {
        Company company = getCompanyOrThrow(id);

        if (dto.getName() != null) {
            validateCompanyNameNotBlank(dto.getName());
            validateCompanyNameUnique(dto.getName(), id);
        }

        patchCompany(company, dto);
        Company updatedCompany = companyRepository.save(company);
        List<UserDTO> users = fetchUsersSafe(updatedCompany.getEmployeeIds());

        CompanyResponseDTO result = convertEntityToDto(updatedCompany, users);
        log.info("Updated company with id: {}", updatedCompany.getId());
        return result;
    }

    @Override
    public CompanyResponseDTO getCompanyById(Long id, boolean includeEmployees) {
        Company company = getCompanyOrThrow(id);
        List<UserDTO> users = includeEmployees ? fetchUsersSafe(company.getEmployeeIds()) : List.of();

        CompanyResponseDTO result = convertEntityToDto(company, users);
        log.info("Fetched company by id: {}", id);
        return result;
    }

    @Override
    public Page<CompanyResponseDTO> getAllCompanies(Pageable pageable, boolean includeEmployees) {
        Page<Company> companyPage = companyRepository.findAll(pageable);

        Page<CompanyResponseDTO> dtoPage = companyPage.map(company -> {
            List<UserDTO> users = includeEmployees ? fetchUsersSafe(company.getEmployeeIds()) : List.of();
            return convertEntityToDto(company, users);
        });

        log.info("Fetched {} companies out of total {}", dtoPage.getNumberOfElements(), dtoPage.getTotalElements());
        return dtoPage;
    }

    @Override
    public void deleteCompany(Long companyId) {
        Company company = getCompanyOrThrow(companyId);
        List<Long> employeeIds = safeCopy(company.getEmployeeIds());

        for (Long userId : employeeIds) {
            try {
                callUserServiceRemoveUserFromCompany(userId, companyId);
            } catch (RestRequestFailedException ex) {
                log.error("Failed to notify user-service to remove user {}: {}", userId, ex.getMessage());
                throw ex;
            }
        }

        companyRepository.deleteById(companyId);
        log.info("Deleted company with id: {}", companyId);
    }

    @Override
    public void addEmployee(Long companyId, Long userId) {
        Company company = getCompanyOrThrow(companyId);

        if (company.getEmployeeIds() == null) {
            company.setEmployeeIds(new ArrayList<>());
        }

        if (!company.getEmployeeIds().contains(userId)) {
            company.getEmployeeIds().add(userId);
            companyRepository.save(company);
            log.info("Added employee {} to company {}", userId, companyId);
        } else {
            log.info("Employee {} already exists in company {}", userId, companyId);
        }
    }

    @Override
    public void removeEmployee(Long companyId, Long userId) {
        Company company = getCompanyOrThrow(companyId);
        List<Long> employeeIds = company.getEmployeeIds();

        validateEmployeeListNotEmpty(companyId, employeeIds);
        validateEmployeeExists(userId, employeeIds);

        employeeIds.remove(userId);
        companyRepository.save(company);
        log.info("Removed employee {} from company {}", userId, companyId);
    }

    @Override
    public List<UserDTO> fetchUsersByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        PageDTO<UserDTO> page = userClient.getUsersByIds(ids, 0, ids.size());
        log.info("Fetched {} users from user-service", page.getContent().size());
        return page.getContent();
    }

    // --- Private helpers ---

    private Company getCompanyOrThrow(Long id) {
        return companyRepository.getCompanyById(id).orElseThrow(() -> new CompanyNotFoundException(id));
    }

    private void callUserServiceRemoveUserFromCompany(Long userId, Long companyId) {
        userClient.removeUserFromCompany(userId, companyId);
    }

    private List<UserDTO> fetchUsersSafe(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        try {
            return fetchUsersByIds(ids);
        } catch (Exception e) {
            log.error("Failed to fetch users for ids {}: {}", ids, e.getMessage());
            return List.of();
        }
    }

    private List<Long> safeCopy(List<Long> list) {
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }

    private void patchCompany(Company company, CompanyUpdateDTO dto) {
        if (dto.getName() != null) company.setName(dto.getName());
        if (dto.getBudget() != null) company.setBudget(dto.getBudget());
        if (dto.getEmployeeIds() != null) company.setEmployeeIds(dto.getEmployeeIds());
    }

    private void validateCompanyNameNotBlank(String name) {
        if (name == null || name.isBlank()) {
            throw new ConflictException("Company name cannot be empty");
        }
    }

    private void validateCompanyNameUnique(String name) {
        if (companyRepository.getCompanyByName(name) != null) {
            throw new ConflictException("Company with name '" + name + "' already exists");
        }
    }

    private void validateCompanyNameUnique(String name, Long excludeCompanyId) {
        Company existing = companyRepository.getCompanyByName(name);
        if (existing != null && !existing.getId().equals(excludeCompanyId)) {
            throw new ConflictException("Company with name '" + name + "' already exists");
        }
    }

    private void validateEmployeeListNotEmpty(Long companyId, List<Long> employeeIds) {
        if (employeeIds == null || employeeIds.isEmpty()) {
            throw new ConflictException("Company " + companyId + " has no employees");
        }
    }

    private void validateEmployeeExists(Long userId, List<Long> employeeIds) {
        if (!employeeIds.contains(userId)) {
            throw new EmployeeNotFoundException(userId);
        }
    }
}

