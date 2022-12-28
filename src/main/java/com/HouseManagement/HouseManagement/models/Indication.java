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
@Table(name = "Indication")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Indication {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "period", length = 16, nullable = false)
    private String period;
    @NotNull(message = "Необходимо заполнить поле")
    @Range(min = 0, max = 999, message = "Значением данного поля должно быть число от 0 до 999")
    @Column(name = "value", nullable = false)
    private Integer value;
    @OneToOne
    //@JsonBackReference
    private Counter counter;
}
