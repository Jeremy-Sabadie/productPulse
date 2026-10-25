package com.product_pulse.product_pulseAPI.feedback.controller;

import com.product_pulse.product_pulseAPI.feedback.dto.PublicFeedbackResponse;
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

class FeedbackPublicQueryControllerTest {

    private FeedbackQueryService queryService;
    private FeedbackPublicQueryController controller;

    @BeforeEach
    void setUp() {
        queryService = mock(FeedbackQueryService.class);
        controller = new FeedbackPublicQueryController(queryService);
    }

    @Test
    void shouldReturnOnlyApprovedFeedbacks() {
        Feedback feedback = new Feedback(
                "Suggestion",
                "Amélioration de My Popote",
                "MY_POPOTE",
                42L,
                Instant.parse("2026-10-15T12:00:00Z")
        );

        feedback.approve();

        Page<Feedback> approvedFeedbacks =
                new PageImpl<>(List.of(feedback));

        when(queryService.findCommunity(0, 10))
                .thenReturn(approvedFeedbacks);

        ResponseEntity<Page<PublicFeedbackResponse>> response =
                controller.findAll(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("Suggestion", response.getBody().getContent().get(0).title());
        assertFalse(java.util.Arrays.stream(
                PublicFeedbackResponse.class.getRecordComponents()
        ).anyMatch(component -> component.getName().equals("authorId")));

        verify(queryService).findCommunity(
                0,
                10
        );

        verifyNoMoreInteractions(queryService);
    }

    @Test
    void shouldReturnEmptyPageWhenNoFeedbackIsApproved() {
        when(queryService.findCommunity(0, 10))
                .thenReturn(Page.empty());

        ResponseEntity<Page<PublicFeedbackResponse>> response =
                controller.findAll(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(queryService).findCommunity(
                0,
                10
        );
    }
}
