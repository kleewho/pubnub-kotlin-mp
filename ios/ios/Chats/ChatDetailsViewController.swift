import UIKit

class ChatDetailsViewController: UITableViewController {
    @IBOutlet weak var nameTextField: UITextField!
    var chat: Chat?
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "SaveChatDetail", let chatName = nameTextField.text {
           chat = Chat(name: chatName, image: "")
        }
        
    }
    
}

