package com.fa25se225.capstone.entity;

import com.fa25se225.capstone.utils.CodeGenerator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "token_transaction_types")
public class TokenTransactionType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;

    @Column(name = "code", nullable = false, unique = true)
    @Builder.Default
    private String code = CodeGenerator.generateRandomCode();

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "affects_balance", nullable = false)
    @Schema(description = "thay transaction kiem tra xem user co nhan token chua")
    private Boolean affectsBalance;

    @Column(name = "multiplier")
    @Schema(description = "He So Nhan Token User vd 100k = 100token * multiplier")
    private Integer multiplier;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        updatedAt = createdAt;
        if (affectsBalance == null) affectsBalance = true;
        if (multiplier == null) multiplier = 1;
        if (deleted == null) deleted = false;
        if (code == null || code.isEmpty()) {
            code = CodeGenerator.generateRandomCode();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }
}
