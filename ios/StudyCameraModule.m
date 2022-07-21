//
//  StudyCameraModule.m
//  StudyCamera
//
//  Created by Sea Solutions on 12/07/2022.
//  Copyright © 2022 Facebook. All rights reserved.
//

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(StudyCameraModule, NSObject)
RCT_EXTERN_METHOD(deleteCaches:(NSString*)subFolder)

RCT_EXTERN_METHOD(getCachedFile:(NSString*)subFolder bodyPart:(NSInteger)bodyPart isThumb:(BOOL)isThumb resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter)

RCT_EXTERN_METHOD(getCachedFilePath:(NSString*)subFolder bodyPart:(NSInteger)bodyPart isThumb:(BOOL)isThumb resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter)

RCT_EXTERN_METHOD(hasCachedFiles:(BOOL)onlyCheckOrigin resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter)
@end
