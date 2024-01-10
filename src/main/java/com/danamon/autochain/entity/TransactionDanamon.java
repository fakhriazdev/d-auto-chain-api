package com.danamon.autochain.entity;

import com.danamon.autochain.constant.financing.FinancingType;
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
@Table(name = "t_transaction")
public class TransactionDanamon {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    private String transactionId;

    @ManyToOne
    @JoinColumn(name = "recipientId")
    private Company recipientId;

    @Column
    private Long amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentstatus;

    @Enumerated(EnumType.STRING)
    private FinancingType financingType;

    @Column
    private Date createdDate;

}
