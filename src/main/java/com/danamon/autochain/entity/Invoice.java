package com.danamon.autochain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "t_invoice")
public class Invoice {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    @Column(name = "invoice_id", nullable = false)
    private String invoiceId;
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private Company senderId;
    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private Company recipientId;
    @Column(nullable = false)
    private Date dueDate;
    @Column(nullable = false)
    private String status;
    @Column(nullable = false)
    private Integer amount;
    @Column(nullable = false)
    private String type;
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private Credential createdBy;
    @Column(name = "modified_date")
    private Date modifiedDate;
    @ManyToOne
    @JoinColumn(name = "modified_by")
    private Credential modifiedBy;
}
