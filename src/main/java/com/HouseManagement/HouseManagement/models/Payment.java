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
@Table(name = "Payment")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Payment {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "period", length = 16, nullable = false)
    private String period;
    @NotNull(message = "Необходимо заполнить поле")
    @Column(name = "amount",  nullable = false)
    private Float amount;
    @Column(name = "status", nullable = false)
    private boolean status;
    @Column(name = "cheque", length = 50)
    private String cheque;
    @OneToOne
    //@JsonBackReference
    private Rate rate;
    @OneToOne
    //@JsonBackReference
    private Normative normative;
    @OneToOne
    //@JsonBackReference
    private Flat flat;
}
