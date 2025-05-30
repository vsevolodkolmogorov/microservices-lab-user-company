package com.avbinvest.company.feignClient;

import com.avbinvest.company.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/api/users/getUsersByIds")
    List<UserDTO> getUsersByIds(@RequestBody List<Long> ids);

    @DeleteMapping("/api/users/{userId}/removeUserFromCompany")
    Void removeUserFromCompany(@PathVariable Long userId, @RequestParam Long companyId);
}
