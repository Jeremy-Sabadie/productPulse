import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { MyFeedbacks } from './my-feedbacks';
import { FeedbackService } from '../services/feedback.service';

describe('MyFeedbacks', () => {
  const page = {
    content: [{
      id: 7, title: 'Améliorer les menus',
      description: 'Ajouter un filtre saisonnier',
      applicationCode: 'MY_POPOTE', authorId: 42,
      createdAt: '2026-10-22T08:00:00Z', status: 'PENDING',
    }],
    number: 0, totalPages: 1, totalElements: 1,
    first: true, last: true,
  };

  async function render(response: unknown) {
    const service = {
      getMyFeedbacks: vi.fn().mockReturnValue(response),
    };
    await TestBed.configureTestingModule({
      imports: [MyFeedbacks],
      providers: [
        provideRouter([]),
        { provide: FeedbackService, useValue: service },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(MyFeedbacks);
    fixture.detectChanges();
    return { fixture, service };
  }

  it('affiche un avis et son statut', async () => {
    const { fixture, service } = await render(of(page));
    expect(fixture.nativeElement.textContent).toContain('Améliorer les menus');
    expect(fixture.nativeElement.textContent).toContain('En attente');
    expect(service.getMyFeedbacks).toHaveBeenCalledWith(0, 10);
  });

  it('affiche un historique vide', async () => {
    const { fixture } = await render(of({
      ...page, content: [], totalElements: 0, totalPages: 0,
    }));
    expect(fixture.nativeElement.textContent).toContain('pas encore envoyé');
  });

  it('charge la page suivante', async () => {
    const { fixture, service } = await render(of({
      ...page, totalPages: 2, last: false,
    }));
    const buttons = Array.from(
      fixture.nativeElement.querySelectorAll('button'),
    ) as HTMLButtonElement[];
    buttons.find(b => b.textContent?.includes('Suivant'))!.click();
    expect(service.getMyFeedbacks).toHaveBeenCalledWith(1, 10);
  });

  it('affiche une erreur réseau', async () => {
    const { fixture } = await render(
      throwError(() => new Error('API indisponible')),
    );
    expect(fixture.nativeElement.querySelector('[role="alert"]')?.textContent)
      .toContain('Impossible de charger votre historique');
  });
});
