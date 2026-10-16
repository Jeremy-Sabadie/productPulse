
import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'admin/feedback',
  },
  {
    path: 'admin/feedback',
    loadComponent: () =>
      import('./features/admin/admin-dashboard/admin-dashboard')
        .then((module) => module.AdminDashboard),
  },
  {
    path: '**',
    redirectTo: 'admin/feedback',
  },
];
