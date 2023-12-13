package com.danamon.autochain.entity;

import com.danamon.autochain.constant.BackofficeRoleType;
import com.danamon.autochain.constant.UserRoleType;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
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
public class BackOfficeUser implements UserDetails {
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


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
        simpleGrantedAuthorities.add(new SimpleGrantedAuthority(user_role.getName()));
        return simpleGrantedAuthorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
