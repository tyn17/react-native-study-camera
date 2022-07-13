//
//  StudyCameraView.swift
//  StudyCamera
//
//  Created by Sea Solutions on 13/07/2022.
//  Copyright © 2022 Facebook. All rights reserved.
//
import UIKit
import CoreGraphics
class StudyCameraView : UIView {
    var textureView: UIView!
    var label: UILabel!
    
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
        
        //Label
        self.label = UILabel()
        self.label.translatesAutoresizingMaskIntoConstraints = false
        self.textureView.addSubview(self.label)
        self.label.text = "The bodyPart: \(bodyPart)"
        self.label.textColor = UIColor.white
        self.label.font = UIFont.systemFont(ofSize: 14)
        let centerX = self.label!.centerXAnchor.constraint(equalTo: self.textureView!.centerXAnchor)
        let centerY = self.label!.centerYAnchor.constraint(equalTo: self.textureView.centerYAnchor)
        self.textureView!.addConstraints([centerX, centerY])
    }
    
    @objc var bodyPart: Any = 1 {
        didSet {
            print(bodyPart)
        }
    }
//    @objc var subFolder: String = ""
//    @objc var visualMask: Bool = true
//    @objc var detectionMode: Int = DetectionMode.NONE.rawValue
//    @objc var usePortraitScene: Bool = false
//    @objc var useBackCamera: Bool = true
//    @objc var onCaptured: RCTBubblingEventBlock? = nil
//    @objc var onDetected: RCTBubblingEventBlock? = nil
//
//    @objc var color: String = "" {
//        didSet {
//          self.backgroundColor = hexStringToUIColor(hexColor: color)
//        }
//    }
}

