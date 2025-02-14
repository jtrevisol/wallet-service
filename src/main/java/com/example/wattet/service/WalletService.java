package com.example.wattet.service;

import com.example.wattet.exception.InsufficientBalanceException;
import com.example.wattet.exception.InvalidAmountException;
import com.example.wattet.exception.WalletAlreadyExistsException;
import com.example.wattet.exception.WalletNotFoundException;
import com.example.wattet.model.Transaction;
import com.example.wattet.model.TransactionType;
import com.example.wattet.model.Wallet;
import com.example.wattet.repository.TransactionRepository;
import com.example.wattet.repository.WalletRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository, TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Wallet createWallet(@NotNull UUID accountId) {
        walletRepository.findByAccountId(accountId).ifPresent(w -> {
            throw new WalletAlreadyExistsException("Account already has a wallet");
        });

        Wallet wallet = new Wallet();
        wallet.setAccountId(accountId);
        wallet.setBalance(BigDecimal.ZERO);
        return walletRepository.save(wallet);
    }

    public BigDecimal getBalance(@NotNull @Valid UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));
        return wallet.getBalance();
    }

    @Transactional
    public Transaction deposit(@NotNull UUID walletId, BigDecimal amount) {
        validateAmount(amount);
        Wallet wallet = getWalletById(walletId);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
        log.info("Deposit of {} made to wallet {}", amount, walletId);
        return createTransaction(wallet, TransactionType.DEPOSIT, amount, null);
    }

    @Transactional
    public Transaction withdraw(@NotNull UUID walletId, BigDecimal amount) {
        validateAmount(amount);
        Wallet wallet = getWalletById(walletId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in wallet: " + walletId);
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
        log.info("Withdrawal of {} made from wallet {}", amount, walletId);
        return createTransaction(wallet, TransactionType.WITHDRAW, amount, null);
    }

    @Transactional
    public Transaction transfer(@NotNull UUID fromWalletId, @NotNull UUID toWalletId, BigDecimal amount) {
        validateAmount(amount);
        if (fromWalletId.equals(toWalletId)) {
            throw new RuntimeException("Cannot transfer to the same wallet");
        }
        Wallet fromWallet = getWalletById(fromWalletId);
        Wallet toWallet = getWalletById(toWalletId);

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in source wallet: " + fromWalletId);
        }

        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);
        log.info("Transfer of {} from wallet {} to wallet {}", amount, fromWalletId, toWalletId);
        return createTransaction(fromWallet, TransactionType.TRANSFER, amount, toWallet);
    }

    private Wallet getWalletById(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));
    }

    private void validateAmount(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
    }

    private Transaction createTransaction(Wallet wallet, TransactionType type, BigDecimal amount, Wallet relatedWallet) {
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setRelatedWallet(relatedWallet);
        return transactionRepository.save(transaction);
    }

    public BigDecimal getHistoricalBalance(@NotNull UUID walletId, LocalDateTime timestamp) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        List<Transaction> transactions = transactionRepository
                .findByWalletIdAndTimestampLessThanEqual(walletId, timestamp);

        transactions.sort(Comparator.comparing(Transaction::getTimestamp)
                .thenComparing(Transaction::getId));

        BigDecimal historicalBalance = wallet.getBalance();

        for (Transaction transaction : transactions) {
            switch (transaction.getType()) {
                case DEPOSIT -> historicalBalance = historicalBalance.subtract(transaction.getAmount());
                case WITHDRAW -> historicalBalance = historicalBalance.add(transaction.getAmount());
                case TRANSFER -> {
                    if (walletId.equals(transaction.getWallet().getId())) {
                        historicalBalance = historicalBalance.add(transaction.getAmount());
                    } else {
                        historicalBalance = historicalBalance.subtract(transaction.getAmount());
                    }
                }
            }
        }

        return historicalBalance;
    }

}