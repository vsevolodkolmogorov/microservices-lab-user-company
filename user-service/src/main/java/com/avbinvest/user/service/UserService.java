package com.avbinvest.user.service;

import com.avbinvest.user.dto.CompanyDTO;
import com.avbinvest.user.dto.UserRequestDTO;
import com.avbinvest.user.dto.UserResponseDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO createUser(UserRequestDTO dto);
    UserResponseDTO updateUser(Long id, UserRequestDTO dto);
    UserResponseDTO getUserById(Long id);
    List<UserResponseDTO> getAllUsers();
    void deleteUser(Long id);
    void removeUserFromCompany(Long companyId, Long userId);
    UserResponseDTO addUserToCompany(Long companyId, Long userId);
    List<UserResponseDTO> getUsersByIds(List<Long> ids);
    CompanyDTO fetchCompanyById(Long id);

}
