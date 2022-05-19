package com.ssafy.smartstore.ui.adapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.ssafy.smartstore.utils.view.getResourceId

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
    view.setImageResource(view.getResourceId(url))
}
