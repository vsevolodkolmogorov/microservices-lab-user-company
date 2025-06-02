package com.avbinvest.company.service;

import com.avbinvest.company.dto.*;
import com.avbinvest.company.exceptions.CompanyNotFoundException;
import com.avbinvest.company.exceptions.ConflictException;
import com.avbinvest.company.feignClient.UserClient;
import com.avbinvest.company.module.Company;
import com.avbinvest.company.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class CompanyServiceImplTest {

    private CompanyRepository companyRepository;
    private UserClient userClient;
    private CompanyServiceImpl companyService;

    @BeforeEach
    void setUp() {
        companyRepository = mock(CompanyRepository.class);
        userClient = mock(UserClient.class);
        companyService = new CompanyServiceImpl(companyRepository, userClient);
    }

    @Test
    void createCompany_shouldCreateCompanyWithValidUsers() {
        CompanyCreateDTO request = new CompanyCreateDTO("NewCompany", BigDecimal.valueOf(50000), List.of(1L, 2L));
        Company savedCompany = new Company(1L, "NewCompany", BigDecimal.valueOf(50000), new ArrayList<>(List.of(1L, 2L)));

        when(companyRepository.getCompanyByName("NewCompany")).thenReturn(null);
        when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);

        // Подготовка PageDTO<UserDTO>
        UserDTO user1 = new UserDTO(1L, "User1", "user1@mail.com", "+79615882385");
        UserDTO user2 = new UserDTO(2L, "User2", "user2@mail.com", "+79615882383");

        PageDTO<UserDTO> userPage = new PageDTO<>(
                List.of(user1, user2),
                0, 10, // pageNumber, pageSize
                2,     // totalElements
                1      // totalPages
        );

        when(userClient.getUsersByIds(eq(List.of(1L, 2L)), anyInt(), anyInt())).thenReturn(userPage);

        CompanyResponseDTO response = companyService.createCompany(request);

        assertEquals("NewCompany", response.getName());
        assertEquals(2, response.getEmployeeIds().size());
    }

    @Test
    void createCompany_shouldThrowConflict_whenNameExists() {
        CompanyCreateDTO request = new CompanyCreateDTO("Existing", BigDecimal.valueOf(10000), List.of());
        when(companyRepository.getCompanyByName("Existing")).thenReturn(new Company());

        assertThrows(ConflictException.class, () -> companyService.createCompany(request));
    }

    @Test
    void updateCompany_shouldUpdateAndReturnDTO() {
        Company existing = new Company(1L, "Old", BigDecimal.valueOf(10000), new ArrayList<>(List.of(1L)));
        CompanyUpdateDTO updateDto = new CompanyUpdateDTO("Updated", BigDecimal.valueOf(20000), null);
        Company updated = new Company(1L, "Updated", BigDecimal.valueOf(20000), new ArrayList<>(List.of(1L)));

        when(companyRepository.getCompanyById(1L)).thenReturn(Optional.of(existing));
        when(companyRepository.getCompanyByName("Updated")).thenReturn(null);
        when(companyRepository.save(any())).thenReturn(updated);

        // Подготовка PageDTO<UserDTO>
        UserDTO user1 = new UserDTO(1L, "User1", "user1@mail.com", "+79615882383");
        PageDTO<UserDTO> userPage = new PageDTO<>(
                List.of(user1),
                0, 10,
                1,
                1
        );

        when(userClient.getUsersByIds(eq(List.of(1L)), anyInt(), anyInt())).thenReturn(userPage);

        CompanyResponseDTO response = companyService.updateCompany(1L, updateDto);

        assertEquals("Updated", response.getName());
        assertEquals(BigDecimal.valueOf(20000), response.getBudget());
        assertEquals(1, response.getEmployeeIds().size());
    }


    @Test
    void getCompanyById_shouldReturnDTO() {
        Company company = new Company(1L, "Comp", BigDecimal.valueOf(1000), List.of(5L));

        when(companyRepository.getCompanyById(1L)).thenReturn(Optional.of(company));

        UserDTO user = new UserDTO(5L, "User", "mail", "+79615882383");
        PageDTO<UserDTO> page = new PageDTO<>(
                List.of(user),
                0, 10,
                1,
                1
        );

        when(userClient.getUsersByIds(eq(List.of(5L)), anyInt(), anyInt())).thenReturn(page);

        CompanyResponseDTO dto = companyService.getCompanyById(1L, true);

        assertEquals("Comp", dto.getName());
        assertEquals(1, dto.getEmployeeIds().size());
    }


    @Test
    void getCompanyById_shouldThrowNotFound() {
        when(companyRepository.getCompanyById(1L)).thenReturn(Optional.empty());
        assertThrows(CompanyNotFoundException.class, () -> companyService.getCompanyById(1L, true));
    }

    @Test
    void deleteCompany_shouldRemoveAllUsersAndDelete() {
        Company company = new Company(1L, "ToDelete", BigDecimal.valueOf(1000), new ArrayList<>(List.of(1L, 2L)));
        when(companyRepository.getCompanyById(1L)).thenReturn(Optional.of(company));

        doNothing().when(userClient).removeUserFromCompany(anyLong(), eq(1L));

        companyService.deleteCompany(1L);

        verify(userClient).removeUserFromCompany(1L, 1L);
        verify(userClient).removeUserFromCompany(2L, 1L);
        verify(companyRepository).deleteById(1L);
    }

    @Test
    void addEmployee_shouldAddAndSave() {
        Company company = new Company(1L, "MyComp", BigDecimal.valueOf(500), new ArrayList<>(List.of(10L)));
        when(companyRepository.getCompanyById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any())).thenReturn(company);

        companyService.addEmployee(1L, 20L);

        ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
        verify(companyRepository).save(captor.capture());

        assertTrue(captor.getValue().getEmployeeIds().contains(20L));
    }

    @Test
    void removeEmployee_shouldRemoveUserAndSave() {
        Company company = new Company(1L, "Comp", BigDecimal.valueOf(0), new ArrayList<>(List.of(10L, 20L)));
        when(companyRepository.getCompanyById(1L)).thenReturn(Optional.of(company));

        companyService.removeEmployee(1L, 20L);

        verify(companyRepository).save(any());
        assertFalse(company.getEmployeeIds().contains(20L));
    }

    @Test
    void removeEmployee_shouldThrow_whenUserNotInCompany() {
        Company company = new Company(1L, "Comp", BigDecimal.valueOf(0), new ArrayList<>(List.of(10L)));
        when(companyRepository.getCompanyById(1L)).thenReturn(Optional.of(company));

        assertThrows(RuntimeException.class, () -> companyService.removeEmployee(1L, 999L));
    }
}
