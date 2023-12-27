package com.danamon.autochain.service;

import com.danamon.autochain.constant.invoice.ProcessingStatusType;
import com.danamon.autochain.dto.Invoice.request.RequestInvoice;
import com.danamon.autochain.dto.Invoice.request.SearchInvoiceRequest;
import com.danamon.autochain.dto.Invoice.response.InvoiceDetailResponse;
import com.danamon.autochain.dto.Invoice.response.InvoiceResponse;
import org.springframework.data.domain.Page;

public interface InvoiceService {
    InvoiceResponse invoiceGeneration(RequestInvoice requestInvoice);

    InvoiceDetailResponse updateInvoiceStatus(String id, ProcessingStatusType processingStatusType);

    Page<InvoiceResponse> getAll(SearchInvoiceRequest request);

    InvoiceDetailResponse getInvoiceDetail(String id);
}
