package com.danamon.autochain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
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
    @JsonIgnore
    private Company company;

    @ManyToOne
    @JoinColumn(name = "partner_id")
    @JsonIgnore
    private Company partner;

    @Column(name = "partner_status", length = 128, nullable = false)
    private String partnerStatus;

    @Column(name = "partner_requested_date", length = 128, nullable = false)
    private LocalDate partnerRequestedDate;

    @Column(name = "partner_confirmation_date", length = 128, nullable = false)
    private LocalDate partnerConfirmationDate;

    @ManyToOne
    @JoinColumn(name = "requested_by")
    @JsonIgnore
    private Company requestedBy;

    @ManyToOne
    @JoinColumn(name = "confirmed_by")
    @JsonIgnore
    private Company confirmedBy;
}
