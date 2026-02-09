package rs.ac.uns.ftn.informatika.jpa.Controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.uns.ftn.informatika.jpa.Dto.SavedTestDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.TestInviteRequestDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.TestRefuseDTO;
import rs.ac.uns.ftn.informatika.jpa.Service.TestService;
import rs.ac.uns.ftn.informatika.jpa.Service.TestStorageService;

import java.io.IOException;

@RestController
@RequestMapping("/api/tests")
public class TestController {
    private final TestService service;
    private final TestStorageService storageService;


    public TestController(TestService service,TestStorageService storageService) {
        this.service = service;
        this.storageService = storageService;
    }

    @PostMapping(
            value = "/invite",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<SavedTestDTO> invite(
            @RequestPart("data") TestInviteRequestDTO dto,
            @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(service.createInvite(dto, file));
    }
    @PostMapping("/{applicationId}/refuse-with-score")
    @PreAuthorize("hasAnyAuthority('HR_MANAGER','HIRING_MANAGER')")
    public ResponseEntity<Void> refuseWithScore(
            @PathVariable Long applicationId,
            @RequestBody TestRefuseDTO dto
    ) {
        service.refuseWithScore(applicationId, dto);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/by-application/{applicationId}/details")
    public ResponseEntity<?> getTestDetailsByApplication(@PathVariable Long applicationId) {
        return service.getDetailsByApplicationId(applicationId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
    @GetMapping("/{relativePath}")
    public ResponseEntity<Resource> download(@PathVariable String relativePath) throws IOException {
        Resource file = storageService.loadAsResource(relativePath);
        MediaType type = storageService.detectMediaType(relativePath, null);

        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + relativePath + "\"")
                .body(file);
    }
    @PatchMapping(path = "/{id}/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SavedTestDTO> updateTestFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required.");
        }

        try {
            SavedTestDTO saved = service.updateTest(id, file);
            return ResponseEntity.ok(saved);
        } catch (IllegalStateException e) {

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Test invite not found.");
        }
    }
}
