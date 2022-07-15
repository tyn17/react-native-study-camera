#import "React/RCTViewManager.h"

@interface RCT_EXTERN_MODULE(StudyCameraViewManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(bodyPart, NSInteger)

RCT_EXPORT_VIEW_PROPERTY(subFolder, NSString)

RCT_EXPORT_VIEW_PROPERTY(visualMask, BOOL)

RCT_EXPORT_VIEW_PROPERTY(detectionMode, NSInteger)

RCT_EXPORT_VIEW_PROPERTY(usePortraitScene, BOOL)

RCT_EXPORT_VIEW_PROPERTY(useBackCamera, BOOL)

//RCT_EXPORT_VIEW_PROPERTY(onCaptured, RCTBubblingEventBlock*)
//
//RCT_EXPORT_VIEW_PROPERTY(onDetected, RCTBubblingEventBlock*)

RCT_EXTERN_METHOD(resumeCamera)

RCT_EXTERN_METHOD(pauseCamera)

RCT_EXTERN_METHOD(capturePhoto)

@end
