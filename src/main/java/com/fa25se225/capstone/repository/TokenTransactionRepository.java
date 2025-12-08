package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.TokenTransaction;
import com.fa25se225.capstone.entity.TokenTransactionType;
import com.fa25se225.capstone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenTransactionRepository extends JpaRepository<TokenTransaction, String> {

    List<TokenTransaction> findAllByUser(User user);

}
