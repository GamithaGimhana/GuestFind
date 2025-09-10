package com.gdse.aad.backend.repository;

import com.gdse.aad.backend.entity.Claim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
}
