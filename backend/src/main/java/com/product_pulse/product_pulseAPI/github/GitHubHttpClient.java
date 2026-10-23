
package com.product_pulse.product_pulseAPI.github;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Client HTTP permettant de créer des GitHub Issues
 * dans le dépôt Jeremy-Sabadie/my_popote.
 *
 * <p>Le jeton GitHub est lu depuis la configuration serveur.
 * Il ne doit jamais être exposé au frontend ou enregistré
 * dans le dépôt Git.</p>
 */
@Component
public class GitHubHttpClient implements GitHubIssueClient {

    private static final String ISSUES_URL =
            "https://api.github.com/repos/Jeremy-Sabadie/my_popote/issues";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String token;

    /**
     * Initialise le client HTTP GitHub.
     *
     * @param objectMapper convertisseur JSON
     * @param token jeton d'authentification GitHub
     */
    public GitHubHttpClient(
            ObjectMapper objectMapper,
            @Value("${GITHUB_TOKEN:}") String token
    ) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.objectMapper = Objects.requireNonNull(
                objectMapper, "objectMapper"
        );
        this.token = token;
    }

    /**
     * Crée une Issue dans le dépôt My Popote.
     *
     * @param title titre du ticket
     * @param body description du ticket
     * @return numéro et URL de l'Issue créée
     * @throws IllegalStateException si la configuration,
     *         la communication ou la réponse GitHub est invalide
     */
    @Override
    public IssueResult createIssue(String title, String body) {

        if (token == null || token.isBlank()) {
            throw new IllegalStateException(
                    "GITHUB_TOKEN is not configured"
            );
        }

        try {
            // Préparation du corps JSON envoyé à GitHub.
            String json = objectMapper.writeValueAsString(
                    Map.of(
                            "title", title,
                            "body", body
                    )
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ISSUES_URL))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            json, StandardCharsets.UTF_8
                    ))
                    .build();

            // Appel de l'API GitHub.
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 201) {
                throw new IllegalStateException(
                        "GitHub API returned HTTP "
                                + response.statusCode()
                );
            }

            // Extraction du numéro et de l'URL du ticket.
            JsonNode result = objectMapper.readTree(
                    response.body()
            );

            long number = result.path("number").asLong();
            String url = result.path("html_url").asText();

            if (number <= 0 || url.isBlank()) {
                throw new IllegalStateException(
                        "Invalid GitHub API response"
                );
            }

            return new IssueResult(number, url);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "GitHub request interrupted",
                    exception
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "GitHub communication failed",
                    exception
            );
        }
    }
}
