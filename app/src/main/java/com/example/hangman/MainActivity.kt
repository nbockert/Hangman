package com.example.hangman

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.content.res.Configuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.graphics.drawscope.Stroke
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Hangman()
        }
    }
}
val words = listOf(
    "SLEEPWALK" to "To travel at night, maybe",
    "AVEDA" to "Skincare company with a Sanskrit name",
    "MINIMALART" to "Frank Stella pieces",
    "ELAPSES" to "Ticks away",
    "OZARK" to "Netflix show set in Missouri",
    "HOTWIRE" to "Start without a key"
)

@Composable
fun Hangman(){
    val config = LocalConfiguration.current
    val isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
    var wordPair by rememberSaveable {mutableStateOf(words.random()) }
    var word by rememberSaveable { mutableStateOf(wordPair.first) }
    var hint by rememberSaveable { mutableStateOf(wordPair.second) }
    var guessedLetters by rememberSaveable { mutableStateOf(setOf<Char>()) }
    var incorrectScore by rememberSaveable { mutableStateOf(0) }
    var hintClicks by rememberSaveable { mutableStateOf(0) }
    var disabledLetters by rememberSaveable { mutableStateOf(setOf<Char>()) }
    val maxIncorrectScore = 6
    val wordDisplay = word.map { if (it in guessedLetters) it else '_' }.joinToString(" ")
    val gameOver = incorrectScore >= maxIncorrectScore
    val gameWon = word.all { it in guessedLetters }
    fun reset(){
        wordPair = words.random()
        word = wordPair.first
        hint = wordPair.second
        guessedLetters = setOf()
        disabledLetters = setOf()
        incorrectScore = 0
        hintClicks = 0
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        if(isLandscape){
            Row(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.weight(1f)) {
                    LetterButtons(guessedLetters, disabledLetters, word, { guessedLetters = guessedLetters + it }) {
                        incorrectScore++
                    }
                    HintButton(
                        hint, hintClicks, { hintClicks++ }, incorrectScore, maxIncorrectScore,
                        word, guessedLetters,
                        onDisableIncorrectLetters = { disabledLetters = disabledLetters + it },
                        onDisableVowels = { guessedLetters = guessedLetters + it},
                        onIncorrect = { incorrectScore++ }
                    )
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    HangmanDrawing(incorrectScore)
                    Spacer(modifier = Modifier.height(16.dp))
                    BasicText(text = wordDisplay, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (gameWon) Text("You Win!", color = Color.Green)
                    if (gameOver) Text("You Lose! The word was $word", color = Color.Red)
                    Row{
                        Button(onClick = { reset() }) {
                            Text("New Game")
                        }
                        HintButton(
                            hint, hintClicks, { hintClicks++ }, incorrectScore, maxIncorrectScore,
                            word, guessedLetters,
                            onDisableIncorrectLetters = { disabledLetters = disabledLetters + it },
                            onDisableVowels = { guessedLetters = guessedLetters + it },
                            onIncorrect = { incorrectScore++ }
                        )

                    }


                }
        }
    }else{

                    HangmanDrawing(incorrectScore)
                    Spacer(modifier = Modifier.height(16.dp))
                    BasicText(text = wordDisplay, style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (gameWon) Text("You Win!", color = Color.Green)
                    if (gameOver) Text("You Lose! The word was $word", color = Color.Red)
                    Spacer(modifier = Modifier.height(16.dp))
            Row {

                LetterButtons(
                    guessedLetters,
                    disabledLetters,
                    word,
                    { guessedLetters = guessedLetters + it }) {
                    incorrectScore++
                }
            }
                    HintButton(
                        hint, hintClicks, { hintClicks++ }, incorrectScore, maxIncorrectScore,
                        word, guessedLetters,
                        onDisableIncorrectLetters = { disabledLetters = disabledLetters + it },
                        onDisableVowels = { guessedLetters = guessedLetters + it },
                        onIncorrect = { incorrectScore++ }
                    )
            Button(onClick = { reset() }) {
                Text("New Game")
            }
                }





    }


}

@Composable
fun LetterButtons(
    guessedLetters: Set<Char>,
    disabledLetters: Set<Char>,
    word: String,
    onLetterSelected: (Char) -> Unit,
    onIncorrect: () -> Unit
) {

    val alphabet = ('A'..'Z').toList()
    Column {
        alphabet.chunked(5).forEach { row ->
            Row {
                row.forEach { letter ->
                    val disabled = letter in guessedLetters || letter in disabledLetters
                    println("disabled Letters at keyboard $disabledLetters")

                    Button(
                        onClick = {
                            onLetterSelected(letter)
                            if (letter !in word) onIncorrect()
                        },
                        enabled = !disabled,
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(letter.toString())
                    }
                }
            }
        }
    }
}

@Composable
fun HintButton(
    hint: String,
    hintClicks: Int,
    onHintClick: () -> Unit,
    incorrectGuesses: Int,
    maxIncorrectGuesses: Int,
    word: String,
    guessedLetters: Set<Char>,
    onDisableIncorrectLetters: (Set<Char>) -> Unit,
    onDisableVowels: (Set<Char>) -> Unit,
    onIncorrect: () -> Unit
) {
    val context = LocalContext.current
    val vowels = setOf('A', 'E', 'I', 'O', 'U')

    var message by rememberSaveable { mutableStateOf("") }
    //source: ChatGPT
    Button(
        onClick = {
            if (incorrectGuesses >= maxIncorrectGuesses - 1) {
                Toast.makeText(context, "Hint not available", Toast.LENGTH_SHORT).show()
            } else {
                when (hintClicks) {
                    0 -> {
                        message = hint
                    }
                    1 -> {
                        val incorrectLetters = ('A'..'Z').filter { it !in word && it !in guessedLetters }
                        val lettersToDisable = incorrectLetters.shuffled().take(incorrectLetters.size / 2).toSet()
                        onDisableIncorrectLetters(lettersToDisable)
                        onIncorrect() // Costs 1 turn
                        message = "Disabled half of the wrong letters (costs 1 turn)"
                    }
                    2 -> {
                        val vowelsToDisable = vowels.filter { it in word && it !in guessedLetters }.toSet()

                        onDisableVowels(vowelsToDisable)
                        onIncorrect() // Costs 1 turn
                        message = "Showing all vowels (costs 1 turn)"
                    }
                }
                onHintClick()
            }
        },
        modifier = Modifier.padding(8.dp)
    ) {
        Text("Hint")
    }

    if (message.isNotEmpty()) {
        Text(message)
    }
}


@Composable
fun HangmanDrawing(incorrectGuesses: Int) {
    //source: ChatGPT
    Canvas(modifier = Modifier.size(200.dp)) {
        val stroke = Stroke(width = 4f)
        drawLine(Color.Black, start = center.copy(y = size.height), end = center.copy(y = 20f), strokeWidth = 4f) // Pole
        drawLine(Color.Black, start = center.copy(y = 20f), end = center.copy(x = center.x + 40, y = 20f), strokeWidth = 4f) // Top Bar
        drawLine(Color.Black, start = center.copy(x = center.x + 40, y = 20f), end = center.copy(x = center.x + 40, y = 50f), strokeWidth = 4f) // Rope

        if (incorrectGuesses > 0) drawCircle(Color.Black, radius = 30f, center = center.copy(x = center.x + 40, y = 70f)) // Head
        if (incorrectGuesses > 1) drawLine(color=Color.Black, start = center.copy(center.x+40,90f), end = center.copy(center.x+40,160f), stroke.width) // Body
        if (incorrectGuesses > 2) drawLine(color=Color.Black, start = center.copy(center.x+20,130f), end = center.copy(center.x+40,130f), stroke.width) // Arms
        if (incorrectGuesses > 3) drawLine(color=Color.Black, start = center.copy(center.x+40,130f), end = center.copy(center.x+60,130f), stroke.width)
        if (incorrectGuesses > 4) drawLine(color=Color.Black, start = center.copy(center.x+40,140f), end = center.copy(center.x+20,180f), stroke.width) // Left Leg
        if (incorrectGuesses > 5) drawLine(color=Color.Black, start = center.copy(center.x+40,140f), end = center.copy(center.x+60,180f), stroke.width) // Right Leg
    }
}




