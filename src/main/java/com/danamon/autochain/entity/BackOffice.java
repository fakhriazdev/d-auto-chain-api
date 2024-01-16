package com.danamon.autochain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import lombok.*;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Table;

import java.util.List;


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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "credentialId")
    private Credential credential;

    private String name;

    @OneToMany(mappedBy = "backOffice", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<BackofficeUserAccess> backofficeUserAccesses;
}
