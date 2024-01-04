package com.danamon.autochain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "t_financing_payable")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@SuperBuilder
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

    @Column
    private String status;

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
    private Date createdDate;

    @Column
    private Integer period_number;


    @OneToMany(mappedBy = "financingPayable")
    private List<Payment> payment;

}
