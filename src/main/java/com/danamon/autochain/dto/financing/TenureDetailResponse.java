package com.danamon.autochain.dto.financing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
@AllArgsConstructor
public class TenureDetailResponse {
    String tenure_id;
    String due_date;
    Double amount;
    String status;
}
