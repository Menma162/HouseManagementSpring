package com.HouseManagement.HouseManagement.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "Userstable")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class User {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Email(message = "Некорректный ввод электронной почты")
    @Size(max = 30, message = "Максимальная длина данного поля - 30")
    @Column(name = "username", unique = true, length = 30)
    private String username;
    @Size(min = 10, max = 30, message = "Минимальная длина данного поля - 10, максимальная - 30")
    @Column(name = "password", length = 30)
    private String password;
    @Column(name = "authority", length = 30)
    private String authority;
    @Column(name = "enabled")
    private boolean enabled;
}
