package tron

import java.awt.Color
import java.awt.Graphics
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

    game = Game(listOf(BotPlayer()))

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
        (0..(size.width / 20 + 1)).forEach {
            val px = it * 20 - 1
            val x = it
            (0..(size.height / 20 + 1)).forEach {
                val py = it * 20 - 1
                val y = it

                var owned = false
                players.forEach {
                    if (it.spots.contains(y * BOARD_WIDTH + x)) {
                        g.color = it.color
                        g.fillRect(px - 10, py - 10, 20, 20)
                        owned = true
                    }
                }
                if (!owned) {
                    g.color = Color.lightGray
                    g.drawLine(px + 10, py, px - 10, py)
                    g.drawLine(px, py + 10, px, py - 10)
                }
            }
        }
        (0..10).forEach {
            val x = it * 20
            (0..10).forEach {
                val y = it * 20
                g.drawString("${x / 20}", x, y)
            }
        }
    }

}

abstract class Player(val color: Color) {
    var x = 12
    var y = 12
    val spots = mutableListOf<Int>()

    abstract fun update()
    abstract fun draw(g: Graphics)
}

class BotPlayer : Player(Color.orange) {
    override fun update() {
        println("update $x $y")
        val dx =ThreadLocalRandom.current().nextInt(-1, 2)
        x = Math.max(0, x + dx)
        val dy = ThreadLocalRandom.current().nextInt(-1, 2)
        y = Math.max(0, y + (if(dx != 0) 0 else dy ))
        spots.add(y * BOARD_WIDTH + x)
    }

    override fun draw(g: Graphics) {
        g.color = Color.yellow
        (0..6).forEach {
            g.drawRoundRect(x * 20 - it - 3, y * 20 - it, it * 2, it * 2 + 6, 7, 5)
        }
    }
}