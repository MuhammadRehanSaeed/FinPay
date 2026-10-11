package com.rehancode.finpay;

import com.rehancode.finpay.Enum.Role;
import com.rehancode.finpay.Enum.Status;
import com.rehancode.finpay.Model.UsersModel;
import com.rehancode.finpay.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableMethodSecurity
@EnableAsync
@EnableScheduling
public class FinPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinPayApplication.class, args);
    }

//    @Bean
//    CommandLineRunner createAdmin(
//            UserRepository userRepository,
//            PasswordEncoder passwordEncoder) {
//
//        return args -> {
//
//            UsersModel usersModel = new UsersModel();
//
//            usersModel.setUsername("admin");
//            usersModel.setPassword(passwordEncoder.encode("admin"));
//            usersModel.setEmail("admin@finpay.com");
//            usersModel.setStatus(Status.ACTIVE);
//            usersModel.setRole(Role.ADMIN);
//
//            userRepository.save(usersModel);
//        };
//    }
}