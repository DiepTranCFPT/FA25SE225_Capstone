package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
