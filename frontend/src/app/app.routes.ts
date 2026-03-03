import { Routes } from '@angular/router';

export const routes: Routes = [
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
];
