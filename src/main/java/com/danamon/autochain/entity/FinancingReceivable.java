package com.danamon.autochain.entity;

import com.danamon.autochain.constant.FinancingStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "t_financing_receivable")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@SuperBuilder
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

    @Column
    private Integer periodNumber;

    @Column
    private Long amount;

    @Column
    private Date disbursment_date;

    @Column
    private String  financingType;

}
