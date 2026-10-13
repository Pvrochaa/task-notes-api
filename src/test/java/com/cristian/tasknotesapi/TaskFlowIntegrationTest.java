package com.cristian.tasknotesapi;

import com.cristian.tasknotesapi.web.dto.AuthResponse;
import com.cristian.tasknotesapi.web.dto.RegisterRequest;
import com.cristian.tasknotesapi.web.dto.TaskRequest;
import com.cristian.tasknotesapi.web.dto.TaskResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskFlowIntegrationTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void registerLoginAndCreateTask_endToEnd() {
        String baseUrl = "http://localhost:" + port;
        RegisterRequest register = new RegisterRequest("integration-user", "integration@example.com", "s3cret-password");

        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/register", register, AuthResponse.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String token = registerResponse.getBody().token();
        assertThat(token).isNotBlank();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        TaskRequest taskRequest = new TaskRequest("Ship the portfolio", "Push to GitHub", null, null);

        ResponseEntity<TaskResponse> createResponse = restTemplate.postForEntity(
                baseUrl + "/api/tasks", new HttpEntity<>(taskRequest, headers), TaskResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody().title()).isEqualTo("Ship the portfolio");

        ResponseEntity<String> unauthorized = restTemplate.getForEntity(baseUrl + "/api/tasks", String.class);
        assertThat(unauthorized.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
