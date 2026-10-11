package com.rehancode.finpay.DTO;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String username;
    private String email;
    private String token;
}
