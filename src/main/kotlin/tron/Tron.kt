package tron

import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.util.concurrent.ThreadLocalRandom
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JFrame
import kotlin.concurrent.fixedRateTimer

val SIZE = 795
val BOARD_WIDTH = SIZE / 20 + 1
var game: Game? = null
var players = mutableListOf<Player>()
val start: JButton = JButton("Begin")
var highScore = 0

fun main(args: Array<String>) {
    val frame = JFrame("it's tron")
    val inputListener = UserInput()
    val player = UserPlayer(inputListener)
    start.setBounds(525, 525, 150, 75)
    start.background = Color.black
    start.foreground = Color.white
    start.isFocusPainted = false
    start.isFocusable = false
    start.font = Font("Lucidia Console", Font.ITALIC + Font.BOLD, 32)
    start.isVisible = true
    start.addActionListener { _ ->
        // Reset game
        game!!.state = Game.State.PLAYING
        player.score = 0
        player.isDead = false
        player.spots.clear()
        player.x = ThreadLocalRandom.current().nextInt(2, BOARD_WIDTH - 3)
        player.y = ThreadLocalRandom.current().nextInt(2, BOARD_WIDTH - 3)
        inputListener.dx = 0
        inputListener.dy = -1
        players.clear()
        for (i in 1..6) {
            players.add(BotPlayer())
        }
        players.add(player)
        start.isVisible = false
    }
    frame.setSize(SIZE, SIZE)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    frame.addKeyListener(inputListener)

    for (i in 1..8) {
        players.add(BotPlayer())
    }
    game = Game(player)

    frame.add(start)
    frame.add(game)

    frame.isResizable = false
    frame.isVisible = true
    game!!.paint(game!!.graphics)
    fixedRateTimer(name = "MainThread", period = 100) {
        if (player.isDead && game!!.state == Game.State.PLAYING) {
            game!!.state = Game.State.GAME_OVER
            players = mutableListOf()
            (1..8).forEach {
                players.add(BotPlayer())
            }
        }
        refreshPlayers()
    }
}

fun refreshPlayers() {
    val playersToRemove = mutableListOf<Player>()
    players.forEach {
        if (it.isDead)
            playersToRemove.add(it)
    }
    playersToRemove.forEach {
        players.remove(it)
        players.add(BotPlayer())
    }
    players.forEach(Player::update)
    game!!.repaint()
}

class Game(val mainPlayer: UserPlayer) : JComponent() {
    enum class State {
        PLAYING,
        GAME_OVER,
        START
    }

    var backColor: Color = Color.black
    var lineColor: Color = Color.lightGray
    var state = State.START

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        drawBackground(g)
        drawTails(g)
        players.forEach { it.draw(g) }
        if (state == State.GAME_OVER) {
            drawTitle(g)
            g.color = Color.red
            g.font = Font(null, 0, 50)
            g.drawString("GAME OVER", 125, 550)
            g.drawString("Score: ${mainPlayer.score}", 175, 600)
            g.font = Font(null, 0, 30)
            if (mainPlayer.score > highScore)
                highScore = mainPlayer.score
            g.drawString("High Score: $highScore", 180, 625)
            start.isVisible = true
            start.text = "Start"
        } else if (state == State.START) {
            drawTitle(g)
        }
    }

    fun drawTitle(g: Graphics) {
        g.color = Color.white
        g.fillRect((size.width / 12), (size.height / 6), ((5 * size.width) / 6), ((2 * size.height) / 3))
        g.color = Color.black
        g.font = Font("Lucidia Console", Font.ITALIC + Font.BOLD, 200)
        g.drawString("TRON", (size.width / 9), (size.height / 2))
        g.font = Font("Lucidia Console", Font.PLAIN, 30)
        g.drawString("Jadon Fowler", (size.width / 6), (size.height / 2) + 35)
        g.drawString("Matthew Ormson", (size.width / 6), (size.height / 2) + 70)
    }

    fun drawBackground(g: Graphics) {
        g.color = backColor
        g.fillRect(0, 0, size.width, size.height)
        (0..BOARD_WIDTH).forEach {
            val p = it * 20 - 1
            g.color = lineColor
            g.drawLine(p, 0, p, SIZE)
            g.drawLine(0, p, SIZE, p)
        }
    }

    fun drawTails(g: Graphics) {
        (0..BOARD_WIDTH).forEach {
            val px = it * 20 - 1
            val x = it
            (0..BOARD_WIDTH).forEach {
                val py = it * 20 - 1
                val y = it
                val spot = y * BOARD_WIDTH + x
                players.forEach {
                    if (it.spots.contains(spot)) {
                        val player = it
                        // Reverse the index
                        val index = player.spots.size - player.spots.indexOf(spot)
                        g.color = player.color
                        (0..index).forEach {
                            val decayConstant = Math.min(0.92 + (if (player is UserPlayer) player.score * .001 else 0.0), 0.999)
                            val red = Math.min(g.color.red * decayConstant, 255.0)
                            val green = Math.min(g.color.green * decayConstant, 255.0)
                            val blue = Math.min(g.color.blue * decayConstant, 255.0)
                            g.color = Color(red.toInt(), green.toInt(), blue.toInt())

                            // Remove dead cells
                            val darknessThreshold = 20
                            if (red < darknessThreshold && blue < darknessThreshold && green < darknessThreshold)
                                player.spots.remove(spot)
                        }
                        g.fillRect(px - 10, py - 10, 20, 20)
                    }
                }
            }
        }
    }
}

abstract class Player(val color: Color) {
    var isDead = false
    var x = ThreadLocalRandom.current().nextInt(2, BOARD_WIDTH - 3)
    var y = ThreadLocalRandom.current().nextInt(2, BOARD_WIDTH - 3)
    val spots = mutableListOf<Int>()

    abstract fun update()
    abstract fun draw(g: Graphics)
}

class BotPlayer : Player(randomColor()) {
    var dx = 0
    var dy = -1
    var tick = 0

    override fun update() {
        tick++
        if (tick % ThreadLocalRandom.current().nextInt(3, 6) == 0) {
            dx = ThreadLocalRandom.current().nextInt(-1, 2)
            dy = if (dx != 0) 0 else ThreadLocalRandom.current().nextInt(-1, 2)
        }
        x = Math.max(1, Math.min(dx + x, BOARD_WIDTH - 2))
        y = Math.max(1, Math.min(dy + y, BOARD_WIDTH - 2))

        val spot = y * BOARD_WIDTH + x
        players.forEach {
            if (it != this && it.spots.contains(spot)) {
                isDead = true
                if (it is UserPlayer)
                    it.score++
            }
        }
        if (!spots.contains(spot))
            spots.add(spot)
    }

    override fun draw(g: Graphics) {
        g.color = color.darker()
        g.drawRoundRect(x * 20 - 6 - 3, y * 20 - 6, 6 * 2, 6 * 2, 7, 5)
    }
}

fun randomColor(): Color {
    val r = ThreadLocalRandom.current().nextFloat()
    val g = ThreadLocalRandom.current().nextFloat()
    val b = ThreadLocalRandom.current().nextFloat()
    return Color(r, g, b)
}

class UserPlayer(val input: UserInput) : Player(Color.red) {
    var score = 0

    override fun update() {
        x = Math.max(1, Math.min(input.dx + x, BOARD_WIDTH - 2))
        y = Math.max(1, Math.min(input.dy + y, BOARD_WIDTH - 2))
        val spot = y * BOARD_WIDTH + x
        players.forEach {
            if (it != this && it.spots.contains(spot)) {
                isDead = true
            }
        }
        if (!spots.contains(spot))
            spots.add(spot)
    }

    override fun draw(g: Graphics) {
        g.color = Color.white
        g.font = Font(null, 0, if (score > 9) 14 else 16)
        g.drawString("$score", x * 20 - 7 - (if (score > 9) 4 else 0), y * 20 + 5)
    }
}

class UserInput : KeyListener {
    var dx = 0
    var dy = -1

    override fun keyPressed(e: KeyEvent?) {
        e?.let {
            when (it.keyCode) {
                KeyEvent.VK_W, KeyEvent.VK_UP -> {
                    dx = 0
                    dy = -1
                }
                KeyEvent.VK_S, KeyEvent.VK_DOWN -> {
                    dx = 0
                    dy = 1
                }
                KeyEvent.VK_A, KeyEvent.VK_LEFT -> {
                    dx = -1
                    dy = 0
                }
                KeyEvent.VK_D, KeyEvent.VK_RIGHT -> {
                    dx = 1
                    dy = 0
                }
            }
        }
    }

    override fun keyReleased(e: KeyEvent?) {}
    override fun keyTyped(e: KeyEvent?) {}
}
