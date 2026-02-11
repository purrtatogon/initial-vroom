import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject, Observable } from 'rxjs';
import { Telemetry } from '../models/telemetry.model';

const WS_URL = '/vroom-ws';
const RACE_TOPIC = '/topic/race';

/**
 * WebSocket service for race telemetry. Uses SockJS + STOMP (first time using WebSockets).
 * Connects to the backend, subscribes to /topic/race, and exposes incoming data as an Observable.
 */
@Injectable({
  providedIn: 'root',
})
export class TelemetryService {
  // Subject receives WebSocket messages; asObservable() exposes a read-only stream to components
  private readonly telemetrySubject = new Subject<Telemetry>();
  readonly telemetry$: Observable<Telemetry> = this.telemetrySubject.asObservable();

  private client: Client | null = null;

  connect(): void {
    this.client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),  // SockJS provides fallbacks if WebSocket is blocked
      onConnect: () => {
        this.client?.subscribe(RACE_TOPIC, (message) => {
          const telemetry = JSON.parse(message.body) as Telemetry;
          this.telemetrySubject.next(telemetry);
        });
      },
    });

    this.client.activate();
  }

  disconnect(): void {
    this.client?.deactivate();
    this.client = null;
  }
}
