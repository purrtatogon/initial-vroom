import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TelemetryService } from '../services/telemetry.service';
import { BattleService } from '../services/battle.service';
import { BattleResultService } from '../services/battle-result.service';
import { BattleTelemetry, Telemetry } from '../models/telemetry.model';
import { TrackMapComponent } from '../track-map/track-map.component';
import { Subscription } from 'rxjs';

/**
 * The live race dashboard — shows telemetry panels, the track map, and a status bar.
 * Connects to WebSocket on init and receives ~20 updates per second.
 * When the race finishes, stores the final snapshot and navigates to /results.
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, TrackMapComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent implements OnDestroy {
  battle: BattleTelemetry | null = null;
  isConnected = false;
  private telemetrySub?: Subscription;
  private resultsNavigationTimer?: ReturnType<typeof setTimeout>;
  private hasNavigatedToResults = false; // guard: duplicate finished payloads shouldn't double-navigate

  constructor(
    private telemetryService: TelemetryService,
    private battleService: BattleService,
    private battleResultService: BattleResultService,
    private router: Router,
  ) {
    // Connect immediately when the component loads
    this.connect();
  }

  // Convenience getters so the template can use car1/car2 without long paths
  get car1(): Telemetry | null {
    return this.battle?.car1 ?? null;
  }

  get car2(): Telemetry | null {
    return this.battle?.car2 ?? null;
  }

  // Formats milliseconds into MM:SS.T for the status bar
  get elapsedFormatted(): string {
    if (!this.battle) return '00:00.0';
    const ms = this.battle.elapsedMs;
    const minutes = Math.floor(ms / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    const tenths = Math.floor((ms % 1000) / 100);
    const mm = String(minutes).padStart(2, '0');
    const ss = String(seconds).padStart(2, '0');
    return `${mm}:${ss}.${tenths}`;
  }

  get gapFormatted(): string {
    if (!this.battle) return '0.0';
    return this.battle.gap.toFixed(1);
  }

  connect(): void {
    this.telemetryService.connect();
    this.isConnected = true;
    this.telemetrySub = this.telemetryService.telemetry$.subscribe((data) => {
      this.battle = data;
      if (data.finished && !this.hasNavigatedToResults) {
        this.hasNavigatedToResults = true;
        this.battleResultService.store(data);
        this.clearResultsNavigationTimer();
        // Brief pause so the last dashboard frame reads before routing away
        this.resultsNavigationTimer = setTimeout(() => this.navigateToResults(), 1500);
      }
    });
  }

  // User clicked "Abort Battle" — stop the sim and go back to picker
  abortBattle(): void {
    this.clearResultsNavigationTimer();
    this.battleService.stopBattle().subscribe({
      next: () => this.navigateBack(),
      error: () => this.navigateBack(),  // navigate even if the stop call fails
    });
  }

  private clearResultsNavigationTimer(): void {
    if (this.resultsNavigationTimer !== undefined) {
      clearTimeout(this.resultsNavigationTimer);
      this.resultsNavigationTimer = undefined;
    }
  }

  private navigateToResults(): void {
    this.clearResultsNavigationTimer();
    this.telemetryService.disconnect();
    this.isConnected = false;
    this.telemetrySub?.unsubscribe();
    this.router.navigate(['/results']);
  }

  private navigateBack(): void {
    this.clearResultsNavigationTimer();
    this.telemetryService.disconnect();
    this.isConnected = false;
    this.telemetrySub?.unsubscribe();
    this.router.navigate(['/battle']);
  }

  // Clean up WebSocket connection if the component is destroyed unexpectedly
  ngOnDestroy(): void {
    this.clearResultsNavigationTimer();
    this.telemetrySub?.unsubscribe();
    this.telemetryService.disconnect();
  }
}
