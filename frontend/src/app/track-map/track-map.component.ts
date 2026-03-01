import {
  Component,
  Input,
  ElementRef,
  ViewChild,
  AfterViewInit,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { CommonModule } from '@angular/common';

interface CarPosition {
  x: number;
  y: number;
  angle: number;      // rotation in degrees to face the direction of travel
  imageUrl: string;
  driverName: string;
}

/**
 * Renders the SVG mountain pass track and positions two car sprites on it.
 * The key trick: getPointAtLength() converts a 0–1 progress value into x,y coordinates
 * on the SVG path, so the backend doesn't need to know anything about pixels or SVG.
 */
@Component({
  selector: 'app-track-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './track-map.component.html',
  styleUrl: './track-map.component.css',
})
export class TrackMapComponent implements AfterViewInit, OnChanges {
  // Progress values (0–1) received from the dashboard, updated every 50ms
  @Input() car1Progress = 0;
  @Input() car2Progress = 0;
  @Input() car1ImageUrl = '';
  @Input() car2ImageUrl = '';
  @Input() car1DriverName = '';
  @Input() car2DriverName = '';

  // Reference to the SVG <path> element so we can call getPointAtLength() on it
  @ViewChild('racingLine') racingLineRef!: ElementRef<SVGPathElement>;

  car1Pos: CarPosition = { x: 0, y: 0, angle: 0, imageUrl: '', driverName: '' };
  car2Pos: CarPosition = { x: 0, y: 0, angle: 0, imageUrl: '', driverName: '' };

  private pathLength = 0;   // total pixel length of the SVG path
  private viewReady = false; // can't call getPointAtLength() until the view is initialized

  ngAfterViewInit(): void {
    // Cache the total path length — it doesn't change after render
    this.pathLength = this.racingLineRef.nativeElement.getTotalLength();
    this.viewReady = true;
    this.updatePositions();
  }

  // Called every time an @Input changes (which is every tick during a race)
  ngOnChanges(_changes: SimpleChanges): void {
    if (this.viewReady) {
      this.updatePositions();
    }
  }

  /** Builds the SVG transform string to position and rotate a car sprite. */
  getCarTransform(pos: CarPosition): string {
    // scale(-1, 1) flips horizontally because the car sprite faces left by default
    return `translate(${pos.x}, ${pos.y}) rotate(${pos.angle}) scale(-1, 1)`;
  }

  private updatePositions(): void {
    const path = this.racingLineRef.nativeElement;

    // Convert progress (0–1) to a point along the SVG path
    const p1 = path.getPointAtLength(this.car1Progress * this.pathLength);
    const a1 = this.getTravelAngle(path, this.car1Progress);
    this.car1Pos = {
      x: p1.x,
      y: p1.y,
      angle: a1,
      imageUrl: this.car1ImageUrl,
      driverName: this.car1DriverName,
    };

    const p2 = path.getPointAtLength(this.car2Progress * this.pathLength);
    const a2 = this.getTravelAngle(path, this.car2Progress);
    this.car2Pos = {
      x: p2.x,
      y: p2.y,
      angle: a2,
      imageUrl: this.car2ImageUrl,
      driverName: this.car2DriverName,
    };
  }

  /**
   * Gets the direction the car should face by sampling two points (before and after)
   * and computing the angle between them with atan2.
   * The small delta (3px) keeps the angle smooth even on tight curves.
   */
  private getTravelAngle(path: SVGPathElement, progress: number): number {
    const len = this.pathLength;
    const pos = progress * len;
    const delta = 3;
    const before = path.getPointAtLength(Math.max(0, pos - delta));
    const after = path.getPointAtLength(Math.min(len, pos + delta));
    return Math.atan2(after.y - before.y, after.x - before.x) * (180 / Math.PI);
  }
}
