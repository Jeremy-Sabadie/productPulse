
package com.product_pulse.product_pulseAPI.feedback.service;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import com.product_pulse.product_pulseAPI.feedback.repository.FeedbackRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires des décisions de modération.
 */
class FeedbackModerationServiceTest {

    private FeedbackRepository repository;
    private FeedbackModerationService service;

    @BeforeEach
    void setUp() {
        repository = mock(FeedbackRepository.class);
        service = new FeedbackModerationService(repository);
    }

    private Feedback newFeedback() {
        return new Feedback(
                "Amélioration",
                "Ajouter un filtre",
                "MY_POPOTE",
                42L,
                Instant.parse("2026-10-15T12:00:00Z")
        );
    }

    @Test
    void shouldApprovePendingFeedback() {
        Feedback feedback = newFeedback();
        when(repository.findById(1L))
                .thenReturn(Optional.of(feedback));

        Feedback result = service.approve(1L);

        assertEquals(FeedbackStatus.APPROVED, result.getStatus());
        verify(repository).findById(1L);
    }

    @Test
    void shouldRejectPendingFeedback() {
        Feedback feedback = newFeedback();
        when(repository.findById(1L))
                .thenReturn(Optional.of(feedback));

        Feedback result = service.reject(1L);

        assertEquals(FeedbackStatus.REJECTED, result.getStatus());
    }

    @Test
    void shouldReturnNotFoundForUnknownFeedback() {
        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.approve(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldRejectInvalidIdentifiers() {
        for (Long id : new Long[]{null, 0L, -1L}) {
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> service.approve(id)
            );

            assertEquals(
                    HttpStatus.BAD_REQUEST,
                    exception.getStatusCode()
            );
        }

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectAlreadyApprovedFeedback() {
        Feedback feedback = newFeedback();
        feedback.approve();

        when(repository.findById(1L))
                .thenReturn(Optional.of(feedback));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.reject(1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void shouldRejectAlreadyRejectedFeedback() {
        Feedback feedback = newFeedback();
        feedback.reject();

        when(repository.findById(1L))
                .thenReturn(Optional.of(feedback));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.approve(1L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
}
