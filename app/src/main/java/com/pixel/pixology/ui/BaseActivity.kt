package com.pixel.pixology.ui
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    private var loader: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    protected fun showLoader() {
        // Create a new ProgressBar and set its properties
        loader = ProgressBar(this)
        loader?.isIndeterminate = true

        // Set the ProgressBar as the content view, replacing the current layout
        setContentView(loader, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    protected fun hideLoader() {
        // Set the original layout as the content view
        setContentView(View(this))
    }

    protected fun startLoader() {
        showLoader()

        // Delay the hiding of the loader for 2 seconds
        Handler().postDelayed({
            hideLoader()
        }, 2000)
    }
}
