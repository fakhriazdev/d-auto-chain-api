package com.danamon.autochain.service;

import com.danamon.autochain.dto.transaction.TransactionRequest;

public interface TransactionService {
    void createTransaction(TransactionRequest request);
}
