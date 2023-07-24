package com.example.acc.repository;

import com.example.acc.domain.Account;
import com.example.acc.domain.AccountUser;
import com.example.acc.domain.Transaction;
import com.example.acc.dto.TransactionDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionId(String transactionId);
}
