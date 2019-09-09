package hr.brajdic.tkd.tkdclient

import android.os.Message
import android.os.Messenger
import android.util.Log
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener


object TKDClient : WebSocketListener() {

    private val messenger = Messenger(MessageHandler)
    private const val TAG = "TKDClient"

    override fun onOpen(webSocket: WebSocket, response: Response) {
        messenger.send(Message().apply { obj = """{"Type":"open"}"""})
        Log.i(TAG, "passed onOpen message to MessageHandler: {Type:open}")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        messenger.send(Message().apply { obj = text })
        Log.i(TAG, "passed onMessage message to MessageHandler: obj = $text")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        messenger.send(Message().apply { obj = """{"Type":"closing", "Message":"$reason"}""" })
        Log.i(TAG, "passed onClosing message to MessageHandler: {Type:closing, Message:$reason}")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        messenger.send(Message().apply { obj = """{"Type":"failure","Message":"${t.message}"}"""})
        Log.i(TAG, "passed onFailure message to MessageHandler: {Type:failure, Message:${t.message}}")
    }

}