import { Routes } from '@angular/router';

// loadComponent splits routes into lazy chunks — starting with the picker only.
export const routes: Routes = [
  { path: '', redirectTo: 'battle', pathMatch: 'full' },
  {
    path: 'battle',
    loadComponent: () =>
      import('./battle-picker/battle-picker.component').then(
        (m) => m.BattlePickerComponent,
      ),
  },
];
