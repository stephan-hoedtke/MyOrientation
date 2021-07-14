package com.stho.myorientation

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class DocumentationViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val methods: List<Method> = listOf(
        Method.ComplementaryFilter,
        Method.ExtendedComplementaryFilter,
        Method.MadgwickFilter,
        Method.SeparatedCorrectionFilter,
    )

    override fun getItemCount(): Int =
        methods.size

    override fun createFragment(position: Int): Fragment =
        DocumentationFragment(methodFor(position))

    private fun methodFor(position: Int): Method =
        methods[position]

    internal fun positionFor(method: Method): Int =
        methods.indexOf(method).coerceAtLeast(0)
}