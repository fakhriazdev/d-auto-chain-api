package com.danamon.autochain.entity;

import com.danamon.autochain.constant.invoice.ProcessingStatusType;
import com.danamon.autochain.constant.invoice.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "t_invoice")
public class Invoice extends HistoryLog {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
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
    private Status status;

    @Column(nullable = false)
    private Long amount;

    @Column(columnDefinition = "TEXT")
    private String itemList;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatusType processingStatus;

    @OneToOne(mappedBy = "invoiceId" ,cascade = CascadeType.ALL)
    private Financing financing;
}
