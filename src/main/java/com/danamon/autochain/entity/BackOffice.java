package com.danamon.autochain.entity;

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

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "credentialId", nullable = false)
    private Credential credential;

//    @OneToMany(mappedBy = "backOffice", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private List<BackofficeUserAccess> backofficeUserAccesses;


//    @Column
//    private String name;
//
//    @ManyToOne
//    @JoinColumn(name = "companyId")
//    private Company company;
//
//    @OneToOne(cascade = CascadeType.REMOVE)
//    @JoinColumn(name = "credentialId")
//    private Credential credential;
//
//    @OneToMany(mappedBy = "user" ,cascade = CascadeType.ALL)
//    private List<UserAccsess> userAccsess;
//
//    @OneToMany(mappedBy = "user" ,cascade = CascadeType.ALL)
//    private List<UserActivityLog> userActivityLogs;
}
