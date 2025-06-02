package com.avbinvest.user.controller;

import com.avbinvest.user.dto.CompanyDTO;
import com.avbinvest.user.dto.UserCreateDTO;
import com.avbinvest.user.dto.UserResponseDTO;
import com.avbinvest.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private final CompanyDTO companyDTO = new CompanyDTO(
            1L, "OOO Company", BigDecimal.ONE
    );

    private final UserResponseDTO userResponse = new UserResponseDTO(
            1L, "John", "Doe", "+79615882388", companyDTO
    );

    private final UserResponseDTO dto = new UserResponseDTO();
    private final List<UserResponseDTO> dtoList = List.of(dto);
    private final Page<UserResponseDTO> page = new PageImpl<>(dtoList);


    @Test
    void getAllUsers_shouldReturnList() throws Exception {
        UserResponseDTO userDto = new UserResponseDTO();
        userDto.setFirstName("John");

        Page<UserResponseDTO> page = new PageImpl<>(List.of(userDto));

        Mockito.when(userService.getAllUsers(PageRequest.of(0, 10))).thenReturn(page);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("John"));
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        Mockito.when(userService.getUserById(1L)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName", is("Doe")));
    }

    @Test
    void addUserToCompany_shouldReturnUser() throws Exception {
        Mockito.when(userService.addUserToCompany(1L, 1L)).thenReturn(userResponse);

        mockMvc.perform(post("/api/users/1/addUserToCompany")
                        .param("companyId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company.name", is(companyDTO.getName())));
    }

    @Test
    void getUsersByIds_shouldReturnUsersList() throws Exception {
        List<Long> ids = List.of(1L, 2L);
        Page<UserResponseDTO> page = new PageImpl<>(List.of(userResponse));
        Mockito.when(userService.getUsersByIds(Mockito.eq(ids), Mockito.any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(post("/api/users/getUsersByIds")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        UserCreateDTO request = new UserCreateDTO("Jane", "Doe", "+79615882388", null);
        UserResponseDTO created = new UserResponseDTO(2L, "Jane", "Doe", "+79615882388", null);

        Mockito.when(userService.createUser(any())).thenReturn(created);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Jane")))
                .andExpect(jsonPath("$.lastName", is("Doe")));
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        UserCreateDTO request = new UserCreateDTO("Jane", "Doe", "+79615882388", null);
        UserResponseDTO updated = new UserResponseDTO(2L, "Jane", "Doe", "+79615882388", null);

        Mockito.when(userService.updateUser(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Jane")))
                .andExpect(jsonPath("$.lastName", is("Doe")));
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(userService).deleteUser(1L);
    }

    @Test
    void removeUserFromCompany_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/1/removeUserFromCompany")
                        .param("companyId", "200"))
                .andExpect(status().isNoContent());

        Mockito.verify(userService).removeUserFromCompany(1L, 200L);
    }
}
