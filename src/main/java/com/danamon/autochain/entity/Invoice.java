package com.danamon.autochain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "t_invoice")
public class Invoice extends HistoryLog {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    @Column(name = "invoiceId", nullable = false)
    private String invoiceId;

    @ManyToOne
    @JoinColumn(name = "senderId", nullable = false)
    private Company senderId;

    @ManyToOne
    @JoinColumn(name = "recipientId", nullable = false)
    private Company recipientId;

    @Column(nullable = false)
    private Date dueDate;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Integer amount;

//    @Column(nullable = false)
//    private String type;
}
