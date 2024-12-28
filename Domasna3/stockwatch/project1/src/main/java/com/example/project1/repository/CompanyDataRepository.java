package com.example.project1.repository;

import com.example.project1.entity.Issuer;
import com.example.project1.entity.CompanyData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyDataRepository extends JpaRepository<CompanyData, Long> {
    Optional<CompanyData> findByDateAndCompany(LocalDate date, Issuer company);
    List<CompanyData> findByCompanyIdAndDateBetween(Long companyId, LocalDate from, LocalDate to);
    List<CompanyData> findByCompanyId(Long companyId);
}
