package com.danamon.autochain.entity;

import com.danamon.autochain.constant.PaymentMethod;
import com.danamon.autochain.constant.PaymentType;
import com.danamon.autochain.constant.invoice.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "t_payment")
public class Payment {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    private String paymentId;

    @ManyToOne
    @JoinColumn(name = "invoiceId")
    private Invoice invoice;

    @OneToOne
    @JoinColumn(name = "financingPayableId")
    private FinancingPayable financingPayable;

    @ManyToOne
    @JoinColumn(name = "recipientId")
    private Company recipientId;

    @ManyToOne
    @JoinColumn(name = "senderId")
    private Company senderId;

    @Column
    private Long amount;

    @Enumerated(EnumType.STRING)
    private PaymentType type;

    @Column
    private Date dueDate;

    @Column
    private Date paidDate;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "paymentId")
    private List<Tenure> tenures;
}
