package com.product_pulse.product_pulseAPI.feedback.controller;

import com.product_pulse.product_pulseAPI.feedback.dto.FeedbackResponse;
import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackCategory;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import com.product_pulse.product_pulseAPI.feedback.repository.FeedbackVoteRepository;
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

/** Vérifie le contrat de consultation admin, dont les filtres US08/US14. */
class FeedbackAdminQueryControllerTest {
    private FeedbackQueryService queryService;
    private FeedbackAdminQueryController controller;

    @BeforeEach
    void setUp() {
        queryService = mock(FeedbackQueryService.class);
        controller = new FeedbackAdminQueryController(queryService, mock(FeedbackVoteRepository.class));
    }

    @Test
    void shouldReturnAllFeedbacks() {
        Feedback feedback = feedback();
        when(queryService.findAll(null, null, null, "recent", 0, 10))
                .thenReturn(new PageImpl<>(List.of(feedback)));
        ResponseEntity<Page<FeedbackResponse>> response = controller.findAll(null, 0, 10);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Page<FeedbackResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.getTotalElements());
        FeedbackResponse item = body.getContent().getFirst();
        assertAll(
                () -> assertEquals(feedback.getId(), item.id()),
                () -> assertEquals("Suggestion", item.title()),
                () -> assertEquals("Amélioration de My Popote", item.description()),
                () -> assertEquals("MY_POPOTE", item.applicationCode()),
                () -> assertEquals(42L, item.authorId()),
                () -> assertEquals(feedback.getCreatedAt(), item.createdAt()),
                () -> assertEquals(FeedbackStatus.PENDING, item.status()),
                () -> assertEquals(FeedbackCategory.IMPROVEMENT, item.category())
        );
        verify(queryService).findAll(null, null, null, "recent", 0, 10);
        verifyNoMoreInteractions(queryService);
    }

    @Test
    void shouldReturnFilteredFeedbacks() {
        when(queryService.findAll(FeedbackStatus.PENDING, null, null, "recent", 1, 20))
                .thenReturn(Page.empty());
        ResponseEntity<Page<FeedbackResponse>> response =
                controller.findAll(FeedbackStatus.PENDING, 1, 20);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(queryService).findAll(FeedbackStatus.PENDING, null, null, "recent", 1, 20);
        verifyNoMoreInteractions(queryService);
    }

    @Test
    void shouldPassCombinedFiltersAndVoteSort() {
        when(queryService.findAll(FeedbackStatus.APPROVED, "MY_POPOTE", FeedbackCategory.BUG,
                "votes_desc", 2, 5)).thenReturn(Page.empty());
        ResponseEntity<Page<FeedbackResponse>> response = controller.findAll(
                FeedbackStatus.APPROVED, "MY_POPOTE", FeedbackCategory.BUG, "votes_desc", 2, 5);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(queryService).findAll(FeedbackStatus.APPROVED, "MY_POPOTE", FeedbackCategory.BUG,
                "votes_desc", 2, 5);
    }

    private Feedback feedback() {
        return new Feedback("Suggestion", "Amélioration de My Popote", "MY_POPOTE",
                42L, Instant.parse("2026-10-15T12:00:00Z"));
    }
}
