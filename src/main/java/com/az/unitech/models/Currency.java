package com.az.unitech.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Currency {
    @Id
    @Column(name = "currencyPair")
    private String currencyPair;
    private LocalDateTime lastUpdate;
    @Column(name = "currencyValue")
    private double value;
}
