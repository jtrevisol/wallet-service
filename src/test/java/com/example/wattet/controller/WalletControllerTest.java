package com.example.wattet.controller;

import com.example.wattet.model.Transaction;
import com.example.wattet.model.TransactionType;
import com.example.wattet.model.Wallet;
import com.example.wattet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @Test
    void testCreateWallet() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setAccountId(accountId);
        wallet.setBalance(BigDecimal.ZERO);

        when(walletService.createWallet(accountId)).thenReturn(wallet);

        mockMvc.perform(post("/wallets")
                        .param("accountId", accountId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value(walletId.toString()));
    }

    @Test
    void testDeposit() throws Exception {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);

        Wallet wallet = new Wallet();
        wallet.setId(walletId);

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setWallet(wallet);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());

        when(walletService.deposit(walletId, amount)).thenReturn(transaction);

        mockMvc.perform(post("/wallets/{walletId}/deposit", walletId)
                        .param("amount", amount.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value("DEPOSIT"));
    }

    @Test
    void testTransfer() throws Exception {
        UUID fromWalletId = UUID.randomUUID();
        UUID toWalletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(50);

        Wallet fromWallet = new Wallet();
        fromWallet.setId(fromWalletId);

        Wallet toWallet = new Wallet();
        toWallet.setId(toWalletId);

        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setWallet(fromWallet);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setRelatedWallet(toWallet);

        when(walletService.transfer(fromWalletId, toWalletId, amount)).thenReturn(transaction);

        mockMvc.perform(post("/wallets/{fromWalletId}/transfer", fromWalletId)
                        .param("toWalletId", toWalletId.toString())
                        .param("amount", amount.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value("TRANSFER"));
    }
}