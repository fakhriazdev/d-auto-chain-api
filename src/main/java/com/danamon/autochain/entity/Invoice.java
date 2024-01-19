package com.danamon.autochain.entity;

import com.danamon.autochain.constant.invoice.ProcessingStatusType;
import com.danamon.autochain.constant.invoice.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "t_invoice")
public class Invoice extends HistoryLog {
    @Id
    @Column(name = "invoiceId", nullable = false)
    private String invoiceId;

    @ManyToOne
    @JoinColumn(name = "senderId", nullable = false)
    private Company senderId;

    @ManyToOne
    @JoinColumn(name = "recipientId", nullable = false)
    private Company recipientId;

    @Column(nullable = false)
    private Date dueDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @Column(nullable = false)
    private Long amount;

    @Column(columnDefinition = "TEXT")
    private String itemList;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatusType processingStatus;

    @OneToOne(mappedBy = "invoice" ,cascade = CascadeType.ALL)
    private FinancingReceivable financingReceivable;

    @OneToOne(mappedBy = "invoice" ,cascade = CascadeType.ALL)
    private FinancingPayable financingPayable;

    @OneToMany(mappedBy = "invoice" ,cascade = CascadeType.ALL)
    private List<Payment> payment;

    @OneToOne(mappedBy = "invoice", cascade = CascadeType.ALL)
    private InvoiceIssueLog invoiceIssueLog;
}
