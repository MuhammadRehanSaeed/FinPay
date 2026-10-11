package com.rehancode.finpay.Service.Impl;

import com.rehancode.finpay.DTO.LoginRequestDTO;
import com.rehancode.finpay.DTO.LoginResponseDTO;
import com.rehancode.finpay.DTO.RegisterRequestDTO;
import com.rehancode.finpay.Enum.Role;
import com.rehancode.finpay.Enum.Status;
import com.rehancode.finpay.Exception.ApiResponse;
import com.rehancode.finpay.DTO.RegisterResponseDTO;
import com.rehancode.finpay.Exception.BusinessRuleFailedException;
import com.rehancode.finpay.JWT.JwtService;
import com.rehancode.finpay.Model.UsersModel;
import com.rehancode.finpay.Repository.UserRepository;
import com.rehancode.finpay.SecurityConfig.UserDetailsImpl;
import com.rehancode.finpay.Service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IAuthService implements AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public IAuthService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public ApiResponse<RegisterResponseDTO> registerUser(RegisterRequestDTO registerRequestDTO) {
        log.info("Register attempt for username: {}", registerRequestDTO.getUsername());
        if (userRepository.existsByEmail(registerRequestDTO.getEmail())) {
            log.warn("Registration failed - email already exists: {}", registerRequestDTO.getEmail());
            throw new BusinessRuleFailedException("Email already exists");
        }
        if (userRepository.existsByUsername(registerRequestDTO.getUsername())) {
            log.warn("Registration failed - username already exists: {}", registerRequestDTO.getUsername());
            throw new BusinessRuleFailedException("Username already exists");
        }
        UsersModel usersModel = new UsersModel();
        usersModel.setEmail(registerRequestDTO.getEmail());
        usersModel.setUsername(registerRequestDTO.getUsername());
        usersModel.setRole(Role.USER);
        usersModel.setStatus(Status.ACTIVE);
        usersModel.setPassword(bCryptPasswordEncoder.encode(registerRequestDTO.getPassword()));
        userRepository.save(usersModel);
        log.info("User registered successfully with username: {}", usersModel.getUsername());

        RegisterResponseDTO registerResponseDTO = new RegisterResponseDTO();
        registerResponseDTO.setEmail(usersModel.getEmail());
        registerResponseDTO.setUsername(usersModel.getUsername());

        return new ApiResponse<>(HttpStatus.CREATED.value(), "User registered successfully", registerResponseDTO);
    }

    @Override
    public ApiResponse<LoginResponseDTO> loginUser(LoginRequestDTO loginRequestDTO) {
        log.info("Login attempt for username: {}", loginRequestDTO.getUsername());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(), loginRequestDTO.getPassword())
            );
        } catch (Exception e) {
            log.error("Authentication failed for username: {} - {}", loginRequestDTO.getUsername(), e.getMessage());
            throw new RuntimeException(e);
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        UsersModel user = userDetails.getUser();
        String token = jwtService.generateToken(user);
        log.info("Login successful for username: {}", user.getUsername());

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setToken(token);
        loginResponseDTO.setUsername(user.getUsername());
        loginResponseDTO.setEmail(user.getEmail());
        return new ApiResponse<>(HttpStatus.OK.value(), "Login successfully", loginResponseDTO);
    }
}
