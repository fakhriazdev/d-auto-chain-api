package com.danamon.autochain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "m_company")
public class Company {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
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

    @OneToMany(mappedBy = "partner")
    private List<Partnership> partnerships;

    @OneToOne(mappedBy = "company" ,cascade = CascadeType.ALL)
    private User user;
}
