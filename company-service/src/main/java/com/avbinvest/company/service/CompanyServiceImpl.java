package com.avbinvest.company.service;

import com.avbinvest.company.dto.CompanyRequestDTO;
import com.avbinvest.company.dto.CompanyResponseDTO;
import com.avbinvest.company.dto.UserDTO;
import com.avbinvest.company.exceptions.CompanyNotFoundException;
import com.avbinvest.company.module.Company;
import com.avbinvest.company.repository.CompanyRepository;
import com.avbinvest.company.util.CompanyConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.avbinvest.company.util.CompanyConverter.*;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final RestTemplate restTemplate;

    @Value("${services.user-service-url}")
    private String userServiceUrl;

    /**
     * @param dto
     * @return CompanyResponseDTO
     */
    @Override
    public CompanyResponseDTO createCompany(CompanyRequestDTO dto) {
        Company company = companyRepository.save(convertDtoToEntity(dto));

        List<UserDTO> users = List.of();
        if (company.getEmployeeIds() != null && !company.getEmployeeIds().isEmpty()) {
            users = fetchUsersByIds(company.getEmployeeIds());
        }

        return convertEntityToDto(company, users);
    }

    /**
     * @param id
     * @param dto
     * @return CompanyResponseDTO
     */
    @Override
    public CompanyResponseDTO updateCompany(Long id, CompanyRequestDTO dto) {
        Company company = companyRepository.getCompanyById(id).orElseThrow(() -> new CompanyNotFoundException(id));
        patchCompany(company, dto);
        Company companyUpdated = companyRepository.save(company);

        List<UserDTO> users = List.of();
        if (company.getEmployeeIds() != null && !company.getEmployeeIds().isEmpty()) {
            users = fetchUsersByIds(company.getEmployeeIds());
        }

        return convertEntityToDto(companyUpdated,users);
    }

    /**
     * @param id
     * @return CompanyResponseDTO
     */
    @Override
    public CompanyResponseDTO getCompanyById(Long id) {
        Company company = companyRepository.getCompanyById(id).orElseThrow(() -> new CompanyNotFoundException(id));

        List<UserDTO> users = List.of();
        if (company.getEmployeeIds() != null && !company.getEmployeeIds().isEmpty()) {
            users = fetchUsersByIds(company.getEmployeeIds());
        }

        return convertEntityToDto(company, users);
    }

    /**
     * @return List<CompanyResponseDTO>
     */
    @Override
    public List<CompanyResponseDTO> getAllCompanies() {
        List<Company> companies = companyRepository.findAll();

        return companies.stream()
                .map(company -> {
                    List<UserDTO> users = List.of();
                    if (company.getEmployeeIds() != null && !company.getEmployeeIds().isEmpty()) {
                        users = fetchUsersByIds(company.getEmployeeIds());
                    }
                    return CompanyConverter.convertEntityToDto(company, users);
                })
                .toList();
    }

    /**
     * @param id
     */
    @Override
    public void deleteCompany(Long id) {
        Company company = companyRepository.getCompanyById(id).orElseThrow(() -> new CompanyNotFoundException(id));
        companyRepository.delete(company);
    }

    /**
     * @param companyId
     * @param userId
     */
    @Override
    public void addEmployee(Long companyId, Long userId) {
        Company company = companyRepository.getCompanyById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException(companyId));

        if (company.getEmployeeIds() == null) {
            company.setEmployeeIds(new ArrayList<>());
        }

        if (!company.getEmployeeIds().contains(userId)) {
            company.getEmployeeIds().add(userId);
            companyRepository.save(company);
        }
    }

    /**
     * @param companyId
     * @param userId
     */
    @Override
    public void removeEmployee(Long companyId, Long userId) {
        Company company = companyRepository.getCompanyById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException(companyId));

        List<Long> employeeIds = company.getEmployeeIds();

        if (employeeIds == null || employeeIds.isEmpty()) {
            throw new RuntimeException("Company has no employees");
        }

        boolean removed = employeeIds.remove(userId);

        if (!removed) {
            throw new RuntimeException("User not found in the company");
        }

        companyRepository.save(company);
    }

    /**
     * @param ids
     * @return List<UserDTO>
     */
    @Override
    public List<UserDTO> fetchUsersByIds(List<Long> ids) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<Long>> request = new HttpEntity<>(ids, headers);

        ResponseEntity<List<UserDTO>> response = restTemplate.exchange(
                userServiceUrl + "getUsersByIds",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<List<UserDTO>>() {}
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }
        throw new RuntimeException("Failed to fetch users from user-service");
    }

    private void patchCompany(Company company, CompanyRequestDTO dto) {
        Optional.ofNullable(dto.getEmployeeIds()).ifPresent(company::setEmployeeIds);
        Optional.ofNullable(dto.getName()).ifPresent(company::setName);
        Optional.ofNullable(dto.getBudget()).ifPresent(company::setBudget);
    }
}
