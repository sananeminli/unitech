package com.az.unitech.repositories;

import com.az.unitech.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    @Query(value = "SELECT a from Account a WHERE a.user.pin = :pin AND a.activityStatus = true ")
    List<Account> getAccounts(String pin);

    Account findByAccountNumber(Long accountNumber);
}
