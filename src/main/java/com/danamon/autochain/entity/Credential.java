package com.danamon.autochain.entity;

import com.danamon.autochain.constant.ActorType;
import com.danamon.autochain.constant.RoleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "m_credential")
public class Credential implements UserDetails {
    private String id;
    private String email;
    private String username;
    private String password;
    private boolean isSupplier;
    private boolean isManufacturer;
    private ActorType actor;
    private RoleType role;

    @OneToOne(mappedBy = "backoffice_id" ,cascade = CascadeType.ALL)
    @JoinColumn(name = "backoffice_id" , foreignKey= @ForeignKey(name = "Fk_user_credential"))
    private BackOffice backOffice;

    @OneToOne(mappedBy = "user_id" ,cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id" , foreignKey= @ForeignKey(name = "Fk_user_id"))
    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
        simpleGrantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
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
