package com.az.unitech.controllers;

import com.az.unitech.models.Account;
import com.az.unitech.services.AccountService;
import com.az.unitech.requests.TransferRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Accounts Api", description = "Getting active accounts and doing money transfers.")

public class AccountController {
    @Autowired
    private  AccountService accountService;
    @GetMapping("/accounts")
    public List<Account> getAccounts(){
        return accountService.getActiveAccounts();
    }


    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest transferRequest){
        return accountService.moneyTransfer( transferRequest.getSenderAccountNumber() , transferRequest.getReceiverAccountNumber(), transferRequest.getAmount());
    }
}
