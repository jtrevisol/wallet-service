package com.example.wattet.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "wallet", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"account_id"})
})
public class Wallet {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal balance;
}
