package com.example.democustomcomponents

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.democustomcomponents.databinding.ActivityMainBinding
import `in`.mandeep_singh.walkmethrough.Placement
import `in`.mandeep_singh.walkmethrough.Walkthrough

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initialise()
    }

    private fun initialise() {
        binding.fileSelectionComponent.initialise(null, this, ::fileSelectionCallback)
        setupDemoCard()
        binding.walkthroughHelpButton.setOnClickListener { showWalkthrough() }
        binding.walkthroughHelpButton.bringToFront()

        if (shouldShowWalkthroughOnLaunch()) {
            binding.root.post { showWalkthrough() }
        }
    }

    private fun setupDemoCard() {
        binding.demoCard.apply {
            clipToOutline = true
            radius = 24f * resources.displayMetrics.density
        }
    }

    private fun shouldShowWalkthroughOnLaunch(): Boolean {
        return getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_WALKTHROUGH_SHOWN, false)
            .not()
    }

    private fun markWalkthroughShown() {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_WALKTHROUGH_SHOWN, true)
            .apply()
    }

    private fun showWalkthrough() {
        val selectionBox = binding.fileSelectionComponent.findViewById<android.view.View>(R.id.selection_box)
        val uploadIcon = binding.fileSelectionComponent.findViewById<android.view.View>(R.id.selection_icon_container)

        Walkthrough.with(this)
            .fullScreen(binding.root) {
                title = getString(R.string.walkthrough_welcome_title)
                description = getString(R.string.walkthrough_welcome_description)
                nextText = getString(R.string.walkthrough_welcome_next)
            }
            .card(binding.demoCard) {
                title = getString(R.string.walkthrough_card_title)
                description = getString(R.string.walkthrough_card_description)
                backText = getString(android.R.string.cancel)
                nextText = getString(R.string.walkthrough_next)
                placement = Placement.BOTTOM
            }
            .spotlight(selectionBox) {
                title = getString(R.string.walkthrough_spotlight_title)
                description = getString(R.string.walkthrough_spotlight_description)
            }
            .tooltip(uploadIcon) {
                title = getString(R.string.walkthrough_tooltip_title)
                description = getString(R.string.walkthrough_tooltip_description)
                nextText = getString(R.string.walkthrough_next)
                placement = Placement.BOTTOM
            }
            .banner(selectionBox) {
                title = getString(R.string.walkthrough_banner_title)
                description = getString(R.string.walkthrough_banner_description)
                backText = getString(android.R.string.cancel)
                nextText = getString(R.string.walkthrough_next)
            }
            .card(binding.walkthroughHelpButton) {
                title = getString(R.string.walkthrough_finish_title)
                description = getString(R.string.walkthrough_finish_description)
                backText = getString(android.R.string.cancel)
                nextText = getString(R.string.walkthrough_done)
                placement = Placement.BOTTOM
            }
            .doOnComplete { markWalkthroughShown() }
            .show()
    }

    private fun fileSelectionCallback(uri: Uri?) {
        uri?.let { Toast.makeText(this, it.toString(), Toast.LENGTH_SHORT).show() }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val PREFS_NAME = "demo_prefs"
        private const val KEY_WALKTHROUGH_SHOWN = "walkthrough_shown"
    }
}
