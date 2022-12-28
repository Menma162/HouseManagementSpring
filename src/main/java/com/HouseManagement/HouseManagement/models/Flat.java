package com.HouseManagement.HouseManagement.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
@Table(name = "Flat")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Flat {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Size(min = 8, max = 20, message = "Длина данного поля должна быть от 8 до 20")
    @Column(name = "personal_account", length = 20, nullable = false, unique = true)
    private String personalAccount;
    @Pattern(regexp = "^[0-9]{1,3}", message = "Значением данного поля должно быть число от 0 до 999")
    @Column(name = "flat_number", length = 3, nullable = false, unique = true)
    private String flatNumber;
    @NotNull(message = "Необходимо заполнить поле")
    @Column(name = "total_area",  nullable = false)
    private Float total;
    @NotNull(message = "Необходимо заполнить поле")
    @Column(name = "usable_area",  nullable = false)
    private Float usablea;
    @Pattern(regexp = "^[0-9]{1,3}", message = "Значением данного поля должно быть число от 0 до 999")
    @Column(name = "entrance_number", length = 3, nullable = false)
    private String entranceNumber;
    @Pattern(regexp = "^[0-9]{1,3}", message = "Значением данного поля должно быть число от 0 до 999")
    @Column(name = "number_of_rooms", length = 3, nullable = false)
    private String numberOfRooms;
    @NotNull(message = "Необходимо заполнить поле")
    @Range(min = 0, max = 999, message = "Значением данного поля должно быть число от 0 до 999")
    @Column(name = "number_of_registered_residents", nullable = false)
    private int numberOfRegisteredResidents;
    @NotNull(message = "Необходимо заполнить поле")
    @Range(min = 1, max = 999, message = "Значением данного поля должно быть число от 1 до 999")
    @Column(name = "number_of_owners", nullable = false)
    private int numberOfOwners;
    @OneToOne
    //@JsonBackReference
    private Tenant tenant;
}
