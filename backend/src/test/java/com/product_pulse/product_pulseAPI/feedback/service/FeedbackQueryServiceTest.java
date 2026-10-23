package com.product_pulse.product_pulseAPI.feedback.service;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackCategory;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import com.product_pulse.product_pulseAPI.feedback.repository.FeedbackRepository;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Tests ciblés des filtres, du tri et de la pagination US08/US14. */
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
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(feedback)));

        Page<Feedback> result = service.findAll(null, 0, 10);
        assertEquals(1, result.getTotalElements());
        assertSame(feedback, result.getContent().getFirst());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), captor.capture());
        Pageable pageable = captor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("createdAt").getDirection());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("id").getDirection());
    }

    @Test
    void shouldFilterFeedbacksByStatus() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());
        assertTrue(service.findAll(FeedbackStatus.PENDING, 0, 10).isEmpty());
        verify(repository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldBuildSpecificationForRequestedStatusAndCategory() {
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());
        service.findAll(FeedbackStatus.APPROVED, "MY_POPOTE", FeedbackCategory.BUG, "recent", 1, 20);

        ArgumentCaptor<Specification<Feedback>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(repository).findAll(captor.capture(), any(Pageable.class));
        Root<Feedback> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        Path<FeedbackStatus> statusPath = mock(Path.class);
        Path<FeedbackCategory> categoryPath = mock(Path.class);
        Path<String> applicationPath = mock(Path.class);
        Predicate statusPredicate = mock(Predicate.class);
        Predicate categoryPredicate = mock(Predicate.class);
        Predicate applicationPredicate = mock(Predicate.class);
        Predicate conjunction = mock(Predicate.class);

        when(root.<FeedbackStatus>get("status")).thenReturn(statusPath);
        when(root.<FeedbackCategory>get("category")).thenReturn(categoryPath);
        when(root.<String>get("applicationCode")).thenReturn(applicationPath);
        when(builder.equal(statusPath, FeedbackStatus.APPROVED)).thenReturn(statusPredicate);
        when(builder.equal(categoryPath, FeedbackCategory.BUG)).thenReturn(categoryPredicate);
        when(builder.equal(applicationPath, "MY_POPOTE")).thenReturn(applicationPredicate);
        when(builder.and(any(Predicate[].class))).thenReturn(conjunction);

        assertSame(conjunction, captor.getValue().toPredicate(root, query, builder));
        verify(builder).and(
                statusPredicate,
                categoryPredicate,
                applicationPredicate
        );
    }

    @Test
    void shouldDelegateDescendingVotesWithCombinedFilters() {
        Pageable pageable = PageRequest.of(2, 5);
        when(repository.findSortedByVotesDesc(eq(FeedbackStatus.APPROVED), eq("MY_POPOTE"),
                eq(FeedbackCategory.FEATURE), any(Pageable.class))).thenReturn(Page.empty(pageable));
        service.findAll(FeedbackStatus.APPROVED, " MY_POPOTE ", FeedbackCategory.FEATURE,
                "votes_desc", 2, 5);
        verify(repository).findSortedByVotesDesc(eq(FeedbackStatus.APPROVED), eq("MY_POPOTE"),
                eq(FeedbackCategory.FEATURE), argThat(p -> p.getPageNumber() == 2 && p.getPageSize() == 5));
    }

    @Test
    void shouldDelegateAscendingVotes() {
        when(repository.findSortedByVotesAsc(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(Page.empty());
        service.findAll(null, null, null, "votes_asc", 0, 10);
        verify(repository).findSortedByVotesAsc(isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void shouldRejectInvalidSort() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.findAll(null, null, null, "unknown", 0, 10));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectNegativePage() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service.findAll(null, -1, 10)).getStatusCode());
        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectZeroPageSize() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service.findAll(null, 0, 0)).getStatusCode());
        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectExcessivePageSize() {
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service.findAll(null, 0, 101)).getStatusCode());
        verifyNoInteractions(repository);
    }

    private Feedback feedback() {
        return new Feedback("Suggestion", "Amélioration de My Popote", "MY_POPOTE",
                42L, Instant.parse("2026-10-15T12:00:00Z"));
    }
}
