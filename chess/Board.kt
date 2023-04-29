package chess

class Board {
    //  standard chess algebraic notation
    private val ranks = (8 downTo 1)
    private val files = ('a'..'h')

    //  white pawns start on rank 2
    private val whitePawns = List(8) { index -> Pawn(Color.WHITE, Pair(files.elementAt(index), 2)) }

    //  black pawns start on rank 7
    private val blackPawns = List(8) { index -> Pawn(Color.BLACK, Pair(files.elementAt(index), 7)) }

    //  all pawns on the board
    val pawns = (whitePawns + blackPawns).toMutableList()

    //  ASCII glyphs for drawing the chess board
    private enum class Draw(val element: String) {
        HORIZONTAL("  +---+---+---+---+---+---+---+---+\n"),
        VERTICAL("|"),
        PADDING(" ")
    }

    /**
     *  Returns the last rank that the pawn must reach to win the game
     */
    fun lastRank(player: Player): Int = when (player.color) {
        Color.WHITE -> 8
        Color.BLACK -> 1
    }

    /**
     *  Checks if there is a pawn on the square and returns 'B' or 'W' depending on pawn color.
     *  If no pawn is found on the square, it returns 'padding'
     */
    private fun getPawnGlyphAt(position: Pair<Char, Int>): Char {
        return try {
            pawns.first { pawn -> pawn.position == position }.color.value.first()
        } catch (e: NoSuchElementException) {
            Draw.PADDING.element.first()
        }
    }

    /**
     *  Checks if there is a pawn on the square and returns it.
     *  If no pawn is found on the square, it returns 'null'
     */
    private fun getPawnAt(position: Pair<Char, Int>): Pawn? {
        return try {
            pawns.first { pawn -> pawn.position == position }
        } catch (e: NoSuchElementException) {
            null
        }
    }

    /**
     *  Checks if the pawn can be moved from 'source' to 'destination' (firstPlayer is moving the pawn)
     *  - pawn exists at 'source' and is of correct color
     *
     *  If pawn moves on the same file:
     *  - pawn moves forward 1 rank or 2 ranks depending on its 'source'
     *  - there is no other pawn at 'destination'
     *
     *  If pawn moves to another file:
     *  - the pawn moves forward diagonally 1 rank and 1 file (rank and file must be adjacent)
     *  - the pawn captures opponent's pawn (directly or 'en passant') if param 'enableCapture' is 'true'
     */
    fun isValidMove(input: String, firstPlayer: Player, secondPlayer: Player, enableCapture: Boolean): Boolean {
        //  convert the chess notation into source and destination coordinate Pairs
        val source = Pair(input[0], input[1].digitToInt())
        val destination = Pair(input[2], input[3].digitToInt())
        val thisPawn = getPawnAt(source)
        val otherPawn = getPawnAt(destination)

        //  no pawn found, or pawn is of wrong color
        if (thisPawn == null || thisPawn.color != firstPlayer.color) {
            if (enableCapture) {
                println(
                    String.format(
                        Message.NO_PAWN.text,
                        firstPlayer.color.value.lowercase(),
                        source.toStringNotation()
                    )
                )
            }
            return false
        }
        // pawn moves on same file
        if (source.file() == destination.file()) {
            //  the pawn can move 1 rank or 2 ranks (only once, if pawn is on starting square)
            val allowedRanks = when (thisPawn.color) {
                //  white moves from rank 2 -> rank 8
                Color.WHITE -> if (thisPawn.position.rank() == 2) 2 else 1
                //  black moves from rank 7 -> rank 1
                Color.BLACK -> if (thisPawn.position.rank() == 7) -2 else -1
            }
            //  range of valid forward movement for pawn
            val rangeOfMovement = when (thisPawn.color) {
                Color.WHITE -> source.rank() + 1..source.rank() + allowedRanks
                Color.BLACK -> source.rank() - 1 downTo source.rank() + allowedRanks
            }
            //  pawn doesn't move forward, or moves forward more than allowed number of ranks
            if (destination.rank() !in rangeOfMovement) {
                if (enableCapture) println(Message.INVALID_INPUT.text)
                return false
            }
            //  destination is already occupied by an existing pawn
            if (otherPawn != null) {
                if (enableCapture) println(Message.INVALID_INPUT.text)
                return false
            }
        } else {    //  pawn moves to another file
            //  check if move is to adjacent file
            val pawnMovesLeftFile = destination.file() + 1 == source.file()
            val pawnMovesRightFile = destination.file() - 1 == source.file()
            //  pawn moved more than 1 file
            if (!(pawnMovesLeftFile || pawnMovesRightFile)) {
                if (enableCapture) println(Message.INVALID_INPUT.text)
                return false
            }
            //  check if pawn moves only 1 rank when changing files
            val allowedRank = when (thisPawn.color) {
                //  white moves from rank 2 -> rank 8
                Color.WHITE -> 1
                //  black moves from rank 7 -> rank 1
                Color.BLACK -> -1
            }
            //  pawn didn't move 1 rank when changing files
            if (source.rank() + allowedRank != destination.rank()) {
                if (enableCapture) println(Message.INVALID_INPUT.text)
                return false
            }
            // there is a pawn at destination
            if (otherPawn != null) {
                //  the pawn at destination belongs to the same player
                if (otherPawn.color == thisPawn.color) {
                    if (enableCapture) println(Message.INVALID_INPUT.text)
                    return false
                } else {
                    //  capture the pawn and remove it from the board
                    if (enableCapture) pawns.remove(otherPawn)
                }
            } else {    // no pawn at destination, so check if we can capture en 'passant'
                //  white's pawn must be on rank 5 and black's pawn must be on rank 4
                val canCaptureEnPassant = when (thisPawn.color) {
                    Color.WHITE -> source.rank() == 5
                    Color.BLACK -> source.rank() == 4
                }
                // pawn can't capture 'en passant' because it's not on the correct rank
                if (!canCaptureEnPassant) {
                    if (enableCapture) println(Message.INVALID_INPUT.text)
                    return false
                }
                //  Look for available pawn to capture 'en passant':
                //  - the captured pawn will have same file as 'destination' and same rank as 'source'
                val capturedPawn = getPawnAt(Pair(destination.file(), source.rank()))
                // no available pawn to be captured 'en passant'
                if (capturedPawn == null) {
                    if (enableCapture) println(Message.INVALID_INPUT.text)
                    return false
                } else {    //  we can capture 'en passant' if we are in the same turn (checking player's last move)
                    //  we need only the destination from the last move
                    val lastMoveFile = secondPlayer.lastMove[2]
                    val lastMoveRank = secondPlayer.lastMove[3].digitToInt()
                    val opponentsLastMove = Pair(lastMoveFile, lastMoveRank)
                    //  the pawn we're capturing wasn't the last move of the opponent
                    if (capturedPawn.position != opponentsLastMove) {
                        if (enableCapture) println(Message.INVALID_INPUT.text)
                        return false
                    } else {
                        //  capture the pawn and remove it from the board
                        if (enableCapture) pawns.remove(capturedPawn)
                    }
                }
            }
        }
        //  update the board with the pawn's new position
        if (enableCapture) updatePawn(source, destination)
        return true
    }

    /**
     *  Update pawn's position after player's move
     */
    private fun updatePawn(source: Pair<Char, Int>, destination: Pair<Char, Int>) {
        val pawn = getPawnAt(source)
        if (pawn != null) {
            pawn.position = destination
        }
        // redraw the board after moving the pawn
        drawBoard()
    }

    /**
     *  Draws the game board and the pawns
     */
    private fun drawBoard() {
        val padding = Draw.PADDING.element
        val horizontal = Draw.HORIZONTAL.element
        val vertical = Draw.VERTICAL.element
        //  each 'rank'
        for (rank in ranks) {
            val row = StringBuilder()
            row.append(horizontal, rank, padding, vertical)
            for (file in files) {
                //  each 'square' in the rank
                val square = StringBuilder()
                square.append(padding, getPawnGlyphAt(Pair(file, rank)), padding, vertical)
                row.append(square)
            }
            println(row)
        }
        //  'files'
        val bottomFiles = StringBuilder()
        bottomFiles.append(horizontal, padding)
        files.forEach { file -> bottomFiles.append(padding.repeat(3), file) }
        println("$bottomFiles\n")
    }

    init {
        //  drawing the starting board
        drawBoard()
    }
}