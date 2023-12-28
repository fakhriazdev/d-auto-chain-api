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

    @OneToOne
    private Invoice invoice;

    // tambahin entity financing

    private Long amount;
    private String type;
    private String dueDate;
    private String paidDate;
    private Boolean method;
    private Boolean source;
    private Boolean outstandingFlag;
}
