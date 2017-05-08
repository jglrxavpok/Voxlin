package org.jglr.voxlin

import org.jglr.voxlin.entity.EnemyEntity
import org.jglr.voxlin.entity.PlayerEntity
import org.jglr.voxlin.entity.behaviors.*
import org.jglr.voxlin.entity.blueprints.PlayerBlueprint
import org.jglr.voxlin.input.ControllerHandler
import org.jglr.voxlin.level.Level
import org.jglr.voxlin.level.LevelLoader
import org.jglr.voxlin.render.*
import org.jglr.voxlin.utils.ResourceLocation
import org.joml.Matrix4f
import org.joml.Vector2f
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11.*

class Game {
    var isRunning: Boolean = false
        private set
    private var shader: ShaderProgram
    private val uiCamera: Camera
    private var texturePos: Vector2f

    // TEST
    private val gameCamera: Camera
    private val level: Level
    private val player: PlayerEntity
    private val playerController: PlayerControllerBehavior

    private val attackCoordinator: PlayerAttackCoordinator

    init {
        shader = ShaderProgram(ResourceLocation("shaders/blit.vert.glsl"), ResourceLocation("shaders/blit.frag.glsl"))
        isRunning = true

        gameCamera = Camera(Matrix4f().setOrtho(0f, screenWidth, 0f, screenHeight, -100f, 100f), shader)
        uiCamera = Camera(Matrix4f().setOrtho(0f, screenWidth, 0f, screenHeight, -100f, 100f), shader)

        shader.bind()
        shader.setUniformi(shader.getLocation("u_texture"), 0)

        texturePos = Vector2f()

        level = LevelLoader.loadLevel("test")
        player = PlayerEntity(level)
        PlayerBlueprint.apply(player)
        playerController = player.behaviors.get<PlayerControllerBehavior>()
        attackCoordinator = player.behaviors.get<PlayerAttackCoordinator>()
        player.behaviors.add(CameraLock(player, gameCamera))

        player.position.set(level.playerSpawn.x.toFloat(), level.playerSpawn.y.toFloat())
        val testEntity = EnemyEntity(level)
        testEntity.behaviors.add(MoveSlowlyTestBehavior(testEntity))
        testEntity.behaviors.add(ResetPositionOnContactTestBehavior(testEntity))
        testEntity.position.set(player.position)
        level.addEntity(testEntity)
        level.addEntity(player)

        ControllerHandler.prepare()
    }

    fun update(delta: Float) {
        ControllerHandler.update()

        if(ControllerHandler.hasJoystick(0)) {
            val leftX = ControllerHandler[0].axes[ControllerHandler.XBoxLeftX]
            playerController.releaseLeft()
            playerController.releaseRight()
            if(leftX < -0.1f) {
                playerController.pressLeft()
            } else if(leftX > 0.1f) {
                playerController.pressRight()
            }

            if(ControllerHandler[0].button(0)) {
                playerController.jump()
            }
        }

        level.update(delta)
    }

    fun render(interpolation: Float) {
        glClearColor(0.196078f, 0.6f, 0.8f, 1f) // sky blue
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDepthFunc(GL_LEQUAL)

        RenderingPipeline.gameCamera = gameCamera
        RenderingPipeline.uiCamera = uiCamera
        RenderingPipeline.shader = shader

        level.render(interpolation, shader)

        RenderingPipeline.render(shader)
    }

    fun onMouseMoved(xpos: Double, ypos: Double, dx: Double, dy: Double) {
    }

    fun keyRelease(key: Int, modifiers: Int) {
        if(key == GLFW.GLFW_KEY_LEFT) {
            playerController.releaseLeft()
        }

        if(key == GLFW.GLFW_KEY_RIGHT) {
            playerController.releaseRight()
        }

    }

    fun keyPress(key: Int, modifiers: Int) {
        if(key == GLFW.GLFW_KEY_LEFT) {
            playerController.pressLeft()
        }

        if(key == GLFW.GLFW_KEY_RIGHT) {
            playerController.pressRight()
        }

        if(key == GLFW.GLFW_KEY_UP) {
            playerController.jump()
        }

        if(key == GLFW.GLFW_KEY_X) {
            attackCoordinator.meleeAttack()
        }
        if(key == GLFW.GLFW_KEY_Z) {
            attackCoordinator.rangeAttack()
        }

        if(key == GLFW.GLFW_KEY_H) {
            player.attack(player, 0) // TODO: remove, only for debugging invulnerability frames
        }
    }

    fun resize(width: Int, height: Int) {
        glViewport(0, 0, width, height)
    }

    companion object {
        val screenWidth = VoxlinMain.WIDTH.toFloat()
        val screenHeight = VoxlinMain.HEIGHT.toFloat()
        var instance: Game? = null
        val aspectRatio: Float = screenWidth / screenHeight
    }
}
