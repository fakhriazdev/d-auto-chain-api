package com.danamon.autochain.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Table;
import jakarta.persistence.*;

import java.util.List;

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

    @Column
    private String name;

    @ManyToOne
    @JoinColumn(name = "companyId")
    private Company company;

    @OneToOne
    @JoinColumn(name = "credentialId")
    private Credential credential;

    @OneToMany(mappedBy = "user" ,cascade = CascadeType.ALL)
    private List<UserAccsess> userAccsess;

}
