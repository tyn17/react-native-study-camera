import {
  BodyPart,
  CaptureBodyPart,
  DetectionMessageKeys,
  KeyPoint,
  PoseDetectOptions,
  PoseResult,
} from './pose_types';

const keyPointAt = (keyPoints: KeyPoint[], position: BodyPart): KeyPoint => {
  return (
    keyPoints.find((x) => x.bodyPart === position) || {
      bodyPart: position,
      x: -1.0,
      y: -1.0,
      z: -1.0,
      score: 0,
    }
  );
};

/** Check Point in frame */
const isInFrame = (point: KeyPoint, score: number) => {
  return (
    point.x > 0 &&
    point.x < 1 &&
    point.y > 0 &&
    point.y < 1 &&
    (!score || point.score >= score)
  );
};

/**
 * Return Angle from 3 points in 2D - angel at B point */
const calcAngel2D = (a: KeyPoint, b: KeyPoint, c: KeyPoint) => {
  var ab = Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
  var bc = Math.sqrt(Math.pow(b.x - c.x, 2) + Math.pow(b.y - c.y, 2));
  var ac = Math.sqrt(Math.pow(c.x - a.x, 2) + Math.pow(c.y - a.y, 2));
  return (
    (Math.acos((bc * bc + ab * ab - ac * ac) / (2 * bc * ab)) * 180) / Math.PI
  );
};

/**
 * Distance between 2 points */
export const distance = (p1: KeyPoint, p2: KeyPoint) => {
  return Math.hypot(p2.x - p1.x, p2.y - p1.y);
};

/**
 * Middle point between 2 points */
export const mid = (p1: KeyPoint, p2: KeyPoint): KeyPoint => {
  return {
    bodyPart: BodyPart.UNDEFINED,
    x: (p1.x + p2.x) / 2,
    y: (p1.y + p2.y) / 2,
    z: (p1.z + p2.z) / 2,
    score: (p1.score + p2.score) / 2,
  };
};

/**
 * Check Body turn 180 degress */
const isBodyTurned = (
  p1: KeyPoint,
  p2: KeyPoint,
  bodyPart: CaptureBodyPart
) => {
  const ltr = p1.x < p2.x;
  if (
    bodyPart === CaptureBodyPart.UPPER_FRONT ||
    bodyPart === CaptureBodyPart.LOWER_FRONT
  ) {
    return ltr;
  }
  return !ltr;
};

export const verifyPose = (
  pose: PoseResult,
  bodyPart: CaptureBodyPart,
  options: PoseDetectOptions
): DetectionMessageKeys => {
  if (pose.score < options.minScore) return DetectionMessageKeys.NOBODY;
  const leftEye = keyPointAt(pose.keyPoints, BodyPart.LEFT_EYE);
  const rightEye = keyPointAt(pose.keyPoints, BodyPart.RIGHT_EYE);
  const leftShoulder = keyPointAt(pose.keyPoints, BodyPart.LEFT_SHOULDER);
  const rightShoulder = keyPointAt(pose.keyPoints, BodyPart.RIGHT_SHOULDER);
  const leftHip = keyPointAt(pose.keyPoints, BodyPart.LEFT_HIP);
  const rightHip = keyPointAt(pose.keyPoints, BodyPart.RIGHT_HIP);
  const leftKnee = keyPointAt(pose.keyPoints, BodyPart.LEFT_KNEE);
  const rightKnee = keyPointAt(pose.keyPoints, BodyPart.RIGHT_KNEE);
  const leftAnkle = keyPointAt(pose.keyPoints, BodyPart.LEFT_ANKLE);
  const rightAnkle = keyPointAt(pose.keyPoints, BodyPart.RIGHT_ANKLE);
  const leftElbow = keyPointAt(pose.keyPoints, BodyPart.LEFT_ELBOW);
  const rightElbow = keyPointAt(pose.keyPoints, BodyPart.RIGHT_ELBOW);
  const leftWrist = keyPointAt(pose.keyPoints, BodyPart.LEFT_WRIST);
  const rightWrist = keyPointAt(pose.keyPoints, BodyPart.RIGHT_WRIST);

  const isEye =
    isInFrame(leftEye, options.minScore) ||
    isInFrame(rightEye, options.minScore);
  const isShoulder =
    isInFrame(leftShoulder, options.minScore) ||
    isInFrame(rightShoulder, options.minScore);
  const isHips =
    isInFrame(leftHip, options.minScore) ||
    isInFrame(rightHip, options.minScore);
  const isKnee =
    isInFrame(leftKnee, options.minScore) ||
    isInFrame(rightKnee, options.minScore);
  const isAnkle =
    isInFrame(leftAnkle, options.minScore) ||
    isInFrame(rightAnkle, options.minScore);

  const dLeftHand = distance(leftWrist, leftElbow);
  const dRightHand = distance(rightWrist, rightElbow);

  //Out of frame
  if (!(isEye || isShoulder || isHips || isKnee || isAnkle)) {
    return DetectionMessageKeys.BODY_OUT_OF_VIEW;
  }
  //- Check Front/Back, but not fire message immediate
  var turned = false;
  if (
    bodyPart === CaptureBodyPart.UPPER_FRONT ||
    bodyPart === CaptureBodyPart.UPPER_BACK
  ) {
    turned = isBodyTurned(leftShoulder, rightShoulder, bodyPart);
  } else if (
    bodyPart === CaptureBodyPart.LOWER_FRONT ||
    bodyPart === CaptureBodyPart.LOWER_BACK
  ) {
    turned = isBodyTurned(leftKnee, rightKnee, bodyPart);
  }

  // 1. Check if the subject poses corresponding to the chosen pose.
  // Lower Front/Back
  if (
    bodyPart === CaptureBodyPart.LOWER_FRONT ||
    bodyPart === CaptureBodyPart.LOWER_BACK
  ) {
    if (isKnee) {
      if (turned) {
        return DetectionMessageKeys.BODY_TURN_180_DEGREES;
      }
    } else {
      if (isHips || isShoulder || isEye) {
        return DetectionMessageKeys.CAMERA_TOO_HIGH;
      }
      if (isAnkle) {
        return DetectionMessageKeys.CAMERA_TOO_LOW;
      }
    }
  }

  // Upper Front/Back
  if (
    bodyPart === CaptureBodyPart.UPPER_FRONT ||
    bodyPart === CaptureBodyPart.UPPER_BACK
  ) {
    if (isShoulder) {
      if (turned) {
        return DetectionMessageKeys.BODY_TURN_180_DEGREES;
      }
    } else {
      if (isHips || isKnee || isAnkle) {
        return DetectionMessageKeys.CAMERA_TOO_LOW;
      }
      if (isEye) {
        return DetectionMessageKeys.CAMERA_TOO_HIGH;
      }
    }
  }

  // 2. Close/ far
  // Lower Front/Back
  if (
    bodyPart === CaptureBodyPart.LOWER_FRONT ||
    bodyPart === CaptureBodyPart.LOWER_BACK
  ) {
    //- Tính khoảng cách hai điểm hông so với chiều rộng của màn hình. Perfect là 10%-35%.
    const d = distance(leftHip, rightHip);
    if (d < options.minHipDistance) {
      return DetectionMessageKeys.CAMERA_TOO_FAR;
    }
    if (d > options.maxHipDitance) {
      return DetectionMessageKeys.CAMERA_TOO_CLOSE;
    }
  }
  // Upper Front/Back
  if (
    bodyPart === CaptureBodyPart.UPPER_FRONT ||
    bodyPart === CaptureBodyPart.UPPER_BACK
  ) {
    //- Tính khoảng cách hai điểm vai so với chiều rộng của màn hình. Perfect là 25%-35%.
    const d = distance(leftShoulder, rightShoulder);
    if (d < options.minShoulderDistance) {
      return DetectionMessageKeys.CAMERA_TOO_FAR;
    }
    if (d > options.maxShoulderDistance) {
      return DetectionMessageKeys.CAMERA_TOO_CLOSE;
    }
  }

  // 3. Top/Down/Left/Right
  // Upper Front/Back
  if (
    bodyPart === CaptureBodyPart.UPPER_FRONT ||
    bodyPart === CaptureBodyPart.UPPER_BACK
  ) {
    //-	Detects if the arms are 30 degrees to the trunk: Tính góc giữa ba điểm khửu tay + vai + hông. Perfect là 20-40 độ
    const leftAngel = calcAngel2D(leftElbow, leftShoulder, leftHip);
    const rightAngel = calcAngel2D(rightElbow, rightShoulder, rightHip);
    if (leftAngel < options.minHandAngel || rightAngel < options.minHandAngel) {
      return DetectionMessageKeys.ARMS_TOO_LOW;
    }
    if (leftAngel > options.maxHandAngel || rightAngel > options.maxHandAngel) {
      return DetectionMessageKeys.ARMS_TOO_HIGH;
    }
    //- Up (check nếu điểm mắt cách cạnh trên của frame gấp ít nhất 1.0 lần khoảng cách từ điểm đó đến đường thẳng tạo thành từ hai vai)
    const midEye = mid(leftEye, rightEye);
    const midShoulder = mid(leftShoulder, rightShoulder);
    const upVal = midEye.y / distance(midEye, midShoulder);
    if (upVal < options.minEyeToTopRate) {
      return DetectionMessageKeys.HEAD_OUT_OF_VIEW;
    }
    //-Down (check nếu điểm cổ tay cách cạnh dưới ít nhất bằng khoảng cách cổ tay đến khủy tay)
    const downVal = Math.min(
      (1.0 - leftWrist.y) / dLeftHand,
      (1.0 - rightWrist.y) / dRightHand
    );
    if (downVal < options.minHandToBottomRate) {
      return DetectionMessageKeys.HAND_OUT_OF_VIEW;
    }
    //-Left (check điểm cổ tay phải cách cạnh frame ít nhất 1 nửa so với khoảng cách cổ tay đến khủy tay)
    const leftVal = (1.0 - leftWrist.x) / dLeftHand;
    if (leftVal < options.minHandToLeftRate) {
      return DetectionMessageKeys.LEFT_HAND_OUT_OF_VIEW;
    }
    //-Right (check điểm cổ tay phải cách cạnh frame ít nhất 1 nửa so với khoảng cách cổ tay đến khủy tay)
    const rightVal = rightWrist.x / dRightHand;
    if (rightVal < options.minHandToLeftRate) {
      return DetectionMessageKeys.RIGHT_HAND_OUT_OF_VIEW;
    }
  }
  // Lower Front/Back
  if (
    bodyPart === CaptureBodyPart.LOWER_FRONT ||
    bodyPart === CaptureBodyPart.LOWER_BACK
  ) {
    //-Detect if legs are 30 degrees: đo góc giữa điểm giữa của hai điểm hông và hai điểm mắt cá chân. Perfect là 25-35 độ.
    const midHips = mid(leftHip, rightHip);
    const angel = calcAngel2D(leftAnkle, midHips, rightAnkle);
    if (angel < options.minLegAngel) {
      return DetectionMessageKeys.ANGEL_LEGS_TOO_SMALL;
    }
    if (angel > options.maxLegAngel) {
      return DetectionMessageKeys.ANGEL_LEGS_TOO_LARGE;
    }
    //-Up(check nếu hai điểm hông xuất hiện)
    if (!isHips) {
      return DetectionMessageKeys.HIPS_OUT_OF_VIEW;
    }
    //-Down(Check nếu điểm mắt cá cách cạnh dưới của frame bằng ít nhất 1 nửa khoảng cách từ gối đến mắt cá)
    const dLeftAnkle = distance(leftKnee, leftAnkle);
    const dRightAnkle = distance(rightKnee, rightAnkle);
    const downVal = Math.min(
      (1.0 - leftAnkle.y) / dLeftAnkle,
      (1.0 - rightAnkle.y) / dRightAnkle
    );
    if (downVal < options.minAnkleToBottomRate) {
      return DetectionMessageKeys.FEET_OUT_OF_VIEW;
    }
    //-Left (check điểm hông phải cách cạnh frame ít nhất bằng 1.2 lần khoảng cách hông đến đầu gối)
    const dLeftHipKnee = distance(leftHip, leftKnee);
    const leftVal = (1.0 - leftHip.x) / dLeftHipKnee;
    if (leftVal < options.minHipToLeftRate) {
      return DetectionMessageKeys.LEFT_LEG_OUT_OF_VIEW;
    }
    //-Right  (check điểm hông trái cách cạnh frame ít nhất bằng 1.2 lần khoảng cách hông đến đầu gối)
    const dRightHipKnee = distance(rightHip, rightKnee);
    const rightVal = rightHip.x / dRightHipKnee;
    if (rightVal < options.minHipToRightRate) {
      return DetectionMessageKeys.RIGHT_LEG_OUT_OF_VIEW;
    }
  }
  return DetectionMessageKeys.PERFECT;
};
