package com.danamon.autochain.service;

import com.danamon.autochain.dto.Invoice.request.RequestInvoice;
import com.danamon.autochain.dto.Invoice.response.InvoiceDetailResponse;
import com.danamon.autochain.dto.Invoice.response.InvoiceResponse;

public interface InvoiceService {
    InvoiceResponse invoiceGeneration(RequestInvoice requestInvoice);
    InvoiceDetailResponse getInvoiceDetail(String id);
}
