package hr.brajdic.tkd.tkdclient

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color.*
import android.os.*
import android.text.Html
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.beust.klaxon.Klaxon
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.net.NetworkInterface
import java.util.concurrent.TimeUnit

object MessageHandler : Handler(Looper.getMainLooper()) {

    private val context: MainActivity
    get() = MainActivity.instance

    private val parser = Klaxon()
    private var id = 0
    private val warningToast: Toast = makeToast(RED, WHITE)!!
    private val infoToast: Toast = makeToast(LTGRAY, BLACK)!!
    private var scores: Scores = Scores(40, 60)
    private val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .build()

    private var ws = connect()

    private val macAddress
        get() = NetworkInterface.getNetworkInterfaces()
            ?.toList()
            ?.first { it.name.toLowerCase() == "wlan0" }
            ?.hardwareAddress
            ?.joinToString(":") { Integer.toHexString(it.toInt() and 0xFF) }
            .also { print(it ?: "MAC address not found!") }

    private val battery
        get() = with(context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))!!) {
            (100f * getIntExtra(BatteryManager.EXTRA_LEVEL, -1) /
                    getIntExtra(BatteryManager.EXTRA_SCALE, -1))
                    .toInt()
        }

    override fun handleMessage(msg: Message) {
        val packet = msg.obj as String
        val inbound = parser.parse<InboundPacket>(packet)!!
        try {
            when (inbound.type) {
                "open" -> {
                    ws.send(OutboundPacket.instructions("connect_request", mac = macAddress, id = id))
                }
                "hello" -> {
                    id = inbound.id ?: id
                    ws.send(OutboundPacket.instructions("hello", id = id))
                    val idText = String.format("%s%s", context.getString(R.string.received), inbound.id)
                    context.statusBar.text = idText
                    infoToast.apply { setText(idText) }.show()
                    vibrate(200)
                }
                "battery" -> if (inbound.battery != null && inbound.battery) {
                    ws.send(OutboundPacket.instructions("battery", battery = battery))
                }
                "init" -> {
                    if (inbound.setScores != null) scores = inbound.setScores else scores.init()
                    with(scores) {
                        with (context) {
                            scoreL.text = (lScore / 10f).toString()
                            scoreR.text = (rScore / 10f).toString()
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
                "scores" -> {
                    ws.send(OutboundPacket.instructions("score", battery = battery, scores = scores))
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
                                        when (true) {
                                            i + 1 == id && remove -> String.format(bfgFormat, res)
                                            i + 1 == id -> String.format(bgFormat, res)
                                            remove -> String.format(fgFormat, res)
                                            else -> res
                                        }
                                    }.joinToString(" ")
                                }

                        @Suppress("DEPRECATION")
                        context.allScores.text = Html.fromHtml(span)
                    }
                }
                "idle" -> {
                    if (inbound.idle != null) {
                        flipButtonsEnabled(!inbound.idle)
                        listOf(context.scoreL, context.scoreR).forEach {
                            it.visibility = if (inbound.idle) INVISIBLE else VISIBLE
                        }
                        context.allScores.text = ""
                    }
                    if (!inbound.message.isNullOrEmpty()) {
                        infoToast.apply { setText(inbound.message) }.show()
                        vibrate(200)
                    }
                }
                "toast" -> {
                    infoToast.apply { setText(inbound.message) }.show()
                    if (inbound.vibrate != null && inbound.vibrate) {
                        repeat(3) {
                            vibrate(150)
                            Thread.sleep(200)
                        }
                    }
                }
                "disconnect" -> {
                    reconnect(NORMAL_CLOSURE_STATUS, "referee released", 30)
                }
                "kill" -> if (inbound.off != null && inbound.off) {
                    ws.close(NORMAL_CLOSURE_STATUS, "")
                    context.finish()
                }
                "failure" -> {
                    reconnect(NORMAL_CLOSURE_STATUS, inbound.message ?: "", 5)
                    context.statusBar.text = context.getString(R.string.disconnected)
                    warningToast.apply { setText(inbound.message) }.show()
                    vibrate()
                }
                "closing" -> {
                    ws.send(OutboundPacket.instructions("Goodbye!"))
                    ws.close(NORMAL_CLOSURE_STATUS, inbound.message)
                }
            }
        } catch (ex: Exception) {
            warningToast.apply { this.setText(ex.message) }.show()
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

    private fun makeToast(bgColor: Int,
                          fgColor: Int,
                          tSize: Float = 30f,
                          duration: Int = Toast.LENGTH_LONG) = Toast.makeText(context, null, duration).apply {
        with(view as ViewGroup) {
            setBackgroundColor(bgColor)
            with(getChildAt(0) as TextView) {
                textSize = tSize
                setBackgroundColor(bgColor)
                setTextColor(fgColor)
            }
        }
    }

    private fun reconnect(code: Int, reason: String, wait: Int = 0) {
        if (wait > 0) Thread.sleep(wait * 1000L)
        ws.close(code, reason)
        ws = connect()
    }

    private fun connect(): WebSocket = Request.Builder().url("ws://192.168.5.12:8088/TKD").build().let {
        client.newWebSocket(it, TKDClient)
    }


    private fun setScores(d: Int, side: Mode, mode: Mode) = run {
        if (side === Mode.LEFT) context.scoreL else context.scoreR
    }.apply {
        text = scores.changeValue(d, side, mode)
        sendScores()
    }

    private fun sendScores() = scores.let {
        OutboundPacket.instructions("score", battery = battery, scores = it)
    }.also {
        ws.send(it)
    }

    private fun flipButtonsEnabled(flip: Boolean) {
        context.decRBg.isEnabled = flip
        context.decRSm.isEnabled = flip
        context.decLBg.isEnabled = flip
        context.decLSm.isEnabled = flip
        context.undoL.isEnabled = flip
        context.undoR.isEnabled = flip
        context.lock.isEnabled = flip
    }
}
