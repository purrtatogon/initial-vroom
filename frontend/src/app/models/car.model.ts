/**
 * Mirrors the Java Car entity exactly — field names match because Jackson
 * serializes Java camelCase to JSON camelCase, and TypeScript reads it as-is.
 * If a field is added in Car.java, it needs to be added here too.
 */
export interface Car {
  carModelId: string;
  carDisplayName: string;
  driverName: string;
  driverTeam: string;
  stageId: string;
  engineCode: string;
  carHp: number;
  torqueNm: number;
  weightKg: number;
  redlineRpm: number;
  aspirationType: string;
  drivetrainCode: string;
  hasSpeedChime: boolean;
  imageUrl: string;
  specsNote: string;
}
