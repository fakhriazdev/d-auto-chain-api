package com.danamon.autochain.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Table;
import jakarta.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Data
@Table(name = "m_user")
public class User {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    @Column(name = "userId", length = 128, nullable = false)
    private String user_id;

    @OneToOne
    @JoinColumn(name = "companyId")
    private Company company;

    @OneToOne
    @JoinColumn(name = "credentialId")
    private Credential credential;

}
