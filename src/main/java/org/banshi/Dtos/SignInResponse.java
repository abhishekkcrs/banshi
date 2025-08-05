package org.banshi.Dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignInResponse {

    private Long userId;
    private String name;
    private String phone;
    private String email;
    private String password;
    private String role;
}
