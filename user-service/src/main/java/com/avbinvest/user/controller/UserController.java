package com.avbinvest.user.controller;

import com.avbinvest.user.dto.UserCreateDTO;
import com.avbinvest.user.dto.UserResponseDTO;
import com.avbinvest.user.dto.UserUpdateDTO;
import com.avbinvest.user.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    public Page<UserResponseDTO> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/users — getAllUsers() page={} size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return userService.getAllUsers(pageable);
    }

    @GetMapping("/{id}")
    public UserResponseDTO getUserById(@PathVariable @Min(1) Long id) {
        log.info("GET /api/users/{} — getUserById", id);
        return userService.getUserById(id);
    }

    @PostMapping("/{userId}/addUserToCompany")
    public UserResponseDTO addUserToCompany(@PathVariable @Min(1) Long userId,
                                            @RequestParam @NotNull Long companyId) {
        log.info("POST /api/users/{}/addUserToCompany — companyId={}", userId, companyId);
        return userService.addUserToCompany(userId, companyId);
    }

    @PostMapping("/getUsersByIds")
    public Page<UserResponseDTO> getUsersByIds(@RequestBody @NotEmpty List<@Min(1) Long> ids,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        log.info("POST /api/users/getUsersByIds — ids size={}", ids.size());
        Pageable pageable = PageRequest.of(page, size);
        return userService.getUsersByIds(ids, pageable);
    }

    @PostMapping
    public UserResponseDTO createUser(@Valid @RequestBody UserCreateDTO userDTO) {
        log.info("POST /api/users — createUser: {}", userDTO);
        return userService.createUser(userDTO);
    }

    @PutMapping("/{id}")
    public UserResponseDTO updateUser(@PathVariable @Min(1) Long id,
                                      @Valid @RequestBody UserUpdateDTO userDTO) {
        log.info("PUT /api/users/{} — updateUser: {}", id, userDTO);
        return userService.updateUser(id, userDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = org.springframework.http.HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable @Min(1) Long id) {
        log.info("DELETE /api/users/{} — deleteUser", id);
        userService.deleteUser(id);
    }

    @DeleteMapping("/{userId}/removeUserFromCompany")
    @ResponseStatus(code = org.springframework.http.HttpStatus.NO_CONTENT)
    public void removeUserFromCompany(@PathVariable @Min(1) Long userId,
                                      @RequestParam @NotNull Long companyId) {
        log.info("DELETE /api/users/{}/removeUserFromCompany — companyId={}", userId, companyId);
        userService.removeUserFromCompany(userId, companyId);
    }
}
