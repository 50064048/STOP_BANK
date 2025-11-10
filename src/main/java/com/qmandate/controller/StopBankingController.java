package com.qmandate.controller;

import com.qmandate.service.StopBankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/stopbank")
public class StopBankingController {

    private final StopBankingService stopBankingService;

    public StopBankingController(StopBankingService stopBankingService) {
        this.stopBankingService = stopBankingService;
    }

    /**
     * CSV Upload endpoint, used by the Stop Bank FE.
     * Accepts a multipart file (CSV), returns how many records were inserted.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadStopBankingFile(@RequestParam("file") MultipartFile file) {
        try {
            int count = stopBankingService.uploadStopBankingFile(file);
            return ResponseEntity.ok("Stop banking file uploaded successfully. " + count + " agreements inserted.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
        }
    }

    /**
     * API endpoint for To Bank microservice to fetch all STOPPED agreement numbers for given format type.
     * For To Bank: GET /api/stopbank/stopped/{formatType}
     * Returns: List of agreement_no (Strings)
     */
    @GetMapping("/stopped/{formatType}")
    public ResponseEntity<List<String>> getStoppedAgreementNosByFormatType(@PathVariable("formatType") String formatType) {
        List<String> stoppedAgreementNos = stopBankingService.getStoppedAgreementNosByFormatType(formatType);
        return ResponseEntity.ok(stoppedAgreementNos);
    }
}