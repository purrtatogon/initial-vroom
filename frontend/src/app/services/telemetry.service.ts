import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject, Observable } from 'rxjs';
import { BattleTelemetry } from '../models/telemetry.model';
import { environment } from '../../environments/environment';

// STOMP topic the backend publishes to every tick
const RACE_TOPIC = '/topic/race';

/**
 * Connects to the backend WebSocket and exposes a telemetry$ observable.
 * Uses SockJS as the transport (falls back to HTTP if WebSocket is blocked).
 * The dashboard subscribes to telemetry$ and updates the UI every 50ms.
 */
@Injectable({
  providedIn: 'root',
})
export class TelemetryService {
  // Subject acts as both producer and consumer — we push into it, dashboard reads from it
  private readonly telemetrySubject = new Subject<BattleTelemetry>();
  readonly telemetry$: Observable<BattleTelemetry> = this.telemetrySubject.asObservable();

  private client: Client | null = null;

  connect(): void {
    this.client = new Client({
      // SockJS wraps the WebSocket connection; needed because raw WS can fail through proxies
      webSocketFactory: () => new SockJS(environment.wsUrl),
      onConnect: () => {
        // Once connected, subscribe to the race topic and push each message to our Subject
        this.client?.subscribe(RACE_TOPIC, (message) => {
          const battle = JSON.parse(message.body) as BattleTelemetry;
          this.telemetrySubject.next(battle);
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
