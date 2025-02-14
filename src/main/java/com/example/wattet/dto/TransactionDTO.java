package com.example.wattet.dto;

import com.example.wattet.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private UUID id;
    private UUID walletId;
    private TransactionType type;
    private double amount;
    private LocalDateTime timestamp;
    private UUID relatedWalletId;
}
