package tron

import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.util.concurrent.ThreadLocalRandom
import javax.swing.JComponent
import javax.swing.JFrame
import kotlin.concurrent.fixedRateTimer

val SIZE = 795
val BOARD_WIDTH = SIZE / 20 + 1
var game: Game? = null
var players = mutableListOf<Player>()

fun main(args: Array<String>) {
    val frame = JFrame("it's tron")
    frame.setSize(SIZE, SIZE)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    val inputListener = UserInput()
    val player = UserPlayer(inputListener)
    frame.addKeyListener(inputListener)

    for (i in 1..6) {
        players.add(BotPlayer())
    }
    game = StartScreen()

    frame.add(game)
    frame.isVisible = true
    game!!.paint(game!!.graphics)
    fixedRateTimer(name = "MainThread", period = 100) {
        refreshPlayers()
        if (players.size == 1){
            for (i in 1..6) {
                players.add(BotPlayer())
            }
        }
    }
}

fun refreshPlayers(){
    val playersToRemove = mutableListOf<Player>()
    players.forEach {
        if (it.isDead)
            playersToRemove.add(it)
    }
    playersToRemove.forEach {
        players.remove(it)
    }
    players.forEach(Player::update)
    game!!.repaint()
}

class StartScreen : Game() {

    override fun paintComponent(g: Graphics) {
        super.backColor = Color.black
        super.lineColor = Color(211,211,211)
        super.paintComponent(g)
        g.color = Color.white
        g.fillRect((size.width / 12), (size.height / 6), ((5 * size.width) /6), ((2 * size.height) / 3))
        g.color = Color.black
        g.font = Font("Lucidia Console", Font.ITALIC + Font.BOLD, 200)
        g.drawString("TRON", (size.width / 9), (size.height / 2))
        g.font = Font("Lucidia Console", Font.PLAIN, 30)
        g.drawString("Jadon Fowler", (size.width / 6), (size.height / 2) + 35)
        g.drawString("Matthew Ormson", (size.width / 6), (size.height / 2) + 70)
    }

    override fun drawBackground(g: Graphics) {
        super.drawBackground(g)
    }

}

abstract class Game : JComponent() {
    var backColor = Color.black
    var lineColor = Color.lightGray

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        drawBackground(g)
        drawTails(g)
        players.forEach { it.draw(g) }
    }

    open fun drawBackground(g: Graphics) {
        g.color = backColor
        g.fillRect(0, 0, size.width, size.height)
        (0..BOARD_WIDTH).forEach {
            val p = it * 20 - 1
            g.color = lineColor
            g.drawLine(p, 0, p, SIZE)
            g.drawLine(0, p, SIZE, p)
        }
    }
    open fun drawTails(g: Graphics){
        (0..BOARD_WIDTH).forEach {
            val px = it * 20 - 1
            val x = it
            (0..BOARD_WIDTH).forEach {
                val py = it * 20 - 1
                val y = it

                players.forEach {
                    if (it.spots.contains(y * BOARD_WIDTH + x)) {
                        g.color = it.color
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
            if (it != this && it.spots.contains(spot))
                isDead = true
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
    override fun update() {
        x = Math.max(1, Math.min(input.dx + x, BOARD_WIDTH - 2))
        y = Math.max(1, Math.min(input.dy + y, BOARD_WIDTH - 2))
        val spot = y * BOARD_WIDTH + x
        players.forEach {
            if (it != this && it.spots.contains(spot))
                isDead = true
        }
        if (!spots.contains(spot))
            spots.add(spot)
    }

    override fun draw(g: Graphics) {
        g.color = color.darker()
        g.drawRoundRect(x * 20 - 6 - 3, y * 20 - 6, 6 * 2, 6 * 2, 7, 5)
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
