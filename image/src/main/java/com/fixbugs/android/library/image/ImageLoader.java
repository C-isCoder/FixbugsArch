package com.fixbugs.android.library.image;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.zhihu.matisse.engine.ImageEngine;

public class ImageLoader implements ImageEngine {
    @Override public void loadThumbnail(Context context, int resize, Drawable placeholder,
      ImageView imageView, Uri uri) {
        Glide.with(imageView)
          .load(uri)
          .apply(
            new RequestOptions().centerCrop().placeholder(placeholder).override(resize, resize))
          .into(imageView);
    }

    @Override public void loadGifThumbnail(Context context, int resize, Drawable placeholder,
      ImageView imageView, Uri uri) {
        Glide.with(imageView)
          .load(uri)
          .apply(
            new RequestOptions().centerCrop().placeholder(placeholder).override(resize, resize))
          .into(imageView);
    }

    @Override
    public void loadImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
        Glide.with(imageView)
          .load(uri)
          .apply(new RequestOptions().centerCrop().override(resizeX, resizeY))
          .into(imageView);
    }

    @Override
    public void loadGifImage(Context context, int resizeX, int resizeY, ImageView imageView,
      Uri uri) {
        Glide.with(imageView)
          .asGif()
          .load(uri)
          .apply(new RequestOptions().centerCrop().override(resizeX, resizeY))
          .into(imageView);
    }

    @Override public boolean supportAnimatedGif() {
        return true;
    }
}
