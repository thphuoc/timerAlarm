package com.example.fossilalarm.screens.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.Unbinder
import com.example.fossilalarm.R

open class BaseFragment : Fragment() {
    open val layoutResId: Int = R.layout.fragment_home
    private lateinit var unBinder: Unbinder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contentView = LayoutInflater
            .from(context)
            .inflate(layoutResId, container, false)
        unBinder = ButterKnife.bind(this, contentView)
        return contentView
    }
}