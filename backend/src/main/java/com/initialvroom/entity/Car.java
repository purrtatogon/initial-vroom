package com.initialvroom.entity;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.initialvroom.config.BooleanConverter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Car entity mapped to the MongoDB "cars" collection.
 * First time using Spring Data MongoDB.
 */
@Document(collection = "cars")  // Maps this class to the "cars" collection in MongoDB
public class Car {

    @Id  // MongoDB document ID field
    @CsvBindByName(column = "car_model_id")
    private String carModelId;

    @CsvBindByName(column = "car_display_name")
    private String carDisplayName;

    @CsvBindByName(column = "driver_name")
    private String driverName;

    @CsvBindByName(column = "driver_team")
    private String driverTeam;

    @CsvBindByName(column = "stage_id")
    private String stageId;

    @CsvBindByName(column = "engine_code")
    private String engineCode;

    @CsvBindByName(column = "car_hp")
    private Integer carHp;

    @CsvBindByName(column = "torque_nm")
    private Integer torqueNm;

    @CsvBindByName(column = "weight_kg")
    private Integer weightKg;

    @CsvBindByName(column = "redline_rpm")
    private Integer redlineRpm;

    @CsvBindByName(column = "aspiration_type")
    private String aspirationType;

    @CsvBindByName(column = "drivetrain_code")
    private String drivetrainCode;

    @CsvCustomBindByName(column = "has_speed_chime", converter = BooleanConverter.class)
    private Boolean hasSpeedChime;

    @CsvBindByName(column = "image_url")
    private String imageUrl;

    @CsvBindByName(column = "specs_note")
    private String specsNote;

    // Default constructor for OpenCSV and MongoDB
    public Car() {
    }

    // Getters and setters
    public String getCarModelId() {
        return carModelId;
    }

    public void setCarModelId(String carModelId) {
        this.carModelId = carModelId;
    }

    public String getCarDisplayName() {
        return carDisplayName;
    }

    public void setCarDisplayName(String carDisplayName) {
        this.carDisplayName = carDisplayName;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverTeam() {
        return driverTeam;
    }

    public void setDriverTeam(String driverTeam) {
        this.driverTeam = driverTeam;
    }

    public String getStageId() {
        return stageId;
    }

    public void setStageId(String stageId) {
        this.stageId = stageId;
    }

    public String getEngineCode() {
        return engineCode;
    }

    public void setEngineCode(String engineCode) {
        this.engineCode = engineCode;
    }

    public Integer getCarHp() {
        return carHp;
    }

    public void setCarHp(Integer carHp) {
        this.carHp = carHp;
    }

    public Integer getTorqueNm() {
        return torqueNm;
    }

    public void setTorqueNm(Integer torqueNm) {
        this.torqueNm = torqueNm;
    }

    public Integer getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(Integer weightKg) {
        this.weightKg = weightKg;
    }

    public Integer getRedlineRpm() {
        return redlineRpm;
    }

    public void setRedlineRpm(Integer redlineRpm) {
        this.redlineRpm = redlineRpm;
    }

    public String getAspirationType() {
        return aspirationType;
    }

    public void setAspirationType(String aspirationType) {
        this.aspirationType = aspirationType;
    }

    public String getDrivetrainCode() {
        return drivetrainCode;
    }

    public void setDrivetrainCode(String drivetrainCode) {
        this.drivetrainCode = drivetrainCode;
    }

    public Boolean getHasSpeedChime() {
        return hasSpeedChime;
    }

    public void setHasSpeedChime(Boolean hasSpeedChime) {
        this.hasSpeedChime = hasSpeedChime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSpecsNote() {
        return specsNote;
    }

    public void setSpecsNote(String specsNote) {
        this.specsNote = specsNote;
    }
}
