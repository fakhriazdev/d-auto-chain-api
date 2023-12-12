package com.danamon.autochain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

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

    @Column(name = "company_email", length = 64, nullable = false)
    private String companyEmail;

    @Column(name = "account_number", length = 128, nullable = false)
    private String accountNumber;

    @Column(name = "financing_limit", nullable = false)
    private Double financingLimit;

    @Column(name = "remaining_limit", nullable = false)
    private Double remainingLimit;


    @OneToOne(mappedBy = "company_id" ,cascade = CascadeType.ALL)
    @JoinColumn(name = "user_credential_id" , foreignKey= @ForeignKey(name = "Fk_user_credential"))
    private User user;

}
