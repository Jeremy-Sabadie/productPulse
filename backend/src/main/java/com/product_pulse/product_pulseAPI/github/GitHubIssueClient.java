package com.product_pulse.product_pulseAPI.github;

public interface GitHubIssueClient {

    IssueResult createIssue(String title, String body);

    record IssueResult(Long number, String url) {
    }
}
