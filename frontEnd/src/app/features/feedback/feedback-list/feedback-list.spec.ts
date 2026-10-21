import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { FeedbackList } from './feedback-list';
import { FeedbackService } from '../services/feedback.service';

describe('FeedbackList', () => {
  it('affiche un avis approuvé', async () => {
    const service = {
      getMyVotes: vi.fn().mockReturnValue(of([])),
      getApprovedFeedbacks: vi.fn().mockReturnValue(of({
        content: [{
          id: 12,
          title: 'Améliorer les recettes',
          description: 'Ajouter un filtre par saison.',
          applicationCode: 'MY_POPOTE',
          createdAt: '2026-10-21T09:00:00Z',
        }],
        number: 0, size: 10, totalElements: 1,
        totalPages: 1, first: true, last: true, empty: false,
      })),
    };

    await TestBed.configureTestingModule({
      imports: [FeedbackList],
      providers: [{ provide: FeedbackService, useValue: service }],
    }).compileComponents();

    const fixture = TestBed.createComponent(FeedbackList);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent)
      .toContain('Améliorer les recettes');
    expect(service.getApprovedFeedbacks).toHaveBeenCalledWith(0, 10);
  });

  it('charge la page suivante', async () => {
    const service = {
      getMyVotes: vi.fn().mockReturnValue(of([])),
      getApprovedFeedbacks: vi.fn().mockReturnValue(of({
        content: [],
        number: 0,
        size: 10,
        totalElements: 15,
        totalPages: 2,
        first: true,
        last: false,
        empty: false,
      })),
    };

    await TestBed.configureTestingModule({
      imports: [FeedbackList],
      providers: [{ provide: FeedbackService, useValue: service }],
    }).compileComponents();

    const fixture = TestBed.createComponent(FeedbackList);
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector(
      '[data-testid="next-page"]',
    ) as HTMLButtonElement;

    button.click();

    expect(service.getApprovedFeedbacks).toHaveBeenLastCalledWith(1, 10);
  });


  it('affiche une erreur si le chargement échoue', async () => {
    const service = {
      getMyVotes: vi.fn().mockReturnValue(of([])),
      getApprovedFeedbacks: vi.fn().mockReturnValue(
        throwError(() => new Error('API indisponible')),
      ),
    };

    await TestBed.configureTestingModule({
      imports: [FeedbackList],
      providers: [{ provide: FeedbackService, useValue: service }],
    }).compileComponents();

    const fixture = TestBed.createComponent(FeedbackList);
    fixture.detectChanges();

    const alert = fixture.nativeElement.querySelector('[role="alert"]');

    expect(alert?.textContent).toContain(
      'Impossible de charger les avis',
    );
  });

});
