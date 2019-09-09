package hr.brajdic.tkd.tkdclient

//import com.beust.klaxon.Json
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
//import com.google.gson.annotations.Expose
//import com.google.gson.annotations.SerializedName
import java.text.DecimalFormat
import java.util.*

enum class Mode {
    INCREMENT,
    DECREMENT,
    LEFT,
    RIGHT
}

data class Scores(
    /*@Json(name = "Accuracy")*/
    //@Expose @SerializedName("Accuracy")
    @JsonProperty("Accuracy") var left: Int,
    //@Json(name = "Presentation")
    //@Expose @SerializedName("Presentation") var right: Int)
	@JsonProperty("Presentation") var right: Int) {

    //@Json(ignored = true)
    @get:JsonIgnore
    var initL = left
    //@Json(ignored = true)
    @get:JsonIgnore
    var initR = right
    //@Json(ignored = true)
    @get:JsonIgnore
    var stackL = Stack<Int>()
    //@Json(ignored = true)
    @get:JsonIgnore
    var stackR = Stack<Int>()

    private fun inc(side: Mode): Int {
        val value: Int
        if (side === Mode.LEFT) {
            left = popStack(side)
            value = left
        }
        else {
            right = popStack(side)
            value = right
        }
        return value
    }

    private fun dec(dec: Int, side: Mode): Int {
        val stack = if (side === Mode.LEFT) stackL else stackR
        val value: Int
        if (side === Mode.LEFT) {
            if (left != 0) {
                stack.push(left)
                left -= if (dec <= left) dec else left
            }
            value = left
        }
        else {
            if (right != 0) {
                stack.push(right)
                right -= if (dec <= right) dec else right
            }
            value = right
        }
        return value
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
        left = initL
        right = initR
    }
}