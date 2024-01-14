package com.danamon.autochain.service;

import com.danamon.autochain.constant.invoice.ProcessingStatusType;
import com.danamon.autochain.dto.Invoice.request.RequestInvoice;
import com.danamon.autochain.dto.Invoice.request.RequestInvoiceStatus;
import com.danamon.autochain.dto.Invoice.request.SearchInvoiceRequest;
import com.danamon.autochain.dto.Invoice.response.InvoiceDetailResponse;
import com.danamon.autochain.dto.Invoice.response.InvoiceResponse;
import com.danamon.autochain.entity.Invoice;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InvoiceService {

    void approve_invoice(String id);

    InvoiceResponse invoiceGeneration(RequestInvoice requestInvoice);

    InvoiceDetailResponse updateInvoiceStatus(RequestInvoiceStatus requestInvoiceStatus);

    void updateInvoiceIssueLog(RequestInvoiceStatus requestInvoiceStatus);

    Page<InvoiceResponse> getAll(SearchInvoiceRequest request);

    InvoiceDetailResponse getInvoiceDetail(String id);
    List<Invoice> getInvoiceByRecepientId(String id);
    List<Invoice> getInvoiceBySenderId(String id);

    Long getTotalPaidInvoicePayable();
    Long getTotalUnpaidInvoicePayable();

    Long getTotalPaidInvoiceReceivable();
    Long getTotalUnpaidInvoiceReceivable();
}