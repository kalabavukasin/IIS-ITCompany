package rs.ac.uns.ftn.informatika.jpa.Controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import rs.ac.uns.ftn.informatika.jpa.Dto.SavedTestDTO;
import rs.ac.uns.ftn.informatika.jpa.Dto.TestInviteRequestDTO;
import rs.ac.uns.ftn.informatika.jpa.Service.TestService;

import java.io.IOException;

@RestController
@RequestMapping("/api/tests")
public class TestController {
    private final TestService service;


    public TestController(TestService service) {
        this.service = service;
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
}
