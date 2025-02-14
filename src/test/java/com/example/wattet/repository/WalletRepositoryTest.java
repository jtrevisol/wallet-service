package com.example.wattet.repository;

import com.example.wattet.model.Wallet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class WalletRepositoryTest {

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void testFindByAccountId() {
        UUID accountId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setAccountId(accountId);
        wallet.setBalance(BigDecimal.ZERO);
        walletRepository.save(wallet);

        Optional<Wallet> foundWallet = walletRepository.findByAccountId(accountId);

        assertTrue(foundWallet.isPresent());
        assertEquals(accountId, foundWallet.get().getAccountId());
    }
}