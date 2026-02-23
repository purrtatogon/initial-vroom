import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * HTTP calls to start and stop battles.
 * Uses responseType: 'text' because the backend returns plain strings, not JSON.
 */
@Injectable({
  providedIn: 'root',
})
export class BattleService {
  private readonly baseUrl = '/api/battles';

  constructor(private http: HttpClient) {}

  // Sends the two car IDs to the backend to kick off the simulation
  startBattle(car1Id: string, car2Id: string): Observable<string> {
    return this.http.post(this.baseUrl, { car1Id, car2Id }, { responseType: 'text' });
  }

  // Tells the backend to stop the race loop
  stopBattle(): Observable<string> {
    return this.http.post(`${this.baseUrl}/stop`, {}, { responseType: 'text' });
  }
}
