package chess

//  white player moves first
data class Player(val name: String, val color: Color) {
    var lastMove: String = ""
}