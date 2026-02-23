import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Car } from '../models/car.model';

/**
 * Fetches car data from the REST API.
 * The base URL '/api/cars' works because nginx proxies it to the backend.
 */
@Injectable({
  providedIn: 'root',
})
export class CarService {
  private readonly baseUrl = '/api/cars';

  constructor(private http: HttpClient) {}

  // Gets all 14 cars — used by the battle picker on load
  getAllCars(): Observable<Car[]> {
    return this.http.get<Car[]>(this.baseUrl);
  }

  // Gets cars for a specific stage (e.g., ?stageId=Stage 1)
  // Not used in the picker right now (we fetch all and group client-side), but available
  getByStage(stageId: string): Observable<Car[]> {
    return this.http.get<Car[]>(this.baseUrl, { params: { stageId } });
  }
}
