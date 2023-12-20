package com.danamon.autochain.service;

import com.danamon.autochain.dto.Invoice.request.RequestInvoice;
import com.danamon.autochain.dto.Invoice.response.ResponseInvoice;

public interface InvoiceService {
    ResponseInvoice invoiceGeneration(RequestInvoice requestInvoice);
}
