package com.example.report.controller;

import com.example.report.model.Item;
import com.example.report.service.ItemService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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

    @Autowired
    private ItemService itemService;

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
    public ResponseEntity<byte[]> generateReportFromData(@RequestBody List<Item> items) throws JRException {
        return generateReport(items);
    }

    @GetMapping("/reports/generate")
    public ResponseEntity<byte[]> generateReportFromDatabase() throws JRException {
        List<Item> items = itemService.getAllItems();
        return generateReport(items);
    }

    @GetMapping("/reports/sample")
    public ResponseEntity<byte[]> generateSampleReport() throws JRException {
        // Fallback to sample data if needed
        List<Item> sampleItems = List.of(
                new Item("Apple", 3, 1.50),
                new Item("Banana", 5, 0.80),
                new Item("Orange", 2, 1.20),
                new Item("Mango", 4, 2.30)
        );
        
        return generateReport(sampleItems);
    }

    private ResponseEntity<byte[]> generateReport(List<Item> items) throws JRException {
        InputStream jrxmlStream = getClass().getResourceAsStream("/reports/sample_report.jrxml");
        if (jrxmlStream == null) {
            return ResponseEntity.internalServerError().body(null);
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("reportTitle", "Sales Inventory Report");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(items);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}


