package com.fa25se225.capstone.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "percentages_config")
public class PercentagesConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal percentTeacherVerified;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal percentAdminVerified;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal percentTeacherUnverified;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal percentAdminUnverified;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getPercentTeacherVerified() {
        return percentTeacherVerified;
    }

    public void setPercentTeacherVerified(BigDecimal percentTeacherVerified) {
        this.percentTeacherVerified = percentTeacherVerified;
    }

    public BigDecimal getPercentAdminVerified() {
        return percentAdminVerified;
    }

    public void setPercentAdminVerified(BigDecimal percentAdminVerified) {
        this.percentAdminVerified = percentAdminVerified;
    }

    public BigDecimal getPercentTeacherUnverified() {
        return percentTeacherUnverified;
    }

    public void setPercentTeacherUnverified(BigDecimal percentTeacherUnverified) {
        this.percentTeacherUnverified = percentTeacherUnverified;
    }

    public BigDecimal getPercentAdminUnverified() {
        return percentAdminUnverified;
    }

    public void setPercentAdminUnverified(BigDecimal percentAdminUnverified) {
        this.percentAdminUnverified = percentAdminUnverified;
    }
}

