package com.az.unitech.services;

import com.az.unitech.models.Currency;
import com.az.unitech.repositories.CurrencyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class CurrencyServiceTest {
    @Mock
    private CurrencyRepository currencyRepository;


    @InjectMocks
    private CurrencyService currencyService;

    @Test
    public void testGetValidCurrencyPair() {
        double value = currencyService.getCurrencyPairValue("USD", "EUR");
        assertTrue(value > 0);
    }

    @Test
    public void testInvalidFirstCurrency() {
        double value = currencyService.getCurrencyPairValue("XXX", "EUR");
        assertEquals(-1, value);
    }

    @Test
    public void testInvalidSecondCurrency() {
        double value = currencyService.getCurrencyPairValue("USD", "YYY");
        assertEquals(-1, value);
    }

    @Test
    public void testNullSecondCurrency() {
        double value = currencyService.getCurrencyPairValue("USD", null);
        assertEquals(-1, value);
    }

    @Test
    public void testNullFirstCurrency() {
        double value = currencyService.getCurrencyPairValue(null, "EUR");
        assertEquals(-1, value);
    }

    @Test
    public void testBothCurrenciesNull() {
        double value = currencyService.getCurrencyPairValue(null, null);
        assertEquals(-1, value);
    }


    @Test
    public void testNewCurrency() {

        when(currencyRepository.findById("AZN/TRY")).thenReturn(Optional.empty());
        double value = currencyService.getCurrencyPairValue("AZN" , "TRY");
        ResponseEntity responseEntity = currencyService.getRate("AZN", "TRY");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(value, responseEntity.getBody());
        verify(currencyRepository, times(2)).save(Mockito.any(Currency.class));

    }
    @Test
    public void testUnsupportedCurrencyCode() {

        when(currencyRepository.findById("XXX/EUR")).thenReturn(Optional.empty());
        ResponseEntity responseEntity = currencyService.getRate("XXX", "EUR");
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Unsupported currency code", responseEntity.getBody());
    }

    @Test
    public void testUnsupportedLongCurrencyCode() {
        ResponseEntity responseEntity = currencyService.getRate("AZNxxx", "EUR");
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Unsupported currency code!", responseEntity.getBody());
    }

    @Test
    public void testCurrencyPairNotNeedsRefresh() {

        LocalDateTime now = LocalDateTime.now();
        Currency existingCurrency = new Currency("USD/EUR", now.minusMinutes(0), 0.85);
        when(currencyRepository.findById("USD/EUR")).thenReturn(Optional.of(existingCurrency));
        when(currencyRepository.findById("EUR/USD")).thenReturn(Optional.of(new Currency("EUR/USD", now.minusMinutes(2), 1.18)));
        ResponseEntity responseEntity = currencyService.getRate("USD", "EUR");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(0.85, responseEntity.getBody());
    }

    @Test
    public void testCurrencyPairNeedsRefresh() {

        LocalDateTime now = LocalDateTime.now();
        Currency existingCurrency = new Currency("USD/EUR", now.minusMinutes(2), 0.85);
        Currency existingReverseCurrency = new Currency("EUR/USD", now.minusMinutes(2), 1/0.85);
        when(currencyRepository.findById("USD/EUR")).thenReturn(Optional.of(existingCurrency));
        when(currencyRepository.findById("EUR/USD")).thenReturn(Optional.of(existingReverseCurrency ));
        double value = currencyService.getCurrencyPairValue("USD" , "EUR");
        ResponseEntity responseEntity = currencyService.getRate("USD", "EUR");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(value, responseEntity.getBody());
        assertEquals(1/value , existingReverseCurrency.getValue());
        verify(currencyRepository, times(2)).save(Mockito.any(Currency.class));

    }

    @Test
    public void testSamePair(){
        ResponseEntity responseEntity = currencyService.getRate("USD", "USD");
        assertEquals( responseEntity.getStatusCode() , HttpStatus.BAD_REQUEST );
    }

}
