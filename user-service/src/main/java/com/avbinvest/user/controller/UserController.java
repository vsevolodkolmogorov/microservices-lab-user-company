package com.avbinvest.user.controller;

import com.avbinvest.user.dto.UserRequestDTO;
import com.avbinvest.user.dto.UserResponseDTO;
import com.avbinvest.user.service.UserService;
import com.avbinvest.user.validation.OnCreate;
import com.avbinvest.user.validation.OnUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> usersList = userService.getAllUsers();
        return ResponseEntity.ok(usersList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/addUserToCompany")
    public ResponseEntity<UserResponseDTO> addUserToCompany(@PathVariable Long userId, @RequestParam Long companyId) {
        UserResponseDTO user = userService.addUserToCompany(userId, companyId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/getUsersByIds")
    public ResponseEntity<List<UserResponseDTO>> getUsersByIds(@RequestBody List<Long> ids) {
        List<UserResponseDTO> users = userService.getUsersByIds(ids);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Validated(OnCreate.class) @RequestBody UserRequestDTO userDTO) {
        UserResponseDTO user = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @Validated(OnUpdate.class) @RequestBody UserRequestDTO userDTO) {
        UserResponseDTO user = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/removeUserFromCompany")
    public ResponseEntity<Void> removeUserFromCompany(@PathVariable Long userId, @RequestParam Long companyId) {
        userService.removeUserFromCompany(userId, companyId);
        return ResponseEntity.noContent().build();
    }
}
