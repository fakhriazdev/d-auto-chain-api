package com.danamon.autochain.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "t_invoice")
public class Invoice extends HistoryLog {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    @Column(name = "invoice_id", nullable = false)
    private String invoiceId;
    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private Company senderId;
    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private Company recipientId;
    @Column(nullable = false)
    private Date dueDate;
    @Column(nullable = false)
    private String status;
    @Column(nullable = false)
    private Long amount;
    @Column(nullable = false)
    private String type;
    @Column(columnDefinition = "TEXT")
    private String itemList;
}
