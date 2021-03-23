import react.dom.render
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.id
import react.dom.div

fun main() {
    window.onload = {
        render(document.getElementById("root")) {
            child(Welcome::class) {
                attrs {
                    message = ""
                }
            }

            div {
                attrs {
                    this.id = "received"
                }
            }
        }
    }
}
