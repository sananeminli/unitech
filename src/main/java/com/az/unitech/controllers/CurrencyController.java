package com.az.unitech.controllers;


import com.az.unitech.services.CurrencyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Currency Api", description = "Getting latest currency pair values.")

public class CurrencyController {
    @Autowired
    CurrencyService currencyService;


    @Operation(summary ="This is for getting the value of the requested currency pair. I have used a real endpoint for obtaining currency pair values. In my solution, I am storing the value of the pair and the requested datetime. I check in every request if that pair's current value is refreshed in one minute. If the last refresh time is more than two minutes, I fetch a new value from the endpoint. Additionally, when I retrieve the value for a pair, I also store the reverse pair by dividing 1 by the received pair value. For example, when I obtain USD/AZN, I also store AZN/USD by dividing 1 by the value. With this solution, I am able to reduce costs by 50 percent.")
    @GetMapping("/currency/{firstValue}/{secondValue}")
    public ResponseEntity getCurrency( @PathVariable String firstValue, @PathVariable String secondValue){
        return currencyService.getRate(firstValue.toUpperCase(), secondValue.toUpperCase());
    }
}
