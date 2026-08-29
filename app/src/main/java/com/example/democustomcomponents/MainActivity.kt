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
        binding.walkthroughHelpButton.setOnClickListener { showWalkthrough() }

        if (shouldShowWalkthroughOnLaunch()) {
            binding.root.post { showWalkthrough() }
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

        Walkthrough.with(this)
            .fullScreen(binding.root) {
                title = getString(R.string.walkthrough_welcome_title)
                description = getString(R.string.walkthrough_welcome_description)
                nextText = getString(R.string.walkthrough_welcome_next)
            }
            .card(binding.demoCard) {
                title = getString(R.string.walkthrough_demo_title)
                description = getString(R.string.walkthrough_demo_description)
                backText = getString(android.R.string.cancel)
                nextText = getString(R.string.walkthrough_file_next)
                placement = Placement.CENTER
            }
            .card(selectionBox) {
                title = getString(R.string.walkthrough_file_title)
                description = getString(R.string.walkthrough_file_description)
                backText = getString(android.R.string.cancel)
                nextText = getString(R.string.walkthrough_file_next)
                placement = Placement.BOTTOM
            }
            .tooltip(binding.fileSelectionComponent) {
                title = getString(R.string.walkthrough_selection_title)
                description = getString(R.string.walkthrough_selection_description)
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
