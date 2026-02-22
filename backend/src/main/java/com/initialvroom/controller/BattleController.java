package com.initialvroom.controller;

import com.initialvroom.dto.BattleRequest;
import com.initialvroom.service.RaceSimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for starting and stopping battles.
 * Uses POST (not GET) because these operations change server state.
 * The actual race simulation runs on a separate scheduled thread — this just triggers it.
 */
@RestController
@RequestMapping("/api/battles")
@CrossOrigin(originPatterns = "*")
public class BattleController {

    private final RaceSimulationService raceSimulationService;

    public BattleController(RaceSimulationService raceSimulationService) {
        this.raceSimulationService = raceSimulationService;
    }

    // POST /api/battles — body must contain { car1Id, car2Id }
    // Returns 400 if either car ID doesn't exist in MongoDB
    @PostMapping
    public ResponseEntity<String> startBattle(@RequestBody BattleRequest request) {
        boolean started = raceSimulationService.startBattle(request.getCar1Id(), request.getCar2Id());
        if (started) {
            return ResponseEntity.ok("Battle started");
        }
        return ResponseEntity.badRequest().body("Could not start battle — check car IDs");
    }

    // POST /api/battles/stop — no body needed, just stops whatever race is running
    @PostMapping("/stop")
    public ResponseEntity<String> stopBattle() {
        raceSimulationService.stopBattle();
        return ResponseEntity.ok("Battle stopped");
    }
}
