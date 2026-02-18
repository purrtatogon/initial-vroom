package com.initialvroom.entity;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.initialvroom.config.BooleanConverter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Maps to the "cars" collection in MongoDB.
 * Each field uses @CsvBindByName so OpenCSV can parse the CSV rows directly into Car objects.
 * The @Id on carModelId means the CSV's car_model_id becomes the Mongo _id — a readable natural key
 * like "ae86_takumi_stg1_stock" instead of a random ObjectId.
 */
@Document(collection = "cars")
public class Car {

    // Using car_model_id as the Mongo _id — natural key so we can query by meaningful IDs
    @Id
    @CsvBindByName(column = "car_model_id")
    private String carModelId;

    @CsvBindByName(column = "car_display_name")
    private String carDisplayName;

    @CsvBindByName(column = "driver_name")
    private String driverName;

    @CsvBindByName(column = "driver_team")
    private String driverTeam;

    // "Stage 1" or "Stage 2" — used to group cars in the battle picker
    @CsvBindByName(column = "stage_id")
    private String stageId;

    @CsvBindByName(column = "engine_code")
    private String engineCode;

    // These three drive the simulation physics (power-to-weight ratio = carHp / weightKg)
    @CsvBindByName(column = "car_hp")
    private Integer carHp;

    @CsvBindByName(column = "torque_nm")
    private Integer torqueNm;

    @CsvBindByName(column = "weight_kg")
    private Integer weightKg;

    // Only used for scaling the RPM gauge on the dashboard, not for sim physics
    @CsvBindByName(column = "redline_rpm")
    private Integer redlineRpm;

    // "NA", "Turbo", or "Twin Turbo" — turbo cars get a 4% straight-line speed bonus
    @CsvBindByName(column = "aspiration_type")
    private String aspirationType;

    // "FR", "FF", "MR", "4WD" — 4WD gets an 8% hairpin speed bonus
    @CsvBindByName(column = "drivetrain_code")
    private String drivetrainCode;

    // Custom converter because CSV has "TRUE"/"FALSE" as strings
    @CsvCustomBindByName(column = "has_speed_chime", converter = BooleanConverter.class)
    private Boolean hasSpeedChime;

    @CsvBindByName(column = "image_url")
    private String imageUrl;

    @CsvBindByName(column = "specs_note")
    private String specsNote;

    // No-arg constructor required by both OpenCSV and Spring Data MongoDB
    public Car() {
    }

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
