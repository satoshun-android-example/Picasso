package com.github.satoshun.example.main

import android.widget.ImageView
import com.squareup.picasso.Picasso

fun ImageView.load() {
  Picasso.get()
    .load("https://pbs.twimg.com/profile_images/1227058593247064064/ssXXDIjO_400x400.jpg")
    .into(this)
}
