package com.github.satoshun.example.main

import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

fun ImageView.load(
  url: String,
  callback: Callback = EmptyCallback,
  builder: RequestCreator.() -> Unit = {}
) {
  val creator = Picasso.get().load(url)
  creator.builder()
  creator.load(this, callback)
}

fun ImageView.load(
  uri: Uri,
  callback: Callback = EmptyCallback,
  builder: RequestCreator.() -> Unit = {}
) {
  val creator = Picasso.get().load(uri)
  creator.builder()
  creator.load(this, callback)
}

fun ImageView.load(
  file: File,
  callback: Callback = EmptyCallback,
  builder: RequestCreator.() -> Unit = {}
) {
  val creator = Picasso.get().load(file)
  creator.builder()
  creator.load(this, callback)
}

fun ImageView.load(
  @DrawableRes resourceId: Int,
  callback: Callback = EmptyCallback,
  builder: RequestCreator.() -> Unit = {}
) {
  val creator = Picasso.get().load(resourceId)
  creator.builder()
  creator.load(this, callback)
}

private object EmptyCallback : Callback {
  override fun onSuccess() {}
  override fun onError(e: java.lang.Exception?) {}
}

private fun RequestCreator.load(target: ImageView, callback: Callback) {
  val scope = target.getLifecycleCoroutineScope()
  if (scope != null) {
    scope.launch { loadAwait(target, callback) }
  } else {
    into(target, callback)
  }
}

private suspend fun RequestCreator.loadAwait(target: ImageView, callback: Callback) =
  suspendCancellableCoroutine<Unit> { continuation ->
    continuation.invokeOnCancellation { target.clear() }

    into(target, object : Callback {
      override fun onSuccess() {
        continuation.resume(Unit)
        callback.onSuccess()
      }

      override fun onError(e: Exception?) {
        continuation.resume(Unit)
        callback.onError(e)
      }
    })
  }

fun ImageView.clear() {
  Picasso.get().cancelRequest(this)
}

private fun View.getLifecycleCoroutineScope(): LifecycleCoroutineScope? {
  findViewTreeLifecycleOwner()?.let {
    return it.lifecycleScope
  }
  findAttachFragment()?.let {
    return it.viewLifecycleOwner.lifecycleScope
  }
  return (context as? LifecycleOwner)?.lifecycleScope
}

private fun View.findAttachFragment(): Fragment? {
  val activity = context as? FragmentActivity ?: return null
  val allFragments = findAllFragments(activity.supportFragmentManager.fragments)

  val root = activity.findViewById<View>(android.R.id.content)
  var result: Fragment? = null
  var current = this
  while (current != root) {
    result = allFragments.firstOrNull { it.view == current }
    if (result != null) break
    current = current.parent as? View ?: break
  }
  return result
}

private fun findAllFragments(candidates: List<Fragment?>): List<Fragment> {
  if (candidates.isEmpty()) return emptyList()
  val fragments = candidates.filter { it?.view != null }.filterNotNull()
  return fragments + findAllFragments(fragments.map { it.childFragmentManager.fragments }.flatten())
}
