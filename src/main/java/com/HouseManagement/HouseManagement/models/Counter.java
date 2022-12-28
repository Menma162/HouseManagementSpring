package com.HouseManagement.HouseManagement.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import org.hibernate.validator.constraints.Range;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Entity
@Table(name = "Counter")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Counter {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotBlank(message = "Необходимо выбрать тип счетчика")
    @Column(name = "type", length = 30, nullable = false)
    private String type;
    @Column(name = "used", nullable = false)
    private boolean used;
    @Size(min = 8, max = 20, message = "Длина данного поля должна быть от 8 до 20")
    @Column(name = "number", length = 20, nullable = false)
    private String number;
    @OneToOne
    //@JsonBackReference
    private Flat flat;
}
