package fr.snoof.casse_briques

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity

// CLass MainActivity
class MainActivity : AppCompatActivity() {

    // Declare GameView
    private var gameView: GameView? = null

    // On Create
    @SuppressWarnings("deprecation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get screen size
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // Set view content
        gameView = GameView(this, screenWidth, screenHeight)
        setContentView(gameView)

        // Hide status and navigation bar
        window.insetsController?.let { controller ->
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    // On Resume
    override fun onResume() {
        super.onResume()
        gameView!!.resume()
    }

    // On Pause
    override fun onPause() {
        super.onPause()
        gameView!!.pause()
    }

    // On Stop
    override fun onStop() {
        super.onStop()
        gameView!!.stop()
    }

}
