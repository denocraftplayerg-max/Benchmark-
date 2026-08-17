package com.denocraft.anglebenchmark

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class FPSFragment : Fragment() {
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var fpsTextView: TextView
    private var frameCount = 0
    private var lastTime = System.currentTimeMillis()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val layout = inflater.inflate(R.layout.fragment_fps, container, false)
        fpsTextView = layout.findViewById(R.id.fpsText)
        glSurfaceView = layout.findViewById(R.id.glSurfaceView)
        
        glSurfaceView.setEGLContextClientVersion(3)
        glSurfaceView.setRenderer(object : GLSurfaceView.Renderer {
            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {}
            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {}
            override fun onDrawFrame(gl: GL10?) {
                frameCount++
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastTime >= 1000) {
                    val fps = frameCount * 1000.0 / (currentTime - lastTime)
                    activity?.runOnUiThread {
                        fpsTextView.text = String.format("FPS: %.2f", fps)
                    }
                    frameCount = 0
                    lastTime = currentTime
                }
            }
        })
        
        return layout
    }
    
    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }
}
