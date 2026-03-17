import { Routes } from '@angular/router';

// loadComponent splits routes into lazy chunks — the picker loads first; dashboard/results fetch on demand.
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
