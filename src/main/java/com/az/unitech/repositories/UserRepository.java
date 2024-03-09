package com.az.unitech.repositories;

import com.az.unitech.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByPin(String pin);
}

