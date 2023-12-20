package com.danamon.autochain.entity;

import com.danamon.autochain.constant.UserActivity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "m_user_activity_log")
public class UserActivityLog {

    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @GeneratedValue(generator = "uuid")
    @Column(name = "acitivityId", length = 128)
    private String id;

    private String user_id;

    private Long timestamp;

    @Enumerated(EnumType.STRING)
    private UserActivity activity;
}
