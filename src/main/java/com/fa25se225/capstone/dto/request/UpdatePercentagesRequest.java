package com.fa25se225.capstone.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class UpdatePercentagesRequest {
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private BigDecimal percentTeacher;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private BigDecimal percentTeacherUnverified;

    public BigDecimal getPercentTeacher() {
        return percentTeacher;
    }

    public void setPercentTeacher(BigDecimal percentTeacher) {
        this.percentTeacher = percentTeacher;
    }

    public BigDecimal getPercentTeacherUnverified() {
        return percentTeacherUnverified;
    }

    public void setPercentTeacherUnverified(BigDecimal percentTeacherUnverified) {
        this.percentTeacherUnverified = percentTeacherUnverified;
    }
}
