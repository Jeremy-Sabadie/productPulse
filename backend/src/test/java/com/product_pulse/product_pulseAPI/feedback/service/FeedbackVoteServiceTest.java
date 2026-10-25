package com.product_pulse.product_pulseAPI.feedback.service;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.repository.FeedbackRepository;
import com.product_pulse.product_pulseAPI.feedback.repository.FeedbackVoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeedbackVoteServiceTest {

    private FeedbackRepository feedbackRepository;
    private FeedbackVoteRepository voteRepository;
    private FeedbackVoteService service;

    @BeforeEach
    void setUp() {
        feedbackRepository = mock(FeedbackRepository.class);
        voteRepository = mock(FeedbackVoteRepository.class);
        service = new FeedbackVoteService(feedbackRepository, voteRepository);
    }

    private Feedback approvedFeedback() {
        Feedback feedback = new Feedback(
                "Amélioration",
                "Ajouter un filtre",
                "MY_POPOTE",
                42L,
                Instant.parse("2026-10-15T12:00:00Z")
        );
        feedback.approve();
        return feedback;
    }

    @Test
    void shouldSupportApprovedFeedback() {
        when(feedbackRepository.findById(1L))
                .thenReturn(Optional.of(approvedFeedback()));
        when(voteRepository.existsByFeedbackIdAndUserId(1L, 42L))
                .thenReturn(false);

        service.support(1L, 42L);

        verify(voteRepository).save(any());
    }

    @Test
    void shouldRejectDuplicateVote() {
        when(feedbackRepository.findById(1L))
                .thenReturn(Optional.of(approvedFeedback()));
        when(voteRepository.existsByFeedbackIdAndUserId(1L, 42L))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.support(1L, 42L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(voteRepository, never()).save(any());
    }

    @Test
    void shouldSupportPendingFeedback() {
        Feedback pending = new Feedback(
                "Idée",
                "Description",
                "MY_POPOTE",
                42L,
                Instant.now()
        );

        when(feedbackRepository.findById(1L))
                .thenReturn(Optional.of(pending));
        when(voteRepository.existsByFeedbackIdAndUserId(1L, 42L))
                .thenReturn(false);

        service.support(1L, 42L);

        verify(voteRepository).save(any());
    }

    @Test
    void shouldRejectVoteOnRejectedFeedback() {
        Feedback rejected = new Feedback(
                "Idée", "Description", "MY_POPOTE", 42L, Instant.now()
        );
        rejected.reject();

        when(feedbackRepository.findById(1L))
                .thenReturn(Optional.of(rejected));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.support(1L, 42L)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verifyNoInteractions(voteRepository);
    }

    @Test
    void shouldRemoveOwnVote() {
        when(voteRepository.existsByFeedbackIdAndUserId(1L, 42L))
                .thenReturn(true);

        service.removeSupport(1L, 42L);

        verify(voteRepository).deleteByFeedbackIdAndUserId(1L, 42L);
    }

    @Test
    void shouldNotRemoveAnotherUsersVote() {
        when(voteRepository.existsByFeedbackIdAndUserId(1L, 99L))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.removeSupport(1L, 99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(voteRepository, never())
                .deleteByFeedbackIdAndUserId(anyLong(), anyLong());
    }
}
