package com.example.rafapp.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.rafapp.ui.fragments.WeatherInfoFragment
import com.example.rafapp.ui.fragments.GraphFragment

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WeatherInfoFragment()
            1 -> GraphFragment()
            else -> WeatherInfoFragment()
        }
    }

    override fun getItemCount(): Int = 2
}
