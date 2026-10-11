package com.rehancode.finpay.Model;


import com.rehancode.finpay.Enum.Role;
import com.rehancode.finpay.Enum.Status;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
@Data
public class UsersModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "Username")
    private String username;

    @Column(name = "Email")
    private String email;
    @Column(name = "Password")
    private String password;

    @Column(name = "Role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "Status")
    @Enumerated(EnumType.STRING)
    private Status status;


    //    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
//
//    private WalletModel walletModel;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude // <--- Prevents recursive toString() loop
    private WalletModel walletModel;
    
    @Column(name = "CreatedAt")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    @UpdateTimestamp
    private LocalDateTime updatedAt;


}
