import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { BattleResultService } from '../services/battle-result.service';
import { BattleTelemetry, Telemetry } from '../models/telemetry.model';

/**
 * Shows the battle outcome — winner, stats comparison, race time, and gap.
 * Reads the result from BattleResultService (stored by the dashboard right before navigating here).
 * If someone tries to hit /results directly without a completed race, we redirect to /battle.
 */
@Component({
  selector: 'app-battle-results',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './battle-results.component.html',
  styleUrl: './battle-results.component.css',
})
export class BattleResultsComponent implements OnInit {
  result: BattleTelemetry | null = null;
  winner: Telemetry | null = null;
  loser: Telemetry | null = null;

  constructor(
    private resultService: BattleResultService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.result = this.resultService.retrieve();

    // Guard: no result in memory means we got here without a completed race
    if (!this.result) {
      this.router.navigate(['/battle']);
      return;
    }

    // Winner = whoever has higher progress (closer to or at 1.0)
    // In a tie, car1 wins — matches the backend's tie-breaking logic
    const c1 = this.result.car1;
    const c2 = this.result.car2;
    if (c1.progress >= c2.progress) {
      this.winner = c1;
      this.loser = c2;
    } else {
      this.winner = c2;
      this.loser = c1;
    }
  }

  get elapsedFormatted(): string {
    if (!this.result) return '—';
    const ms = this.result.elapsedMs;
    const minutes = Math.floor(ms / 60000);
    const seconds = Math.floor((ms % 60000) / 1000);
    const tenths = Math.floor((ms % 1000) / 100);
    return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}.${tenths}`;
  }

  get gapFormatted(): string {
    if (!this.result) return '—';
    return this.result.gap.toFixed(1);
  }

  // Both buttons clear the stored result and go back to pick new cars
  rematch(): void {
    this.resultService.clear();
    this.router.navigate(['/battle']);
  }

  backToPicker(): void {
    this.resultService.clear();
    this.router.navigate(['/battle']);
  }
}
