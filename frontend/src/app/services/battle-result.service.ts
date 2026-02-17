import { Injectable } from '@angular/core';
import { BattleTelemetry } from '../models/telemetry.model';

/**
 * Holds the final telemetry snapshot in memory so the results screen can read it.
 * No localStorage or server persistence — results are ephemeral.
 * The dashboard stores the data here right before navigating to /results.
 */
@Injectable({
  providedIn: 'root',
})
export class BattleResultService {
  private result: BattleTelemetry | null = null;

  store(result: BattleTelemetry): void {
    this.result = result;
  }

  retrieve(): BattleTelemetry | null {
    return this.result;
  }

  // Called when user clicks "New Battle" or "Back to Picker" on the results screen
  clear(): void {
    this.result = null;
  }
}
