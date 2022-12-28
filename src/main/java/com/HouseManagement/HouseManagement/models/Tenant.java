package com.HouseManagement.HouseManagement.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

import java.time.LocalDate;

@Entity
@Table(name = "Tenant")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Tenant {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "full_name", length = 50, nullable = false)
    @Size(min = 10, max = 50, message = "Длина данного поля должна быть от 10 до 50")
    private String fullName;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, fallbackPatterns = { "dd.MM.yyyy" })
    @NotNull(message = "Необходимо заполнить поле")
    @Column(name = "date_of_registration", nullable = false)
    private LocalDate dateOfRegistration;
    @Pattern(regexp = "^[0-9]{1,3}", message = "Значением данного поля должно быть число от 0 до 999")
    @Column(name = "number_of_family_members",length = 3, nullable = false)
    private String numberOfFamilyMembers;
    @Pattern(regexp = "(\\+7)\\d{10}", message = "Некорректный ввод номера телефона")
    @Column(name = "phone_number", length = 12, unique = true, nullable = false)
    private String phoneNumber;
    @Email(message = "Некорректный ввод электронной почты")
    @Size(max = 30, message = "Максимальная длина данного поля - 30")
    @Nullable
    @Column(name = "email", unique = true, length = 30)
    private String email;
}
