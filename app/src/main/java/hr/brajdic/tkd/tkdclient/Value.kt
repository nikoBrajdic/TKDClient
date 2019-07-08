package hr.brajdic.tkd.tkdclient

class Value(private var v: Int) {
    operator fun plus(i: Int) = v + i
    operator fun minus(i: Int) = if (v - i < 0) 0 else v - i
    operator fun times(i: Int) = v * i
    operator fun div(i: Int) = v / i
    operator fun div(i: Float): Double = (v.toFloat() / i).toDouble()
    operator fun rem(i: Int) = v % i
    operator fun compareTo(i: Int) = v.compareTo(i)
    operator fun plusAssign(i: Int) { v += i }
    operator fun minusAssign(i: Int) { v = if (v - i < 0) 0 else v - i }
    operator fun timesAssign(i: Int) { v *= i }
    operator fun divAssign(i: Int) { v /= i }
    operator fun remAssign(i: Int) { v %= i }
    operator fun unaryPlus() = +v
    operator fun unaryMinus() = -v
    override fun toString(): String = v.toString()

    val int: Int
    get() = v

    infix fun set(i :Int): Value = this.apply { v = i }
    infix fun neq(i :Int) = v != i
}