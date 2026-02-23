import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Car } from '../models/car.model';
import { CarService } from '../services/car.service';
import { BattleService } from '../services/battle.service';

/**
 * First screen the user sees — shows the car roster and lets them pick two opponents.
 * Flow: select car 1 → select car 2 → click "Start Battle" → navigate to /dashboard.
 */
@Component({
  selector: 'app-battle-picker',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './battle-picker.component.html',
  styleUrl: './battle-picker.component.css',
})
export class BattlePickerComponent implements OnInit {
  cars: Car[] = [];
  stages: { id: string; cars: Car[] }[] = [];  // cars grouped by stageId for the roster UI
  car1: Car | null = null;   // left lane selection
  car2: Car | null = null;   // right lane selection
  loading = true;
  error = '';
  starting = false;  // prevents double-clicking "Start Battle"

  constructor(
    private carService: CarService,
    private battleService: BattleService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    // Fetch all cars at once and group client-side (only 14 cars, not worth two API calls)
    this.carService.getAllCars().subscribe({
      next: (cars) => {
        this.cars = cars;
        this.stages = this.groupByStage(cars);
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load cars. Is the backend running?';
        this.loading = false;
      },
    });
  }

  /**
   * Groups the flat car array into stages for the UI sections.
   * Uses a Map to collect cars by stageId, then sorts alphabetically.
   */
  private groupByStage(cars: Car[]): { id: string; cars: Car[] }[] {
    const map = new Map<string, Car[]>();
    for (const car of cars) {
      const key = car.stageId || 'Unknown';
      if (!map.has(key)) map.set(key, []);
      map.get(key)!.push(car);
    }
    return Array.from(map.entries())
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([id, stageCars]) => ({ id, cars: stageCars }));
  }

  // Clicking a car either selects it (fills first empty slot) or deselects it
  selectCar(car: Car): void {
    if (this.isSelected(car)) {
      this.deselectCar(car);
      return;
    }
    if (!this.car1) {
      this.car1 = car;
    } else if (!this.car2) {
      this.car2 = car;
    }
  }

  deselectCar(car: Car): void {
    if (this.car1?.carModelId === car.carModelId) {
      this.car1 = null;
    } else if (this.car2?.carModelId === car.carModelId) {
      this.car2 = null;
    }
  }

  // Using carModelId for comparison since it's the unique key (same as Mongo _id)
  isSelected(car: Car): boolean {
    return (
      this.car1?.carModelId === car.carModelId ||
      this.car2?.carModelId === car.carModelId
    );
  }

  isCar1(car: Car): boolean {
    return this.car1?.carModelId === car.carModelId;
  }

  isCar2(car: Car): boolean {
    return this.car2?.carModelId === car.carModelId;
  }

  canStart(): boolean {
    return this.car1 !== null && this.car2 !== null && !this.starting;
  }

  // Sends the two car IDs to the backend, then navigates to the live dashboard
  startBattle(): void {
    if (!this.car1 || !this.car2) return;
    this.starting = true;
    this.battleService.startBattle(this.car1.carModelId, this.car2.carModelId).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.error = 'Failed to start battle.';
        this.starting = false;
      },
    });
  }
}
