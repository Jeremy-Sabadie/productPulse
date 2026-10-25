import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { FeedbackService } from './feedback.service';

describe('FeedbackService', () => {
  let service: FeedbackService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(FeedbackService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should submit a feedback to the API', () => {
    const feedback = {
      title: 'Problème de liste de courses',
      description: 'Un ingrédient apparaît deux fois.',
      category: 'BUG' as const,
    };

    let createdId: number | undefined;

    service.createFeedback(feedback).subscribe((response) => {
      createdId = response.id;
    });

    const request = httpMock.expectOne('/api/feedback');

    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(feedback);

    request.flush(
      { id: 42, ...feedback, status: 'PENDING' },
      { status: 201, statusText: 'Created' },
    );

    expect(createdId).toBe(42);
  });

  it('should fetch approved feedbacks with pagination', () => {
    const page = {
      content: [
        {
          id: 12,
          title: 'Améliorer les recettes',
          description: 'Ajouter un filtre par saison.',
          applicationCode: 'MY_POPOTE',
          createdAt: '2026-10-21T09:00:00Z',
        },
      ],
      number: 0,
      size: 10,
      totalElements: 1,
      totalPages: 1,
      first: true,
      last: true,
      empty: false,
    };

    service.getApprovedFeedbacks(0, 10).subscribe((response) => {
      expect(response).toEqual(page);
      expect(response.content[0]).not.toHaveProperty('authorId');
      expect(response.content[0]).not.toHaveProperty('status');
    });

    const request = httpMock.expectOne(
      '/api/feedback?page=0&size=10',
    );

    expect(request.request.method).toBe('GET');
    request.flush(page);
  });

});
