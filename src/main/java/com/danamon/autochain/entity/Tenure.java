package com.danamon.autochain.entity;

import com.danamon.autochain.constant.TenureStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;

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
    private Payment paymentId;

    @Column
    private Long Amount;

    @Column
    private Date dueDate;

    @Column
    private Date paidDate;

    @Enumerated(EnumType.STRING)
    private TenureStatus status;
}
