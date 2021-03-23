//
//  ViewController.swift
//  ios
//
//  Created by Lukasz Klich on 17/03/2021.
//

import UIKit
import pubnub_kotlin_mp

class ViewController: UIViewController {
    
    //MARK: Properties
    @IBOutlet weak var publishButton: UIButton!
    @IBOutlet weak var messagesTextView: UITextView!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Do any additional setup after loading the view.
    }

    @IBAction func touchDown(_ sender: Any) {
        let config = Configuration(subscribeKey: "***REMOVED***", publishKey: "***REMOVED***", host: "ps.pndsn.com", logVerbosity: PNLogVerbosity.body)
        let pubnub = PubNub(configuration: config)
        let res = pubnub.publish(channel: "testChannel", message: Message(payload: "Hello world"))
        res.receive { (it, err) in
            if let result = it as? PublishResult {
                self.messagesTextView.insertText(String(describing: result.timetoken))
            }
        
        }
        
    }    
}
