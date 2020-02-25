package com.github.satoshun.example.main

import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
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

  val scope = getLifecycleCoroutineScope()
  if (scope != null) {
    scope.launch { load(creator) }
  } else {
    creator.into(this)
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

internal fun View.getLifecycleCoroutineScope(): LifecycleCoroutineScope? {
  val fragment = findAttachFragment()
  if (fragment != null) {
    return fragment.viewLifecycleOwner.lifecycleScope
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

private fun findAllFragments(
  fragments: List<Fragment?>
): List<Fragment> {
  if (fragments.isEmpty()) return emptyList()
  val fragments = fragments.filter { it?.view != null }.filterNotNull()
  return fragments + findAllFragments(fragments.map { it.childFragmentManager.fragments }.flatten())
}
