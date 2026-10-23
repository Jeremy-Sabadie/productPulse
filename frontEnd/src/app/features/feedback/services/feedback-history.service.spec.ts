import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { FeedbackService } from './feedback.service';

describe('FeedbackService - historique utilisateur', () => {
  let service: FeedbackService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        FeedbackService,
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

  it('récupère uniquement les avis de l’utilisateur connecté', () => {
    const response = {
      content: [
        {
          id: 1,
          title: 'Ajouter un calendrier',
          description: 'Améliorer la planification',
          applicationCode: 'MY_POPOTE',
          authorId: 42,
          createdAt: '2026-10-22T08:00:00Z',
          status: 'PENDING',
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

    service.getMyFeedbacks(0, 10).subscribe(result => {
      expect(result.content.length).toBe(1);
      expect(result.content[0].status).toBe('PENDING');
      expect(result.content[0].authorId).toBe(42);
    });

    const request = httpMock.expectOne(
      '/api/feedback/mine?page=0&size=10',
    );

    expect(request.request.method).toBe('GET');
    request.flush(response);
  });

  it('transmet correctement les paramètres de pagination', () => {
    service.getMyFeedbacks(2, 5).subscribe();

    const request = httpMock.expectOne(
      '/api/feedback/mine?page=2&size=5',
    );

    expect(request.request.method).toBe('GET');

    request.flush({
      content: [],
      number: 2,
      size: 5,
      totalElements: 0,
      totalPages: 0,
      first: false,
      last: true,
      empty: true,
    });
  });

  it('refuse une pagination invalide', () => {
    expect(() => service.getMyFeedbacks(-1, 10))
      .toThrowError(RangeError);

    expect(() => service.getMyFeedbacks(0, 101))
      .toThrowError(RangeError);
  });
});
