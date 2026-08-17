package com.denocraft.anglebenchmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class BenchmarkFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val layout = inflater.inflate(R.layout.fragment_benchmark, container, false)
        val resultText = layout.findViewById<TextView>(R.id.benchmarkResult)
        val runButton = layout.findViewById<Button>(R.id.runBenchmarkButton)
        
        runButton.setOnClickListener {
            resultText.text = "Running benchmark..."
            Thread {
                try {
                    val runner = BenchmarkRunner()
                    val result = runner.runNativeBenchmark()
                    activity?.runOnUiThread {
                        resultText.text = result
                    }
                } catch (e: Exception) {
                    activity?.runOnUiThread {
                        resultText.text = "ERROR: ${e.message}"
                    }
                }
            }.start()
        }
        
        return layout
    }
}
