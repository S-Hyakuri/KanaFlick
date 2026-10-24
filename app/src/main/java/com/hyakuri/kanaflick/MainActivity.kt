package com.hyakuri.kanaflick

import android.os.Bundle
import android.os.Build
import android.animation.ValueAnimator
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.widget.ImageView
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.hyakuri.kanaflick.ui.KanaFlickApp
import com.hyakuri.kanaflick.ui.theme.KanaFlickTheme
import com.hyakuri.kanaflick.ui.startup.LogoDrawable
import com.hyakuri.kanaflick.ui.startup.LogoMotion
import com.hyakuri.kanaflick.ui.startup.WordmarkDrawable
import com.hyakuri.kanaflick.ui.startup.SplashWordmarkLayout

class MainActivity : ComponentActivity() {
    private var contentReady = false
    private var logoAnimator: ValueAnimator? = null
    private var removeSplash: (() -> Unit)? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        // Start motion on the first app frame while vocabulary IO runs underneath.
        // Reduced-motion and restored launches still cover actual loading.
        val playLogo = savedInstanceState == null && ValueAnimator.areAnimatorsEnabled()
        splash.setKeepOnScreenCondition { !playLogo && !contentReady }
        splash.setOnExitAnimationListener { provider ->
            // Android may deliver an iconless view on warm/package-replacement starts.
            val rawIcon = runCatching { provider.iconView }.getOrNull()
            val icon = rawIcon as? ImageView
            if (savedInstanceState != null || !ValueAnimator.areAnimatorsEnabled() || icon == null) {
                provider.remove()
            } else {
                val logo = LogoDrawable(icon.drawable?.intrinsicWidth ?: -1,
                    if (Build.VERSION.SDK_INT >= 31) LogoMotion.PLATFORM_ICON_NORMALIZATION else 1f)
                icon.setImageDrawable(logo)
                val wordmark = WordmarkDrawable()
                val host = provider.view as? ViewGroup
                if (host != null) SplashWordmarkLayout.attach(host, icon, wordmark)
                removeSplash = { provider.remove() }
                logoAnimator = ValueAnimator.ofFloat(0f, (LogoMotion.DURATION + LogoMotion.FADE).toFloat()).apply {
                    duration = LogoMotion.DURATION + LogoMotion.FADE
                    interpolator = LinearInterpolator()
                    addUpdateListener {
                        val rawTime = it.animatedValue as Float
                        val waitingForContent = !contentReady && rawTime >= LogoMotion.DURATION
                        val time = if (waitingForContent) LogoMotion.DURATION.toFloat() else rawTime
                        if (waitingForContent && !it.isPaused) {
                            // Hold the fully formed brand, never a half-transparent loading page.
                            it.pause()
                            it.currentPlayTime = LogoMotion.DURATION
                        }
                        logo.elapsed = time.coerceAtMost(LogoMotion.DURATION.toFloat())
                        wordmark.elapsed = time
                        val fade = ((time - LogoMotion.DURATION) / LogoMotion.FADE).coerceIn(0f, 1f)
                        // One frame clock for motion + fade; no second animator or transparent gap.
                        provider.view.alpha = 1f - fade * fade * (3 - 2 * fade)
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            removeSplash?.invoke(); removeSplash = null
                            logoAnimator = null
                        }
                    })
                    start()
                }
            }
        }
        setContent { KanaFlickTheme { KanaFlickApp(onReady = {
            contentReady = true
            logoAnimator?.takeIf { it.isPaused }?.resume()
        }) } }
    }
    override fun onStop() {
        logoAnimator?.removeAllListeners(); logoAnimator?.cancel(); logoAnimator = null
        removeSplash?.invoke(); removeSplash = null
        super.onStop()
    }
}


