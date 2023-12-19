package com.danamon.autochain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import lombok.*;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Table;


@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Data
@Table(name = "m_backoffice")
public class BackOffice {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    @Column(name = "backofficeId", length = 128, nullable = false)
    private String backoffice_id;

    @OneToOne
    @JoinColumn(name= "credentialId", nullable=false)
    private Credential credential;
}
