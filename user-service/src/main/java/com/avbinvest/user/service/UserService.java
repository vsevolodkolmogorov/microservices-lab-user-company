package com.avbinvest.user.service;

import com.avbinvest.user.dto.CompanyDTO;
import com.avbinvest.user.dto.UserCreateDTO;
import com.avbinvest.user.dto.UserResponseDTO;
import com.avbinvest.user.dto.UserUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    UserResponseDTO createUser(UserCreateDTO dto);
    UserResponseDTO updateUser(Long id, UserUpdateDTO dto);
    UserResponseDTO getUserById(Long id);
    Page<UserResponseDTO> getAllUsers(Pageable pageable);
    void deleteUser(Long id);
    void removeUserFromCompany(Long companyId, Long userId);
    UserResponseDTO addUserToCompany(Long companyId, Long userId);
    Page<UserResponseDTO> getUsersByIds(List<Long> ids, Pageable pageable);
}
