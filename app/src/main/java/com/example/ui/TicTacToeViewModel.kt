package com.example.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

enum class GameMode {
    VS_AI,
    PASS_AND_PLAY
}

enum class Difficulty {
    EASY,
    MEDIUM,
    IMPOSSIBLE
}

data class TicTacToeState(
    val board: List<String> = List(9) { "" }, // Empty, "X", "O"
    val isXTurn: Boolean = true,
    val gameActive: Boolean = true,
    val winner: String? = null, // "X", "O", "Draw", or null if active
    val winLine: List<Int>? = null, // Indices of winning cells (e.g., listOf(0, 1, 2))
    val xWins: Int = 0,
    val oWins: Int = 0,
    val draws: Int = 0,
    val gameMode: GameMode = GameMode.VS_AI,
    val difficulty: Difficulty = Difficulty.MEDIUM
)

class TicTacToeViewModel : ViewModel() {
    private val _state = MutableStateFlow(TicTacToeState())
    val state: StateFlow<TicTacToeState> = _state.asStateFlow()

    fun selectGameMode(mode: GameMode) {
        _state.value = _state.value.copy(
            gameMode = mode,
            board = List(9) { "" },
            isXTurn = true,
            gameActive = true,
            winner = null,
            winLine = null,
            xWins = 0,
            oWins = 0,
            draws = 0
        )
    }

    fun selectDifficulty(difficulty: Difficulty) {
        _state.value = _state.value.copy(
            difficulty = difficulty,
            board = List(9) { "" },
            isXTurn = true,
            gameActive = true,
            winner = null,
            winLine = null
        )
    }

    fun makeMove(index: Int) {
        val currentState = _state.value
        if (!currentState.gameActive || currentState.board[index].isNotEmpty()) return

        val currentPlayer = if (currentState.isXTurn) "X" else "O"
        val newBoard = currentState.board.toMutableList()
        newBoard[index] = currentPlayer

        val checkResult = checkWinner(newBoard)
        val winner = checkResult.winner
        val winLine = checkResult.winLine
        val isGameOver = winner != null || !newBoard.contains("")

        val nextIsXTurn = !currentState.isXTurn

        // Update scores
        var newXWins = currentState.xWins
        var newOWins = currentState.oWins
        var newDraws = currentState.draws

        if (isGameOver) {
            when (winner) {
                "X" -> newXWins++
                "O" -> newOWins++
                null -> newDraws++
            }
        }

        _state.value = currentState.copy(
            board = newBoard,
            isXTurn = nextIsXTurn,
            gameActive = !isGameOver,
            winner = winner ?: (if (!newBoard.contains("")) "Draw" else null),
            winLine = winLine,
            xWins = newXWins,
            oWins = newOWins,
            draws = newDraws
        )

        // Trigger AI Turn if Mode is VS_AI, game remains active, and it is now O's turn (AI is always O)
        if (
            currentState.gameMode == GameMode.VS_AI &&
            !isGameOver &&
            !nextIsXTurn
        ) {
            triggerAiMove(newBoard)
        }
    }

    private fun triggerAiMove(currentBoard: List<String>) {
        val aiIndex = when (_state.value.difficulty) {
            Difficulty.EASY -> getEasyMove(currentBoard)
            Difficulty.MEDIUM -> getMediumMove(currentBoard)
            Difficulty.IMPOSSIBLE -> getImpossibleMove(currentBoard)
        }

        if (aiIndex != -1) {
            val nextBoard = currentBoard.toMutableList()
            nextBoard[aiIndex] = "O"

            val checkResult = checkWinner(nextBoard)
            val winner = checkResult.winner
            val winLine = checkResult.winLine
            val isGameOver = winner != null || !nextBoard.contains("")

            var newXWins = _state.value.xWins
            var newOWins = _state.value.oWins
            var newDraws = _state.value.draws

            if (isGameOver) {
                when (winner) {
                    "X" -> newXWins++
                    "O" -> newOWins++
                    null -> newDraws++
                }
            }

            _state.value = _state.value.copy(
                board = nextBoard,
                isXTurn = true,
                gameActive = !isGameOver,
                winner = winner ?: (if (!nextBoard.contains("")) "Draw" else null),
                winLine = winLine,
                xWins = newXWins,
                oWins = newOWins,
                draws = newDraws
            )
        }
    }

    private fun getEasyMove(board: List<String>): Int {
        val emptyIndices = board.indices.filter { board[it].isEmpty() }
        return if (emptyIndices.isNotEmpty()) {
            emptyIndices[Random.nextInt(emptyIndices.size)]
        } else {
            -1
        }
    }

    private fun getMediumMove(board: List<String>): Int {
        // 1. Try to win in one move
        val winMove = findWinningMove(board, "O")
        if (winMove != -1) return winMove

        // 2. Try to block opponent's win
        val blockMove = findWinningMove(board, "X")
        if (blockMove != -1) return blockMove

        // 3. Play center if available
        if (board[4].isEmpty()) return 4

        // 4. Play random empty index
        return getEasyMove(board)
    }

    private fun getImpossibleMove(board: List<String>): Int {
        var bestVal = -1000
        var bestMove = -1
        
        for (i in 0..8) {
            if (board[i].isEmpty()) {
                val tempBoard = board.toMutableList()
                tempBoard[i] = "O"
                val moveVal = minimax(tempBoard, 0, false)
                if (moveVal > bestVal) {
                    bestVal = moveVal
                    bestMove = i
                }
            }
        }
        return bestMove
    }

    private fun minimax(board: MutableList<String>, depth: Int, isMax: Boolean): Int {
        val winResult = checkWinner(board)
        val score = when (winResult.winner) {
            "O" -> 10 - depth
            "X" -> depth - 10
            else -> if (!board.contains("")) 0 else null
        }

        if (score != null) return score

        if (isMax) {
            var best = -1000
            for (i in 0..8) {
                if (board[i].isEmpty()) {
                    board[i] = "O"
                    best = maxOf(best, minimax(board, depth + 1, false))
                    board[i] = ""
                }
            }
            return best
        } else {
            var best = 1000
            for (i in 0..8) {
                if (board[i].isEmpty()) {
                    board[i] = "X"
                    best = minOf(best, minimax(board, depth + 1, true))
                    board[i] = ""
                }
            }
            return best
        }
    }

    private fun findWinningMove(board: List<String>, player: String): Int {
        val winPatterns = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columns
            listOf(0, 4, 8), listOf(2, 4, 6)                  // Diagonals
        )

        for (pattern in winPatterns) {
            val a = board[pattern[0]]
            val b = board[pattern[1]]
            val c = board[pattern[2]]

            if (a == player && b == player && c.isEmpty()) return pattern[2]
            if (a == player && c == player && b.isEmpty()) return pattern[1]
            if (b == player && c == player && a.isEmpty()) return pattern[0]
        }
        return -1
    }

    data class WinResult(val winner: String?, val winLine: List<Int>?)

    private fun checkWinner(board: List<String>): WinResult {
        val winPatterns = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columns
            listOf(0, 4, 8), listOf(2, 4, 6)                  // Diagonals
        )

        for (pattern in winPatterns) {
            val a = board[pattern[0]]
            val b = board[pattern[1]]
            val c = board[pattern[2]]

            if (a.isNotEmpty() && a == b && a == c) {
                return WinResult(a, pattern)
            }
        }
        return WinResult(null, null)
    }

    fun resetRound() {
        val currentState = _state.value
        _state.value = currentState.copy(
            board = List(9) { "" },
            isXTurn = true,
            gameActive = true,
            winner = null,
            winLine = null
        )
    }

    fun resetMatch() {
        _state.value = TicTacToeState(
            gameMode = _state.value.gameMode,
            difficulty = _state.value.difficulty
        )
    }
}
