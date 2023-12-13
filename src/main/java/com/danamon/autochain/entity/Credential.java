package com.danamon.autochain.entity;

import com.danamon.autochain.constant.ActorType;
import com.danamon.autochain.constant.RoleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
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
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    @Column(name = "credential_id", length = 128, nullable = false)
    private String credential_id;

    @Column
    private String email;

    @Column
    private String username;

    @Column
    private String password;
    @Column
    private boolean isSupplier;
    @Column
    private boolean isManufacturer;

    @Enumerated(EnumType.STRING)
    private ActorType actor;

    @Enumerated(EnumType.STRING)
    private RoleType role;

    @OneToOne(mappedBy = "credential" ,cascade = CascadeType.ALL)
//    @JoinColumn(name = "backoffice_id" , foreignKey= @ForeignKey(name = "Fk_backoffice_id"))
    private BackOffice backOffice;

    @OneToOne(mappedBy = "credential" ,cascade = CascadeType.ALL)
//    @JoinColumn(name = "user_id" , foreignKey= @ForeignKey(name = "Fk_user_id"))
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
