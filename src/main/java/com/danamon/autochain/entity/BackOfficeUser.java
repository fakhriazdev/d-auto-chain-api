package com.danamon.autochain.entity;

import com.danamon.autochain.constant.BackofficeRoleType;
import com.danamon.autochain.constant.UserRoleType;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Table;


@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Data
@Table(name = "m_backoffice_user")
public class BackOfficeUser {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    @Column(name = "user_id", length = 128, nullable = false)
    private String user_id;

    @Column(name = "username", nullable = false, length =128, unique = true)
    private String username;

    @Column(name = "email",nullable = false, length =128, unique = true)
    private String email;

    @Column(name = "password", nullable = false, length =128)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length =64)
    private BackofficeRoleType user_role;

}
