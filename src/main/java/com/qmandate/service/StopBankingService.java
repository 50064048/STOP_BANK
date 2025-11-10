package com.qmandate.service;

import com.qmandate.model.StopBankingAgreement;
import com.qmandate.repository.StopBankingAgreementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StopBankingService {

    private final StopBankingAgreementRepository stopBankingAgreementRepository;
    private static final Logger logger = LoggerFactory.getLogger(StopBankingService.class);
    private final JdbcTemplate jdbcTemplate;
    private final Environment env;

    public StopBankingService(
            StopBankingAgreementRepository stopBankingAgreementRepository,
            JdbcTemplate jdbcTemplate,
            Environment env
    ) {
        this.stopBankingAgreementRepository = stopBankingAgreementRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.env = env;
    }

    // Batch size and SQL should be configurable!
    public int uploadStopBankingFile(MultipartFile file) throws Exception {
        int batchSize = Integer.parseInt(env.getProperty("stopbank.batch.size", "1000"));
        String insertSql = env.getProperty("stopbank.batch.insert",
            "INSERT INTO stop_banking_agreement (agreement_no, format_type, upload_date) VALUES (?, ?, ?)");

        List<StopBankingAgreement> batchList = new ArrayList<>(batchSize);
        int insertCount = 0;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                if (isFirstLine) { isFirstLine = false; continue; }
                String[] parts = line.split(",");
                if (parts.length < 2) continue;
                String agreementNo = parts[0].trim();
                String formatType = parts[1].trim();
                if (agreementNo.isEmpty() || formatType.isEmpty()) continue;

                StopBankingAgreement sba = new StopBankingAgreement();
                sba.setAgreementNo(agreementNo);
                sba.setFormatType(formatType);
                sba.setUploadDate(LocalDateTime.now());
                batchList.add(sba);

                if (batchList.size() == batchSize) {
                    batchInsertStopBankAgreements(batchList, insertSql);
                    insertCount += batchList.size();
                    batchList.clear();
                }
            }
            if (!batchList.isEmpty()) {
                batchInsertStopBankAgreements(batchList, insertSql);
                insertCount += batchList.size();
            }
        }
        logger.info("Upload complete. Agreements inserted in StopBank: {}", insertCount);
        return insertCount;
    }

    private void batchInsertStopBankAgreements(List<StopBankingAgreement> batch, String insertSql) {
        jdbcTemplate.batchUpdate(
            insertSql,
            batch,
            batch.size(),
            (ps, sba) -> {
                ps.setString(1, sba.getAgreementNo());
                ps.setString(2, sba.getFormatType());
                ps.setTimestamp(3, java.sql.Timestamp.valueOf(sba.getUploadDate()));
            }
        );
        logger.info("Inserted batch of size {}", batch.size());
    }

    public List<String> getStoppedAgreementNosByFormatType(String formatType) {
        return stopBankingAgreementRepository.findAgreementNosByFormatType(formatType);
    }
}