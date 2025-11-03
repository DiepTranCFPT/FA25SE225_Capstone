package com.fa25se225.capstone.repository.v2;

import com.fa25se225.capstone.entity.v2.AnswerV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface AnswerV2Repository extends JpaRepository<AnswerV2, String> {

}