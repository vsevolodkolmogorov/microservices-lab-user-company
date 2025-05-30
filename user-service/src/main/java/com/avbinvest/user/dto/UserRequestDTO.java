package com.avbinvest.user.dto;

import com.avbinvest.user.validation.OnCreate;
import com.avbinvest.user.validation.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDTO {

    @NotBlank(message = "FirstName must not be blank", groups = OnCreate.class)
    private String FirstName;

    @NotBlank(message = "LastName must not be blank", groups = OnCreate.class)
    private String LastName;

    @NotBlank(message = "Phone must not be blank", groups = OnCreate.class)
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number", groups = {OnCreate.class, OnUpdate.class})
    private String phoneNumber;

    private Long companyId;
}
