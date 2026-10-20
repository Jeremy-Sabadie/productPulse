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
});
