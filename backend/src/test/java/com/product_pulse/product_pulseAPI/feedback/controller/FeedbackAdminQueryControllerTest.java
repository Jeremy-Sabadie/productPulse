
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

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests du contrat REST de consultation administrateur.
 *
 * Vérifie la conversion des entités en DTO,
 * le filtrage et la pagination.
 */
class FeedbackAdminQueryControllerTest {

    private FeedbackQueryService queryService;
    private FeedbackAdminQueryController controller;

    @BeforeEach
    void setUp() {
        queryService = mock(FeedbackQueryService.class);
        controller = new FeedbackAdminQueryController(
                queryService,
                org.mockito.Mockito.mock(
                    com.product_pulse.product_pulseAPI.feedback.repository.FeedbackVoteRepository.class
                )
        );
    }

    @Test
    void shouldReturnAllFeedbacks() {
        Feedback feedback = feedback();

        Page<Feedback> expected =
                new PageImpl<>(List.of(feedback));

        when(queryService.findAll(null, 0, 10))
                .thenReturn(expected);

        ResponseEntity<Page<FeedbackResponse>> response =
                controller.findAll(null, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Page<FeedbackResponse> body = response.getBody();
        assertNotNull(body);

        assertEquals(1, body.getTotalElements());
        assertEquals(1, body.getContent().size());

        FeedbackResponse item = body.getContent().getFirst();

        assertAll(
                () -> assertEquals(feedback.getId(), item.id()),
                () -> assertEquals("Suggestion", item.title()),
                () -> assertEquals(
                        "Amélioration de My Popote",
                        item.description()
                ),
                () -> assertEquals(
                        "MY_POPOTE",
                        item.applicationCode()
                ),
                () -> assertEquals(42L, item.authorId()),
                () -> assertEquals(
                        feedback.getCreatedAt(),
                        item.createdAt()
                ),
                () -> assertEquals(
                        FeedbackStatus.PENDING,
                        item.status()
                )
        );

        verify(queryService).findAll(null, 0, 10);
        verifyNoMoreInteractions(queryService);
    }

    @Test
    void shouldReturnFilteredFeedbacks() {
        Page<Feedback> expected = Page.empty();

        when(queryService.findAll(FeedbackStatus.PENDING, 1, 20))
                .thenReturn(expected);

        ResponseEntity<Page<FeedbackResponse>> response =
                controller.findAll(FeedbackStatus.PENDING, 1, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Page<FeedbackResponse> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isEmpty());
        assertEquals(0, body.getTotalElements());

        verify(queryService).findAll(
                FeedbackStatus.PENDING,
                1,
                20
        );

        verifyNoMoreInteractions(queryService);
    }

    private Feedback feedback() {
        return new Feedback(
                "Suggestion",
                "Amélioration de My Popote",
                "MY_POPOTE",
                42L,
                Instant.parse("2026-10-15T12:00:00Z")
        );
    }
}
