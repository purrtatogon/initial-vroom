import { Routes } from '@angular/router';

// All three routes use lazy loading (loadComponent) so each screen's code
// is only downloaded when the user navigates there — better initial load time.
export const routes: Routes = [
  // Landing page redirects to the battle picker
  { path: '', redirectTo: 'battle', pathMatch: 'full' },
  {
    path: 'battle',
    loadComponent: () =>
      import('./battle-picker/battle-picker.component').then(
        (m) => m.BattlePickerComponent,
      ),
  },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./dashboard/dashboard.component').then(
        (m) => m.DashboardComponent,
      ),
  },
  {
    path: 'results',
    loadComponent: () =>
      import('./battle-results/battle-results.component').then(
        (m) => m.BattleResultsComponent,
      ),
  },
];
