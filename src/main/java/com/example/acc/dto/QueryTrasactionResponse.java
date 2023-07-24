package com.example.acc.dto;

import com.example.acc.type.TransactionResultType;
import com.example.acc.type.TransactionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryTrasactionResponse {
    private String accountNumber;
    private TransactionType transactionType;
    private TransactionResultType transactionResult;
    private String transactionId;
    private Long amount;
    private LocalDateTime transactedAt;

    public static QueryTrasactionResponse from(TransactionDto transactionDto) {
        return QueryTrasactionResponse.builder()
                .accountNumber(transactionDto.getAccountNumber())
                .transactionType(transactionDto.getTransactionType())
                .transactionResult(transactionDto.getTransactionResultType())
                .transactionId(transactionDto.getTransactionId())
                .transactedAt(transactionDto.getTransactedAt())
                .amount(transactionDto.getAmount())
                .build();
    }
}
