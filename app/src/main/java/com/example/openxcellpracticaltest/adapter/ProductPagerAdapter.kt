package com.example.openxcellpracticaltest.adapter

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.openxcellpracticaltest.ui.fragment.MapFragment
import com.example.openxcellpracticaltest.ui.fragment.ProductListFragment

class ProductPagerAdapter(
    private val application: Application,
    supportFragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val tabCount: Int
) : FragmentStateAdapter(supportFragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return tabCount
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MapFragment(application)
            1 -> ProductListFragment(application)
            else -> null!!
        }
    }
}
