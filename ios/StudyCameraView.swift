//
//  StudyCameraView.swift
//  StudyCamera
//
//  Created by Sea Solutions on 13/07/2022.
//  Copyright © 2022 Facebook. All rights reserved.
//
import UIKit
import AVFoundation

@available(macCatalyst 14.0, *)
@available(iOS 11.0, *)
class StudyCameraView : UIView, AVCapturePhotoCaptureDelegate {
    // These variables will be set from React-Native
    @objc var bodyPart: Int = 0
    @objc var subFolder: String = ""
    @objc var visualMask: Bool = true
    @objc var detectionMode: Int = Constants.DetectionMode.NONE.rawValue
    @objc var usePortraitScene: Bool = false
    @objc var useBackCamera: Bool = true {
        didSet {
            switchCamera()
        }
    }
//    @objc var onCaptured: RCTBubblingEventBlock? = nil
//    @objc var onDetected: RCTBubblingEventBlock? = nil
    
    // Inside variables
    var textureView: UIView!
    var captureSession: AVCaptureSession!
    var stillImageOutput: AVCapturePhotoOutput!
    var videoPreviewLayer: AVCaptureVideoPreviewLayer!
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        initialize()
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        initialize()
    }
    
    private func initialize() {
        self.textureView = UIView()
        self.textureView.backgroundColor = UIColor.red
        self.textureView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(self.textureView)
        
        let topConstrait = NSLayoutConstraint(item: self.textureView!, attribute: NSLayoutConstraint.Attribute.top, relatedBy: NSLayoutConstraint.Relation.equal, toItem: self, attribute: NSLayoutConstraint.Attribute.top, multiplier: 1, constant: 0)
        
        let leftConstrait = NSLayoutConstraint(item: self.textureView!, attribute: NSLayoutConstraint.Attribute.left, relatedBy: NSLayoutConstraint.Relation.equal, toItem: self, attribute: NSLayoutConstraint.Attribute.left, multiplier: 1, constant: 0)
        
        let rightConstrait = NSLayoutConstraint(item: self.textureView!, attribute: NSLayoutConstraint.Attribute.right, relatedBy: NSLayoutConstraint.Relation.equal, toItem: self, attribute: NSLayoutConstraint.Attribute.right, multiplier: 1, constant: 0)
        
        let ratioConstrait = NSLayoutConstraint(item: self.textureView!, attribute: NSLayoutConstraint.Attribute.height, relatedBy: NSLayoutConstraint.Relation.equal, toItem: self.textureView!, attribute: NSLayoutConstraint.Attribute.width, multiplier: 4.0/3.0, constant: 1)
        
        self.addConstraints([topConstrait, leftConstrait, rightConstrait, ratioConstrait])
        
        //self.setupCamera()
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        if (self.videoPreviewLayer != nil && self.videoPreviewLayer.superlayer != nil) {
            self.videoPreviewLayer.frame = self.textureView.bounds
        }
    }
    
    // Setup Camera
    func setupCamera() {
        do {
            //Select camera
            let camera = self.getCaptureDevice()
            //Prepare the Input
            let input = try AVCaptureDeviceInput(device: camera)
            //Config the Output
            self.stillImageOutput = AVCapturePhotoOutput()
            self.stillImageOutput.isHighResolutionCaptureEnabled = true
            
            //Init Session
            self.captureSession = AVCaptureSession()
            self.captureSession.sessionPreset = .photo
            
            if (self.captureSession.canAddInput(input) && self.captureSession.canAddOutput(self.stillImageOutput)) {
                self.captureSession.addInput(input)
                self.captureSession.addOutput(self.stillImageOutput)
                
                //Setup Preview
                self.videoPreviewLayer = AVCaptureVideoPreviewLayer(session: self.captureSession)
                self.videoPreviewLayer.videoGravity = .resizeAspect
                self.videoPreviewLayer.connection?.videoOrientation = .portrait
                self.textureView.layer.addSublayer(self.videoPreviewLayer)
                self.videoPreviewLayer.frame = self.textureView.bounds
                
                //Start the Session on Background thread
                DispatchQueue.global(qos: .userInitiated).async {
                    self.captureSession.startRunning()
                }
            }
        } catch let error {
            print(error)
        }
    }
    
    // Get Device Input
    func getCaptureDevice() -> AVCaptureDevice {
        let position = useBackCamera ? AVCaptureDevice.Position.back : AVCaptureDevice.Position.front
        let devices = AVCaptureDevice.devices(for: AVMediaType.video)
        for item in devices {
            if (item.position == position) {
                return item
            }
        }
        return AVCaptureDevice.default(for: .video)!
    }
    
    // Switch Camera
    func switchCamera() {
        if (self.captureSession != nil) {
            do {
                let position = useBackCamera ? AVCaptureDevice.Position.back : AVCaptureDevice.Position.front
                var input = self.captureSession.inputs[0] as! AVCaptureDeviceInput
                if (input.device.position == position) {
                    return
                }
                
                // Switch Camera if need
                self.captureSession.beginConfiguration()
                // Remove existing input
                self.captureSession.removeInput(input)
                // Add new input
                let device = getCaptureDevice()
                input = try AVCaptureDeviceInput(device: device)
                self.captureSession.addInput(input)
                
                self.captureSession.commitConfiguration()
            }
            catch let err {
                print(err)
            }
        }
    }
    
    // Take a Photo
    func takePhoto() {
        let settings = AVCapturePhotoSettings(format: [
            AVVideoCodecKey: AVVideoCodecType.jpeg
        ])
        settings.flashMode = AVCaptureDevice.FlashMode.on
        settings.isHighResolutionPhotoEnabled = true
        stillImageOutput.capturePhoto(with: settings, delegate: self)
    }
    
    //DELEGATE
    func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
        guard let imageData = photo.fileDataRepresentation()
        else {return}
        
    }
    
    // Resume Camera
    func resumeCamera() {
        if let session = self.captureSession {
            DispatchQueue.global(qos: .userInitiated).async {
                session.startRunning()
            }
        } else {
            DispatchQueue.main.async {
                self.setupCamera()
            }
        }
    }
    
    // Pause Camera
    func pauseCamera() {
        if let session = self.captureSession {
            session.stopRunning()
        }
    }
    
    deinit {
        self.captureSession?.stopRunning()
        self.captureSession = nil
        self.videoPreviewLayer?.removeFromSuperlayer()
        self.videoPreviewLayer = nil
        self.stillImageOutput = nil
    }
}

