package com.example.report.controller;

import com.example.report.model.Item;
import com.example.report.service.ItemService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class ReportController {
    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ItemService itemService;

    @GetMapping("/reports/preview")
    public ResponseEntity<byte[]> previewReport(@RequestParam(defaultValue = "pdf") String format) {
        logger.info("Received request for /reports/preview");
        try {
            List<Item> items = itemService.getAllItems();
            logger.debug("Fetched {} items for preview", items.size());
            logger.info("Successfully generated preview in format {}", format);
            return exportReport(items, format);
        } catch (Exception e) {
            logger.error("Error generating report preview", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // CRUD Operations
    @GetMapping("/items")
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.getItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/items")
    public ResponseEntity<Item> createItem(@RequestBody Item item) {
        Item savedItem = itemService.saveItem(item);
        return ResponseEntity.ok(savedItem);
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        if (!itemService.getItemById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        item.setId(id);
        Item updatedItem = itemService.saveItem(item);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        if (!itemService.getItemById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    // Report Generation
    @PostMapping("/reports/generate")
    public ResponseEntity<byte[]> generateReportFromData(@RequestBody List<Item> items,
                                                         @RequestParam(defaultValue = "pdf") String format) throws JRException {
        return exportReport(items, format);
    }

    @GetMapping("/reports/generate")
    public ResponseEntity<byte[]> generateReportFromDatabase(@RequestParam(defaultValue = "pdf") String format) throws JRException {
        List<Item> items = itemService.getAllItems();
        return exportReport(items, format);
    }

    @GetMapping("/reports/sample")
    public ResponseEntity<byte[]> generateSampleReport(@RequestParam(defaultValue = "pdf") String format) throws JRException {
        // Fallback to sample data if needed
        List<Item> sampleItems = List.of(
                new Item("Apple", 3, 1.50),
                new Item("Banana", 5, 0.80),
                new Item("Orange", 2, 1.20),
                new Item("Mango", 4, 2.30)
        );
        
        return exportReport(sampleItems, format);
    }

    private ResponseEntity<byte[]> generateReport(List<Item> items) throws JRException {
        logger.info("Generating report for {} items", items.size());
        InputStream jrxmlStream = getClass().getResourceAsStream("/reports/sample_report.jrxml");
        if (jrxmlStream == null) {
            logger.error("Could not find JRXML template at /reports/sample_report.jrxml");
            return ResponseEntity.internalServerError().body(null);
        }
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("reportTitle", "Sales Inventory Report");
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(items);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
        logger.info("Report PDF generated, size: {} bytes", pdfBytes.length);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    private ResponseEntity<byte[]> exportReport(List<Item> items, String format) throws JRException {
        logger.info("Exporting report in format: {}", format);
        InputStream jrxmlStream = getClass().getResourceAsStream("/reports/sample_report.jrxml");
        if (jrxmlStream == null) {
            logger.error("Could not find JRXML template at /reports/sample_report.jrxml");
            return ResponseEntity.internalServerError().body(null);
        }
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("reportTitle", "Sales Inventory Report");
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(items);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        String lower = format == null ? "pdf" : format.toLowerCase();
        try {
            switch (lower) {
                case "pdf":
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory-report.pdf")
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(JasperExportManager.exportReportToPdf(jasperPrint));
                case "xlsx": {
                    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                    JRXlsxExporter exporter = new JRXlsxExporter();
                    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
                    SimpleXlsxReportConfiguration cfg = new SimpleXlsxReportConfiguration();
                    cfg.setDetectCellType(true);
                    cfg.setWhitePageBackground(false);
                    cfg.setOnePagePerSheet(false);
                    exporter.setConfiguration(cfg);
                    exporter.exportReport();
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory-report.xlsx")
                            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                            .body(out.toByteArray());
                }
                case "csv": {
                    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                    JRCsvExporter exporter = new JRCsvExporter();
                    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    exporter.setExporterOutput(new SimpleWriterExporterOutput(out, java.nio.charset.StandardCharsets.UTF_8.name()));
                    SimpleCsvExporterConfiguration cfg = new SimpleCsvExporterConfiguration();
                    cfg.setFieldDelimiter(",");
                    exporter.setConfiguration(cfg);
                    exporter.exportReport();
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory-report.csv")
                            .contentType(MediaType.parseMediaType("text/csv"))
                            .body(out.toByteArray());
                }
                case "docx": {
                    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                    JRDocxExporter exporter = new JRDocxExporter();
                    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
                    exporter.exportReport();
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory-report.docx")
                            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                            .body(out.toByteArray());
                }
                case "pptx": {
                    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                    JRPptxExporter exporter = new JRPptxExporter();
                    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(out));
                    exporter.exportReport();
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory-report.pptx")
                            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation"))
                            .body(out.toByteArray());
                }
                case "html": {
                    java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                    HtmlExporter exporter = new HtmlExporter();
                    exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    exporter.setExporterOutput(new SimpleHtmlExporterOutput(out));
                    exporter.exportReport();
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory-report.html")
                            .contentType(MediaType.TEXT_HTML)
                            .body(out.toByteArray());
                }
                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(("Unsupported format: " + lower).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
        } catch (JRException e) {
            logger.error("Error exporting report in format {}", lower, e);
            throw e;
        }
    }
}


