package rs.ac.uns.ftn.informatika.jpa.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import rs.ac.uns.ftn.informatika.jpa.Model.Application;
import rs.ac.uns.ftn.informatika.jpa.Model.Requestion;
import rs.ac.uns.ftn.informatika.jpa.Repository.ApplicationRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GeminiScoringService {

    private static final Logger log = LoggerFactory.getLogger(GeminiScoringService.class);

    private final RestTemplate restTemplate;
    private final ApplicationRepository appRepo;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public GeminiScoringService(RestTemplate restTemplate,
                                ApplicationRepository appRepo,
                                ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.appRepo = appRepo;
        this.objectMapper = objectMapper;
    }

    @Async("aiScoringExecutor")
    @Transactional
    public void autoScoreAsync(Long applicationId) {
        try {
            Application app = appRepo.findByIdWithCvAndRequestion(applicationId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Application not found for AI scoring: " + applicationId));

            Requestion req = app.getJobPosting().getRequestion();
            String cvText = app.getCandidate().getCvTextContent();

            if (cvText == null || cvText.isBlank()) {
                log.warn("AutoScore skipped for applicationId={}: no CV text available", applicationId);
                return;
            }

            String prompt = buildSingleCandidatePrompt(req, cvText);
            String rawJson = callGemini(prompt);
            SingleScoreResult result = parseSingleScore(rawJson);

            app.setAutoAiScore(result.score());
            app.setAutoAiScoreNote(result.note());
            app.setAutoAiScoredAt(OffsetDateTime.now());
            appRepo.save(app);

            log.info("AutoScore complete for applicationId={}: score={}", applicationId, result.score());

        } catch (Exception e) {
            // Must NOT propagate — the application submission already returned to the candidate
            log.error("AutoScore failed for applicationId={}: {}", applicationId, e.getMessage(), e);
        }
    }

    @Transactional
    public int bulkScore(Long postingId) {
        List<Application> applications = appRepo.findActiveByPostingIdWithCv(postingId);

        if (applications.isEmpty()) {
            log.info("BulkScore: no active applications for postingId={}", postingId);
            return 0;
        }

        String prompt = buildBulkCandidatesPrompt(applications);
        String rawJson = callGemini(prompt); // exceptions propagate to controller

        List<BulkScoreResult> results = parseBulkScores(rawJson);

        Map<Long, Application> appById = applications.stream()
                .collect(Collectors.toMap(Application::getId, Function.identity()));

        OffsetDateTime now = OffsetDateTime.now();
        int updated = 0;

        for (BulkScoreResult result : results) {
            Application app = appById.get(result.applicationId());
            if (app == null) {
                // Security guard — never write to an application outside the requested posting
                log.warn("BulkScore: Gemini returned applicationId={} not belonging to postingId={}; skipping",
                        result.applicationId(), postingId);
                continue;
            }
            app.setBulkAiScore(Math.max(0, Math.min(100, result.score())));
            app.setBulkAiScoreNote(result.note());
            app.setBulkAiScoredAt(now);
            appRepo.save(app);
            updated++;
        }

        log.info("BulkScore complete for postingId={}: {}/{} applications scored",
                postingId, updated, applications.size());
        return updated;
    }

    private String callGemini(String promptText) {
        String url = apiUrl + "?key=" + apiKey;

        Map<String, Object> request = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", promptText))
                )),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "responseMimeType", "application/json"
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            log.error("Gemini API HTTP error: status={}, body={}", response.getStatusCode(), response.getBody());
            throw new RuntimeException("Gemini API error: HTTP " + response.getStatusCode());
        }

        log.debug("Gemini raw response: {}", response.getBody());

        // Gemini wraps the generated content inside candidates[0].content.parts[0].text
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            String extracted = root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text")
                    .asText();
            log.info("Gemini extracted JSON: {}", extracted);
            return extracted;
        } catch (Exception e) {
            log.error("Failed to parse Gemini response structure. Raw body: {}", response.getBody());
            throw new RuntimeException("Failed to extract text from Gemini response: " + e.getMessage(), e);
        }
    }

    private String buildSingleCandidatePrompt(Requestion req, String cvText) {
        return """
                You are an expert technical recruiter. Score the candidate's CV against the job requirements.

                ## Job Requirements
                - Position: %s
                - Description: %s
                - Required Skills: %s
                - Seniority Level: %s
                - Minimum Experience Required: %d years
                - Nice-to-Have Skills: %s

                ## Candidate CV
                %s

                ## Instructions
                Analyze how well the candidate matches the job requirements.
                Return ONLY a valid JSON object with no markdown formatting:
                {"score": <integer 0-100>, "note": "<concise explanation in English, max 300 characters>"}

                Scoring guide:
                - 80-100: Excellent match, meets all requirements
                - 60-79: Good match, meets most requirements
                - 40-59: Partial match, meets some requirements
                - 20-39: Weak match, missing key requirements
                - 0-19: Poor match, does not meet requirements
                """.formatted(
                req.getName(),
                req.getDescription(),
                req.getSkills(),
                req.getSeniority() != null ? req.getSeniority().name() : "Not specified",
                req.getMinExperienceYears() != null ? req.getMinExperienceYears() : 0,
                req.getNiceToHaveSkills() != null ? req.getNiceToHaveSkills() : "None",
                truncate(cvText, 8000)
        );
    }

    private String buildBulkCandidatesPrompt(List<Application> applications) {
        Requestion req = applications.get(0).getJobPosting().getRequestion();

        StringBuilder sb = new StringBuilder();
        sb.append("""
                You are an expert technical recruiter. Score EVERY candidate below against the job requirements.

                ## Job Requirements
                - Position: %s
                - Description: %s
                - Required Skills: %s
                - Seniority Level: %s
                - Minimum Experience Required: %d years
                - Nice-to-Have Skills: %s

                ## Candidates
                """.formatted(
                req.getName(),
                req.getDescription(),
                req.getSkills(),
                req.getSeniority() != null ? req.getSeniority().name() : "Not specified",
                req.getMinExperienceYears() != null ? req.getMinExperienceYears() : 0,
                req.getNiceToHaveSkills() != null ? req.getNiceToHaveSkills() : "None"
        ));

        for (Application app : applications) {
            String cv = app.getCandidate().getCvTextContent();
            sb.append("\n--- APPLICATION ID: ").append(app.getId()).append(" ---\n");
            sb.append(cv != null && !cv.isBlank() ? truncate(cv, 3000) : "[No CV text available]");
            sb.append("\n");
        }

        sb.append("""

                ## Instructions
                Score EVERY candidate listed above. Use the APPLICATION ID values exactly as shown above.
                Return ONLY a valid JSON array with no markdown formatting:
                [{"applicationId": <Long>, "score": <integer 0-100>, "note": "<concise explanation in English, max 300 characters>"}, ...]

                Scoring guide:
                - 80-100: Excellent match
                - 60-79: Good match
                - 40-59: Partial match
                - 20-39: Weak match
                - 0-19: Poor match
                """);

        return sb.toString();
    }

    private SingleScoreResult parseSingleScore(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            int score = Math.max(0, Math.min(100, node.path("score").asInt()));
            String note = node.path("note").asText("No explanation provided.");
            return new SingleScoreResult(score, note);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini single-score response: " + e.getMessage(), e);
        }
    }

    private List<BulkScoreResult> parseBulkScores(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            if (!root.isArray()) {
                throw new RuntimeException("Expected JSON array from Gemini bulk response, got: " + root.getNodeType());
            }
            return objectMapper.convertValue(root, new TypeReference<List<BulkScoreResult>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini bulk-score response: " + e.getMessage(), e);
        }
    }

    private String truncate(String text, int maxChars) {
        if (text == null) return "";
        return text.length() > maxChars ? text.substring(0, maxChars) + "...[truncated]" : text;
    }

    private record SingleScoreResult(int score, String note) {}
    private record BulkScoreResult(Long applicationId, int score, String note) {}
}
