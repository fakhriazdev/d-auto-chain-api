package com.danamon.autochain.entity;

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
@Table(name = "t_payment")
public class Payment {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    private String transactionId;

    @ManyToOne
    @JoinColumn(name = "invoiceId")
    private Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "financingPayableId")
    private FinancingPayable financingPayable;

    @Column
    private Long amount;

    @Column
    private String type;

    @Column
    private String dueDate;

    @Column
    private String paidDate;

    @Column
    private Boolean method;

    @Column
    private Boolean source;

    @Column
    private Boolean outstandingFlag;
}
