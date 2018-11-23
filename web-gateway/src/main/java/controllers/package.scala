import play.api.i18n.MessagesProvider
import play.i18n.Messages
import views.html.foundationFieldConstructor
import views.html.helper.FieldConstructor

/**
  * Created by jroper on 27/09/16.
  */
package object controllers {
  implicit def fieldConstructor: FieldConstructor = FieldConstructor(foundationFieldConstructor.apply)

  implicit def navToMessages(nav: Nav): Messages = {
    println("converted")
    nav.messages()
  }

  implicit def message(key: String, args: Any*)(implicit messagesProvider: MessagesProvider): String = {
    messagesProvider.messages.apply(key, args: _*)
  }
}
