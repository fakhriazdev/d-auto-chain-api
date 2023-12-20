package com.danamon.autochain.service;

import com.danamon.autochain.dto.Invoice.RequestInvoice;
import com.danamon.autochain.entity.Invoice;

public interface InvoiceService {
    Invoice invoiceGeneration(RequestInvoice requestInvoice);
}
