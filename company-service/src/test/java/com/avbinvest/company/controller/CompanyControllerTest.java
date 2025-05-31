package com.avbinvest.company.controller;

import com.avbinvest.company.dto.CompanyRequestDTO;
import com.avbinvest.company.dto.CompanyResponseDTO;
import com.avbinvest.company.service.CompanyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyController.class)
@ActiveProfiles("test")
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllCompanies_ShouldReturnList() throws Exception {
        CompanyResponseDTO dto = new CompanyResponseDTO();
        Mockito.when(companyService.getAllCompanies(true)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getCompanyById_ShouldReturnCompany() throws Exception {
        CompanyResponseDTO dto = new CompanyResponseDTO();
        Mockito.when(companyService.getCompanyById(1L, true)).thenReturn(dto);

        mockMvc.perform(get("/api/company/1"))
                .andExpect(status().isOk());
    }

    @Test
    void createCompany_ShouldCreateAndReturnCompany() throws Exception {
        CompanyRequestDTO request = CompanyRequestDTO.builder().name("OOO Company").budget(BigDecimal.valueOf(10)).build();
        CompanyResponseDTO response = new CompanyResponseDTO();

        Mockito.when(companyService.createCompany(any())).thenReturn(response);

        mockMvc.perform(post("/api/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void addEmployee_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/company/1/addEmployee")
                        .param("userId", "2"))
                .andExpect(status().isNoContent());

        Mockito.verify(companyService).addEmployee(1L, 2L);
    }

    @Test
    void removeEmployee_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/company/1/removeEmployee")
                        .param("userId", "2"))
                .andExpect(status().isNoContent());

        Mockito.verify(companyService).removeEmployee(1L, 2L);
    }

    @Test
    void updateCompany_ShouldReturnUpdatedCompany() throws Exception {
        CompanyRequestDTO request = new CompanyRequestDTO();
        CompanyResponseDTO response = new CompanyResponseDTO();

        Mockito.when(companyService.updateCompany(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/company/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCompany_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/company/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(companyService).deleteCompany(1L);
    }
}
