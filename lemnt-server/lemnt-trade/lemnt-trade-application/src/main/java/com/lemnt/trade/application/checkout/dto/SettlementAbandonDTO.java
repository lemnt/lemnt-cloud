package com.lemnt.trade.application.checkout.dto;

public record SettlementAbandonDTO(boolean abandoned, CheckoutReceiptDTO order) {}
