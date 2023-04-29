package chess

class Chess {
    //  player types this command to stop the game
    private enum class Command(val text: String) {
        EXIT("exit")
    }

    /**
     *  Check if input is in standard chess algebraic notation
     */
    private fun isValidNotation(input: String): Boolean {
        val regex = "(?:[a-h][1-8]){2}".toRegex()
        return regex.matches(input)
    }

    //  let's get ready to rumble !
    fun run() {
        println(Message.TITLE.text)
        println(Message.PLAYER_ONE.text)
        var firstPlayer = Player(readln(), Color.WHITE)
        println(Message.PLAYER_TWO.text)
        var secondPlayer = Player(readln(), Color.BLACK)
        val board = Board()
        //  main game loop
        do {
            //  white player moves first
            println(String.format(Message.PLAYERS_TURN.text, firstPlayer.name))
            val input = readln().lowercase()
            //  player stops game
            if (input == Command.EXIT.text) continue
            //  input not in standard chess algebraic notation
            if (!isValidNotation(input)) {
                println(Message.INVALID_INPUT.text)
                continue
            }
            //  we can move the pawn
            if (board.isValidMove(input, firstPlayer, secondPlayer, true)) {
                val destinationRank = input.last().digitToInt()
                //  check if player's pawn reached the last rank on the board
                if (destinationRank == board.lastRank(firstPlayer)) {
                    //  player won
                    println(String.format(Message.WIN.text, firstPlayer.color.value))
                    //  end the game
                    break
                } else {    //  player's pawn didn't reach the last rank on the board
                    val remainingPawns = board.pawns.filter { pawn -> pawn.color == secondPlayer.color }
                    //  player won by capturing all pawns
                    if (remainingPawns.isEmpty()) {
                        println(String.format(Message.WIN.text, firstPlayer.color.value))
                        //  end the game
                        break
                    }
                    //  check for stalemate
                    var hasValidMove = false
                    loop@ for (pawn in remainingPawns) {
                        for (move in pawn.getPossibleMoves()) {
                            if (board.isValidMove(
                                    "${pawn.position.toStringNotation()}${move.toStringNotation()}",
                                    secondPlayer,
                                    firstPlayer,
                                    false
                                )
                            ) {
                                hasValidMove = true
                                break@loop
                            }
                        }
                    }
                    if (!hasValidMove) {
                        //  stalemate because of no available moves left
                        println(Message.STALEMATE.text)
                        //  end the game
                        break
                    }
                    //  save player's last move
                    firstPlayer.lastMove = input
                    //  switch player's turn
                    firstPlayer = secondPlayer.also { secondPlayer = firstPlayer }
                }
            }
        } while (input != Command.EXIT.text)
        //  Thanks for playing !
        println(Message.BYE.text)
    }
}