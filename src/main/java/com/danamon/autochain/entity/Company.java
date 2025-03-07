package com.danamon.autochain.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "m_company")
public class Company {
    @Id
//    @GenericGenerator(name = "uuid", strategy = "uuid")
//    @GeneratedValue(generator = "uuid")
    @Column(name = "companyId", nullable = false)
    private String company_id;

    @Column(name = "company_name", length = 128, nullable = false)
    private String companyName;

    @Column(length = 64, nullable = false)
    private String province;

    @Column(length = 64, nullable = false)
    private String city;

    @Column(length = 128, nullable = false)
    private String address;

    @Column(name = "phone_number", length = 64, nullable = false)
    private String phoneNumber;

    @Column(name = "company_email", length = 64, nullable = false, unique = true)
    private String companyEmail;

    @Column(name = "account_number", length = 128, nullable = false)
    private String accountNumber;

    @Column(name = "financing_limit", nullable = false)
    private Double financingLimit;

    @Column(name = "remaining_limit", nullable = false)
    private Double remainingLimit;

    @OneToMany
    @JoinColumn(name = "company_file_id")
    private List<CompanyFile> companyFiles;

    @OneToMany
    @JoinColumn(name = "partner_id")
    @JsonManagedReference
    private List<Partnership> partnerships;

    @OneToMany(mappedBy = "company" ,cascade = CascadeType.ALL)
    private List<User> user;

    @OneToMany(mappedBy = "company" ,cascade = CascadeType.ALL)
    private List<FinancingReceivable> financingReceivable;

    @OneToMany(mappedBy = "company" ,cascade = CascadeType.ALL)
    private List<FinancingPayable> financingPayable;

    @OneToMany(mappedBy = "recipientId" ,cascade = CascadeType.ALL)
    private List<Payment> payments;

    @OneToMany(mappedBy = "company", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<BackofficeUserAccess> backofficeUserAccesses;
}
