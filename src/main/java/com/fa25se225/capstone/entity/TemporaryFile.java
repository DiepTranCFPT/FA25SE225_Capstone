package com.fa25se225.capstone.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "temporary_files")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemporaryFile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String publicId;

    @Column(nullable = false, unique = true)
    private String url;

    @CreationTimestamp
    private LocalDateTime createdAt;
}