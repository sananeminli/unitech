package com.az.unitech.repositories;

import com.az.unitech.models.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyRepository extends JpaRepository<Currency , String> {
}
