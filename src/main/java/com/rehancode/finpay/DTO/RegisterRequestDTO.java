package com.rehancode.finpay.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class RegisterRequestDTO {
    @NotBlank(message = "Email cannot be empty")
    @Email
    private String email;
    @NotBlank(message = "Username cannot be empty")
    @Length(min = 3, max = 20)
    private String username;
    @NotBlank(message = "Password cannot be empty")
    @Length(min = 6, max = 20)
    private String password;
}
