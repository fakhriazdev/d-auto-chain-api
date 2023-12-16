package com.danamon.autochain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "m_roles")
public class Roles {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    @Column(name = "credentialId", length = 128, nullable = false)
    private String id;

    @Column(name = "role_name")
    private String roleName;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    private List<UserRole> userRole;
}
