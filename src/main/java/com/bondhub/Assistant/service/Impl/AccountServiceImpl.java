package com.bondhub.Assistant.service.Impl;

import com.bondhub.Assistant.dto.request.AccountRequest;
import com.bondhub.Assistant.dto.response.AccountResponse;
import com.bondhub.Assistant.dto.response.PageResponse;
import com.bondhub.Assistant.entity.Account;
import com.bondhub.Assistant.entity.enums.UserStatus;
import com.bondhub.Assistant.exception.AppException;
import com.bondhub.Assistant.exception.ErrorCode;
import com.bondhub.Assistant.repository.AccountRepository;
import com.bondhub.Assistant.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        if (accountRepository.findByEmail(request.getEmail()).isPresent())
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        if (request.getCode() != null && accountRepository.findByCode(request.getCode()).isPresent())
            throw new AppException(ErrorCode.USER_CODE_EXISTED);

        Account account = Account.builder()
                .email(request.getEmail())
                .code(request.getCode())
                .passwordHash(passwordEncoder.encode(request.getPassword() != null ? request.getPassword() : "123456"))
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return toResponse(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountResponse updateAccount(UUID id, AccountRequest request) {
        Account account = accountRepository.findActiveById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));

        if (request.getEmail() != null && !request.getEmail().equals(account.getEmail())) {
            if (accountRepository.findByEmail(request.getEmail()).isPresent())
                throw new AppException(ErrorCode.EMAIL_EXISTED);
            account.setEmail(request.getEmail());
        }
        if (request.getCode() != null && !request.getCode().equals(account.getCode())) {
            if (accountRepository.findByCode(request.getCode()).isPresent())
                throw new AppException(ErrorCode.USER_CODE_EXISTED);
            account.setCode(request.getCode());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getIsActive() != null) {
            account.setIsActive(request.getIsActive());
        }
        
        return toResponse(accountRepository.save(account));
    }

    @Override
    @Transactional
    public void deleteAccount(UUID id) {
        Account account = accountRepository.findActiveById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));
        account.setIsActive(false);
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void recoverAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));
        account.setIsActive(true);
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void deleteResources(List<UUID> ids) {
        List<Account> accounts = accountRepository.findAllById(ids);
        accounts.forEach(account -> account.setIsActive(false));
        accountRepository.saveAll(accounts);
    }

    @Override
    @Transactional
    public void recoverResources(List<UUID> ids) {
        List<Account> accounts = accountRepository.findAllById(ids);
        accounts.forEach(account -> account.setIsActive(true));
        accountRepository.saveAll(accounts);
    }

    @Override
    public PageResponse<List<AccountResponse>> getAllAccounts(Pageable pageable) {
        Page<Account> page = accountRepository.findAll(pageable);
        return PageResponse.fromPage(page, this::toResponse);
    }

    @Override
    public AccountResponse getAccountDetails(UUID id) {
        Account account = accountRepository.findActiveById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));
        return toResponse(account);
    }

    @Override
    @Transactional
    public void changePassword(UUID id, String newPassword) {
        Account account = accountRepository.findActiveById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXISTED));
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .email(account.getEmail())
                .code(account.getCode())
                .isActive(account.getIsActive())
                .lastLogin(account.getLastLogin())
                .createdAt(account.getCreatedAt())
                .createdBy(account.getCreatedBy())
                .updatedAt(account.getUpdatedAt())
                .updatedBy(account.getUpdatedBy())
                .build();
    }
}
