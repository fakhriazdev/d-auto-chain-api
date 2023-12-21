package com.danamon.autochain.service;

import com.danamon.autochain.dto.Invoice.InvoiceResponse;
import com.danamon.autochain.dto.Invoice.RequestInvoice;
import com.danamon.autochain.dto.Invoice.SearchInvoiceRequest;
import com.danamon.autochain.entity.Invoice;
import org.springframework.data.domain.Page;

public interface InvoiceService {
    Invoice invoiceGeneration(RequestInvoice requestInvoice);

    Page<InvoiceResponse> getAll(SearchInvoiceRequest request);

}
