package com.github.satoshun.example.main.simple

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.github.satoshun.example.R
import com.github.satoshun.example.databinding.SimpleFragBinding
import com.github.satoshun.example.main.load

class SimpleFragment : Fragment(R.layout.simple_frag) {
  private lateinit var binding: SimpleFragBinding

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding = SimpleFragBinding.bind(view)

    binding.image1.load("https://pbs.twimg.com/profile_images/1227058593247064064/ssXXDIjO_400x400.jpg")

    binding.image2.load("https://assets.razerzone.com/eedownloads/desktop-wallpapers/1920x1080_Byte.jpg")
  }
}
