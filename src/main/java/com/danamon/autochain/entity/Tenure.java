package com.danamon.autochain.entity;

import com.danamon.autochain.constant.invoice.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "t_tenure")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tenure {
    @Id
    @UuidGenerator
    @Column(name = "tenureId", length = 128, nullable = false)
    private String tenureId;

    @ManyToOne
    @JoinColumn(name = "paymentId")
    private Payment payment;

    @Column
    private Long Amount;

    @Column
    private Status status;
}
