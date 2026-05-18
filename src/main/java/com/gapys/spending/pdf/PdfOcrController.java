package com.gapys.spending.pdf;

import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/pdf")
public class PdfOcrController {

    private final PdfOcrService pdfOcrService;

    public PdfOcrController(PdfOcrService pdfOcrService) {
        this.pdfOcrService = pdfOcrService;
    }

    @PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PdfOcrService.OcrResult ocr(@RequestParam("file") MultipartFile file)
            throws IOException, TesseractException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }
        try (var in = file.getInputStream()) {
            return pdfOcrService.extract(in);
        }
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handlePasswordError(InvalidPasswordException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(Map.of("error", "invalid_pdf_password", "message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "bad_request", "message", ex.getMessage()));
    }

    @ExceptionHandler(TesseractException.class)
    public ResponseEntity<Map<String, String>> handleOcrError(TesseractException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "ocr_failed", "message", ex.getMessage()));
    }
}
