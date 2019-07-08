package hr.brajdic.tkd.tkdclient

import com.beust.klaxon.Json
import java.text.DecimalFormat
import java.util.*

enum class Mode {
    INCREMENT,
    DECREMENT,
    LEFT,
    RIGHT
}

data class Scores(
        @Json(ignored = true) private val left: Int,
        @Json(ignored = true) private val right: Int) {

    @Json(ignored = true) private val initL = left
    @Json(ignored = true) private val initR = right
    @Json(ignored = true) private var valL = Value(left)
    @Json(ignored = true) private var valR = Value(right)
    @Json(ignored = true) private val stackL = Stack<Int>()
    @Json(ignored = true) private val stackR = Stack<Int>()
    @Json(name = "Accuracy") val lScore get() = valL.int
    @Json(name = "Presentation") val rScore get() = valR.int

    private fun inc(side: Mode): Value = run {
        if (side === Mode.LEFT) valL else valR
    }.apply {
        this set popStack(side)
    }

    private fun dec(dec: Int, side: Mode): Value = run {
        if (side === Mode.LEFT) valL to stackL else valR to stackR
    }.let { (v, s) ->
        if (v neq 0) v set (s.push(v.int) - dec) else v
    }

    private fun popStack(side: Mode): Int = run {
        if (side === Mode.LEFT) stackL to left else stackR to right
    }.let { (s, v) ->
        if (s.empty()) v else s.pop()
    }

    fun changeValue(d: Int, side: Mode, mode: Mode): String = run {
        if (mode === Mode.INCREMENT) inc(side) else dec(d, side)
    }.let {
        DecimalFormat("0.0").format(it / 10f)
    }

    fun init() {
        valL = Value(initL)
        valR = Value(initR)
    }
}