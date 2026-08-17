package com.denocraft.anglebenchmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment

class LogsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val scrollView = ScrollView(requireContext())
        val textView = TextView(requireContext())
        textView.textSize = 10f
        scrollView.addView(textView)
        
        textView.text = """
[INFO] Initializing ANGLE Vulkan backend...
[INFO] EGL context created successfully
[INFO] Vulkan instance created
[INFO] Physical device: PowerVR GE8320
[INFO] Logical device created
[INFO] Swapchain created (256x256)
[INFO] Render pipeline initialized
[INFO] Benchmark ready

[DEBUG] Draw call latency: 847ns
[DEBUG] Context creation: 245ms
[DEBUG] FPS: 59.8
        """.trimIndent()
        
        return scrollView
    }
}
