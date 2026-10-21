package com.product_pulse.product_pulseAPI.feedback.controller;

import com.product_pulse.product_pulseAPI.feedback.service.FeedbackVoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests du contrôleur des votes et de l'identité JWT.
 */
class FeedbackVoteControllerTest {

    private FeedbackVoteService service;
    private FeedbackVoteController controller;
    private Jwt jwt;

    @BeforeEach
    void setUp() {
        service = mock(FeedbackVoteService.class);
        controller = new FeedbackVoteController(service);
        jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn("42");
    }

    @Test
    void shouldSupportWithAuthenticatedUserId() {
        ResponseEntity<Void> response = controller.support(1L, jwt);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service).support(1L, 42L);
    }

    @Test
    void shouldRemoveOwnSupport() {
        ResponseEntity<Void> response = controller.removeSupport(1L, jwt);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service).removeSupport(1L, 42L);
    }

    @Test
    void shouldRejectMissingAuthentication() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.support(1L, null)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verifyNoInteractions(service);
    }

    @Test
    void shouldRejectInvalidJwtSubject() {
        when(jwt.getSubject()).thenReturn("invalid");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.support(1L, jwt)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verifyNoInteractions(service);
    }
}
