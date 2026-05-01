package pl.netia.troubleticket.bdd.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import pl.netia.troubleticket.bdd.TestJwtUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TroubleTicketSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    private String currentToken;
    private ResponseEntity<String> lastResponse;
    private String lastCreatedId;

    // ── State reset between scenarios ─────────────────────────────────────────

    @Before
    public void resetState() {
        currentToken = null;
        lastResponse = null;
        lastCreatedId = null;
    }

    // ── Given ─────────────────────────────────────────────────────────────────

    @Given("a valid JWT token for tenant {string}")
    public void givenValidToken(String tenantId) {
        currentToken = TestJwtUtils.generateToken(tenantId);
    }

    @Given("no authentication token")
    public void givenNoToken() {
        currentToken = null;
    }

    @Given("a trouble ticket exists with externalId {string} for tenant {string}")
    public void givenTicketExists(String externalId, String tenantId) throws Exception {
        String token = TestJwtUtils.generateToken(tenantId);
        String body = """
                {
                  "externalId": "%s",
                  "serviceId": 100001,
                  "description": "Pre-existing ticket",
                  "status": "new",
                  "note": "Setup note"
                }
                """.formatted(externalId);

        ResponseEntity<String> response = post("/api/v1/troubleTicket", body, token);
        JsonNode json = mapper.readTree(response.getBody());
        if (json.has("id")) {
            lastCreatedId = json.get("id").asText();
        }
    }

    // ── When ──────────────────────────────────────────────────────────────────

    @When("I send POST {string} with body:")
    public void sendPost(String path, String body) throws Exception {
        String resolvedPath = resolvePath(path);
        lastResponse = post(resolvedPath, body, currentToken);
        tryExtractId();
    }

    @When("I send GET {string}")
    public void sendGet(String path) {
        String resolvedPath = resolvePath(path);
        lastResponse = get(resolvedPath, currentToken);
    }

    @When("I send PATCH {string} with body:")
    public void sendPatch(String path, String body) {
        String resolvedPath = resolvePath(path);
        lastResponse = patch(resolvedPath, body, currentToken);
    }

    // ── Then ──────────────────────────────────────────────────────────────────

    @Then("the response status is {int}")
    public void thenStatusIs(int expectedStatus) {
        assertThat(lastResponse.getStatusCode().value())
                .as("Expected HTTP %d but got %d. Body: %s", expectedStatus,
                        lastResponse.getStatusCode().value(), lastResponse.getBody())
                .isEqualTo(expectedStatus);
    }

    @Then("the response has field {string}")
    public void thenHasField(String fieldPath) throws Exception {
        JsonNode json = mapper.readTree(lastResponse.getBody());
        assertThat(resolveJsonPath(json, fieldPath))
                .as("Field '%s' not found in: %s", fieldPath, lastResponse.getBody())
                .isNotNull();
    }

    @Then("the response field {string} equals {string}")
    public void thenFieldEquals(String fieldPath, String expected) throws Exception {
        JsonNode json = mapper.readTree(lastResponse.getBody());
        JsonNode node = resolveJsonPath(json, fieldPath);
        assertThat(node).isNotNull();
        assertThat(node.asText())
                .as("Field '%s'", fieldPath)
                .isEqualTo(expected);
    }

    @Then("the response is a list with at least {int} items")
    public void thenListAtLeast(int minSize) throws Exception {
        JsonNode json = mapper.readTree(lastResponse.getBody());
        assertThat(json.isArray()).isTrue();
        assertThat(json.size()).isGreaterThanOrEqualTo(minSize);
    }

    @Then("the response list does not contain externalId {string}")
    public void thenListNotContains(String externalId) throws Exception {
        JsonNode json = mapper.readTree(lastResponse.getBody());
        assertThat(json.isArray()).isTrue();
        for (JsonNode item : json) {
            assertThat(item.get("externalId").asText())
                    .as("List should not contain externalId '%s'", externalId)
                    .isNotEqualTo(externalId);
        }
    }

    // ── HTTP helpers ──────────────────────────────────────────────────────────

    private ResponseEntity<String> post(String path, String body, String token) {
        HttpHeaders headers = buildHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url(path), HttpMethod.POST, entity, String.class);
    }

    private ResponseEntity<String> get(String path, String token) {
        HttpHeaders headers = buildHeaders(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url(path), HttpMethod.GET, entity, String.class);
    }

    private ResponseEntity<String> patch(String path, String body, String token) {
        HttpHeaders headers = buildHeaders(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url(path), HttpMethod.PATCH, entity, String.class);
    }

    private HttpHeaders buildHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    // ── Path/JSON helpers ─────────────────────────────────────────────────────

    private String resolvePath(String path) {
        if (lastCreatedId != null) {
            return path.replace("{lastCreatedId}", lastCreatedId);
        }
        return path;
    }

    private void tryExtractId() throws Exception {
        if (lastResponse != null && lastResponse.getBody() != null) {
            JsonNode json = mapper.readTree(lastResponse.getBody());
            if (json.has("id")) {
                lastCreatedId = json.get("id").asText();
            }
        }
    }

    /**
     * Resolves simple dot-notation and array access, e.g. "notes[0].text"
     */
    private JsonNode resolveJsonPath(JsonNode root, String path) {
        JsonNode current = root;
        for (String part : path.split("\\.")) {
            if (part.contains("[")) {
                String field = part.substring(0, part.indexOf('['));
                int index = Integer.parseInt(part.replaceAll(".*\\[(\\d+)\\].*", "$1"));
                if (!field.isEmpty()) {
                    current = current.get(field);
                }
                if (current != null) {
                    current = current.get(index);
                }
            } else {
                current = current == null ? null : current.get(part);
            }
            if (current == null) return null;
        }
        return current;
    }
}
