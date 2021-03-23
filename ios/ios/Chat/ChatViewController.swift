import UIKit
import pubnub_kotlin_mp

func dateFormatter() -> DateFormatter {
    let df = DateFormatter()
    df.dateFormat = "yyyy-MM-dd hh:mm:ss"
    return df
    
}

class ChatViewController: UIViewController {
    let df = dateFormatter()
    
    @IBOutlet weak var pubTextField: UITextField!
    @IBOutlet weak var pubButton: UIButton!
    @IBOutlet weak var messagesTextView: UITextView!
    
    var chat: Chat? = nil
    var chatsDataSource: ChatsDataSource? = nil
    let pubnub = PubNub(configuration: Configuration(subscribeKey: "***REMOVED***", publishKey: "***REMOVED***", host: "https://ps.pndns.com", logVerbosity: PNLogVerbosity.body))
    
    @IBAction func publishAction(_ sender: Any) {
        chat = chatsDataSource?.chat(at: 0)
        if let forSureChat = chat {
            let message = Message(payload: pubTextField.text ?? "")
            pubTextField.text = ""
            pubnub.publish(channel: forSureChat.name, message: message)
        }

    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        sendDataToServer()
        // Do any additional setup after loading the view.
    }

    func sendDataToServer() {
       // Perform the task on a background queue.
        let chat = self.chatsDataSource?.chat(at: 0)
        if let forSureChat = chat {
            let subscription = self.pubnub.subscribe(channel: forSureChat.name)
            subscription.forEach { data in
                let date = Date(timeIntervalSince1970: TimeInterval(data.timetoken/10000000))
                let message = UIMessage(timestamp: self.df.string(from: date), content: data.payload.content, user: data.publisher)
                
                    DispatchQueue.main.async{
                        self.messagesTextView.insertText("\(message.timestamp) \(message.user):\(String(repeating: " ", count: 10 - message.user.count)) \(message.content)\n\n")
                    }
            }
        }
    }
    
}

