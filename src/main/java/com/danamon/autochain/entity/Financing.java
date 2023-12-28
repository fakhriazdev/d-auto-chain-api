package com.danamon.autochain.entity;

import com.danamon.autochain.constant.FinancingStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.validator.constraints.UUID;

import java.util.Date;

@Entity
@Table(name = "t_financing")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@SuperBuilder
public class Financing extends HistoryLog {
    @Id
    @UuidGenerator
    @Column(name = "financingId", length = 128, nullable = false)
    private String financingId;

    @JoinColumn
    @OneToOne
    private Invoice invoiceId;

    @JoinColumn
    @ManyToOne
    private  Company companyId;

    @Enumerated(EnumType.STRING)
    private FinancingStatus status;

    @Column
    private Integer periodNumber;

    @Column
    private Long amount;

    @Column
    Date disbursment_date;

    @Column
    private String  financingType;
}
