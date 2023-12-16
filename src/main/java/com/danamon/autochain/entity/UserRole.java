package com.danamon.autochain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;




@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "m_user_role")
public class UserRole {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    @Column(name = "userRoleId", length = 128, nullable = false)
    private String id;

    @ManyToOne
    @JoinColumn(name = "credentialId")
    private Credential credential;

    @ManyToOne
    @JoinColumn(name = "roleId")
    private Roles role;
}
