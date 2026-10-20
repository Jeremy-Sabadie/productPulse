import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

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
    path: 'feedback/new',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/feedback/feedback-form/feedback-form')
        .then((module) => module.FeedbackForm),
  },
  {
    path: 'admin/feedback',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/admin/admin-dashboard/admin-dashboard')
        .then((module) => module.AdminDashboard),
  },
  {
    path: '**',
    redirectTo: 'admin/feedback',
  },
];
