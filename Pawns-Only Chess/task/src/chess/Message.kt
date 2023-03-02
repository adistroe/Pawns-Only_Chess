package chess

enum class Message(val text: String) {
    TITLE("Pawns-Only Chess"),
    PLAYER_ONE("First Player's name:"),
    PLAYER_TWO("Second Player's name:"),
    PLAYERS_TURN("%s's turn:"),
    INVALID_INPUT("Invalid Input"),
    NO_PAWN("No %s pawn at %s"),
    WIN("%s Wins!"),
    STALEMATE("Stalemate!"),
    BYE("Bye!")
}