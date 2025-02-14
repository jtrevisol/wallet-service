package com.example.wattet.controller;

import com.example.wattet.dto.TransactionDTO;
import com.example.wattet.dto.WalletResponseDTO;
import com.example.wattet.model.Transaction;
import com.example.wattet.repository.TransactionRepository;
import com.example.wattet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/wallets")
@Tag(name = "Wallet Service", description = "Manage wallets, balances, and transactions for accounts")
@Validated
@Slf4j
public class WalletController {

    private final WalletService walletService;
    private final ModelMapper modelMapper;
    private final TransactionRepository transactionRepository;

    public WalletController(WalletService walletService, ModelMapper modelMapper, TransactionRepository transactionRepository) {
        this.walletService = walletService;
        this.modelMapper = modelMapper;
        this.transactionRepository = transactionRepository;
    }

    @Operation(summary = "Create a new wallet for an account")
    @PostMapping
    public ResponseEntity<WalletResponseDTO> createWallet(
            @Parameter(description = "ID of the account to associate with the wallet", required = true)
            @RequestParam @NotNull UUID accountId) {
        log.info("Creating wallet for accountId: {}", accountId);
        return ResponseEntity.ok(new WalletResponseDTO(walletService.createWallet(accountId).getId()));
    }

    @Operation(summary = "Get the current balance of a wallet")
    @GetMapping("/{walletId}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @Parameter(description = "ID of the wallet to retrieve the balance for", required = true)
            @PathVariable @NotNull UUID walletId) {
        log.info("Retrieving balance for walletId: {}", walletId);
        return ResponseEntity.ok(walletService.getBalance(walletId));
    }

    @Operation(summary = "Get the historical balance of a wallet at a specific time")
    @GetMapping("/{walletId}/historical-balance")
    public ResponseEntity<BigDecimal> getHistoricalBalance(
            @Parameter(description = "ID of the wallet to retrieve the historical balance for", required = true)
            @PathVariable @NotNull UUID walletId,
            @Parameter(description = "Timestamp to retrieve the historical balance at", required = true)
            @RequestParam LocalDateTime timestamp) {
        log.info("Retrieving historical balance for walletId: {} at timestamp: {}", walletId, timestamp);
        return ResponseEntity.ok(walletService.getHistoricalBalance(walletId, timestamp));
    }

    @Operation(summary = "Deposit funds into a wallet")
    @PostMapping("/{walletId}/deposit")
    public ResponseEntity<Transaction> deposit(
            @Parameter(description = "ID of the wallet to deposit funds into", required = true)
            @PathVariable @NotNull UUID walletId,
            @Parameter(description = "Amount to deposit", required = true)
            @RequestParam BigDecimal amount) {
        log.info("Depositing {} into walletId: {}", amount, walletId);
        return ResponseEntity.ok(walletService.deposit(walletId, amount));
    }

    @Operation(summary = "Withdraw funds from a wallet")
    @PostMapping("/{walletId}/withdraw")
    public ResponseEntity<TransactionDTO> withdraw(
            @Parameter(description = "ID of the wallet to withdraw funds from", required = true)
            @PathVariable @NotNull UUID walletId,
            @Parameter(description = "Amount to withdraw", required = true)
            @RequestParam BigDecimal amount) {
        log.info("Withdrawing {} from walletId: {}", amount, walletId);
        return ResponseEntity.ok(modelMapper.map(walletService.withdraw(walletId, amount), TransactionDTO.class));
    }

    @Operation(summary = "Transfer funds between wallets")
    @PostMapping("/{fromWalletId}/transfer")
    public ResponseEntity<TransactionDTO> transfer(
            @Parameter(description = "ID of the source wallet to transfer funds from", required = true)
            @PathVariable @NotNull UUID fromWalletId,
            @Parameter(description = "ID of the destination wallet to transfer funds to", required = true)
            @RequestParam @NotNull UUID toWalletId,
            @Parameter(description = "Amount to transfer", required = true)
            @RequestParam BigDecimal amount) {
        log.info("Transferring {} from walletId: {} to walletId: {}", amount, fromWalletId, toWalletId);
        return ResponseEntity.ok(modelMapper.map(walletService.transfer(fromWalletId, toWalletId, amount), TransactionDTO.class));
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<List<TransactionDTO>> getTransactionHistory(@PathVariable UUID walletId) {
        List<Transaction> transactions = transactionRepository.findByWalletId(walletId);
        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(transaction -> modelMapper.map(transaction, TransactionDTO.class))
                .toList();
        return ResponseEntity.ok(transactionDTOs);
    }

}