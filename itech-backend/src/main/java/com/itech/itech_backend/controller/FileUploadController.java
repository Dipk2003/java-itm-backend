package com.itech.itech_backend.controller;

import com.itech.itech_backend.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "directory", defaultValue = "general") String directory) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String filePath = fileUploadService.uploadFile(file, directory);
            String fileUrl = fileUploadService.getFileUrl(filePath);
            
            response.put("success", true);
            response.put("message", "File uploaded successfully");
            response.put("filePath", filePath);
            response.put("fileUrl", fileUrl);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("error", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<Map<String, Object>> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "directory", defaultValue = "general") String directory) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> filePaths = fileUploadService.uploadMultipleFiles(files, directory);
            List<String> fileUrls = filePaths.stream()
                    .map(fileUploadService::getFileUrl)
                    .toList();
            
            response.put("success", true);
            response.put("message", "Files uploaded successfully");
            response.put("filePaths", filePaths);
            response.put("fileUrls", fileUrls);
            response.put("count", filePaths.size());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("error", "Failed to upload files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("error", "Failed to upload files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteFile(@RequestParam("filePath") String filePath) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            fileUploadService.deleteFile(filePath);
            response.put("success", true);
            response.put("message", "File deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to delete file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
