package com.product_pulse.product_pulseAPI.feedback.service;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FeedbackHistoryServiceTest {

    private FeedbackRepository repository;
    private FeedbackQueryService service;

    @BeforeEach
    void setUp() {
        repository = mock(FeedbackRepository.class);
        service = new FeedbackQueryService(repository);
    }

    @Test
    void shouldFindOnlyAuthenticatedUsersFeedbacks() {
        when(repository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        service.findMine(42L, 0, 10);

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(repository).findAll(
                any(Specification.class),
                captor.capture()
        );

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
    @SuppressWarnings("unchecked")
    void shouldFilterFeedbacksByAuthenticatedAuthorId() {
        when(repository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        service.findMine(42L, 0, 10);

        ArgumentCaptor<Specification<Feedback>> captor =
                ArgumentCaptor.forClass(Specification.class);

        verify(repository).findAll(
                captor.capture(),
                any(Pageable.class)
        );

        Root<Feedback> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        Path<Long> authorPath = mock(Path.class);
        Predicate expectedPredicate = mock(Predicate.class);

        when(root.<Long>get("authorId"))
                .thenReturn(authorPath);

        when(builder.equal(authorPath, 42L))
                .thenReturn(expectedPredicate);

        Predicate actualPredicate = captor.getValue()
                .toPredicate(root, query, builder);

        assertSame(expectedPredicate, actualPredicate);

        verify(builder).equal(authorPath, 42L);
    }
}
