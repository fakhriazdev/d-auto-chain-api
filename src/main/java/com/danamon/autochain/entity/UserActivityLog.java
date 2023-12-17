package com.danamon.autochain.entity;

import com.danamon.autochain.constant.UserActivity;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

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
