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
    private Long income;
    private Long incomeLastMonth;
    private Long expense;
    private Long expenseLastMonth;
}
