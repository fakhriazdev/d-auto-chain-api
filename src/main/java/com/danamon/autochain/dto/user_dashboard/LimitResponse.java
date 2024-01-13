package com.danamon.autochain.dto.user_dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LimitResponse {
    private Double limit;
    private Double limitUsed;
    private Double income;
    private Double incomeLastMonth;
    private Double incomeDifferencePercentage;
    private Double expense;
    private Double expenseLastMonth;
    private Double expenseDifferencePercentage;
}
