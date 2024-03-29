package hr.brajdic.tkd.tkdclient

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.Color.*
import android.os.*
import android.os.BatteryManager.EXTRA_SCALE
import android.os.BatteryManager.EXTRA_LEVEL
import android.provider.Settings.Global.getString
import android.text.Html
import android.util.Log
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
//import com.beust.klaxon.Klaxon
//import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.io.File
import java.net.NetworkInterface
import java.util.*
import java.util.concurrent.TimeUnit

object MessageHandler : Handler() {

    private val parser = ObjectMapper().registerModule(KotlinModule())
    private var id = 0.also { Log.i("MessageHandler", "initialising id") }
    private val warningToast: Toast = makeToast(RED, WHITE, Toast.LENGTH_SHORT).also { Log.i("MessageHandler", "initialising warningtoast") }
    private val infoToast: Toast = makeToast(LTGRAY, BLACK).also { Log.i("MessageHandler", "initialising infoToast") }
    private var scores: Scores = Scores(40, 60).also { Log.i("MessageHandler", "initialising scores") }
    private val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .build().also { Log.i("MessageHandler", "initialising httpclient") }

    private var ws = connect().also { Log.i("MessageHandler", "initialising ws client") }

    private val mainThread = Handler(Looper.getMainLooper())

    private val context: MainActivity
        get() = MainActivity.instance

    private val macAddress
        get() = NetworkInterface.getNetworkInterfaces()
            ?.toList()
            ?.first { it.name.toLowerCase(Locale.ROOT) == "wlan0" }
            ?.hardwareAddress
            ?.joinToString(":") { Integer.toHexString(it.toInt() and 0xFF) }
            .also { Log.i(TAG, it ?: "MAC address not found!") }

    private val battery
        get() = with(context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))!!) {
            (100f * getIntExtra(EXTRA_LEVEL, -1) /
                    getIntExtra(EXTRA_SCALE, -1))
                    .toInt()
        }

    private const val TAG = "MessageHandler"
    private const val NORMAL_CLOSURE_STATUS = 1000

    override fun handleMessage(msg: Message) {
        Log.i(TAG, "entering handleMessage")
        try {
            Log.i(TAG, "parsing inbound packet")
            val inbound = parser.readValue<InboundPacket>(msg.obj as String)
            Log.i(TAG, "inbound packet parsed: ${msg.obj}")
            when (inbound.type) {
                "open" -> {
                    Handler().post {
                        ws.send(OutboundPacket.instructions("connect_request", mac = macAddress, id = id))
                    }
                    Log.i(TAG, "Sent connect_request to server")
                }
                "hello" -> {
                    id = inbound.id ?: id
                    ws.send(OutboundPacket.instructions("hello", id = id))
                    val idText = String.format("%s%s", context.getString(R.string.received), id)
                    mainThread.post {
                        context.statusBar.text = idText
                        infoToast.apply { setText(idText) }.show()
                    }
                    vibrate(200)
                }
                "battery" -> if (inbound.battery != null && inbound.battery) {
                    ws.send(OutboundPacket.instructions("battery", battery = battery))
                }
                "init" -> {
                    if (inbound.setScores != null) scores = inbound.setScores else scores.init()
                    with (context) {
                        mainThread.post {
                            scoreL.text = (scores.left / 10f).toString()
                            scoreR.text = (scores.right / 10f).toString()
                            scoreL.visibility = VISIBLE
                            scoreR.visibility = VISIBLE
                        }
                    }
                }
                "set_score" -> {
                    when (inbound.message) {
                        "SDL" -> setScores( 1, Mode.LEFT , Mode.DECREMENT)
                        "SDR" -> setScores( 1, Mode.RIGHT, Mode.DECREMENT)
                        "BDL" -> setScores( 3, Mode.LEFT , Mode.DECREMENT)
                        "BDR" -> setScores( 3, Mode.RIGHT, Mode.DECREMENT)
                        "UL"  -> setScores( 0, Mode.LEFT , Mode.INCREMENT)
                        "UR"  -> setScores( 0, Mode.RIGHT, Mode.INCREMENT)
                    }
                }
                "all_scores" -> {
                    if (!inbound.message.isNullOrEmpty()) {
                        val fg = "#ff1f1f"
                        val bg = "#203d00"
                        val fgAttr = "color: $fg;"
                        val bgAttr = "background-color: $bg;"
                        val spanFormat = """<span style="%s %s">%%s</span>"""
                        val bgFormat = String.format(spanFormat, bgAttr, "")
                        val fgFormat = String.format(spanFormat, fgAttr, "")
                        val bfgFormat = String.format(spanFormat, fgAttr, bgAttr)

                        val span = inbound.message
                                .split("::")
                                .map { it.split(" ") }
                                .joinToString("\n") {
                                    it.mapIndexed { i, str ->
                                        val remove = str.endsWith('R')
                                        val res = if (remove) str.trim('R') else str
                                        when {
                                            i + 1 == id && remove -> String.format(bfgFormat, res)
                                            i + 1 == id -> String.format(bgFormat, res)
                                            remove -> String.format(fgFormat, res)
                                            else -> res
                                        }
                                    }.joinToString(" ")
                                }

                        mainThread.post {
                            @Suppress("DEPRECATION")
                            context.allScores.text = Html.fromHtml(span)
                        }
                    }
                }
                "idle" -> {
                    if (inbound.idle != null) {
                        mainThread.post {
                            flipButtonsEnabled(!inbound.idle)
                            listOf(context.scoreL, context.scoreR).forEach {
                                it.visibility = if (inbound.idle) INVISIBLE else VISIBLE
                            }
                            context.allScores.text = ""
                        }
                    }
                    if (!inbound.message.isNullOrEmpty()) {
                        mainThread.post {
                            infoToast.apply { setText(inbound.message) }.show()
                            vibrate(200)
                        }
                    }
                }
                "confirm" -> {
                    ws.send(OutboundPacket.instructions("confirm", battery = battery, scores = scores))
                }
                "toast" -> {
                    mainThread.post {
                        infoToast.apply { setText(inbound.message) }.show()
                        if (inbound.vibrate != null && inbound.vibrate) {
                            repeat(3) {
                                vibrate(150)
                                Thread.sleep(200)
                            }
                        }
                    }
                }
                "disconnect" -> {
                    ws.close(NORMAL_CLOSURE_STATUS, "referee $id released")
                }
                "kill" -> if (inbound.off != null && inbound.off) {
                    ws.close(NORMAL_CLOSURE_STATUS, "referee killed by server")
                    mainThread.post {
                        context.finish()
                    }
                }
                "failure" -> {
                    reconnect(NORMAL_CLOSURE_STATUS, inbound.message ?: "")
                    mainThread.post {
                        context.statusBar.text = context.getString(R.string.disconnected)
                        warningToast.apply { setText(inbound.message) }.show()
                        vibrate()
                    }
                    Log.i(TAG, "failed to connect: " + inbound.message)
                }
                "closing" -> {
                    ws.send(OutboundPacket.instructions("Goodbye!"))
                    ws.close(NORMAL_CLOSURE_STATUS, inbound.message)
                    Log.i(TAG, "closing connection: " + inbound.message)
                }
            }
        } catch (ex: Exception) {
            mainThread.post {
                warningToast.apply { setText(ex.message) }.show()
            }
            ex.printStackTrace()
        }
    }

    private fun vibrate(milliseconds: Long = 500) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        milliseconds,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                // API level <= 25
                @Suppress("DEPRECATION")
                vibrator.vibrate(milliseconds)
            }
        }
    }

    private fun makeToast(bgColor: Int, fgColor: Int, duration: Int = Toast.LENGTH_LONG, tSize: Float = 30f) =
        Toast.makeText(context, null, duration).apply {
            with(view as ViewGroup) {
                setBackgroundColor(bgColor)
                with(getChildAt(0) as TextView) {
                    textSize = tSize
                    setBackgroundColor(bgColor)
                    setTextColor(fgColor)
                }
            }
        }

    private fun reconnect(code: Int, reason: String, wait: Int = 2) {
        Thread.sleep(wait * 1000L)
        ws.close(code, reason)
        ws = connect()
    }

    private fun ipFromFile(): String {
        val fileName = context.getString(R.string.configFileName)
        val file = File(Environment.getExternalStorageDirectory(), fileName)
        val ip = parser.readValue<Settings>(file.readText()).ip
        return "ws://$ip:8088/TKD"
    }

    private fun connect(): WebSocket = Request.Builder().url(ipFromFile()).build().let {
        client.newWebSocket(it, TKDClient)
    }.also { Log.i(TAG, "WebSocket client connected") }


    private fun setScores(d: Int, side: Mode, mode: Mode) = run {
        if (side === Mode.LEFT) context.scoreL else context.scoreR
    }.apply {
        text = scores.changeValue(d, side, mode)
        sendScores()
    }

    private fun sendScores() = scores.let {
        OutboundPacket.instructions("score", battery = battery, scores = scores)
    }.also {
        ws.send(it)
    }

    private fun flipButtonsEnabled(flip: Boolean) = with (context) {
        decRBg.isEnabled = flip
        decRSm.isEnabled = flip
        decLBg.isEnabled = flip
        decLSm.isEnabled = flip
        undoL.isEnabled = flip
        undoR.isEnabled = flip
        lock.isEnabled = flip
    }

}
