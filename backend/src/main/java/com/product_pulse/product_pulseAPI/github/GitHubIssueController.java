package com.product_pulse.product_pulseAPI.github;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * API REST permettant aux administrateurs de transformer
 * un feedback ProductPulse en GitHub Issue.
 *
 * <p>Les règles d'autorisation sont appliquées
 * par la configuration Spring Security.</p>
 */
@RestController
@RequestMapping("/api/admin/feedback")
public class GitHubIssueController {

    private final GitHubIssueService issueService;

    /**
     * Initialise le contrôleur.
     *
     * @param issueService service de création des tickets
     */
    public GitHubIssueController(GitHubIssueService issueService) {
        this.issueService = Objects.requireNonNull(
                issueService, "issueService"
        );
    }

    /**
     * Crée un ticket GitHub pour un feedback existant.
     *
     * @param feedbackId identifiant du feedback
     * @return numéro et URL du ticket créé
     */
    @PostMapping("/{feedbackId}/github-issue")
    public ResponseEntity<GitHubIssueClient.IssueResult> createIssue(
            @PathVariable Long feedbackId
    ) {
        GitHubIssueClient.IssueResult result =
                issueService.createFromFeedback(feedbackId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(result);
    }
}
