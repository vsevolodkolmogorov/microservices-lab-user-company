package com.avbinvest.company.controller;

import com.avbinvest.company.dto.CompanyCreateDTO;
import com.avbinvest.company.dto.CompanyResponseDTO;
import com.avbinvest.company.service.CompanyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
        List<CompanyResponseDTO> dtoList = List.of(dto);

        Page<CompanyResponseDTO> page = new PageImpl<>(dtoList);

        Mockito.when(companyService.getAllCompanies(Mockito.any(Pageable.class), Mockito.eq(true)))
                .thenReturn(page);

        mockMvc.perform(get("/api/company")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1));
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
        CompanyCreateDTO request = CompanyCreateDTO.builder().name("OOO Company").budget(BigDecimal.valueOf(10)).build();
        CompanyResponseDTO response = new CompanyResponseDTO();

        Mockito.when(companyService.createCompany(any())).thenReturn(response);

        mockMvc.perform(post("/api/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void addEmployee_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/company/1/addEmployee")
                        .param("userId", "2"))
                .andExpect(status().isOk());

        Mockito.verify(companyService).addEmployee(1L, 2L);
    }

    @Test
    void removeEmployee_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/company/1/removeEmployee")
                        .param("userId", "2"))
                .andExpect(status().isOk());

        Mockito.verify(companyService).removeEmployee(1L, 2L);
    }

    @Test
    void updateCompany_ShouldReturnUpdatedCompany() throws Exception {
        CompanyCreateDTO request = new CompanyCreateDTO();
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
                .andExpect(status().isOk());

        Mockito.verify(companyService).deleteCompany(1L);
    }
}
