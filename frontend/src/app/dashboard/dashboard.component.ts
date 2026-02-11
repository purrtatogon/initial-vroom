import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TelemetryService } from '../services/telemetry.service';
import { Telemetry } from '../models/telemetry.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnDestroy {
  telemetry: Telemetry | null = null;
  isConnected = false;
  private telemetrySubscription?: Subscription;

  constructor(private telemetryService: TelemetryService) {}

  startSimulation(): void {
    this.telemetryService.connect();  // Opens WebSocket connection to backend
    this.isConnected = true;
    this.telemetrySubscription = this.telemetryService.telemetry$.subscribe((data) => {
      this.telemetry = data;  // Each WebSocket message updates the UI
    });
  }

  stopSimulation(): void {
    this.telemetryService.disconnect();
    this.isConnected = false;
    this.telemetrySubscription?.unsubscribe();
  }

  ngOnDestroy(): void {
    this.telemetrySubscription?.unsubscribe();
  }
}
