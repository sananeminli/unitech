package com.az.unitech.services;

import com.az.unitech.repositories.AccountRepository;
import com.az.unitech.enums.CurrencyCode;
import com.az.unitech.models.Account;
import com.az.unitech.models.User;
import com.az.unitech.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;


    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(authentication.getName()).thenReturn("testUser");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        accountService = new AccountService(accountRepository, userRepository);
    }



    @Test
    void testMoneyTransferSuccess() {

        User user = new User();
        user.setId(1L);
        Account sender = new Account(12345L, true, CurrencyCode.USD, 1000.0, user);
        Account receiver = new Account(54321L, true, CurrencyCode.USD, 500.0, user);
        double amount = 200.0;
        when(userRepository.findByPin("testUser")).thenReturn(user);
        when(accountRepository.findByAccountNumber(12345L)).thenReturn(sender);
        when(accountRepository.findByAccountNumber(54321L)).thenReturn(receiver);
        ResponseEntity<String> response = accountService.moneyTransfer(sender.getAccountNumber(), receiver.getAccountNumber(), amount);
        assertEquals("Money transfer was successful!", response.getBody());
        assertEquals(800.0, sender.getBalance());
        assertEquals(700.0, receiver.getBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
    }


    @Test
    void testMoneyTransferNegativeAmount() {

        User user = new User();
        user.setId(1L);
        Account sender = new Account(12345L, true, CurrencyCode.USD, 1000.0, user);
        Account receiver = new Account(54321L, true, CurrencyCode.USD, 500.0, user);
        double amount = -200.0;
        when(userRepository.findByPin("testUser")).thenReturn(user);
        when(accountRepository.findByAccountNumber(12345L)).thenReturn(sender);
        when(accountRepository.findByAccountNumber(54321L)).thenReturn(receiver);
        ResponseEntity<String> response = accountService.moneyTransfer(sender.getAccountNumber(), receiver.getAccountNumber(), amount);
        assertEquals("Amount cannot be less than zero!", response.getBody());
        verify(accountRepository, times(0)).save(any(Account.class));
    }


    @Test
    void testMoneyTransferInactiveAccount() {

        User user = new User();
        user.setId(1L);
        Account sender = new Account(12345L, true, CurrencyCode.USD, 1000.0, user);
        Account receiver = new Account(54321L, false, CurrencyCode.USD, 500.0, user);
        double amount = -200.0;
        when(userRepository.findByPin("testUser")).thenReturn(user);
        when(accountRepository.findByAccountNumber(12345L)).thenReturn(sender);
        when(accountRepository.findByAccountNumber(54321L)).thenReturn(receiver);
        ResponseEntity<String> response = accountService.moneyTransfer(sender.getAccountNumber(), receiver.getAccountNumber(), amount);
        assertEquals("One or both accounts are not active!", response.getBody());
        verify(accountRepository, times(0)).save(any(Account.class));
    }


    @Test
    void testMoneyTransferSameAccount() {
        Long accountNumber = 12345L;
        double amount = 200.0;
        ResponseEntity<String> response = accountService.moneyTransfer(accountNumber, accountNumber, amount);
        assertEquals("Cannot make transfer to the same account!", response.getBody());
        verify(accountRepository, times(0)).save(any());
    }

    @Test
    void testMoneyTransferSenderReceiverNotFound() {

        Long senderAccountNumber = 12345L;
        Long receiverAccountNumber = 54321L;
        double amount = 200.0;
        when(accountRepository.findByAccountNumber(senderAccountNumber)).thenReturn(null);
        when(accountRepository.findByAccountNumber(receiverAccountNumber)).thenReturn(null);
        ResponseEntity<String> response = accountService.moneyTransfer(senderAccountNumber, receiverAccountNumber, amount);
        assertEquals("Sender account or receiver account does not exist!", response.getBody());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testMoneyTransferAccountNotBelongToUser() {
        Long senderAccountNumber = 12345L;
        Long receiverAccountNumber = 54321L;
        double amount = 200.0;
        User otherUser = new User();
        otherUser.setId(1L);
        User loggedInUser = new User();
        loggedInUser.setId(2L);
        Account sender = new Account(senderAccountNumber, true, CurrencyCode.USD, 1000.0, otherUser);
        Account receiver = new Account(receiverAccountNumber, true, CurrencyCode.USD, 1000.0, otherUser);
        when(userRepository.findByPin("testUser")).thenReturn(loggedInUser);
        when(accountRepository.findByAccountNumber(senderAccountNumber)).thenReturn(sender);
        when(accountRepository.findByAccountNumber(receiverAccountNumber)).thenReturn(receiver);
        ResponseEntity<String> response = accountService.moneyTransfer(senderAccountNumber, receiverAccountNumber, amount);
        assertEquals("Account does not belong to the logged-in user!", response.getBody());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void testMoneyTransferInsufficientFunds() {
        Long senderAccountNumber = 12345L;
        Long receiverAccountNumber = 54321L;
        double amount = 2000.0;
        User user = new User(); // Create a new User instance
        user.setId(1L);
        Account sender = new Account(senderAccountNumber, true, CurrencyCode.USD, 1000.0, user);
        Account receiver = new Account(receiverAccountNumber, true, CurrencyCode.USD, 500.0, user);
        when(accountRepository.findByAccountNumber(senderAccountNumber)).thenReturn(sender);
        when(accountRepository.findByAccountNumber(receiverAccountNumber)).thenReturn(receiver);
        when(userRepository.findByPin(any())).thenReturn(user); // Allow any User object
        ResponseEntity<String> response = accountService.moneyTransfer(senderAccountNumber, receiverAccountNumber, amount);
        assertEquals("Insufficient funds in the account!", response.getBody());
        verify(accountRepository, never()).save(any());
    }
}
