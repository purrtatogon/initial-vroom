import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Car } from '../models/car.model';
import { environment } from '../../environments/environment';

/**
 * Fetches car data from the REST API.
 * The URL comes from environment.apiUrl — localhost in dev, production backend URL in prod.
 */
@Injectable({
  providedIn: 'root',
})
export class CarService {
  private readonly baseUrl = `${environment.apiUrl}/cars`;

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
