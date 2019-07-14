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
    @Json(name = "Accuracy") val left: Int,
    @Json(name = "Presentation") val right: Int) {

    private val initL = left
    private val initR = right
    private var valL = Value(left)
    private var valR = Value(right)
    private val stackL = Stack<Int>()
    private val stackR = Stack<Int>()
    val lScore get() = valL.int
    val rScore get() = valR.int

    private fun inc(side: Mode): Value = run {
        if (side === Mode.LEFT) valL else valR
    }.apply {
        this set popStack(side)
    }

    private fun dec(dec: Int, side: Mode): Value = run {
        if (side === Mode.LEFT) stackL to valL else stackR to valR
    }.let { (s, v) ->
        if (v neq 0) v set (s.push(v.int) - dec) else v
    }

    private fun popStack(side: Mode): Int = run {
        if (side === Mode.LEFT) stackL to initL else stackR to initR
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