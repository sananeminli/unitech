package com.az.unitech.services;

import com.az.unitech.repositories.CurrencyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.az.unitech.models.Currency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CurrencyService {


    @Autowired
    private CurrencyRepository currencyRepository;

    public double getCurrencyPairValue(String firstCurrency, String secondCurrency) {
        try {
            String apiUrl = "https://open.er-api.com/v6/latest/" + firstCurrency;
            URL url = new URL(apiUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            String jsonResponse = response.toString();

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseData = mapper.readValue(jsonResponse, Map.class);

            if (responseData.containsKey("error-type") && responseData.get("error-type").equals("unsupported-code")) {
                System.err.println("Error: Unsupported currency code");
                return -1;
            }

            Map<String, Double> rates = (Map<String, Double>) responseData.get("rates");

            if (rates == null || !rates.containsKey(secondCurrency)) {
                System.err.println("Error: Rates map is null or doesn't contain the second currency");
                return -1;
            }

            double value = rates.get(secondCurrency);
            con.disconnect();
            return value;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }



    public ResponseEntity getRate(String firstValue, String secondValue) {
        if (firstValue.equals(secondValue)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Currency codes can't be same!");
        }
        String pair = firstValue + "/" + secondValue;
        String reversePair  = secondValue + "/" + firstValue;
        Optional<Currency> optionalCurrency = currencyRepository.findById(pair);
        LocalDateTime now = LocalDateTime.now();

        if (!optionalCurrency.isPresent()) {
            double value = getCurrencyPairValue(firstValue, secondValue);
            if (value < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unsupported currency code");            }
            currencyRepository.save(new Currency(pair,now, value));
            currencyRepository.save(new Currency(reversePair, now,1/ value));
            return ResponseEntity.ok(value);
        }
        Optional<Currency> optionalReverseCurrency = currencyRepository.findById(reversePair);

        Currency currencyReverse = optionalReverseCurrency.get();

        Currency currency = optionalCurrency.get();
        if (currency.getLastUpdate().isBefore(LocalDateTime.now().minusMinutes(1))) {
            double value = getCurrencyPairValue(firstValue, secondValue);
            if (value < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unsupported currency code");            }

            currency.setValue(value);
            currency.setLastUpdate(now);
            currencyReverse.setValue(1/value);
            currencyReverse.setLastUpdate(now);
            currencyRepository.save(currency);
            currencyRepository.save(currencyReverse);
            return ResponseEntity.ok(value);
        }
        return ResponseEntity.ok(currency.getValue());
    }



}
