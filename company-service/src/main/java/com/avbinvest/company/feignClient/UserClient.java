package com.avbinvest.company.feignClient;

import com.avbinvest.company.dto.PageDTO;
import com.avbinvest.company.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feign client for interacting with the User Service.
 * Provides methods to fetch users by their IDs and
 * to remove a user from a company.
 */
@FeignClient(name = "user-service")
public interface UserClient {

    @PostMapping("/api/users/getUsersByIds")
    PageDTO<UserDTO> getUsersByIds(
            @RequestBody List<Long> ids,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @DeleteMapping("/api/users/{userId}/removeUserFromCompany")
    Void removeUserFromCompany(@PathVariable Long userId, @RequestParam Long companyId);
}
