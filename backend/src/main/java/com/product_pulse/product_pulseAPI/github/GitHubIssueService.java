package com.product_pulse.product_pulseAPI.github;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.repository.FeedbackRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GitHubIssueService {

    private final FeedbackRepository feedbackRepository;
    private final GitHubIssueClient githubClient;

    public GitHubIssueService(
            FeedbackRepository feedbackRepository,
            GitHubIssueClient githubClient
    ) {
        this.feedbackRepository = feedbackRepository;
        this.githubClient = githubClient;
    }

    @Transactional
    public GitHubIssueClient.IssueResult createFromFeedback(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Feedback not found"
                ));

        if (feedback.getGithubIssueNumber() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "GitHub issue already exists"
            );
        }

        String title = "[" + feedback.getCategory() + "] "
                + feedback.getTitle();

        String body = """
                ## Description

                %s

                ---
                Source : ProductPulse — Feedback #%d
                """.formatted(
                feedback.getDescription(),
                feedback.getId()
        );

        GitHubIssueClient.IssueResult issue =
                githubClient.createIssue(title, body);

        feedback.linkGithubIssue(issue.number(), issue.url());
        feedbackRepository.save(feedback);

        return issue;
    }
}
