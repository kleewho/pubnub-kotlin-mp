

import UIKit

class ChatsDataSource {
    var chats: [Chat] = [Chat(name: "testChannel", image: "image")]
    
    func numberOfChats() -> Int {
        return chats.count
    }
    
    func chat(at: IndexPath) -> Chat? {
        return chats[at.item]
    }
    
    func chat(at: Int) -> Chat? {
        return chats[at]
    }
    
    func append(chat: Chat, to: UITableView) {
        chats.append(chat)
        to.reloadData()
    }
}

class ChatsViewController: UITableViewController {
    let chatsDataSource = ChatsDataSource()
    var currentChat: Chat? = nil
}


extension ChatsViewController {
  override func tableView(
    _ tableView: UITableView,
    numberOfRowsInSection section: Int
  ) -> Int {
    chatsDataSource.numberOfChats()
  }

  override func tableView(
    _ tableView: UITableView,
    cellForRowAt indexPath: IndexPath
  ) -> UITableViewCell {
    let cell = tableView.dequeueReusableCell(
      withIdentifier: "ChatCell",
      for: indexPath) as! ChatCell
    cell.chat = chatsDataSource.chat(at: indexPath)

    return cell
  }
}

extension ChatsViewController {
  @IBAction func cancelToChatsViewController(_ segue: UIStoryboardSegue) {
  }

  @IBAction func saveChatDetail(_ segue: UIStoryboardSegue) {
    guard
      let chatDetailsViewController = segue.source as? ChatDetailsViewController,
      let chat = chatDetailsViewController.chat
      else {
        return
    }
    chatsDataSource.append(chat: chat, to: tableView)
  }
}

extension ChatsViewController {
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let chatViewController = segue.destination as? ChatViewController {
            chatViewController.chatsDataSource = chatsDataSource
        }
    }
}
