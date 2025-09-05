package com.retroscore.controller;

import com.retroscore.service.DataImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class DataImportController   {
    private final DataImportService dataImportService;

    @PostMapping("/preview")
    public ResponseEntity<Map<String,Object>> previewImport(@RequestParam("file")MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {

            String tempDir = System.getProperty("java.io.tmpdir");
            Path tempFile = Paths.get(tempDir, file.getOriginalFilename());
            Files.write(tempFile, file.getBytes());


            DataImportService.ImportStatistics stats = dataImportService.getImportStatistics(tempFile.toString());

            Files.deleteIfExists(tempFile);

            response.put("Statistics", Map.of(
                    "totalRows", stats.getTotalRows(),
                    "validRows", stats.getValidRows(),
                    "invalidRows", stats.getInvalidRows()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to preview import:", e);
            response.put("success", false);
            response.put("message", "failed to preview import: " + e.getMessage());
           return ResponseEntity.badRequest().body(response);
        }


    }


    @PostMapping("/season")
    public  ResponseEntity<Map<String,Object>> importSeasonData(@RequestParam("file") MultipartFile file, @RequestParam("seasonName") String seasonName){

        Map<String, Object> response = new HashMap<>();

        try{
            String tempDir = System.getProperty("java.io.tmpdir");
            Path tempFile = Paths.get(tempDir, file.getOriginalFilename());
            Files.write(tempFile,file.getBytes());

            DataImportService.ImportStatistics stats = dataImportService.getImportStatistics(tempFile.toString());

            log.info("import stats for {}:{}", seasonName, stats);

            dataImportService.importSeasonData(tempFile.toString(),seasonName);

            Files.deleteIfExists(tempFile);

            response.put("success", true);
            response.put("message", "season data imported successfully");
            response.put("seasonName", seasonName);
            response.put("statistics", Map.of(
                    "totalRows", stats.getTotalRows(),
                    "validRows", stats.getValidRows(),
                    "invalidRows", stats.getInvalidRows()
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e){
            log.error("failed to import season data", e);
            response.put("success", false);
            response.put("message", "Failed to import season data" +e.getMessage());

            return ResponseEntity.badRequest().body(response);

        }
    }

}
