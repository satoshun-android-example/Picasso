package com.github.satoshun.example.main

import android.view.View
import android.widget.ImageView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun ImageView.load(
  url: String,
  builder: RequestCreator.() -> Unit = {}
) {
  val creator = Picasso.get()
    .load(url)
  creator.builder()

  getLifecycleCoroutineScope()!!.launch {
    println("started")
    load(creator)
    println("finished")
  }
}

private suspend fun ImageView.load(creator: RequestCreator) =
  suspendCancellableCoroutine<Unit> { continuation ->
    continuation.invokeOnCancellation {
      println("cancel")
      clear()
    }

    creator.into(this@load, object : Callback {
      override fun onSuccess() {
        println("onSuccess")
        continuation.resume(Unit)
      }

      override fun onError(e: Exception?) {
        continuation.resume(Unit)
      }
    })
  }

fun ImageView.clear() {
  Picasso.get().cancelRequest(this)
}

// TODO more smart way
// get Fragment lifecycle
internal fun View.getLifecycleCoroutineScope(): LifecycleCoroutineScope? =
  (context as? LifecycleOwner)?.lifecycleScope
