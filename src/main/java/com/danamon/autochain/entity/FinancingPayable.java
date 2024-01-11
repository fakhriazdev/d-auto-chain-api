package com.danamon.autochain.entity;

import com.danamon.autochain.constant.financing.FinancingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

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

    @OneToOne
    @JoinColumn(name = "paymentId")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    private FinancingStatus status;

    @Column
    private Long amount; // total amount financing requested

    @Column
    private Double interest; // suku bunga

    @Column
    private Double total; //total amount financing (amount + interest)

    @Column
    private Integer tenure; // banyak cicilan / tenure financing

    @Column
    private Double monthly_installment; // jumlah amount cicilan perbulan

    @Column
    private Integer period_number; // intallment progress count, status will change if matches with installments number

//    @OneToOne(mappedBy = "financingPayable")
//    private Payment payment;

    @OneToMany(mappedBy = "financingPayableId")
    private List<Tenure> tenures;

}
