package rs.ac.uns.ftn.informatika.jpa.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import rs.ac.uns.ftn.informatika.jpa.Dto.SavedTestDTO;

import java.io.IOException;
import java.nio.file.*;
import java.util.Locale;
import java.util.UUID;

@Service
public class TestStorageService {

    @Value("${app.test.upload-dir}")
    private String uploadDir; // npr. src/main/resources/static/uploads/tests

    public SavedTestDTO save(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Test file is empty.");
        }

        String original = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "test.pdf" : file.getOriginalFilename()
        );
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        String lower = original.toLowerCase(Locale.ROOT);

        boolean okByExt = lower.endsWith(".pdf")
                || lower.endsWith(".doc")
                || lower.endsWith(".docx")
                || lower.endsWith(".odt");

        boolean okByMime = contentType.equals("application/pdf")
                || contentType.equals("application/msword")
                || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                || contentType.equals("application/vnd.oasis.opendocument.text");

        if (!(okByExt || okByMime)) {
            throw new IllegalArgumentException("Unsupported test file type.");
        }

        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);

        String ext = okByExt ? lower.substring(lower.lastIndexOf('.')) : ".pdf";
        String storedName = UUID.randomUUID().toString() + ext;
        Path target = root.resolve(storedName);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // Sastavi rezultat
       /* SavedCv out = new SavedCv();
        out.path = storedName;
        out.originalName = original;
        out.sizeBytes = file.getSize();
        out.mime = detectMimeByExt(storedName);*/
        return new SavedTestDTO(original,storedName,storedName);
    }

    public Resource loadAsResource(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return null;
        Path root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path resolved = root.resolve(relativePath).normalize();
        return new FileSystemResource(resolved);
    }

    public MediaType contentTypeFor(String relativePath) {
        String mime = detectMimeByExt(relativePath);
        try {
            return MediaType.parseMediaType(mime);
        } catch (Exception ignore) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }


    private String detectMimeByExt(String relativePath) {
        if (relativePath == null) return "application/octet-stream";
        String lower = relativePath.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".doc")) return "application/msword";
        if (lower.endsWith(".docx"))
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lower.endsWith(".odt")) return "application/vnd.oasis.opendocument.text";
        return "application/octet-stream";
    }

    public static class SavedCv {
        public String path;
        public String originalName;
        public String mime;
        public long sizeBytes;
    }
}
