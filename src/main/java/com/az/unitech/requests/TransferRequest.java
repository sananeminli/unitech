package com.az.unitech.requests;

import lombok.Data;

@Data
public class TransferRequest {
private Long receiverAccountNumber;
private Long senderAccountNumber;
private double amount;
}
