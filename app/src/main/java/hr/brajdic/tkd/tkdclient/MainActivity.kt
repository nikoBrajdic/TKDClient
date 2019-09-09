package hr.brajdic.tkd.tkdclient

import android.os.Bundle
import android.os.Message
import android.os.Messenger
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var instance: MainActivity
    }

    private lateinit var messenger: Messenger

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "Entering onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(TAG, "Content view set")

        instance = this
        Log.i(TAG, "stored MainActivity to instance")

        messenger = Messenger(MessageHandler)
        Log.i(TAG, "Initialised messenger")

        allScores.text = ""
        statusBar.text = getString(R.string.disconnected)
        Log.i(TAG, "allscores and statusbar set")

        decLSm.setOnClickListener {
            messenger.send(Message().apply { obj = """{"Type":"set_score","Message":"SDL"}""" })
        }
        Log.i(TAG, "declsm onclicklistener set")

        decLBg.setOnClickListener {
            messenger.send(Message().apply { obj = """{"Type":"set_score","Message":"BDL"}""" })
        }
        Log.i(TAG, "declbg onclicklistener set")

        undoL.setOnClickListener {
            messenger.send(Message().apply { obj = """{"Type":"set_score","Message":"UL"}""" })
        }
        Log.i(TAG, "undol onclicklistener set")

        decRSm.setOnClickListener {
            messenger.send(Message().apply { obj = """{"Type":"set_score","Message":"SDR"}""" })
        }
        Log.i(TAG, "decrsm onclicklistener set")

        decRBg.setOnClickListener {
            messenger.send(Message().apply { obj = """{"Type":"set_score","Message":"BDR"}""" })
        }
        Log.i(TAG, "decrbg onclicklistener set")

        undoR.setOnClickListener {
            messenger.send(Message().apply { obj = """{"Type":"set_score","Message":"UR"}""" })
        }
        Log.i(TAG, "undor onclicklistener set")

        lock.setOnClickListener {
            messenger.send(Message().apply { obj = """{"Type":"confirm"}""" })
        }
        Log.i(TAG, "lock onclicklistener set")
    }

    override fun onResume() {
        Log.i(TAG, "Entering onResume")

        super.onResume()
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        // This work only for android 4.4+

        window.decorView.systemUiVisibility = flags
        Log.i(TAG, "layout flags set")
        // Code below is to handle presses of Volume up or Volume down.
        // Without this, after pressing volume buttons, the navigation bar will
        // show up and won't hide
        window.decorView.apply {
            this.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    this.systemUiVisibility = flags
                }
            }
        }
        Log.i(TAG, "layout flags on visibility change set")


        actionBar?.hide()
        Log.i(TAG, "action bar hidden")

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        Log.i(TAG, "window focus changed, visibility flags reset")

    }
}
