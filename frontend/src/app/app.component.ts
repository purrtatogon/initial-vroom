import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

// Root component — just a shell that renders whatever route is active.
// Using inline template since it's literally just a router-outlet, no extra markup needed.
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet></router-outlet>',
})
export class AppComponent {}
