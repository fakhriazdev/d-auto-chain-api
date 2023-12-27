package com.danamon.autochain.entity;

import com.danamon.autochain.constant.invoice.ReasonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "invoice_issue_log")
public class InvoiceIssueLog {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    private String id;
    @OneToOne
    private Invoice invoice;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReasonType issueType;
    @Column(columnDefinition = "TEXT")
    private String reason;
}
