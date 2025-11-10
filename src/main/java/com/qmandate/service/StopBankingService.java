package com.qmandate.service;

import com.qmandate.model.StopBankingAgreement;
import com.qmandate.repository.StopBankingAgreementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to handle business logic for Stop Banking flows,
 * including CSV upload and API data retrieval.
 */
@Service
public class StopBankingService {

    private final StopBankingAgreementRepository stopBankingAgreementRepository;
    private static final Logger logger = LoggerFactory.getLogger(StopBankingService.class);

    public StopBankingService(StopBankingAgreementRepository stopBankingAgreementRepository) {
        this.stopBankingAgreementRepository = stopBankingAgreementRepository;
    }

    /**
     * Processes the Stop Banking CSV file, storing each valid row in the database.
     * CSV format must be: agreement_no,format_type
     * Returns the number of new records inserted.
     */
    public int uploadStopBankingFile(MultipartFile file) throws Exception {
        List<StopBankingAgreement> toSave = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue; // skip blank lines
                if (isFirstLine) { // skip header
                    isFirstLine = false;
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length < 2) continue;
                String agreementNo = parts[0].trim();
                String formatType = parts[1].trim();
                if (agreementNo.isEmpty() || formatType.isEmpty()) continue;

                // DEBUG: log every agreementNo and formatType to be saved
             // logger.info("Parsed StopBankingAgreement - agreementNo: '{}', formatType: '{}'", agreementNo, formatType);

                StopBankingAgreement sba = new StopBankingAgreement();
                sba.setAgreementNo(agreementNo);
                sba.setFormatType(formatType);
                sba.setUploadDate(LocalDateTime.now());
                toSave.add(sba);
            }
        }
        stopBankingAgreementRepository.saveAll(toSave);

        // DEBUG: log how many agreements were stored
    // logger.info("Upload complete. Agreements inserted in StopBank: {}", toSave.size());
        return toSave.size();
    }

    /**
     * Retrieves all STOPPED agreement numbers (as Strings) for a given format type.
     * Used by To Bank microservice via API for filtering output records.
     */
    public List<String> getStoppedAgreementNosByFormatType(String formatType) {
     //  logger.info("Fetching STOPPED agreementNos for formatType: '{}'", formatType);
        List<String> stoppedAgreementNos = stopBankingAgreementRepository.findAgreementNosByFormatType(formatType);

        // DEBUG: log the returned agreement numbers for this formatType request
      // logger.info("STOPPED agreements returned: {}", stoppedAgreementNos);

        return stoppedAgreementNos;
    }
}