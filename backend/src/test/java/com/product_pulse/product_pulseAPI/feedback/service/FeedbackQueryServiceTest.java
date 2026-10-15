
package com.product_pulse.product_pulseAPI.feedback.service;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import com.product_pulse.product_pulseAPI.feedback.repository.FeedbackRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FeedbackQueryServiceTest {

    private FeedbackRepository repository;
    private FeedbackQueryService service;

    @BeforeEach
    void setUp() {
        repository = mock(FeedbackRepository.class);
        service = new FeedbackQueryService(repository);
    }

    @Test
    void shouldListFeedbacksWithPaginationAndDescendingDate() {
        Feedback feedback = feedback();

        when(repository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(feedback)));

        Page<Feedback> result = service.findAll(null, 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals(feedback, result.getContent().getFirst());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(repository).findAll(captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        assertEquals(
                Sort.Direction.DESC,
                pageable.getSort()
                        .getOrderFor("createdAt")
                        .getDirection()
        );

        assertEquals(
                Sort.Direction.DESC,
                pageable.getSort()
                        .getOrderFor("id")
                        .getDirection()
        );
    }

    @Test
    void shouldFilterFeedbacksByStatus() {
        when(repository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        Page<Feedback> result =
                service.findAll(FeedbackStatus.PENDING, 0, 10);

        assertTrue(result.isEmpty());

        verify(repository).findAll(
                any(Specification.class),
                any(Pageable.class)
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldBuildSpecificationForRequestedStatus() {
        when(repository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        service.findAll(FeedbackStatus.APPROVED, 1, 20);

        ArgumentCaptor<Specification<Feedback>> captor =
                ArgumentCaptor.forClass(Specification.class);

        verify(repository).findAll(
                captor.capture(),
                any(Pageable.class)
        );

        Root<Feedback> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        Path<FeedbackStatus> statusPath = mock(Path.class);
        Predicate expectedPredicate = mock(Predicate.class);

        when(root.<FeedbackStatus>get("status"))
                .thenReturn(statusPath);

        when(builder.equal(statusPath, FeedbackStatus.APPROVED))
                .thenReturn(expectedPredicate);

        Predicate actualPredicate = captor.getValue()
                .toPredicate(root, query, builder);

        assertSame(expectedPredicate, actualPredicate);

        verify(builder).equal(
                statusPath,
                FeedbackStatus.APPROVED
        );
    }

    @Test
    void shouldRejectNegativePage() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.findAll(null, -1, 10)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectZeroPageSize() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.findAll(null, 0, 0)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectExcessivePageSize() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.findAll(null, 0, 101)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(repository);
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
