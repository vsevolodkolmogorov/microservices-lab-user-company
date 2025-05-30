package com.avbinvest.user.feignClient;

import com.avbinvest.user.dto.CompanyDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "company-service")
public interface CompanyClient {

    @GetMapping("/api/company/{id}")
    CompanyDTO getCompanyById(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean includeEmployees);

    @PostMapping("/api/company/{id}/addEmployee")
    void addEmployee(@PathVariable Long id, @RequestParam Long userId);

    @DeleteMapping("/api/company/{id}/removeEmployee")
    void removeEmployee(@PathVariable Long id, @RequestParam Long userId);
}
