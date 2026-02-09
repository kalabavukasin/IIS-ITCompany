package rs.ac.uns.ftn.informatika.jpa.Controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.uns.ftn.informatika.jpa.Service.CvStorageService;

import java.io.IOException;

@RestController
@RequestMapping("/api/cv")
public class CvController {
    private final CvStorageService cvStorageService;

    public CvController(CvStorageService cvStorageService) {
        this.cvStorageService = cvStorageService;
    }

    @GetMapping("/{relativePath}")
    public ResponseEntity<Resource> download(@PathVariable String relativePath) throws IOException {
        Resource file = cvStorageService.loadAsResource(relativePath);
        MediaType type = cvStorageService.detectMediaType(relativePath, null);

        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + relativePath + "\"")
                .body(file);
    }
}
