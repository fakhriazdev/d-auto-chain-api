package com.danamon.autochain.dto.financing;

import com.danamon.autochain.entity.Company;

import java.util.Date;

public class FinancingResponse {
    String financing_id;
    Date date;
    String invoice_number;
    Long Amount;
    String company_name;
    String status;
}
