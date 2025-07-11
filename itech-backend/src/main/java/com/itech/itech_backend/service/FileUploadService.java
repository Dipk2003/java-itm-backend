package com.itech.itech_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class FileUploadService {

    @Autowired
    private CloudStorageService cloudStorageService;

    @Value("${file.upload.directory:uploads}")
    private String uploadDirectory;

    @Value("${file.upload.max-size:5242880}") // 5MB default
    private long maxFileSize;
    
    @Value("${image.upload.allowed-extensions:jpg,jpeg,png,gif,webp}")
    private String allowedExtensions;
    
    @Value("${image.max-width:1920}")
    private int maxWidth;
    
    @Value("${image.max-height:1080}")
    private int maxHeight;
    
    @Value("${image.thumbnail.width:300}")
    private int thumbnailWidth;
    
    @Value("${image.thumbnail.height:300}")
    private int thumbnailHeight;
    
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    // Counter for generating sequential IDs
    private static final AtomicLong imageIdCounter = new AtomicLong(System.currentTimeMillis());

    @Value("${file.upload.allowed-types:image/jpeg,image/png,image/gif,image/webp}")
    private String allowedTypesStr;

    private static final List<String> DEFAULT_ALLOWED_TYPES = Arrays.asList(
        "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    public String uploadFile(MultipartFile file, String subDirectory) throws IOException {
        validateFile(file);
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDirectory, subDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename using Long ID
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        Long imageId = imageIdCounter.incrementAndGet();
        String uniqueFilename = imageId.toString() + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path for URL access
        return subDirectory + "/" + uniqueFilename;
    }

    public List<String> uploadMultipleFiles(List<MultipartFile> files, String subDirectory) throws IOException {
        return files.stream()
            .map(file -> {
                try {
                    return uploadFile(file, subDirectory);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename(), e);
                }
            })
            .toList();
    }

    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(uploadDirectory, filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Log the error but don't throw exception
            System.err.println("Failed to delete file: " + filePath + " - " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
        }

        // Check file type
        String contentType = file.getContentType();
        List<String> allowedTypes = getAllowedTypes();
        
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: " + String.join(", ", allowedTypes));
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasValidExtension(originalFilename)) {
            throw new IllegalArgumentException("Invalid file extension");
        }
    }

    private List<String> getAllowedTypes() {
        if (allowedTypesStr != null && !allowedTypesStr.trim().isEmpty()) {
            return Arrays.asList(allowedTypesStr.split(","));
        }
        return DEFAULT_ALLOWED_TYPES;
    }

    private boolean hasValidExtension(String filename) {
        String[] validExtensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
        String lowerCaseFilename = filename.toLowerCase();
        return Arrays.stream(validExtensions).anyMatch(lowerCaseFilename::endsWith);
    }

    public String getFileUrl(String filePath) {
        return "/api/files/" + filePath;
    }
    
    /**
     * Upload and process product images with validation and resizing
     */
    public List<String> uploadProductImages(MultipartFile[] images, String subDirectory) throws IOException {
        if (images == null || images.length == 0) {
            throw new IllegalArgumentException("No images provided");
        }
        
        // Use cloud storage if enabled
        if (cloudStorageService.isCloudStorageEnabled()) {
            String productId = extractProductIdFromSubDirectory(subDirectory);
            return cloudStorageService.uploadProductImages(images, productId);
        }
        
        // Fallback to local storage
        List<String> uploadedImagePaths = new ArrayList<>();
        
        for (int i = 0; i < images.length; i++) {
            MultipartFile image = images[i];
            try {
                validateImageFile(image);
                String imagePath = uploadProcessedImageWithSequence(image, subDirectory, i + 1);
                uploadedImagePaths.add(imagePath);
                log.info("Successfully uploaded image: {}", imagePath);
            } catch (Exception e) {
                log.error("Failed to upload image: {}", image.getOriginalFilename(), e);
                // Continue with other images, but log the error
                throw new IOException("Failed to upload image: " + image.getOriginalFilename() + " - " + e.getMessage());
            }
        }
        
        return uploadedImagePaths;
    }
    
    /**
     * Upload and process a single image with resizing and sequence number
     */
    private String uploadProcessedImageWithSequence(MultipartFile image, String subDirectory, int sequence) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDirectory, subDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Extract product ID from subdirectory (assumes format: "products/{productId}")
        String productId = extractProductIdFromSubDirectory(subDirectory);
        
        // Generate filename: {productId}_{sequence}{extension}
        String originalFilename = image.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = productId + "_" + sequence + fileExtension;
        
        // Process and resize image
        BufferedImage originalImage = ImageIO.read(image.getInputStream());
        if (originalImage == null) {
            throw new IOException("Invalid image file: " + originalFilename);
        }
        
        // Resize image if it's too large
        BufferedImage resizedImage = resizeImageIfNeeded(originalImage);
        
        // Save processed image
        Path filePath = uploadPath.resolve(uniqueFilename);
        String formatName = getImageFormat(fileExtension);
        ImageIO.write(resizedImage, formatName, filePath.toFile());
        
        // Also create thumbnail
        createThumbnail(resizedImage, uploadPath, "thumb_" + uniqueFilename, formatName);
        
        // Return relative path for URL access
        return subDirectory + "/" + uniqueFilename;
    }
    
    /**
     * Upload and process a single image with resizing (legacy method)
     */
    private String uploadProcessedImage(MultipartFile image, String subDirectory) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDirectory, subDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename using Long ID
        String originalFilename = image.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        Long imageId = imageIdCounter.incrementAndGet();
        String uniqueFilename = imageId.toString() + fileExtension;
        
        // Process and resize image
        BufferedImage originalImage = ImageIO.read(image.getInputStream());
        if (originalImage == null) {
            throw new IOException("Invalid image file: " + originalFilename);
        }
        
        // Resize image if it's too large
        BufferedImage resizedImage = resizeImageIfNeeded(originalImage);
        
        // Save processed image
        Path filePath = uploadPath.resolve(uniqueFilename);
        String formatName = getImageFormat(fileExtension);
        ImageIO.write(resizedImage, formatName, filePath.toFile());
        
        // Also create thumbnail
        createThumbnail(resizedImage, uploadPath, "thumb_" + uniqueFilename, formatName);
        
        // Return relative path for URL access
        return subDirectory + "/" + uniqueFilename;
    }
    
    /**
     * Validate image file specifically
     */
    private void validateImageFile(MultipartFile file) {
        validateFile(file); // Use existing validation
        
        // Additional image-specific validation
        try (var inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IllegalArgumentException("File is not a valid image: " + file.getOriginalFilename());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read image file: " + file.getOriginalFilename());
        }
    }
    
    /**
     * Resize image if it exceeds maximum dimensions
     */
    private BufferedImage resizeImageIfNeeded(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        // Check if resizing is needed
        if (width <= maxWidth && height <= maxHeight) {
            return originalImage;
        }
        
        // Calculate new dimensions maintaining aspect ratio
        double aspectRatio = (double) width / height;
        int newWidth = maxWidth;
        int newHeight = maxHeight;
        
        if (aspectRatio > 1) {
            // Landscape - width is limiting factor
            newHeight = (int) (maxWidth / aspectRatio);
        } else {
            // Portrait - height is limiting factor
            newWidth = (int) (maxHeight * aspectRatio);
        }
        
        log.info("Resizing image from {}x{} to {}x{}", width, height, newWidth, newHeight);
        return Scalr.resize(originalImage, Scalr.Method.QUALITY, newWidth, newHeight);
    }
    
    /**
     * Create thumbnail for the image
     */
    private void createThumbnail(BufferedImage originalImage, Path uploadPath, String thumbnailFilename, String formatName) {
        try {
            BufferedImage thumbnail = Scalr.resize(originalImage, Scalr.Method.SPEED, thumbnailWidth, thumbnailHeight);
            Path thumbnailPath = uploadPath.resolve(thumbnailFilename);
            ImageIO.write(thumbnail, formatName, thumbnailPath.toFile());
            log.debug("Created thumbnail: {}", thumbnailFilename);
        } catch (IOException e) {
            log.warn("Failed to create thumbnail for {}: {}", thumbnailFilename, e.getMessage());
            // Don't fail the upload if thumbnail creation fails
        }
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg"; // Default extension
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
    
    /**
     * Extract product ID from subdirectory path
     */
    private String extractProductIdFromSubDirectory(String subDirectory) {
        // Assumes format: "products/{productId}" or "products/{productId}/..."
        if (subDirectory != null && subDirectory.startsWith("products/")) {
            String[] parts = subDirectory.split("/");
            if (parts.length >= 2) {
                return parts[1]; // Return the product ID part
            }
        }
        // Fallback to timestamp if pattern doesn't match
        return String.valueOf(System.currentTimeMillis());
    }
    
    /**
     * Get image format name for ImageIO
     */
    private String getImageFormat(String fileExtension) {
        switch (fileExtension.toLowerCase()) {
            case ".jpg":
            case ".jpeg":
                return "jpeg";
            case ".png":
                return "png";
            case ".gif":
                return "gif";
            case ".webp":
                return "webp";
            default:
                return "jpeg"; // Default format
        }
    }
    
    /**
     * Get thumbnail URL for an image
     */
    public String getThumbnailUrl(String imagePath) {
        if (imagePath == null) return null;
        
        // Use cloud storage if enabled
        if (cloudStorageService.isCloudStorageEnabled()) {
            return cloudStorageService.getThumbnailUrl(imagePath);
        }
        
        // Fallback to local storage
        int lastSlash = imagePath.lastIndexOf('/');
        if (lastSlash == -1) {
            return "/api/files/thumb_" + imagePath;
        }
        
        String directory = imagePath.substring(0, lastSlash + 1);
        String filename = imagePath.substring(lastSlash + 1);
        return "/api/files/" + directory + "thumb_" + filename;
    }
    
    /**
     * Delete image and its thumbnail
     */
    public void deleteImageAndThumbnail(String imagePath) {
        if (imagePath == null) return;
        
        // Use cloud storage if enabled
        if (cloudStorageService.isCloudStorageEnabled()) {
            cloudStorageService.deleteFileByUrl(imagePath);
            // Cloud storage service handles thumbnail deletion
            return;
        }
        
        // Fallback to local storage
        // Delete main image
        deleteFile(imagePath);
        
        // Delete thumbnail
        int lastSlash = imagePath.lastIndexOf('/');
        if (lastSlash != -1) {
            String directory = imagePath.substring(0, lastSlash + 1);
            String filename = imagePath.substring(lastSlash + 1);
            String thumbnailPath = directory + "thumb_" + filename;
            deleteFile(thumbnailPath);
        }
    }
}
