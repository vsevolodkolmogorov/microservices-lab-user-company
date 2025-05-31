package com.avbinvest.company.feignClient;

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

    /**
     * Retrieves a list of users by their IDs.
     *
     * @param ids list of user IDs to fetch
     * @return list of UserDTO objects corresponding to the provided IDs
     */
    @GetMapping("/api/users/getUsersByIds")
    List<UserDTO> getUsersByIds(@RequestBody List<Long> ids);

    /**
     * Removes a user from a specified company.
     *
     * @param userId    the ID of the user to remove
     * @param companyId the ID of the company from which the user will be removed
     * @return Void
     */
    @DeleteMapping("/api/users/{userId}/removeUserFromCompany")
    Void removeUserFromCompany(@PathVariable Long userId, @RequestParam Long companyId);
}
