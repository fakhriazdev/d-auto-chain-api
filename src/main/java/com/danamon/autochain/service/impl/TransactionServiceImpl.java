package com.danamon.autochain.service.impl;

import com.danamon.autochain.dto.transaction.TransactionRequest;
import com.danamon.autochain.entity.TransactionDanamon;
import com.danamon.autochain.repository.TransactionRepository;
import com.danamon.autochain.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    @Override
    public void createTransaction(TransactionRequest request) {
        TransactionDanamon transaction = TransactionDanamon.builder()
                .createdDate(request.getCreatedDate())
                .recipientId(request.getRecipientId())
                .paymentstatus(request.getPaymentStatus())
                .financingType(request.getFinancingType())
                .amount(request.getAmount())
                .build();

        transactionRepository.saveAndFlush(transaction);
    }
}
