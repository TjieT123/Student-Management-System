package cn.edu.sdu.sms.server.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.Base64;

/**
 * Extracts readable text from file attachments for AI processing.
 * Supports: TXT, PDF, DOCX, DOC, XLSX, XLS
 */
public class AttachmentTextExtractor {

    private static final Logger log = LoggerFactory.getLogger(AttachmentTextExtractor.class);
    private static final int MAX_TEXT_LENGTH = 6000; // Per-file text limit

    /**
     * Extract text from an attachment (base64 encoded). Returns null if unsupported or fails.
     */
    public static String extract(String fileName, String fileType, String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        String lowerName = fileName != null ? fileName.toLowerCase() : "";
        try {
            byte[] data = Base64.getDecoder().decode(base64);
            if (data.length == 0) return null;

            if (lowerName.endsWith(".txt") || "text/plain".equals(fileType)) {
                return truncate(new String(data, java.nio.charset.StandardCharsets.UTF_8));
            }
            if (lowerName.endsWith(".pdf") || "application/pdf".equals(fileType)) {
                return extractPdf(data);
            }
            if (lowerName.endsWith(".docx") || fileType != null && fileType.contains("openxmlformats")) {
                return extractDocx(data);
            }
            if (lowerName.endsWith(".xlsx") || fileType != null && fileType.contains("openxmlformats.spreadsheet")) {
                return extractXlsx(data);
            }
            if (lowerName.endsWith(".md") || lowerName.endsWith(".json") || lowerName.endsWith(".xml")
                    || lowerName.endsWith(".csv") || lowerName.endsWith(".java") || lowerName.endsWith(".py")
                    || lowerName.endsWith(".cpp") || lowerName.endsWith(".c") || lowerName.endsWith(".html")
                    || lowerName.endsWith(".css") || lowerName.endsWith(".js") || lowerName.endsWith(".sql")) {
                return truncate(new String(data, java.nio.charset.StandardCharsets.UTF_8));
            }
            return null; // Unsupported type
        } catch (Exception e) {
            log.warn("Failed to extract text from {}: {}", fileName, e.getMessage());
            return null;
        }
    }

    private static String extractPdf(byte[] data) throws Exception {
        try (PDDocument doc = Loader.loadPDF(data)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return truncate(stripper.getText(doc));
        }
    }

    private static String extractDocx(byte[] data) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(data));
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return truncate(extractor.getText());
        }
    }

    private static String extractXlsx(byte[] data) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(data))) {
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet sheet = wb.getSheetAt(i);
                sb.append("[").append(sheet.getSheetName()).append("]\n");
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        sb.append(getCellText(cell)).append("\t");
                    }
                    sb.append("\n");
                    if (sb.length() > MAX_TEXT_LENGTH) break;
                }
                if (sb.length() > MAX_TEXT_LENGTH) break;
            }
        }
        return truncate(sb.toString());
    }

    private static String getCellText(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                }
                double val = cell.getNumericCellValue();
                yield val == (long) val ? String.valueOf((long) val) : String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield cell.getStringCellValue(); } catch (Exception e) { yield String.valueOf(cell.getNumericCellValue()); }
            }
            default -> "";
        };
    }

    private static String truncate(String text) {
        if (text == null) return null;
        String cleaned = text.replaceAll("\\s+", " ").trim();
        if (cleaned.length() > MAX_TEXT_LENGTH) {
            return cleaned.substring(0, MAX_TEXT_LENGTH) + "...[内容已截断]";
        }
        return cleaned;
    }
}
