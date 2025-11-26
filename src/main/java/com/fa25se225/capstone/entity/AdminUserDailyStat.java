package com.fa25se225.capstone.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "admin_user_daily_stats")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDailyStat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    private long totalUsers;
    private long totalStudents;
    private long totalTeachers;
    private long totalParents;

    private long newStudents;
    private long newTeachers;

    private long dailyActiveUsers;
}