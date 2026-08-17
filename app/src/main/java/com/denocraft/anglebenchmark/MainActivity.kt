package com.denocraft.anglebenchmark

import android.os.Bundle
import android.widget.TextView
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val scrollView = ScrollView(this)
        val textView = TextView(this)
        textView.text = "Initializing Angle Benchmark Extreme...\n"
        textView.textSize = 14f
        scrollView.addView(textView)
        setContentView(scrollView)
        
        Thread {
            try {
                val runner = BenchmarkRunner()
                val result = runner.runNativeBenchmark()
                runOnUiThread {
                    textView.text = result
                }
            } catch (e: Exception) {
                runOnUiThread {
                    textView.text = "FATAL ERROR:\n${e.message}"
                }
            }
        }.start()
    }
}
