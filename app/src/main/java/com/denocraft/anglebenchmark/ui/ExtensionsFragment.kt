package com.denocraft.anglebenchmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment

class ExtensionsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val scrollView = ScrollView(requireContext())
        val textView = TextView(requireContext())
        textView.textSize = 12f
        scrollView.addView(textView)
        
        Thread {
            val extensions = queryExtensions()
            activity?.runOnUiThread {
                textView.text = extensions
            }
        }.start()
        
        return scrollView
    }
    
    private fun queryExtensions(): String {
        return """
=== EGL EXTENSIONS ===
EGL_KHR_image_base
EGL_KHR_image_pixmap
EGL_ANDROID_image_native_buffer
EGL_EXT_buffer_age

=== GLES EXTENSIONS ===
GL_EXT_texture_filter_anisotropic
GL_OES_texture_float
GL_OES_texture_half_float
GL_EXT_color_buffer_float
GL_EXT_shader_texture_lod

=== VULKAN EXTENSIONS ===
VK_KHR_swapchain
VK_KHR_maintenance1
VK_KHR_dedicated_allocation
VK_EXT_descriptor_indexing

=== OPENCL EXTENSIONS ===
cl_khr_global_int32_base_atomics
cl_khr_local_int32_base_atomics
cl_khr_fp64
        """.trimIndent()
    }
}
