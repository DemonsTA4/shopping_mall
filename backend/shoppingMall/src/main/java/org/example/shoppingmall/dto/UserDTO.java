package org.example.shoppingmall.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String country;
    private String avatar;
    private Integer role;
    private Integer gender;
    private LocalDate birthday;
    private LocalDateTime createTime;
}