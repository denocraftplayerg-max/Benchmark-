package com.denocraft.anglebenchmark

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        
        viewPager.adapter = BenchmarkPagerAdapter(this)
        
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> "FPS"
                1 -> "Benchmark"
                2 -> "Extensões"
                3 -> "Limites"
                4 -> "Logs"
                else -> "Tab $position"
            }
        }.attach()
    }
}

class BenchmarkPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 5
    
    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> FPSFragment()
            1 -> BenchmarkFragment()
            2 -> ExtensionsFragment()
            3 -> LimitsFragment()
            4 -> LogsFragment()
            else -> FPSFragment()
        }
    }
}
