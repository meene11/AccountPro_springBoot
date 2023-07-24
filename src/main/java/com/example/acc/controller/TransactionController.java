package com.example.acc.controller;

import com.example.acc.dto.CancelBalance;
import com.example.acc.dto.QueryTrasactionResponse;
import com.example.acc.dto.TransactionDto;
import com.example.acc.dto.UseBalance;
import com.example.acc.exception.AccountException;
import com.example.acc.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/*
잔액 관련 컨트롤러
1 잔액 사용
2 잔액 사용 취소
3 거래 확인
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transaction/use")
    public  UseBalance.Response useBalance(
        @Valid @RequestBody UseBalance.Request request
    ) {
        try {
            return UseBalance.Response.from(
                    transactionService.useBalance(request.getUserId(),
                            request.getAccountNumber(), request.getAmount()));
        } catch (AccountException e){
            log.error("Failed to use balance.");

            transactionService.saveFailedUseTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );

            throw  e;
        }
    }

    @PostMapping("/transaction/cancel")
    public CancelBalance.Response cancelBalance(
            @Valid @RequestBody CancelBalance.Request request
    ) {
        try {
            return CancelBalance.Response.from(
                    transactionService.cancelBalance(request.getTransactionId(),
                            request.getAccountNumber(), request.getAmount()));
        } catch (AccountException e){
            log.error("Failed to use balance.");

            transactionService.saveFailedCancelTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );

            throw  e;
        }
    }


    @GetMapping("/transaction/{transactionId}")
    public QueryTrasactionResponse queryTrasactionResponse(
            @PathVariable String transactionId) {

        return QueryTrasactionResponse.from(transactionService.queryTransaction(transactionId));

    }










}
