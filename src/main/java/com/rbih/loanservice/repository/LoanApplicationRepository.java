package com.rbih.loanservice.repository;

import com.rbih.loanservice.domain.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, String> {
}
