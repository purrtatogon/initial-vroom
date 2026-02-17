import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

// Standalone bootstrap — no AppModule needed (Angular 18+ pattern)
bootstrapApplication(AppComponent, appConfig).catch((err) => console.error(err));
