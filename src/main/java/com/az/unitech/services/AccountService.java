package com.az.unitech.services;

import com.az.unitech.models.Account;
import com.az.unitech.repositories.AccountRepository;
import com.az.unitech.enums.CurrencyCode;
import com.az.unitech.models.User;
import com.az.unitech.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository,
                          UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public Set<Account> generateRandomAccounts(User user) {

        Random random = new Random();

        CurrencyCode[] currencyCodes = CurrencyCode.values();
        int accountCount = random.nextInt(2, 5);

        Set<Account> accounts   = new HashSet<Account>();
        accountRepository.save(new Account(random.nextLong(1,500) , true  , CurrencyCode.AZN , 100.0 , user));

        for (int i = 0; i < accountCount; i++) {
            int randomCurrencyIndex = random.nextInt(0,currencyCodes.length);
            Long randomAccNumber  = random.nextLong(1,500);
            Account newAccount  = new Account(randomAccNumber , random.nextBoolean() , currencyCodes[randomCurrencyIndex] ,  random.nextDouble(100.0,1000.0) , user);
            accountRepository.save(newAccount);

        }
        return  accounts;

    }

    public ResponseEntity<String> moneyTransfer(Long senderAccountNumber, Long receiverAccountNumber, double amount) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String pin = authentication.getName();

        User user = userRepository.findByPin(pin);
        Account sender = accountRepository.findByAccountNumber(senderAccountNumber);
        Account receiver = accountRepository.findByAccountNumber(receiverAccountNumber);

        if (senderAccountNumber.equals(receiverAccountNumber)) {
            return ResponseEntity.badRequest().body("Cannot make transfer to the same account!");
        } else if (sender == null || receiver == null) {
            return ResponseEntity.badRequest().body("Sender account or receiver account does not exist!");
        } else if (!sender.getUser().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body("Account does not belong to the logged-in user!");
        } else if (!sender.isActivityStatus() || !receiver.isActivityStatus()) {
            return ResponseEntity.badRequest().body("One or both accounts are not active!");
        } else if (sender.getBalance() < amount) {
            return ResponseEntity.badRequest().body("Insufficient funds in the account!");
        } else if (amount<  0 ) {
            return ResponseEntity.badRequest().body("Amount cannot be less than zero!");
        }

        double newSenderBalance = sender.getBalance() - amount;
        double newReceiverBalance = receiver.getBalance() + amount;
        sender.setBalance(newSenderBalance);
        receiver.setBalance(newReceiverBalance);
        accountRepository.save(sender);
        accountRepository.save(receiver);

        return ResponseEntity.ok("Money transfer was successful!");
    }


    public List<Account> getActiveAccounts(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String pin = authentication.getName();
        return accountRepository.getAccounts(pin);
    }
}
