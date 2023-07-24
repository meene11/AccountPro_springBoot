package com.example.acc.service;

import com.example.acc.domain.Account;
import com.example.acc.domain.AccountUser;
import com.example.acc.dto.AccountDto;
import com.example.acc.dto.AccountInfo;
import com.example.acc.exception.AccountException;
import com.example.acc.repository.AccountRepository;
import com.example.acc.repository.AccountUserRepository;
import com.example.acc.type.AccountStatus;
import com.example.acc.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.acc.type.AccountStatus.IN_USE;
import static com.example.acc.type.AccountStatus.UNREGISTERED;
import static com.example.acc.type.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    /**
     사용자가 있는지 조회
     계좌의 번호를 생성하고
     계좌를 저장하고, 그정보를 넘긴다
     */
    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(()->new AccountException(USER_NOT_FOUND));

        validateCreateAccount(accountUser);

        String newAccounNumber = accountRepository.findFirstByOrderByIdDesc()
                .map(account->(Integer.parseInt(account.getAccountNumber()))+1+"")
                .orElse("1000000000");

        return AccountDto.fromEntity(
                accountRepository.save(Account.builder()
                                .accountUser(accountUser)
                                .accountStatus(IN_USE)
                                .accountNumber(newAccounNumber)
                                .balance(initialBalance)
                                .registeredAt(LocalDateTime.now())
                                .build())
        );

    }

    private void validateCreateAccount(AccountUser accountUser) {
        if(accountRepository.countByAccountUser(accountUser) >=10){
            throw new AccountException(ErrorCode.MAX_ACCOUNT_PER_USER_10);
        }
    }

    @Transactional
    public Account getAccount(Long id) {
        if(id < 0){
            throw new RuntimeException("Minus");
        }
        return accountRepository.findById(id).get();
    }

    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(()->new AccountException(USER_NOT_FOUND));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(()->new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateDeleteAccount(accountUser, account);

          account.setAccountStatus(AccountStatus.UNREGISTERED);
          account.setUnregisteredAt(LocalDateTime.now());

          accountRepository.save(account);

          return AccountDto.fromEntity(account);
    }
    private void validateDeleteAccount(AccountUser accountUser, Account account) {
        if(!Objects.equals(accountUser.getId(), account.getAccountUser().getId())){
            throw new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
        }

//        if(account.getAccountStatus().equals("UNREGISTERED")){
        if(account.getAccountStatus() == UNREGISTERED ){
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        }

        if(account.getBalance()>0){
            throw new AccountException(ErrorCode.BALANCE_NOT_EMPTY);
        }
    }
    @Transactional
    public List<AccountDto> getAccountsByUserId(Long userId) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(()->new AccountException(USER_NOT_FOUND));

        List<Account> accounts = accountRepository
            .findByAccountUser(accountUser);
/*
        // map 사용방법1
         return accounts.stream()
                .map(account -> AccountDto.fromEntity(account))
                .collect(Collectors.toList());
 */
        // map 사용방법2 이걸더권장
        return accounts.stream()
                .map(AccountDto::fromEntity)
                .collect(Collectors.toList());

    }

}
