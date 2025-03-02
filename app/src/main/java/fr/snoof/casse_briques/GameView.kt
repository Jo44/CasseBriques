package fr.snoof.casse_briques

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Runnable
import java.util.Random
import kotlin.math.pow
import kotlin.math.sqrt

// Class GameView
class GameView(context: Context, private var screenWidth: Int, private var screenHeight: Int) :
    SurfaceView(context), Runnable {

    // Class Brick
    private class Brick(
        var x: Float,
        var y: Float,
        var width: Float,
        var height: Float,
        var color: Int
    ) {
        var isBroken: Boolean = false
    }

    // Variables globales
    private var gameThread: Thread? = null
    private var isPlaying = false
    private var backgroundMusic: MediaPlayer
    private var soundPool: SoundPool
    private var winSound = 0
    private var looseSound = 0
    private var holder: SurfaceHolder? = null
    private var paint: Paint? = null
    private val slide = 250
    private var ballX = 0f
    private var ballY = 0f
    private var ballSpeedX = 0f
    private var ballSpeedY = 0f
    private var ballRadius = 0f
    private var paddleX = 0f
    private var paddleY = 0f
    private var paddleWidth = 0f
    private var paddleHeight = 0f
    private var paddleSpeed = 0f
    private val numRows = 6
    private val numCols = 6
    private var bricks: Array<Brick?> = arrayOfNulls(numRows * numCols)

    // Constructeur
    constructor(context: Context) : this(context, 0, 0) {
        screenWidth = 1000
        screenHeight = 2000
    }

    // Initilisation
    init {
        holder = getHolder()
        paint = Paint()

        // Initialisation des sons
        soundPool = SoundPool.Builder().setMaxStreams(5).build()
        winSound = soundPool.load(context, R.raw.win, 1)
        looseSound = soundPool.load(context, R.raw.loose, 1)

        // Initialisation de la musique de fond
        backgroundMusic = MediaPlayer.create(context, R.raw.music)
        backgroundMusic.isLooping = true
        backgroundMusic.setVolume(0.5f, 0.5f)
        backgroundMusic.start()

        // Initialisation de la balle
        ballRadius = 20f
        ballSpeedX = 12f
        ballSpeedY = 25f
        ballX = (screenWidth / 2).toFloat()
        ballY = (screenHeight / 2).toFloat()

        // Initialisation de la raquette
        paddleWidth = 200f
        paddleHeight = 20f
        paddleX = screenWidth / 2 - paddleWidth / 2
        paddleY = screenHeight - paddleHeight - slide

        // Initialisation des briques
        val brickWidth = (screenWidth - 110) / 6
        val brickHeight = 50
        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                bricks[row * numCols + col] = Brick(
                    (col * (brickWidth + 10) + 30).toFloat(),
                    (row * (brickHeight + 10) + 50).toFloat(),
                    brickWidth.toFloat(),
                    brickHeight.toFloat(),
                    Random().nextInt(4) + 1
                )
            }
        }
    }

    // Boucle principal d'exécution du jeu
    override fun run() {
        while (isPlaying) {
            update()
            if (isPlaying) {
                draw()
            }
            sleep()
        }
    }

    // Met à jour le jeu
    private fun update() {
        // Mise à jour de la balle
        ballX += ballSpeedX
        ballY -= ballSpeedY

        // Vérification de la fin de partie
        if (allBricksBroken()) {
            // Condition de victoire
            backgroundMusic.stop()
            backgroundMusic.release()
            soundPool.play(winSound, 1f, 1f, 0, 0, 1f)
            displayGameOverMessage("Gagné")
        } else if (ballY > screenHeight - slide) {
            // Condition de défaite
            backgroundMusic.stop()
            backgroundMusic.release()
            soundPool.play(looseSound, 1f, 1f, 0, 0, 1f)
            displayGameOverMessage("Perdu")
        } else {
            // Vérification des collisions

            // Collision avec les murs
            if (ballX < ballRadius || ballX > screenWidth - ballRadius) {
                ballSpeedX = -ballSpeedX
            }
            if (ballY < ballRadius) {
                ballSpeedY = -ballSpeedY
            }

            // Collision avec la raquette
            if (ballY + ballRadius >= paddleY && ballY - ballRadius <= paddleY + paddleHeight && ballX + ballRadius >= paddleX && ballX - ballRadius <= paddleX + paddleWidth) {
                ballSpeedY = -ballSpeedY
                // Ajoute un effet à la balle
                ballSpeedX -= paddleSpeed * 0.5f
                ballSpeedX = ballSpeedX.coerceIn(-20f, 20f)
            }

            // Collision avec les briques
            for (brick in bricks) {
                if (!brick!!.isBroken) {
                    // Calcul de la distance entre la balle et la brique
                    val closestX = ballX.coerceIn(brick.x, brick.x + brick.width)
                    val closestY = ballY.coerceIn(brick.y, brick.y + brick.height)
                    val distance = sqrt(
                        (ballX - closestX).toDouble().pow(2.0) + (ballY - closestY).toDouble()
                            .pow(2.0)
                    )
                    if (distance <= ballRadius) {
                        // Collision détectée
                        brick.isBroken = true
                        ballSpeedY = -ballSpeedY
                        break
                    }
                }
            }
        }
    }

    // Détermine si toutes les briques ont été cassées
    private fun allBricksBroken(): Boolean {
        for (brick in bricks) {
            if (!brick!!.isBroken) {
                return false
            }
        }
        return true
    }

    // Affiche le message de fin de jeu et termine l'application
    private fun displayGameOverMessage(message: String) {
        isPlaying = false

        // Dessine le message de fin
        val canvas = holder!!.lockCanvas()
        canvas.drawColor(Color.BLACK)
        paint!!.color = Color.WHITE
        paint!!.textSize = 60f
        val textWidth = paint!!.measureText(message)
        canvas.drawText(
            message, canvas.width / 2 - textWidth / 2,
            (canvas.height / 2).toFloat(), paint!!
        )
        holder!!.unlockCanvasAndPost(canvas)

        // Attends 3sec et termine l'application
        Handler(Looper.getMainLooper()).postDelayed({
            (context as Activity).finish()
        }, 3000)
    }

    // Dessine le jeu
    private fun draw() {
        if (holder!!.surface.isValid) {
            val canvas = holder!!.lockCanvas()
            canvas.drawColor(Color.BLACK)

            // Dessine le fond
            paint!!.color = ContextCompat.getColor(context, R.color.darkgray)
            canvas.drawRect(
                0F,
                0F,
                screenWidth.toFloat(),
                (screenHeight - slide + 30).toFloat(),
                paint!!
            )

            // Dessine la barre de fin de jeu
            paint!!.color = Color.WHITE
            canvas.drawRect(
                0F,
                (screenHeight - slide + 30).toFloat(),
                screenWidth.toFloat(),
                (screenHeight - slide + 30).toFloat() + 2,
                paint!!
            )

            // Dessine les briques
            for (brick in bricks) {
                if (!brick!!.isBroken) {
                    when (brick.color) {
                        1 -> {
                            paint!!.color = Color.RED
                        }

                        2 -> {
                            paint!!.color = Color.GREEN
                        }

                        3 -> {
                            paint!!.color = Color.BLUE
                        }

                        4 -> {
                            paint!!.color = Color.YELLOW
                        }
                    }
                    canvas.drawRect(
                        brick.x, brick.y, brick.x + brick.width, brick.y + brick.height,
                        paint!!
                    )
                }
            }

            // Dessine la balle
            paint!!.color = Color.WHITE
            canvas.drawCircle(ballX, ballY, ballRadius, paint!!)

            // Dessine la raquette
            paint!!.color = Color.RED
            canvas.drawRect(
                paddleX, paddleY, paddleX + paddleWidth, paddleY + paddleHeight,
                paint!!
            )

            holder!!.unlockCanvasAndPost(canvas)
        } else {
            Log.d("GameView", "Surface is not valid")
        }
    }

    // Gestion du déplacement du paddle
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE) {
            val newPaddleX = event.x - paddleWidth / 2f
            paddleSpeed = newPaddleX - paddleX
            paddleX = newPaddleX
        }
        return true
    }

    // Sleep
    private fun sleep() {
        try {
            Thread.sleep(16) // ~60 FPS
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        }
    }

    // Resume
    fun resume() {
        isPlaying = true
        if (!backgroundMusic.isPlaying) {
            backgroundMusic.start()
        }
        gameThread = Thread(this)
        gameThread!!.start()
    }

    // Pause
    fun pause() {
        isPlaying = false
        if (backgroundMusic.isPlaying) {
            backgroundMusic.pause()
        }
        try {
            gameThread!!.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    // Stop
    fun stop() {
        if (backgroundMusic.isPlaying) {
            backgroundMusic.stop()
            backgroundMusic.release()
            soundPool.release()
        }
    }

}
