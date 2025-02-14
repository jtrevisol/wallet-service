package com.example.wattet.repository;

import com.example.wattet.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByWalletId(UUID walletId);

    List<Transaction> findByWalletIdAndTimestampLessThanEqual(UUID walletId, LocalDateTime timestamp);
}