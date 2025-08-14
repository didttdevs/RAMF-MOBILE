package com.example.rafapp.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.rafapp.ui.fragments.GraphFragment
import com.example.rafapp.ui.fragments.WeatherInfoFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val fragmentList = listOf(
        WeatherInfoFragment(),
        GraphFragment()
    )

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment = fragmentList[position]

    fun getFragmentAt(position: Int): Fragment? {
        return if (position in fragmentList.indices) fragmentList[position] else null
    }
}
