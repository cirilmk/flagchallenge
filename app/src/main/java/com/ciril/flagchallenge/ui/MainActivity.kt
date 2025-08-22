package com.ciril.flagchallenge.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.ciril.flagchallenge.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}
