package fr.snoof.casse_briques

import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // New SDK version
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Old SDK version
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
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
    override  fun onStop() {
        super.onStop()
        gameView!!.stop()
    }

}
