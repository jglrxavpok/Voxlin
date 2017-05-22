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

    private val NORTH_BIT = 1
    private val SOUTH_BIT = 2
    private val WEST_BIT = 4
    private val EAST_BIT = 8

    // unused
    private val NORTH_EAST_BIT = 16
    private val NORTH_WEST_BIT = 32
    private val SOUTH_EAST_BIT = 64
    private val SOUTH_WEST_BIT = 128

    // TODO: val
    private var connectionLookup = hashMapOf(Pair(NORTH_BIT, Pair(3, 2)),
            Pair(NORTH_BIT or SOUTH_BIT, Pair(3, 1)),
            Pair(SOUTH_BIT, Pair(3, 0)),
            Pair(WEST_BIT, Pair(2, 3)),
            Pair(EAST_BIT, Pair(0, 3)),
            Pair(EAST_BIT or WEST_BIT, Pair(1, 3))
    )

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
        connectionLookup = hashMapOf(
                Pair(0, Pair(3, 3)),
                Pair(NORTH_BIT, Pair(3, 2)),
                Pair(SOUTH_BIT, Pair(3, 0)),
                Pair(WEST_BIT, Pair(2, 3)),
                Pair(EAST_BIT, Pair(0, 3)),
                Pair(NORTH_BIT or SOUTH_BIT, Pair(3, 1)),
                Pair(EAST_BIT or WEST_BIT, Pair(1, 3)),
                Pair(NORTH_BIT or SOUTH_BIT or EAST_BIT, Pair(0, 1)),
                Pair(NORTH_BIT or SOUTH_BIT or WEST_BIT, Pair(2, 1)),
                Pair(SOUTH_BIT or EAST_BIT, Pair(0, 0)),
                Pair(SOUTH_BIT or WEST_BIT, Pair(2, 0)),
                Pair(NORTH_BIT or EAST_BIT, Pair(0, 2)),
                Pair(NORTH_BIT or WEST_BIT, Pair(2, 2)),
                Pair(SOUTH_BIT or EAST_BIT or WEST_BIT, Pair(1, 0)),
                Pair(NORTH_BIT or EAST_BIT or WEST_BIT, Pair(1, 2)),
                Pair(NORTH_BIT or EAST_BIT or WEST_BIT or SOUTH_BIT, Pair(1, 1))

        )

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

            val u = ((posX.toFloat()) / TileTextures.width) + slotMinU
            val v = (posY.toFloat() / TileTextures.height) + slotMinV
            return Vector2f(u, v)
        }


        val connectionField = getConnections(x, y)
        if(connectionField != 0)
            println(connectionField)
        val (posX, posY) = connectionLookup.getOrDefault(connectionField, Pair(0, 0))

        val minUV = getCoord(posX*TILES_TO_PIXELS, posY*TILES_TO_PIXELS)
        val maxUV = getCoord(posX*TILES_TO_PIXELS+TILES_TO_PIXELS, posY*TILES_TO_PIXELS+TILES_TO_PIXELS)
        return TextureRegion(minUV.x, minUV.y, maxUV.x, maxUV.y)
    }

    private fun getConnections(x: Int, y: Int): Int {
        var bitfield = 0
        val centerTile = this[x, y]
        fun canConnectTo(other: Tile): Boolean {
            // TODO: Connect different tile types?
            return other.solid && centerTile.solid
        }
        if(canConnectTo(this[x, y+1])) {
            bitfield = bitfield or NORTH_BIT
        }
        if(canConnectTo(this[x, y-1])) {
            bitfield = bitfield or SOUTH_BIT
        }
        if(canConnectTo(this[x+1, y])) {
            bitfield = bitfield or EAST_BIT
        }
        if(canConnectTo(this[x-1, y])) {
            bitfield = bitfield or WEST_BIT
        }
        return bitfield
    }

    companion object {
        const val TILES_TO_PIXELS = 36
    }

    fun debugAskRebuildBatch() {
        dirtyTiles = true
    }
}