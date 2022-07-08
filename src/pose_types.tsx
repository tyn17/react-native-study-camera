export enum CaptureBodyPart {
  UPPER_FRONT = 0,
  UPPER_BACK = 1,
  LOWER_FRONT = 2,
  LOWER_BACK = 3,
}

/**
 * Body Part for Pose Detection - Move Net
 */
export enum BodyPart {
  UNDEFINED = -1,
  NOSE = 0,
  LEFT_EYE = 1,
  RIGHT_EYE = 2,
  LEFT_EAR = 3,
  RIGHT_EAR = 4,
  LEFT_SHOULDER = 5,
  RIGHT_SHOULDER = 6,
  LEFT_ELBOW = 7,
  RIGHT_ELBOW = 8,
  LEFT_WRIST = 9,
  RIGHT_WRIST = 10,
  LEFT_HIP = 11,
  RIGHT_HIP = 12,
  LEFT_KNEE = 13,
  RIGHT_KNEE = 14,
  LEFT_ANKLE = 15,
  RIGHT_ANKLE = 16,
}

/**
 * KeyPoint of Body Part
 */
export interface KeyPoint {
  bodyPart: BodyPart;
  x: number;
  y: number;
  z: number;
  score: number;
}

/**
 * Result of single Body
 */
export interface PoseResult {
  imageWidth: number;
  imageHeight: number;
  keyPoints: Array<KeyPoint>;
  score: number;
}

/**
 * Detection Mode
 */
export enum DetectionMode {
  NONE = 0,
  POSE = 1,
}

/**
 * Pose Detect Options
 */
export interface PoseDetectOptions {
  /**
   * Min Score to detect Pose
   */
  minScore: number;
  /**
   * From 0.0 -> 1.0.
   */
  minDetectionConfidence: number;
  /**
   * From 0.0 -> 1.0
   */
  minTrackingConfidence: number;
  /**
   * To check Upper Body is close or far from Camera. Value from 0.0 -> 1.0
   */
  minShoulderDistance: number;
  /**
   * To check Upper Body is close or far from Camera. Value from 0.0 -> 1.0
   */
  maxShoulderDistance: number;
  /**
   * To check Lower Body is close or far from Camera. Value from 0.0 -> 1.0
   */
  minHipDistance: number;
  /**
   * To check Lower Body is close or far from Camera. Value from 0.0 -> 1.0
   */
  maxHipDitance: number;
  /**
   * Angel Hand-Shoulder-Hip. From 0 -> 180. Less than maxHandAngel
   */
  minHandAngel: number;
  /**
   * Angel Hand-Shoulder-Hip. From 0 -> 180. Greater than minHandAngel
   */
  maxHandAngel: number;
  /**
   * Angel LeftKnee-BetweenOfHips-RightKnee. From 0 -> 180
   */
  minLegAngel: number;
  /**
   * Angel LeftKnee-BetweenOfHips-RightKnee. From 0 -> 180
   */
  maxLegAngel: number;
  /**
   * Ratio of Distance Eye to Top side per Distance Eye to Shoulder.
   */
  minEyeToTopRate: number;
  /**
   * Ratio of Distance Wrist to Bottom side per Distance Elbow to Wrist.
   */
  minHandToBottomRate: number;
  /**
   * Ratio of Distance Left Wrist to Left side per Distance Elbow to Wrist.
   */
  minHandToLeftRate: number;
  /**
   * Ratio of Distance Right Wrist to Right side per Distance Elbow to Wrist.
   */
  minHandToRightRate: number;
  /**
   * Ratio of Distance Ankle to Bottom side per Distance Knee to Ankle
   */
  minAnkleToBottomRate: number;
  /**
   * Ratio of Distance Left Hip to Left side per Distance Hip to Knee
   */
  minHipToLeftRate: number;
  /**
   * Ratio of Distance Right Hip to Right side per Distance Hip to Knee
   */
  minHipToRightRate: number;
}

/**
 * Default Pose Detect Options
 */
export const defaultOptions: PoseDetectOptions = {
  minScore: 0.3,
  minDetectionConfidence: 0.5,
  minTrackingConfidence: 0.5,
  minShoulderDistance: 0.25,
  maxShoulderDistance: 0.35,
  minHipDistance: 0.1,
  maxHipDitance: 0.35,
  minHandAngel: 20,
  maxHandAngel: 40,
  minLegAngel: 25,
  maxLegAngel: 35,
  minEyeToTopRate: 1.0,
  minHandToBottomRate: 1.0,
  minHandToLeftRate: 0.5,
  minHandToRightRate: 0.5,
  minAnkleToBottomRate: 0.5,
  minHipToLeftRate: 1.2,
  minHipToRightRate: 1.2,
};

/**
 * Pose Verification Message Keys
 */
export enum DetectionMessageKeys {
  NOBODY = 'NO_BODY',
  BODY_OUT_OF_VIEW = 'BODY_OUT_OF_VIEW',
  BODY_TURN_180_DEGREES = 'BODY_TURN_180_DEGREES',
  CAMERA_TOO_HIGH = 'CAMERA_TOO_HIGH',
  CAMERA_TOO_LOW = 'CAMERA_TOO_LOW',
  CAMERA_TOO_CLOSE = 'CAMERA_TOO_CLOSE',
  CAMERA_TOO_FAR = 'CAMERA_TOO_FAR',
  ARMS_TOO_HIGH = 'ARMS_TOO_HIGH',
  ARMS_TOO_LOW = 'ARMS_TOO_LOW',
  HEAD_OUT_OF_VIEW = 'HEAD_OUT_OF_VIEW',
  HAND_OUT_OF_VIEW = 'HAND_OUT_OF_VIEW',
  LEFT_HAND_OUT_OF_VIEW = 'LEFT_HAND_OUT_OF_VIEW',
  RIGHT_HAND_OUT_OF_VIEW = 'RIGHT_HAND_OUT_OF_VIEW',
  ANGEL_LEGS_TOO_LARGE = 'ANGEL_LEGS_TOO_LARGE',
  ANGEL_LEGS_TOO_SMALL = 'ANGEL_LEGS_TOO_SMALL',
  HIPS_OUT_OF_VIEW = 'HIPS_OUT_OF_VIEW',
  FEET_OUT_OF_VIEW = 'FEET_OUT_OF_VIEW',
  LEFT_LEG_OUT_OF_VIEW = 'LEFT_LEG_OUT_OF_VIEW',
  RIGHT_LEG_OUT_OF_VIEW = 'RIGHT_LEG_OUT_OF_VIEW',
  PERFECT = 'PERFECT',
}
