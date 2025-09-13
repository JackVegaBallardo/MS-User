package com.example.user_ms;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_identity", columnNames = {"kc_iss", "kc_sub"})
    }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @Column(name = "kc_iss", nullable = false, length = 255)
    private String kcIss; 

    @Column(name = "kc_sub", nullable = false, length = 255)
    private String kcSub;  

    
    @Column(nullable = true, length = 100)
    private String Name;

}
