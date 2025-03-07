package com.danamon.autochain.entity;

import com.danamon.autochain.constant.payment.PaymentMethod;
import com.danamon.autochain.constant.payment.PaymentStatus;
import com.danamon.autochain.constant.payment.PaymentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "t_payment")
public class Payment {
    @Id
    private String paymentId;

    @ManyToOne
    @JoinColumn(name = "invoiceId")
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "recipientId")
    private Company recipientId;

    @ManyToOne
    @JoinColumn(name = "senderId")
    private Company senderId;

    @Column
    private Long amount;

    @Column
    private Date dueDate;

    @Column
    private Date paidDate;

    @Column
    private Date createdDate;

    @Enumerated(EnumType.STRING)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @OneToOne(mappedBy = "payment")
    private FinancingPayable financingPayable;
}
