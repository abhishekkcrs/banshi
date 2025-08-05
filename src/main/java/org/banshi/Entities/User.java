package org.banshi.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.banshi.Entities.Enums.Role;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String name;
    private String phone;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Double balance;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PaymentTransaction> transactions;
}
