package chess

//  override functions to make code more readable
fun Pair<Char, Int>.file(): Char = first
fun Pair<Char, Int>.rank(): Int = second
fun Pair<Char, Int>.toStringNotation(): String = "$first$second"

//  Can't do without it
fun main() {
    Chess().run()
}