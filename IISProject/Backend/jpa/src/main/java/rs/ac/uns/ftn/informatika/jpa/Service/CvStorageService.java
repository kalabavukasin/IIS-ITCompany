package rs.ac.uns.ftn.informatika.jpa.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import java.util.Locale;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class CvStorageService {

    @Value("${app.cv.upload-dir}")
    private String uploadDir;

    public SavedCv save(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("CV file is empty.");
        }

        // Validacija tipa
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "cv.pdf" : file.getOriginalFilename());
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        String lower = original.toLowerCase();

        boolean okByExt = lower.endsWith(".pdf") || lower.endsWith(".doc") || lower.endsWith(".docx") || lower.endsWith(".odt");
        boolean okByMime = contentType.contains("pdf") || contentType.contains("word") || contentType.contains("officedocument") || contentType.contains("opendocument");
        if (!okByExt && !okByMime) {
            throw new IllegalArgumentException("Unsupported CV file type.");
        }

        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);

        String ext = okByExt ? lower.substring(lower.lastIndexOf('.')) : ".pdf";
        String storedName = UUID.randomUUID().toString() + ext;
        Path target = root.resolve(storedName);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        SavedCv scv = new SavedCv();
        scv.path = storedName;
        scv.originalName = original;
        scv.mime = contentType;
        scv.sizeBytes = file.getSize();
        return scv;
    }

    public static class SavedCv {
        public String path;
        public String originalName;
        public String mime;
        public long sizeBytes;
    }
    public Resource loadAsResource(String relativePath) throws IOException {
        if (relativePath == null || relativePath.isBlank()) {
            throw new IllegalArgumentException("CV path is empty.");
        }
        Path filePath = resolveAbsolute(relativePath);
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new NoSuchFileException("CV file not found: " + filePath);
        }
        return new FileSystemResource(filePath.toFile());
    }

    //Pravi apsolutnu od relativne putanje
    public Path resolveAbsolute(String relativePath) {
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        return root.resolve(Paths.get(relativePath).getFileName().toString());
    }
    public MediaType detectMediaType(String relativePath, String fallbackMime) throws IOException {
        Path p = resolveAbsolute(relativePath);
        String probed = Files.probeContentType(p);
        String mime = (probed != null ? probed : (fallbackMime != null ? fallbackMime : "application/octet-stream")).toLowerCase(Locale.ROOT);

        String lower = relativePath.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) mime = "application/pdf";
        else if (lower.endsWith(".doc")) mime = "application/msword";
        else if (lower.endsWith(".docx"))
            mime = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        else if (lower.endsWith(".odt")) mime = "application/vnd.oasis.opendocument.text";

        try {
            return MediaType.parseMediaType(mime);
        } catch (Exception ignore) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
