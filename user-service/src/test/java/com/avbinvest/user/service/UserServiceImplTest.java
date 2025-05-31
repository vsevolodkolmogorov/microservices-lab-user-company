package com.avbinvest.user.service;

import com.avbinvest.user.dto.CompanyDTO;
import com.avbinvest.user.dto.UserRequestDTO;
import com.avbinvest.user.dto.UserResponseDTO;
import com.avbinvest.user.exception.CompanyNotFoundException;
import com.avbinvest.user.exception.ConflictException;
import com.avbinvest.user.exception.UserNotFoundException;
import com.avbinvest.user.feignClient.CompanyClient;
import com.avbinvest.user.module.User;
import com.avbinvest.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyClient companyClient;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private final User user = new User(1L, "John", "Doe", "+1234567890", null);
    private final CompanyDTO company = new CompanyDTO(1L, "Test Company", BigDecimal.ONE);

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateUserWithoutCompany() {
        UserRequestDTO dto = new UserRequestDTO("John", "Doe", "+1234567890", null);

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponseDTO response = userService.createUser(dto);

        assertThat(response).isNotNull();
        assertThat(response.getFirstName()).isEqualTo("John");

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPhoneNumber()).isEqualTo("+1234567890");
    }

    @Test
    void shouldCreateUserAndAddToCompany() {
        UserRequestDTO dto = new UserRequestDTO("John", "Doe", "+1234567890", 1L);

        when(companyClient.getCompanyById(1L, false)).thenReturn(company);
        when(userRepository.save(any())).thenReturn(user);
        when(userRepository.getUserById(1L)).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.createUser(dto);

        assertThat(response.getCompany()).isNotNull();
    }

    @Test
    void shouldThrowConflictOnDuplicatePhoneNumber() {
        UserRequestDTO dto = new UserRequestDTO("John", "Doe", "+1234567890", null);

        when(userRepository.findUserByPhoneNumber("+1234567890")).thenReturn(new User());

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldUpdateUser() {
        User existing = new User(1L, "Old", "User", "+1234567890", 2L);
        UserRequestDTO dto = new UserRequestDTO("Updated", "User", "+1234567890", 2L);

        when(userRepository.getUserById(1L)).thenReturn(Optional.of(existing));
        when(companyClient.getCompanyById(3L, false)).thenReturn(company);
        when(userRepository.save(any())).thenReturn(existing);

        UserResponseDTO response = userService.updateUser(1L, dto);

        assertThat(response.getFirstName()).isEqualTo("Updated");
    }

    @Test
    void shouldThrowNotFoundOnMissingUser() {
        when(userRepository.getUserById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(42L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void shouldDeleteUser() {
        User userToDelete = new User(1L, "Del", "User", "+123456", 10L);

        when(userRepository.getUserById(1L)).thenReturn(Optional.of(userToDelete));
        when(companyClient.getCompanyById(10L, false)).thenReturn(company);

        userService.deleteUser(1L);

        verify(companyClient).removeEmployee(10L, 1L);
        verify(userRepository).delete(userToDelete);
    }

    @Test
    void shouldAddUserToCompanyIfNotAlreadyAssigned() {
        user.setCompanyId(null);

        when(userRepository.getUserById(1L)).thenReturn(Optional.of(user));
        when(companyClient.getCompanyById(1L, false)).thenReturn(company);
        when(userRepository.save(any())).thenReturn(user);

        UserResponseDTO dto = userService.addUserToCompany(1L, 1L);

        assertThat(dto.getCompany().getId()).isEqualTo(1L);
        verify(companyClient).addEmployee(1L, 1L);
    }

    @Test
    void shouldThrowWhenUserAlreadyInDifferentCompany() {
        user.setCompanyId(99L);

        when(userRepository.getUserById(1L)).thenReturn(Optional.of(user));
        when(companyClient.getCompanyById(1L, false)).thenReturn(company);

        assertThatThrownBy(() -> userService.addUserToCompany(1L, 1L))
                .isInstanceOf(ConflictException.class);
    }


    @Test
    void shouldGetAllUsersWithCompanies() {
        user.setCompanyId(1L);

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(companyClient.getCompanyById(1L, false)).thenReturn(company);

        List<UserResponseDTO> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getCompany().getId()).isEqualTo(1L);
    }
}
