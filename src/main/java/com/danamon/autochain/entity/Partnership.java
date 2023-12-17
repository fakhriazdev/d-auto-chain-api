package com.danamon.autochain.entity;

import com.danamon.autochain.constant.PartnershipStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "t_partnership")
public class Partnership {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    private String partnership_no;

    @ManyToOne
    @JoinColumn(name = "company_id")
    @JsonBackReference
    private Company company;

    @ManyToOne
    @JoinColumn(name = "partner_id")
    @JsonBackReference
    private Company partner;

    @Column(name = "partner_status", length = 128, nullable = false)
    @Enumerated(EnumType.STRING)
    private PartnershipStatus partnerStatus;

    @Column(name = "partner_requested_date", length = 128, nullable = false)
    private LocalDateTime partnerRequestedDate;

    @Column(name = "partner_confirmation_date", length = 128)
    private LocalDateTime partnerConfirmationDate;

    @ManyToOne
    @JoinColumn(name = "requested_by")
    @JsonBackReference
    private Company requestedBy;

    @ManyToOne
    @JoinColumn(name = "confirmed_by")
    @JsonBackReference
    private Company confirmedBy;
}
