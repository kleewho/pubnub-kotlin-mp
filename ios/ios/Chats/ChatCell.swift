//
//  ChatCell.swift
//  ios
//
//  Created by Lukasz Klich on 18/03/2021.
//

import UIKit

struct Chat {
    let name: String
    let image: String
}

class ChatCell: UITableViewCell {
    
    @IBOutlet weak var chatLabel: UILabel!
    @IBOutlet weak var chatImageView: UIImageView!
    @IBOutlet weak var openChatButton: UIButton!
    
    // 1:
    var chat: Chat? {
      didSet {
        guard let chat = chat else { return }

        chatLabel.text = chat.name
        chatImageView.image = UIImage(named: chat.image)
      }
    }
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }
    
    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

}
