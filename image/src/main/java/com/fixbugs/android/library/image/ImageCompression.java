package com.fixbugs.android.library.image;

import android.net.Uri;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileCallback;
import com.zxy.tiny.common.FileResult;

public class ImageCompression {

    public static String syncFromUri(Uri uri) {
        Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
        FileResult fileResult = Tiny.getInstance().source(uri.getPath())
          .asFile().withOptions(options).compressSync();
        return fileResult.outfile;
    }

    public static void asyncFromUri(Uri uri, final Callback callback) {
        Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
        Tiny.getInstance()
          .source(uri.getPath())
          .asFile()
          .withOptions(options)
          .compress(new FileCallback() {
              @Override
              public void callback(boolean isSuccess, String outfile, Throwable t) {
                  callback.result(outfile);
              }
          });
    }

    public static String syncFromPath(String path) {
        Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
        FileResult fileResult = Tiny.getInstance().source(path)
          .asFile().withOptions(options).compressSync();
        return fileResult.outfile;
    }

    public static void asyncFromPaht(String path, final Callback callback) {
        Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
        Tiny.getInstance()
          .source(path)
          .asFile()
          .withOptions(options)
          .compress(new FileCallback() {
              @Override
              public void callback(boolean isSuccess, String outfile, Throwable t) {
                  callback.result(outfile);
              }
          });
    }

    public interface Callback {
        void result(String outfile);
    }
}
