package com.example.wattet.service;

import com.example.wattet.exception.InsufficientBalanceException;
import com.example.wattet.model.Transaction;
import com.example.wattet.model.TransactionType;
import com.example.wattet.model.Wallet;
import com.example.wattet.repository.TransactionRepository;
import com.example.wattet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateWallet() {
        UUID accountId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setAccountId(accountId);
        wallet.setBalance(BigDecimal.ZERO);

        when(walletRepository.findByAccountId(accountId)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        Wallet createdWallet = walletService.createWallet(accountId);

        assertNotNull(createdWallet);
        assertEquals(accountId, createdWallet.getAccountId());
        assertEquals(BigDecimal.ZERO, createdWallet.getBalance());
    }

    @Test
    void testDeposit() {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.ZERO);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        Transaction mockTransaction = new Transaction();
        mockTransaction.setType(TransactionType.DEPOSIT);
        mockTransaction.setAmount(amount);
        mockTransaction.setWallet(wallet);
        mockTransaction.setTimestamp(LocalDateTime.now());

        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

        Transaction transaction = walletService.deposit(walletId, amount);

        assertNotNull(transaction);
        assertEquals(TransactionType.DEPOSIT, transaction.getType());
        assertEquals(amount, transaction.getAmount());
    }

    @Test
    void testWithdrawInsufficientBalance() {
        UUID walletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(100);
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.ZERO);

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientBalanceException.class, () -> walletService.withdraw(walletId, amount));
    }

    @Test
    void testTransfer() {
        UUID fromWalletId = UUID.randomUUID();
        UUID toWalletId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(50);

        Wallet fromWallet = new Wallet();
        fromWallet.setId(fromWalletId);
        fromWallet.setBalance(BigDecimal.valueOf(100));

        Wallet toWallet = new Wallet();
        toWallet.setId(toWalletId);
        toWallet.setBalance(BigDecimal.ZERO);

        when(walletRepository.findById(fromWalletId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findById(toWalletId)).thenReturn(Optional.of(toWallet));

        Transaction mockTransaction = new Transaction();
        mockTransaction.setType(TransactionType.TRANSFER);
        mockTransaction.setAmount(amount);
        mockTransaction.setWallet(fromWallet);
        mockTransaction.setRelatedWallet(toWallet);
        mockTransaction.setTimestamp(LocalDateTime.now());

        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);

        Transaction transaction = walletService.transfer(fromWalletId, toWalletId, amount);

        assertNotNull(transaction);
        assertEquals(TransactionType.TRANSFER, transaction.getType());
        assertEquals(amount, transaction.getAmount());
    }

    @Test
    void testCalculateHistoricalBalance() {
        UUID walletId = UUID.randomUUID();
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.valueOf(100));

        LocalDateTime timestamp = LocalDateTime.now();

        Transaction deposit = new Transaction();
        deposit.setType(TransactionType.DEPOSIT);
        deposit.setAmount(BigDecimal.valueOf(50));
        deposit.setWallet(wallet);
        deposit.setTimestamp(timestamp.minusMinutes(40));

        Transaction withdraw = new Transaction();
        withdraw.setType(TransactionType.WITHDRAW);
        withdraw.setAmount(BigDecimal.valueOf(30));
        withdraw.setWallet(wallet);
        withdraw.setTimestamp(timestamp.minusMinutes(3));

        Transaction transferOut = new Transaction();
        transferOut.setType(TransactionType.TRANSFER);
        transferOut.setAmount(BigDecimal.valueOf(20));
        transferOut.setWallet(wallet);
        transferOut.setTimestamp(timestamp.minusMinutes(2));

        Transaction transferIn = new Transaction();
        transferIn.setType(TransactionType.TRANSFER);
        transferIn.setAmount(BigDecimal.valueOf(10));
        transferIn.setWallet(wallet);
        transferIn.setTimestamp(timestamp.minusMinutes(1));

        // Use a mutable list instead of List.of()
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(deposit);
        transactions.add(withdraw);
        transactions.add(transferOut);
        transactions.add(transferIn);

        // Mock para encontrar a carteira
        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        // Mock para retornar as transações
        when(transactionRepository.findByWalletIdAndTimestampLessThanEqual(walletId, timestamp))
                .thenReturn(transactions);

        BigDecimal historicalBalance = walletService.getHistoricalBalance(walletId, timestamp);

        // Saldo inicial: 100
        // DEPOSIT: +50 → 150
        // WITHDRAW: -30 → 120
        // TRANSFER (out): -20 → 100
        // TRANSFER (in): +10 → 110
        assertEquals(BigDecimal.valueOf(110), historicalBalance);
    }

}