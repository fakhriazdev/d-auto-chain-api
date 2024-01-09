package com.danamon.autochain.entity;

import com.danamon.autochain.constant.FinancingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "t_financing_payable")
public class FinancingPayable extends HistoryLog{
    @Id
    @UuidGenerator
    @Column(name = "financingPayableId", length = 128, nullable = false)
    private String financingPayableId;

    @ManyToOne
    @JoinColumn(name = "companyId")
    private Company company;

    @JoinColumn(name = "invoiceId")
    @OneToOne
    private Invoice invoice;

//    @OneToMany
//    @JoinColumn(name = "paymentId")
//    private List<Payment> payment;

    @Enumerated(EnumType.STRING)
    private FinancingStatus status;

    @Column
    private Long amount;

    @Column
    private Integer interest;

    @Column
    private Long total; //total amount financing (amount + interest)

    @Column
    private Integer tenure;

    @Column
    private Integer installments_number;

    @Column
    private Integer period_number;

    @OneToOne(mappedBy = "financingPayable")
    private Payment payment;

}
