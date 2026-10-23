import { Routes } from '@angular/router';
import { authGuard, adminGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'admin/feedback',
  },
  {
    path: 'auth',
    loadComponent: () =>
      import('./features/auth/auth-page')
        .then((module) => module.AuthPage),
  },
  {
    path: 'feedback',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/feedback/feedback-list/feedback-list')
        .then((module) => module.FeedbackList),
  },
  {
    path: 'feedback/mine',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/feedback/my-feedbacks/my-feedbacks')
        .then((module) => module.MyFeedbacks),
  },
  {
    path: 'feedback/new',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/feedback/feedback-form/feedback-form')
        .then((module) => module.FeedbackForm),
  },
  {
    path: 'admin/feedback',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./features/admin/admin-dashboard/admin-dashboard')
        .then((module) => module.AdminDashboard),
  },
  {
    path: '**',
    redirectTo: 'admin/feedback',
  },
];
