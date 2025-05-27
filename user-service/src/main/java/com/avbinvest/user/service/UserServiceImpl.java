package com.avbinvest.user.service;

import com.avbinvest.user.dto.UserRequestDTO;
import com.avbinvest.user.dto.UserResponseDTO;
import com.avbinvest.user.exception.UserNotFoundException;
import com.avbinvest.user.module.User;
import com.avbinvest.user.repository.UserRepository;
import com.avbinvest.user.util.UserConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.avbinvest.user.util.UserConverter.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * @param dto
     * @return UserResponseDTO
     */
    @Override
    public UserResponseDTO createUser(UserRequestDTO dto) {
        User user = userRepository.save(convertDtoToEntity(dto));
        return convertEntityToDto(user);
    }

    /**
     * @param id
     * @param dto
     * @return UserResponseDTO
     */
    @Override
    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {
        User user = userRepository.getUserById(id).orElseThrow(() -> new UserNotFoundException(id));
        patchUser(user, dto);
        User userUpdated = userRepository.save(user);
        return convertEntityToDto(userUpdated);
    }

    /**
     * @param id
     * @return UserResponseDTO
     */
    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.getUserById(id).orElseThrow(() -> new UserNotFoundException(id));
        return convertEntityToDto(user);
    }

    /**
     * @return List<UserResponseDTO>
     */
    @Override
    public List<UserResponseDTO> getAllUsers() {
        List<User> usersList = userRepository.findAll();
        return usersList.stream().map(UserConverter::convertEntityToDto).toList();
    }

    /**
     * @param id
     */
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.getUserById(id).orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(user);
    }

    private void patchUser(User user, UserRequestDTO dto) {
        Optional.ofNullable(dto.getCompanyId()).ifPresent(user::setCompanyId);
        Optional.ofNullable(dto.getLastName()).ifPresent(user::setLastName);
        Optional.ofNullable(dto.getFirstName()).ifPresent(user::setFirstName);
        Optional.ofNullable(dto.getPhoneNumber()).ifPresent(user::setPhoneNumber);
    }
}
