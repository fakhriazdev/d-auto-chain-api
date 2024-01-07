package com.danamon.autochain.entity;

import com.danamon.autochain.constant.financing.FinancingStatus;
import com.danamon.autochain.constant.financing.FinancingType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;


@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "t_financing_receivable")
public class FinancingReceivable extends HistoryLog {
    @Id
    @UuidGenerator
    @Column(name = "financingId", length = 128, nullable = false)
    private String financingId;

    @JoinColumn(name = "invoiceId")
    @OneToOne
    private Invoice invoice;

    @JoinColumn(name = "companyId")
    @ManyToOne
    private  Company company;

    @Enumerated(EnumType.STRING)
    private FinancingStatus status;

    @Enumerated(EnumType.STRING)
    private FinancingType financingType;

    @Column
    private Double fee;

    @Column
    private Double total;

    @Column
    private Long amount;

    @Column
    private Date disbursment_date;

}
