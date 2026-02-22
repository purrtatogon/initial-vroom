package com.initialvroom.dto;

/**
 * Request body for POST /api/battles.
 * Jackson deserializes the JSON { car1Id, car2Id } into this.
 * Needs the no-arg constructor and setters for Jackson to work.
 */
public class BattleRequest {

    private String car1Id;
    private String car2Id;

    public BattleRequest() {
    }

    public BattleRequest(String car1Id, String car2Id) {
        this.car1Id = car1Id;
        this.car2Id = car2Id;
    }

    public String getCar1Id() {
        return car1Id;
    }

    public void setCar1Id(String car1Id) {
        this.car1Id = car1Id;
    }

    public String getCar2Id() {
        return car2Id;
    }

    public void setCar2Id(String car2Id) {
        this.car2Id = car2Id;
    }
}
