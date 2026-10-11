package com.rehancode.finpay.Service;

import com.rehancode.finpay.DTO.LoginRequestDTO;
import com.rehancode.finpay.DTO.LoginResponseDTO;
import com.rehancode.finpay.DTO.RegisterRequestDTO;
import com.rehancode.finpay.Exception.ApiResponse;
import com.rehancode.finpay.DTO.RegisterResponseDTO;
import jakarta.validation.Valid;

public interface AuthService {
    ApiResponse<RegisterResponseDTO> registerUser(RegisterRequestDTO registerRequestDTO);

    ApiResponse<LoginResponseDTO> loginUser(@Valid LoginRequestDTO loginRequestDTO);
}
