package com.danamon.autochain.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class HistoryLog {

    @CreatedBy
    private String created_by;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime created_data;

    @LastModifiedBy
    private String modified_by;

    @LastModifiedDate
    private LocalDateTime modified_date;

}