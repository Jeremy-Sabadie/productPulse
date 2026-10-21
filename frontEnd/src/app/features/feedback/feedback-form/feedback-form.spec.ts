import { TestBed } from '@angular/core/testing';
import { FormsModule, NgForm } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { of } from 'rxjs';

import { FeedbackForm } from './feedback-form';
import { FeedbackService } from '../services/feedback.service';
import { NotificationService } from '../../../core/services/notification.service';

describe('FeedbackForm', () => {
  const feedbackServiceMock = {
    createFeedback: vi.fn(),
  };

  beforeEach(async () => {
    feedbackServiceMock.createFeedback.mockReset();

    await TestBed.configureTestingModule({
      imports: [FeedbackForm],
      providers: [
        { provide: NotificationService, useValue: {
          feedbackSubmitted: vi.fn().mockResolvedValue(undefined),
          confirm: vi.fn().mockResolvedValue(true),
          error: vi.fn().mockResolvedValue(undefined)
        } },
        {
          provide: FeedbackService,
          useValue: feedbackServiceMock,
        },
      ],
    }).compileComponents();
  });

  it('should display the feedback form fields', () => {
    const fixture = TestBed.createComponent(FeedbackForm);
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;

    expect(element.querySelector('input[name="title"]')).not.toBeNull();
    expect(element.querySelector('textarea[name="description"]')).not.toBeNull();
    expect(element.querySelector('button[type="submit"]')).not.toBeNull();
  });

  it('should submit feedback and display a confirmation', async () => {
    feedbackServiceMock.createFeedback.mockReturnValue(
      of({
        id: 42,
        title: 'Problème de courses',
        description: 'Un ingrédient apparaît deux fois.',
        status: 'PENDING',
      }),
    );

    const fixture = TestBed.createComponent(FeedbackForm);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component.title = 'Problème de courses';
    component.description = 'Un ingrédient apparaît deux fois.';

    fixture.detectChanges();
    await fixture.whenStable();

    await component.submit();
    fixture.detectChanges();

    expect(feedbackServiceMock.createFeedback).toHaveBeenCalledWith({
      title: 'Problème de courses',
      description: 'Un ingrédient apparaît deux fois.',
    });

    expect(fixture.nativeElement.textContent).toContain('Avis envoyé ! Merci pour votre retour.');
  });
});
