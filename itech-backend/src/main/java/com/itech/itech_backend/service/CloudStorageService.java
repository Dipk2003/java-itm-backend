package com.itech.itech_backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CloudStorageService {

    @Value("${cloud.storage.enabled:false}")
    private boolean cloudStorageEnabled;

    @Value("${aws.s3.bucket-name:}")
    private String bucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.s3.access-key:}")
    private String accessKey;

    @Value("${aws.s3.secret-key:}")
    private String secretKey;

    @Value("${aws.s3.base-url:}")
    private String baseUrl;

    private S3Client s3Client;

    private S3Client getS3Client() {
        if (s3Client == null && cloudStorageEnabled) {
            try {
                AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
                s3Client = S3Client.builder()
                        .region(Region.of(region))
                        .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                        .build();
            } catch (Exception e) {
                log.error("Failed to initialize S3 client", e);
                throw new RuntimeException("Failed to initialize cloud storage", e);
            }
        }
        return s3Client;
    }

    public boolean isCloudStorageEnabled() {
        return cloudStorageEnabled && hasValidCredentials();
    }

    private boolean hasValidCredentials() {
        return bucketName != null && !bucketName.trim().isEmpty() &&
               accessKey != null && !accessKey.trim().isEmpty() &&
               secretKey != null && !secretKey.trim().isEmpty();
    }

    public String uploadFile(MultipartFile file, String directory) throws IOException {
        if (!isCloudStorageEnabled()) {
            throw new RuntimeException("Cloud storage is not enabled or configured");
        }

        validateFile(file);
        
        String key = generateFileKey(directory, file.getOriginalFilename());
        
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            getS3Client().putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            String fileUrl = generateFileUrl(key);
            log.info("Successfully uploaded file to cloud storage: {}", fileUrl);
            return fileUrl;
            
        } catch (S3Exception e) {
            log.error("Failed to upload file to S3: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to cloud storage", e);
        }
    }

    public List<String> uploadMultipleFiles(List<MultipartFile> files, String directory) throws IOException {
        List<String> uploadedUrls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                String url = uploadFile(file, directory);
                uploadedUrls.add(url);
            } catch (IOException e) {
                log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                // Delete any successfully uploaded files to maintain consistency
                for (String uploadedUrl : uploadedUrls) {
                    try {
                        deleteFile(extractKeyFromUrl(uploadedUrl));
                    } catch (Exception deleteException) {
                        log.warn("Failed to cleanup uploaded file: {}", uploadedUrl, deleteException);
                    }
                }
                throw new IOException("Failed to upload files - batch upload failed", e);
            }
        }
        
        return uploadedUrls;
    }

    public List<String> uploadProductImages(MultipartFile[] images, String productId) throws IOException {
        if (!isCloudStorageEnabled()) {
            throw new RuntimeException("Cloud storage is not enabled or configured");
        }

        if (images == null || images.length == 0) {
            throw new IllegalArgumentException("No images provided");
        }

        List<String> uploadedUrls = new ArrayList<>();
        String directory = "products/" + productId;

        for (int i = 0; i < images.length; i++) {
            MultipartFile image = images[i];
            try {
                validateImageFile(image);
                
                // Process and resize image
                BufferedImage processedImage = processImage(image);
                
                // Convert to bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                String format = getImageFormat(image.getOriginalFilename());
                ImageIO.write(processedImage, format, baos);
                
                // Create key for the image
                String key = generateImageKey(directory, productId, i + 1, getFileExtension(image.getOriginalFilename()));
                
                // Upload to S3
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(getContentType(image.getOriginalFilename()))
                        .build();

                byte[] imageBytes = baos.toByteArray();
                getS3Client().putObject(putObjectRequest, RequestBody.fromBytes(imageBytes));
                
                String imageUrl = generateFileUrl(key);
                uploadedUrls.add(imageUrl);
                
                // Upload thumbnail
                uploadThumbnail(processedImage, directory, "thumb_" + productId + "_" + (i + 1), format);
                
                log.info("Successfully uploaded product image: {}", imageUrl);
                
            } catch (Exception e) {
                log.error("Failed to upload product image: {}", image.getOriginalFilename(), e);
                // Cleanup uploaded files
                for (String uploadedUrl : uploadedUrls) {
                    try {
                        deleteFile(extractKeyFromUrl(uploadedUrl));
                    } catch (Exception deleteException) {
                        log.warn("Failed to cleanup uploaded file: {}", uploadedUrl, deleteException);
                    }
                }
                throw new IOException("Failed to upload product images", e);
            }
        }

        return uploadedUrls;
    }

    private void uploadThumbnail(BufferedImage originalImage, String directory, String filename, String format) {
        try {
            // Create thumbnail (300x300)
            BufferedImage thumbnail = createThumbnail(originalImage, 300, 300);
            
            // Convert to bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, format, baos);
            
            // Upload thumbnail
            String key = directory + "/thumbnails/" + filename + "." + format;
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("image/" + format)
                    .build();

            getS3Client().putObject(putObjectRequest, RequestBody.fromBytes(baos.toByteArray()));
            log.debug("Successfully uploaded thumbnail: {}", key);
            
        } catch (Exception e) {
            log.warn("Failed to upload thumbnail: {}", filename, e);
            // Don't fail the main upload if thumbnail fails
        }
    }

    public void deleteFile(String key) {
        if (!isCloudStorageEnabled()) {
            log.warn("Cloud storage is not enabled - cannot delete file: {}", key);
            return;
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            getS3Client().deleteObject(deleteObjectRequest);
            log.info("Successfully deleted file from cloud storage: {}", key);
            
        } catch (S3Exception e) {
            log.error("Failed to delete file from S3: {}", e.getMessage(), e);
        }
    }

    public void deleteFileByUrl(String fileUrl) {
        if (fileUrl != null && !fileUrl.trim().isEmpty()) {
            String key = extractKeyFromUrl(fileUrl);
            if (key != null) {
                deleteFile(key);
            }
        }
    }

    private String generateFileKey(String directory, String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uniqueId = UUID.randomUUID().toString();
        return directory + "/" + uniqueId + extension;
    }

    private String generateImageKey(String directory, String productId, int sequence, String extension) {
        return directory + "/" + productId + "_" + sequence + extension;
    }

    private String generateFileUrl(String key) {
        if (baseUrl != null && !baseUrl.trim().isEmpty()) {
            return baseUrl + "/" + key;
        }
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
    }

    private String extractKeyFromUrl(String url) {
        if (url == null) return null;
        
        try {
            if (baseUrl != null && !baseUrl.trim().isEmpty() && url.startsWith(baseUrl)) {
                return url.substring(baseUrl.length() + 1);
            }
            
            // Handle standard S3 URLs
            String bucketUrl = "https://" + bucketName + ".s3." + region + ".amazonaws.com/";
            if (url.startsWith(bucketUrl)) {
                return url.substring(bucketUrl.length());
            }
            
            // Handle s3:// URLs
            if (url.startsWith("s3://")) {
                return url.substring(("s3://" + bucketName + "/").length());
            }
            
        } catch (Exception e) {
            log.warn("Failed to extract key from URL: {}", url, e);
        }
        
        return null;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Check file size (5MB limit)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB");
        }
    }

    private void validateImageFile(MultipartFile file) {
        validateFile(file);
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Validate image by trying to read it
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IllegalArgumentException("Invalid image file");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot read image file", e);
        }
    }

    private BufferedImage processImage(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IOException("Invalid image file");
        }

        // Resize if too large (max 1920x1080)
        int maxWidth = 1920;
        int maxHeight = 1080;
        
        if (originalImage.getWidth() > maxWidth || originalImage.getHeight() > maxHeight) {
            return resizeImage(originalImage, maxWidth, maxHeight);
        }
        
        return originalImage;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int maxWidth, int maxHeight) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        
        // Calculate new dimensions maintaining aspect ratio
        double aspectRatio = (double) width / height;
        int newWidth = maxWidth;
        int newHeight = maxHeight;
        
        if (aspectRatio > 1) {
            // Landscape
            newHeight = (int) (maxWidth / aspectRatio);
        } else {
            // Portrait
            newWidth = (int) (maxHeight * aspectRatio);
        }
        
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
        resizedImage.getGraphics().drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        return resizedImage;
    }

    private BufferedImage createThumbnail(BufferedImage originalImage, int width, int height) {
        BufferedImage thumbnail = new BufferedImage(width, height, originalImage.getType());
        thumbnail.getGraphics().drawImage(originalImage, 0, 0, width, height, null);
        return thumbnail;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    private String getImageFormat(String filename) {
        String extension = getFileExtension(filename);
        switch (extension) {
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
                return "jpeg";
        }
    }

    private String getContentType(String filename) {
        String extension = getFileExtension(filename);
        switch (extension) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".webp":
                return "image/webp";
            default:
                return "image/jpeg";
        }
    }

    public String getThumbnailUrl(String imageUrl) {
        if (imageUrl == null) return null;
        
        String key = extractKeyFromUrl(imageUrl);
        if (key == null) return null;
        
        // Extract directory and filename
        int lastSlash = key.lastIndexOf('/');
        if (lastSlash == -1) return null;
        
        String directory = key.substring(0, lastSlash);
        String filename = key.substring(lastSlash + 1);
        
        // Remove extension and add thumb prefix
        int dotIndex = filename.lastIndexOf('.');
        String nameWithoutExt = dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
        String extension = dotIndex > 0 ? filename.substring(dotIndex) : "";
        
        String thumbnailKey = directory + "/thumbnails/thumb_" + nameWithoutExt + extension;
        return generateFileUrl(thumbnailKey);
    }
}
