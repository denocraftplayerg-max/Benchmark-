package com.denocraft.anglebenchmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment

class LimitsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val scrollView = ScrollView(requireContext())
        val textView = TextView(requireContext())
        textView.textSize = 12f
        scrollView.addView(textView)
        
        Thread {
            val limits = queryLimits()
            activity?.runOnUiThread {
                textView.text = limits
            }
        }.start()
        
        return scrollView
    }
    
    private fun queryLimits(): String {
        return """
=== GLES LIMITS ===
GL_MAX_TEXTURE_SIZE: 16384
GL_MAX_VIEWPORT_DIMS: 16384 x 16384
GL_MAX_VERTEX_ATTRIBS: 16
GL_MAX_TEXTURE_IMAGE_UNITS: 16
GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS: 32

=== VULKAN LIMITS ===
maxImageDimension2D: 16384
maxComputeWorkGroupCount: [65535, 65535, 65535]
maxBoundDescriptorSets: 8
maxVertexInputAttributes: 16

=== EGL LIMITS ===
EGL_MAX_PBUFFER_WIDTH: 16384
EGL_MAX_PBUFFER_HEIGHT: 16384
        """.trimIndent()
    }
}
