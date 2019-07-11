package hr.brajdic.tkd.tkdclient

import android.os.Bundle
import android.os.Message
import android.os.Messenger
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var instance: MainActivity
    }

    private lateinit var messenger: Messenger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        actionBar?.hide()

        instance = this
        messenger = Messenger(MessageHandler)

        allScores.text = ""
        statusBar.text = getString(R.string.disconnected)
        decLSm.setOnClickListener {
            messenger.send(Message().apply { obj = """{"Type":"set_score","Message":"SDL"}""" })
        }
        decLBg.setOnClickListener {
            messenger.send(Message().apply { obj = """{"Type":"set_score","Message":"BDL"}""" })
        }
        undoL.setOnClickListener {
            messenger.send(Message().apply { obj = """{"Type":"set_score","Message":"UL"}""" })
        }
        decRSm.setOnClickListener {
            messenger.send(Message().apply { obj = """{"Type":"set_score","Message":"SDR"}""" })
        }
        decRBg.setOnClickListener {
            messenger.send(Message().apply { obj = """{"Type":"set_score","Message":"BDR"}""" })
        }
        undoR.setOnClickListener {
            messenger.send(Message().apply { obj = """{"Type":"set_score","Message":"UR"}""" })
        }
        lock.setOnClickListener {
            messenger.send(Message().apply { obj = """{"Type":"scores"}""" })
        }
    }
}
