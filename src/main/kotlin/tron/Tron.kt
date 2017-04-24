package tron

import java.awt.Color
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

fun main(args: Array<String>) {
    val frame = JFrame("it's tron")
    frame.setSize(SIZE, SIZE)
    frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    val inputListener = UserInput()
    val player = UserPlayer(inputListener)
    frame.addKeyListener(inputListener)

    game = Game(listOf(BotPlayer(), player))

    frame.add(game)
    frame.isVisible = true

    game!!.paint(game!!.graphics)
    fixedRateTimer(name = "MainThread", period = 100){
        println("tick")
        game!!.players.forEach(Player::update)
        game!!.repaint()
    }
}

class Game(val players: List<Player>) : JComponent() {

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        drawBackground(g)
        players.forEach { it.draw(g) }
    }

    fun drawBackground(g: Graphics) {
        g.color = Color.black
        g.fillRect(0, 0, size.width, size.height)
        (0..BOARD_WIDTH).forEach {
            val p = it * 20 - 1
            g.color = Color.lightGray
            g.drawLine(p, 0, p, SIZE)
            g.drawLine(0, p, SIZE, p)
        }
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
    var x = ThreadLocalRandom.current().nextInt(2, BOARD_WIDTH - 2)
    var y = ThreadLocalRandom.current().nextInt(2, BOARD_WIDTH - 2)
    val spots = mutableListOf<Int>()

    abstract fun update()
    abstract fun draw(g: Graphics)
}

class BotPlayer : Player(Color.orange) {
    var dx = 0
    var dy = -1
    var tick = 0

    override fun update() {
        tick++
        if (tick % 3 == 0) {
            dx = ThreadLocalRandom.current().nextInt(-1, 2)
            dy = if (dx != 0) 0 else ThreadLocalRandom.current().nextInt(-1, 2)

        }
        x = Math.max(1, Math.min(dx + x, BOARD_WIDTH - 1))
        y = Math.max(1, Math.min(dy + y, BOARD_WIDTH - 1))

        val spot = y * BOARD_WIDTH + x
        if (!spots.contains(spot)) {
            spots.add(spot)
        } else {
        }
    }

    override fun draw(g: Graphics) {
        g.color = color.darker()
        g.drawRoundRect(x * 20 - 6 - 3, y * 20 - 6, 6 * 2, 6 * 2, 7, 5)
    }
}

fun randomColor(): Color = when (ThreadLocalRandom.current().nextInt(1, 6)) {
    1 -> Color.red
    2 -> Color.blue
    3 -> Color.green
    4 -> Color.magenta
    5 -> Color.pink
    else -> Color.cyan
}

class UserPlayer(val input: UserInput) : Player(randomColor()) {
    override fun update() {
        x += input.dx
        y += input.dy
        val spot = y * BOARD_WIDTH + x
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
