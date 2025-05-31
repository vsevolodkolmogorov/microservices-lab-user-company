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

/**
 * REST controller for managing user entities.
 * Provides endpoints to create, update, retrieve, and delete users,
 * as well as to manage their association with companies.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * Retrieves all users.
     *
     * @return HTTP 200 with the list of all users
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> usersList = userService.getAllUsers();
        return ResponseEntity.ok(usersList);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id the ID of the user to retrieve
     * @return HTTP 200 with user data if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Adds a user to a company.
     *
     * @param userId    ID of the user to add
     * @param companyId ID of the company to add the user to (request param)
     * @return HTTP 200 with updated user data
     */
    @PostMapping("/{userId}/addUserToCompany")
    public ResponseEntity<UserResponseDTO> addUserToCompany(@PathVariable Long userId, @RequestParam Long companyId) {
        UserResponseDTO user = userService.addUserToCompany(userId, companyId);
        return ResponseEntity.ok(user);
    }

    /**
     * Retrieves multiple users by their IDs.
     *
     * @param ids list of user IDs to fetch
     * @return HTTP 200 with the list of found users
     */
    @PostMapping("/getUsersByIds")
    public ResponseEntity<List<UserResponseDTO>> getUsersByIds(@RequestBody List<Long> ids) {
        List<UserResponseDTO> users = userService.getUsersByIds(ids);
        return ResponseEntity.ok(users);
    }

    /**
     * Creates a new user.
     *
     * @param userDTO the user data to create (validated on OnCreate group)
     * @return HTTP 201 with created user data
     */
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Validated(OnCreate.class) @RequestBody UserRequestDTO userDTO) {
        UserResponseDTO user = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    /**
     * Updates an existing user.
     *
     * @param id      ID of the user to update
     * @param userDTO user data to update (validated on OnUpdate group)
     * @return HTTP 200 with updated user data
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @Validated(OnUpdate.class) @RequestBody UserRequestDTO userDTO) {
        UserResponseDTO user = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(user);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id ID of the user to delete
     * @return HTTP 204 No Content on successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Removes a user from a company.
     *
     * @param userId    ID of the user to remove
     * @param companyId ID of the company to remove the user from (request param)
     * @return HTTP 204 No Content on successful removal
     */
    @DeleteMapping("/{userId}/removeUserFromCompany")
    public ResponseEntity<Void> removeUserFromCompany(@PathVariable Long userId, @RequestParam Long companyId) {
        userService.removeUserFromCompany(userId, companyId);
        return ResponseEntity.noContent().build();
    }
}
