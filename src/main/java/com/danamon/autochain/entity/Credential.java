package com.danamon.autochain.entity;

import com.danamon.autochain.constant.ActorType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "m_credential")
public class Credential extends HistoryLog implements UserDetails {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    @Column(name = "credentialId", length = 128, nullable = false)
    private String credentialId;

    @Column(unique = true)
    @Email( message = "Invalid email format")
    private String email;

    @Column
    private String username;

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    private ActorType actor;

    @OneToMany(mappedBy = "credential", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<UserRole> roles;

    @OneToOne(mappedBy = "credential", cascade = CascadeType.ALL, orphanRemoval = true)
    private BackOffice backOffice;

    @OneToOne(mappedBy = "credential" ,cascade = CascadeType.ALL)
    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        for (UserRole userRole : roles) {
//            System.out.println("ALL ROLE USER "+ userRole.getRole().getRoleName());
            authorities.add(new SimpleGrantedAuthority(userRole.getRole().getRoleName()));
        }
        return authorities;
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
