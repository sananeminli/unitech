package com.az.unitech.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.az.unitech.enums.CurrencyCode;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor


public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private Long accountNumber;

    private boolean activityStatus;

    @Enumerated(EnumType.STRING)
    private CurrencyCode currencyCode;

    private double balance;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public Account(Long accountNumber, boolean activityStatus , CurrencyCode currencyCode , double balance , User user){
        this.accountNumber = accountNumber;
        this.activityStatus  = activityStatus;
        this.currencyCode = currencyCode;
        this.balance  = balance;
        this.user = user;

    }
}
