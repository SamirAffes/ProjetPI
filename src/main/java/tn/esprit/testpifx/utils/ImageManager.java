package tn.esprit.testpifx.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for managing image uploads and storage
 */
public class ImageManager {
    private static final Logger logger = LoggerFactory.getLogger(ImageManager.class);
    
    // Define the directory where profile images will be stored
    private static final String PROFILE_IMAGES_DIR = "profile_images";
    
    /**
     * Save a profile image to the application's image directory
     * 
     * @param sourceFilePath The path to the source image file
     * @return The path to the saved image file (relative to the application)
     * @throws IOException If there's an error copying the file
     */
    public static String saveProfileImage(String sourceFilePath) throws IOException {
        // Create the profile images directory if it doesn't exist
        Path imagesDir = getOrCreateProfileImagesDirectory();
        
        // Generate a unique filename for the image to prevent conflicts
        String originalFilename = Paths.get(sourceFilePath).getFileName().toString();
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String newFilename = UUID.randomUUID().toString() + extension;
        
        // Copy the file to our application's directory
        Path sourcePath = Paths.get(sourceFilePath);
        Path targetPath = imagesDir.resolve(newFilename);
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        logger.info("Profile image saved: {}", targetPath);
        
        // Return the relative path to the image
        return PROFILE_IMAGES_DIR + "/" + newFilename;
    }
    
    /**
     * Get the absolute file path for a stored image
     * 
     * @param relativePath The relative path of the image as stored in the database
     * @return The absolute path to the image file
     */
    public static String getAbsolutePath(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        
        // If the path is already absolute, return it as is
        if (Paths.get(relativePath).isAbsolute()) {
            return relativePath;
        }
        
        // If it's a URL (for placeholder images), return it as is
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath;
        }
        
        // Otherwise, resolve it against the application directory
        return getApplicationDirectory().resolve(relativePath).toString();
    }
    
    /**
     * Get or create the directory for storing profile images
     */
    private static Path getOrCreateProfileImagesDirectory() throws IOException {
        Path appDir = getApplicationDirectory();
        Path imagesDir = appDir.resolve(PROFILE_IMAGES_DIR);
        
        if (!Files.exists(imagesDir)) {
            Files.createDirectories(imagesDir);
            logger.info("Created profile images directory: {}", imagesDir);
        }
        
        return imagesDir;
    }
    
    /**
     * Get the application's base directory
     */
    private static Path getApplicationDirectory() {
        // Get the directory where the application is running
        String userDir = System.getProperty("user.dir");
        return Paths.get(userDir);
    }
    
    /**
     * Check if the given path points to an existing image file
     */
    public static boolean imageExists(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return true; // Assume URLs are valid
        }
        
        return Files.exists(Paths.get(getAbsolutePath(path)));
    }
}