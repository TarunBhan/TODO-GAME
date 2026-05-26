package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TicTacToeScreen(
    viewModel: TicTacToeViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Natural Accent Tonal aura
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 84.dp), // Height budget for Bottom Nav
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Screen Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Zen Arena",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tic Tac Toe Challenge",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                    )
                }

                // Restart Match Button (Resets scores too)
                IconButton(
                    onClick = { viewModel.resetMatch() },
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = CircleShape
                        )
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset scoreboard and match history",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game Mode Chip Segment Selector
            GameModeSelector(
                selectedMode = state.gameMode,
                onModeSelect = { viewModel.selectGameMode(it) }
            )

            // Difficulty selections if AI mode is selected
            AnimatedVisibility(
                visible = state.gameMode == GameMode.VS_AI,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(10.dp))
                    DifficultySelector(
                        selectedDifficulty = state.difficulty,
                        onDifficultySelect = { viewModel.selectDifficulty(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scoreboard Widget
            Scoreboard(state = state)

            Spacer(modifier = Modifier.height(16.dp))

            // Game Board Container (Adhering beautiful to "Natural Tones" Draft panel: bg secondaryContainer/SageAccent, heavy rounded corners, soft border)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(32.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        RoundedCornerShape(32.dp)
                    )
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Turn Status Banner
                    GameStatusBanner(state = state)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3x3 Grid Board
                    BoardGrid(
                        board = state.board,
                        winLine = state.winLine,
                        onCellClick = { viewModel.makeMove(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Clear / Next Round triggering button
                    Button(
                        onClick = { viewModel.resetRound() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(48.dp)
                            .testTag("reset_round_button")
                    ) {
                        Text(
                            text = if (state.gameActive) "Clear Board" else "Play Again",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameModeSelector(
    selectedMode: GameMode,
    onModeSelect: (GameMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        GameMode.values().forEach { mode ->
            val isSelected = mode == selectedMode
            val bgSelectedColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                label = "modeBg"
            )
            val textSelectedColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "modeText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgSelectedColor)
                    .clickable { onModeSelect(mode) }
                    .testTag("mode_chip_${mode.name}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (mode) {
                        GameMode.VS_AI -> "vs Computer"
                        GameMode.PASS_AND_PLAY -> "Pass & Play"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = textSelectedColor
                )
            }
        }
    }
}

@Composable
fun DifficultySelector(
    selectedDifficulty: Difficulty,
    onDifficultySelect: (Difficulty) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Difficulty.values().forEach { diff ->
            val isSelected = diff == selectedDifficulty
            val chipBgColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                label = "diffBackground"
            )
            val chipBorderColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                label = "diffBorder"
            )
            val chipTextColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "diffText"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(chipBgColor)
                    .border(1.dp, chipBorderColor, RoundedCornerShape(10.dp))
                    .clickable { onDifficultySelect(diff) }
                    .testTag("difficulty_${diff.name}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (diff) {
                        Difficulty.EASY -> "Easy"
                        Difficulty.MEDIUM -> "Medium"
                        Difficulty.IMPOSSIBLE -> "Expert"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = chipTextColor
                )
            }
        }
    }
}

@Composable
fun Scoreboard(state: TicTacToeState) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreColumn(
                label = if (state.gameMode == GameMode.VS_AI) "You (X)" else "Player X",
                score = state.xWins,
                color = MaterialTheme.colorScheme.primary
            )

            DividerVertical()

            ScoreColumn(
                label = "Ties",
                score = state.draws,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            DividerVertical()

            ScoreColumn(
                label = if (state.gameMode == GameMode.VS_AI) "AI (O)" else "Player O",
                score = state.oWins,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun ScoreColumn(label: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

@Composable
fun DividerVertical() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    )
}

@Composable
fun GameStatusBanner(state: TicTacToeState) {
    val message = when {
        state.winner == "X" -> if (state.gameMode == GameMode.VS_AI) "Victory! You Won." else "Player X Wins!"
        state.winner == "O" -> if (state.gameMode == GameMode.VS_AI) "AI Wins. Try again!" else "Player O Wins!"
        state.winner == "Draw" -> "Peaceful Tie. Play again!"
        state.isXTurn -> if (state.gameMode == GameMode.VS_AI) "Your Turn (X)" else "Player X's Turn"
        else -> if (state.gameMode == GameMode.VS_AI) "AI is calculating..." else "Player O's Turn"
    }

    val bannerColor = when (state.winner) {
        "X" -> MaterialTheme.colorScheme.primary
        "O" -> MaterialTheme.colorScheme.tertiary
        "Draw" -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onBackground
    }

    Text(
        text = message,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = bannerColor,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().testTag("game_status_banner")
    )
}

@Composable
fun BoardGrid(
    board: List<String>,
    winLine: List<Int>?,
    onCellClick: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .testTag("tictactoe_board_grid")
    ) {
        for (row in 0..2) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                for (col in 0..2) {
                    val index = row * 3 + col
                    val isWinningCell = winLine?.contains(index) == true
                    BoardCell(
                        symbol = board[index],
                        isWinningCell = isWinningCell,
                        onClick = { onCellClick(index) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun BoardCell(
    symbol: String,
    isWinningCell: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (symbol.isNotEmpty()) 1.0f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "cellAnimation"
    )

    val backgroundCellColor by animateColorAsState(
        targetValue = when {
            isWinningCell -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            symbol.isNotEmpty() -> MaterialTheme.colorScheme.surface
            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
        },
        label = "cellColor"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundCellColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (symbol.isEmpty()) 0.dp else 2.dp),
        modifier = modifier
            .fillMaxHeight()
            .scale(scale)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            this@Card.AnimatedVisibility(
                visible = symbol.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut()
            ) {
                if (symbol == "X") {
                    XSymbolDrawing(modifier = Modifier.size(54.dp))
                } else if (symbol == "O") {
                    OSymbolDrawing(modifier = Modifier.size(54.dp))
                }
            }
        }
    }
}

@Composable
fun XSymbolDrawing(modifier: Modifier = Modifier) {
    val drawColor = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        val strokeWidth = 8.dp.toPx()
        val padding = size.width * 0.22f

        drawLine(
            color = drawColor,
            start = Offset(padding, padding),
            end = Offset(size.width - padding, size.height - padding),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        drawLine(
            color = drawColor,
            start = Offset(size.width - padding, padding),
            end = Offset(padding, size.height - padding),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun OSymbolDrawing(modifier: Modifier = Modifier) {
    val drawColor = MaterialTheme.colorScheme.tertiary
    Canvas(modifier = modifier) {
        val strokeWidth = 8.dp.toPx()
        val padding = size.width * 0.22f
        val radius = (size.width - padding * 2) / 2

        drawCircle(
            color = drawColor,
            radius = radius,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
