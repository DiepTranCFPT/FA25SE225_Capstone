package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.TemporaryFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TemporaryFileRepository extends JpaRepository<TemporaryFile, String> {
    Optional<TemporaryFile> findByUrl(String url);

    List<TemporaryFile> findByCreatedAtBefore(LocalDateTime expiryDate);
}