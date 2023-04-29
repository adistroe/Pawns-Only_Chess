package chess

//  the foot-soldier of chess
data class Pawn(val color: Color, var position: Pair<Char, Int>) {
    /**
     *  Returns the possible moves of a pawn: forward and diagonally left/right
     */
    fun getPossibleMoves(): List<Pair<Char, Int>> {
        val file = position.file()
        val rank = position.rank()
        val files = 'a'..'h'
        val ranks = 1..8
        return when (color) {
            Color.WHITE -> listOf(Pair(file - 1, rank + 1), Pair(file, rank + 1), Pair(file + 1, rank + 1))
            Color.BLACK -> listOf(Pair(file - 1, rank - 1), Pair(file, rank - 1), Pair(file + 1, rank - 1))
          //  checking for out-of-bounds and returning only the valid moves
        }.filter { pair -> pair.file() in files && pair.rank() in ranks }
    }
}