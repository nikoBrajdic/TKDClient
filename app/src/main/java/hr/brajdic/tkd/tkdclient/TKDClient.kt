package hr.brajdic.tkd.tkdclient

import android.os.Message
import android.os.Messenger
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener


const val NORMAL_CLOSURE_STATUS = 1000

object TKDClient : WebSocketListener() {

    private val messenger = Messenger(MessageHandler)

    override fun onOpen(webSocket: WebSocket, response: Response) {
        messenger.send(Message().apply { obj = """{"Type":"open"}"""})
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        messenger.send(Message().apply { obj = text })
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        messenger.send(Message().apply { obj = """{"Type":"closing","Message":"$reason"}""" })
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        messenger.send(Message().apply { obj = """{"Type":"failure","Message":"${t.message}"}"""})
    }

}