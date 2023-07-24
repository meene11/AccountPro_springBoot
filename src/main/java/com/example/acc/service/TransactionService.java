package com.example.acc.service;

import com.example.acc.domain.Account;
import com.example.acc.domain.AccountUser;
import com.example.acc.domain.Transaction;
import com.example.acc.dto.TransactionDto;
import com.example.acc.exception.AccountException;
import com.example.acc.repository.AccountRepository;
import com.example.acc.repository.AccountUserRepository;
import com.example.acc.repository.TransactionRepository;
import com.example.acc.type.AccountStatus;
import com.example.acc.type.ErrorCode;
import com.example.acc.type.TransactionResultType;
import com.example.acc.type.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

//import static com.example.acc.type.TransactionResultType.F;
import static com.example.acc.type.TransactionType.USE;
//import static com.example.acc.type.TransactionResultType.S;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountUserRepository accountUserRepository;
    private final AccountRepository accountRepository;

    /*
    사용자 없는경우, 계좌 없는 경우, 사용자 아이디, 계좌 소유주 다른경우
    계좌가 이미 해지 상태경우, 거래금액이 잔액보다 큰경우,
    거래금액이 너무 작거나 큰 경우 실패 응답
     */
    @Transactional
    public TransactionDto useBalance (Long userId, String accountNumber, Long amount){
        AccountUser user = accountUserRepository.findById(userId)
                .orElseThrow(()->new AccountException(ErrorCode.USER_NOT_FOUND));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(()->new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateUseBalance(user, account, amount);

        account.useBalance(amount);

        return TransactionDto.fromEntity(saveAndGetTransaction(USE,TransactionResultType.S, account, amount));
    }

    private void validateUseBalance(AccountUser user, Account account, Long amount) {
        if(!Objects.equals(user.getId(), account.getAccountUser().getId())){
            throw  new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
        }
        if(account.getAccountStatus() != AccountStatus.IN_USE){
            throw  new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        }
        if(account.getBalance() < amount){
            throw  new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }
    }
    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()->new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        saveAndGetTransaction(TransactionType.USE, TransactionResultType.F, account, amount);
    }
    private Transaction saveAndGetTransaction(
                                    TransactionType transactionType,
                                    TransactionResultType transactionResultType,
                                    Account account,
                                    Long amount) {


        return transactionRepository.save(
                Transaction.builder()
                        .transactionType(TransactionType.CANCEL)
                        .transactionResultType(transactionResultType)
                        .account(account)
                        .amount(amount)
                        .balanceSnapshot(account.getBalance())
                        .transactionId(UUID.randomUUID().toString().replace("-", ""))
                        .transactedAt(LocalDateTime.now())
                        .build()
        );
    }
    @Transactional
    public TransactionDto cancelBalance(String transactionId, String accountNumber, Long amount){


        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(()->new AccountException(ErrorCode.TRANSACTION_NOT_FOUND));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()->new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateCancelBalance(transaction, account, amount);

        account.cancelBalance(amount);

        return  TransactionDto.fromEntity(
                saveAndGetTransaction(TransactionType.CANCEL, TransactionResultType.S, account, amount)
        );

    }

    private void validateCancelBalance(Transaction transaction, Account account, Long amount) {
        if(!Objects.equals(transaction.getAccount().getId(), account.getId())){
            throw new AccountException(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH);
        }
        if(!Objects.equals(transaction.getAmount(), amount)){
            throw new AccountException(ErrorCode.CANCEL_MUST_FULLY);
        }
        if(transaction.getTransactedAt().isBefore(LocalDateTime.now().minusYears(1))){
            throw new AccountException(ErrorCode.TO_OLD_ORDER_TO_CANCEL);
        }
    }

    @Transactional
    public void saveFailedCancelTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(()->new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        saveAndGetTransaction(TransactionType.CANCEL, TransactionResultType.F, account, amount);

    }

    public TransactionDto queryTransaction(String transactionId) {
        return TransactionDto.fromEntity(
                transactionRepository.findByTransactionId(transactionId)
                        .orElseThrow(()->new AccountException(ErrorCode.TRANSACTION_NOT_FOUND))
        );

    }
}
