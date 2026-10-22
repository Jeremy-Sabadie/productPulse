package com.product_pulse.product_pulseAPI.feedback.controller;

import com.product_pulse.product_pulseAPI.feedback.dto.FeedbackResponse;
import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import com.product_pulse.product_pulseAPI.feedback.service.FeedbackQueryService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeedbackHistoryControllerTest {

    private FeedbackQueryService queryService;
    private FeedbackHistoryController controller;

    @BeforeEach
    void setUp() {
        queryService = mock(FeedbackQueryService.class);
        controller = new FeedbackHistoryController(queryService);
    }

    @Test
    void shouldReturnOnlyAuthenticatedUsersHistory() {
        Jwt jwt = jwtForUser("42");

        Feedback pending = feedback("En attente", 42L);
        Feedback approved = feedback("Approuvé", 42L);
        approved.approve();

        Feedback rejected = feedback("Refusé", 42L);
        rejected.reject();

        Page<Feedback> history = new PageImpl<>(
                List.of(pending, approved, rejected)
        );

        when(queryService.findMine(42L, 0, 10))
                .thenReturn(history);

        ResponseEntity<Page<FeedbackResponse>> response =
                controller.findMine(0, 10, jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getTotalElements());

        List<FeedbackStatus> statuses = response.getBody()
                .getContent()
                .stream()
                .map(FeedbackResponse::status)
                .toList();

        assertEquals(
                List.of(
                        FeedbackStatus.PENDING,
                        FeedbackStatus.APPROVED,
                        FeedbackStatus.REJECTED
                ),
                statuses
        );

        verify(queryService).findMine(42L, 0, 10);
        verifyNoMoreInteractions(queryService);
    }

    @Test
    void shouldUsePaginationParameters() {
        Jwt jwt = jwtForUser("73");

        when(queryService.findMine(73L, 2, 5))
                .thenReturn(Page.empty());

        ResponseEntity<Page<FeedbackResponse>> response =
                controller.findMine(2, 5, jwt);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(queryService).findMine(73L, 2, 5);
    }

    @Test
    void shouldRejectInvalidJwtSubject() {
        Jwt jwt = jwtForUser("invalid");

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.findMine(0, 10, jwt)
        );

        verifyNoInteractions(queryService);
    }

    private Jwt jwtForUser(String subject) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject(subject)
                .build();
    }

    private Feedback feedback(String title, Long authorId) {
        return new Feedback(
                title,
                "Description du retour",
                "MY_POPOTE",
                authorId,
                Instant.parse("2026-10-22T08:00:00Z")
        );
    }
}
