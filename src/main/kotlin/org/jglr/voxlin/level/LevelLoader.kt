package org.jglr.voxlin.level

import javax.imageio.ImageIO

object LevelLoader {

    const val PLAYER_SPAWN = (0xFFFF00DC).toInt()
    const val GROUND_TILE = (0xFF000000).toInt()

    fun loadLevel(name: String): Level {
        val image = ImageIO.read(javaClass.getResourceAsStream("/levels/$name.png"))
        val width = image.width
        val height = image.height
        val result = Level(width, height)
        for(i in 0 until height) {
            for(j in 0 until width) {
                val x = j
                val y = height-i-1
                val color = image.getRGB(j, i)
                result[x, y] = Air
                when(color) {
                    PLAYER_SPAWN -> result.playerSpawn.set(x, y)
                    GROUND_TILE -> result[x, y] = Ground
                }
            }
        }
        return result
    }
}