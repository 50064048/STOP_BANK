package com.qmandate.repository;

import com.qmandate.model.StopBankingAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for the StopBankingAgreement entity.
 * Provides basic CRUD operations and a custom query for retrieving agreement numbers by format type.
 */
public interface StopBankingAgreementRepository extends JpaRepository<StopBankingAgreement, Long> {

    /**
     * Custom query to return agreement numbers for a given formatType.
     * This is used by the To Bank microservice via REST API for filtering stopped mandates.
     */
    @Query("SELECT s.agreementNo FROM StopBankingAgreement s WHERE s.formatType = :formatType")
    List<String> findAgreementNosByFormatType(@Param("formatType") String formatType);
}