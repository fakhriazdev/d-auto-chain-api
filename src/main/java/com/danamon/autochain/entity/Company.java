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
    @Column(name = "company_id", length = 128)
    private String company_id;

    @NotNull
    @Column(length = 128)
    private String company_name;

    @NotNull
    @Column(length = 64)
    private String province;

    @NotNull
    @Column(length = 64)
    private String city;

    @NotNull
    @Column(length = 128)
    private String address;

    @NotNull
    @Column(length = 64)
    private String phone_number;

    @NotNull
    @Column(length = 64)
    private String company_email;

    @NotNull
    @Column(length = 128)
    private String account_number;

    @NotNull
    @Column(length = 128)
    private Double financing_limit;

    @NotNull
    @Column(length = 128)
    private Double remaining_limit;

    @OneToOne(mappedBy = "company_id" ,cascade = CascadeType.ALL)
    @JoinColumn(name = "user_credential_id" , foreignKey= @ForeignKey(name = "Fk_user_credential"))
    private User user;
}
