package com.danamon.autochain.dto.financing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AcceptRequest {
    String financing_id;
    String type = "receivable";
}
