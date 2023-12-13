package com.danamon.autochain.entity;

import com.danamon.autochain.constant.BackofficeRoleType;
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
@Table(name = "m_backoffice")
public class BackOffice {
    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    @Column(name = "backoffice_id", length = 128, nullable = false)
    private String backoffice_id;


}
