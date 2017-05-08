package org.jglr.voxlin.level

import org.jglr.voxlin.entity.Entity
import org.jglr.voxlin.render.ShaderProgram
import org.jglr.voxlin.render.RenderingPipeline
import org.jglr.voxlin.render.TextureRegion
import org.joml.Vector2f
import org.joml.Vector2i

class Level(val width: Int, val height: Int) {

    private val entities = mutableListOf<Entity>()
    private val entitiesToAdd = mutableListOf<Entity>()
    private val entitiesToRemove = mutableListOf<Entity>()
    private val tiles = Array(width) { IntArray(height) }
    private var updating = false
    private var dirtyTiles = true

    val playerSpawn = Vector2i()

    private operator fun Array<IntArray>.get(x: Int, y: Int): Int = this[x][y]
    private operator fun Array<IntArray>.set(x: Int, y: Int, value: Int) {
        this[x][y] = value
    }

    operator fun get(x: Int, y: Int) = getTile(x, y)

    fun getTile(x: Int, y: Int): Tile {
        if(x in 0..width-1 && y in 0..height-1) {
            return TileDictionary.getOrDefault(tiles[x, y], Invalid)
        }
        return OutOfBounds
    }

    operator fun set(x: Int, y: Int, tile: Tile) = setTile(x, y, tile)

    fun setTile(x: Int, y: Int, tile: Tile) {
        if(x in 0..width-1 && y in 0..height-1) {
            tiles[x, y] = tile.id
        } else {
            throw IllegalArgumentException("Out of bounds coordinates: $x;$y, size is: $width x $height")
        }
        dirtyTiles = true
    }

    fun getEntities() = entities

    fun removeEntity(entity: Entity) {
        if(updating)
            entitiesToRemove.add(entity)
        else
            entities.remove(entity)
    }

    fun addEntity(entity: Entity) {
        if(updating)
            entitiesToAdd.add(entity)
        else
            entities.add(entity)
    }

    fun update(delta: Float) {
        checkCollisions()
        updating = true
        entities.forEach {
            it.update(delta)
            if(it.isDead) {
                removeEntity(it)
            }
        }
        updating = false

        entities.addAll(entitiesToAdd)
        entities.removeAll(entitiesToRemove)
        entitiesToAdd.clear()
        entitiesToRemove.clear()
    }

    private fun checkCollisions() {
        entities.forEach(Entity::resetCollisions)
        for(e in entities) {
            val box = e.boundingBox
            entities
                    .filter { e != it && it.needsToRebuildCollisions() && box.intersects(it.boundingBox) }
                    .forEach {
                        e.addCollision(it)
                        it.addCollision(e)
                    }
        }
    }

    fun render(interpolation: Float, shader: ShaderProgram) {
        entities.forEach { it.render(interpolation, shader) }

        if(dirtyTiles) {
            dirtyTiles = false
            buildTileBatch()
        }
    }

    private fun buildTileBatch() {
        RenderingPipeline.resetTiles()
        for(j in 0..height-1) {
            for(i in 0..width-1) {
                val tile = getTile(i, j)
                if(tile != Air && tile != OutOfBounds && tile != Invalid) {
                    val region = getIcon(tile, i, j)

                    val x = i.toFloat()
                    val y = j.toFloat()
                    RenderingPipeline.tile(region, x, y)
                }
            }
        }
        RenderingPipeline.uploadTiles()
    }

    private fun getIcon(tile: Tile, x: Int, y: Int): TextureRegion {
        val slot = tile.tilesetArea
        fun getCoord(posX: Int, posY: Int): Vector2f {
            val slotMinU: Float = slot.minU
            val slotMinV: Float = slot.minV

            val u = (posX.toFloat() / TileTextures.width) + slotMinU
            val v = (posY.toFloat() / TileTextures.height) + slotMinV

            println("$u ; $v")
            return Vector2f(u, v)
        }

        val solidNeighbors = getConnections(x, y).map { it.map(Tile::solid) }
        val solidCount = solidNeighbors.map { it.filter { it }.count() }.reduce { acc, i -> acc+i }

        var posX = 1
        var posY = 1

        if( ! solidNeighbors[0][1] && ! solidNeighbors[1][0] && ! solidNeighbors[1][2] && ! solidNeighbors[2][1]) {// if no neighbors on all sides
            posX = 3
            posY = 3
        }
        val minUV = getCoord(posX*TILES_TO_PIXELS, posY*TILES_TO_PIXELS)
        val maxUV = getCoord(posX*TILES_TO_PIXELS+TILES_TO_PIXELS, posY*TILES_TO_PIXELS+TILES_TO_PIXELS)
        return TextureRegion(minUV.x, minUV.y, maxUV.x, maxUV.y)
    }

    private fun getConnections(x: Int, y: Int): List<List<Tile>> {
        val topRow = listOf(getTile(x-1, y+1), getTile(x, y+1), getTile(x+1, y+1))
        val midRow = listOf(getTile(x-1, y), getTile(x, y), getTile(x+1, y))
        val bottomRow = listOf(getTile(x-1, y-1), getTile(x, y-1), getTile(x+1, y-1))
        return listOf(topRow, midRow, bottomRow)
    }

    companion object {
        const val TILES_TO_PIXELS = 36
    }
}