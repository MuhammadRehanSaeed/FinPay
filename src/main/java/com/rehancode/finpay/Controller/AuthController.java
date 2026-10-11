package com.rehancode.finpay.Controller;


import com.rehancode.finpay.DTO.LoginRequestDTO;
import com.rehancode.finpay.DTO.LoginResponseDTO;
import com.rehancode.finpay.DTO.RegisterRequestDTO;
import com.rehancode.finpay.Exception.ApiResponse;
import com.rehancode.finpay.DTO.RegisterResponseDTO;
import com.rehancode.finpay.Service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController extends BaseController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO, HttpServletRequest request) {
        String ip = getIpAddress(request);
        String userAgent = getUserAgent(request);
        log.info("POST /register - ip: {}, userAgent: {}", ip, userAgent);
        ApiResponse<RegisterResponseDTO> response = authService.registerUser(registerRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO, HttpServletRequest request) {
        String ip = getIpAddress(request);
        String userAgent = getUserAgent(request);
        log.info("POST /login - ip: {}, userAgent: {}", ip, userAgent);
        ApiResponse<LoginResponseDTO> response = authService.loginUser(loginRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
