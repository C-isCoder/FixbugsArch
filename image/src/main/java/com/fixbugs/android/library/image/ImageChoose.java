package com.fixbugs.android.library.image;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.wildma.idcardcamera.camera.CameraActivity;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

@SuppressLint("ValidFragment")
public class ImageChoose extends BottomSheetDialogFragment
  implements View.OnClickListener {
    private static final String TAG = ImageChoose.class.getSimpleName();

    private static final int REQUEST_CODE_CHOOSE = 10010;
    private static final int REQUEST_CODE_CORP = 123;
    private static final int REQUEST_CODE_CHOOSE_SYSTEM = 10086;
    private static final int REQUEST_PERMISSION_CODE = 369;
    private static final int REQUEST_TAKE_PHOTO = 361;

    private ImageChooseListener mListener;
    private int maxCount;
    private Context mContext;
    private Activity mActivity;
    private int type;
    private boolean needCrop;
    private boolean needFree;
    private int[] aspect;
    private File mTempFile;
    private Uri mCurrentPhotoURI;
    private boolean isIdCardFront;
    private boolean isIdCardBack;

    private ImageChoose(int maxCount, boolean isIdCardFront, boolean isIdCardBack, boolean needFree,
      boolean needCorp,
      int[] aspect,
      ImageChooseListener listener) {
        this.aspect = aspect;
        this.maxCount = maxCount;
        this.needCrop = needCorp;
        this.needFree = needFree;
        this.isIdCardFront = isIdCardFront;
        this.isIdCardBack = isIdCardBack;
        if (listener == null) throw new NullPointerException("ImageChooseListener not null.");
        mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mContext = getContext();
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo_select_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.photo_select_btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.photo_select_btn_image).setOnClickListener(this);
        view.findViewById(R.id.photo_select_btn_take).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.photo_select_btn_cancel) {
            dismiss();
        } else if (id == R.id.photo_select_btn_image) {
            type = 1;
            if (checkPermission()) {
                selectImage();
            } else {
                requestPermission();
            }
        } else if (id == R.id.photo_select_btn_take) {
            type = 2;
            if (checkPermission()) {
                dispatchTakePictureIntent();
            } else {
                requestPermission();
            }
        }
    }

    public void image() {
        type = 1;
        if (checkPermission()) {
            selectImage();
        } else {
            requestPermission();
        }
    }

    private void requestPermission() {
        requestPermissions(new String[] {
          Manifest.permission.CAMERA,
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_EXTERNAL_STORAGE
        }, REQUEST_PERMISSION_CODE);
    }

    public void selectImage() {
        if (needCrop) {
            Intent intent = new Intent(Intent.ACTION_PICK, null);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(intent, REQUEST_CODE_CHOOSE_SYSTEM);
        } else {
            Matisse.from(this)
              .choose(MimeType.ofImage())
              .originalEnable(true)
              .countable(true)
              .maxSelectable(maxCount)
              .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
              .thumbnailScale(0.85f)
              .imageEngine(new ImageLoader())
              .forResult(REQUEST_CODE_CHOOSE);
        }
    }

    public void show(FragmentManager manager) {
        show(manager, "Photo");
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(mContext,
          Manifest.permission.READ_EXTERNAL_STORAGE)
          == PackageManager.PERMISSION_GRANTED
          && ContextCompat.checkSelfPermission(mContext,
          Manifest.permission.WRITE_EXTERNAL_STORAGE)
          == PackageManager.PERMISSION_GRANTED
          && ContextCompat.checkSelfPermission(mContext,
          Manifest.permission.CAMERA)
          == PackageManager.PERMISSION_GRANTED;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private void dispatchTakePictureIntent() {
        if (isIdCardFront) {
            CameraActivity.toCameraActivity(mActivity, CameraActivity.TYPE_IDCARD_FRONT);
        } else if (isIdCardBack) {
            CameraActivity.toCameraActivity(mActivity, CameraActivity.TYPE_IDCARD_BACK);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
                try {
                    mTempFile = createImageFile();
                } catch (IOException ignored) {
                }
                if (mTempFile != null) {
                    mCurrentPhotoURI = FileProvider.getUriForFile(mContext,
                      mContext.getPackageName() + ".FileProvider", mTempFile);
                    Log.i(TAG, "take photo uri: " + mCurrentPhotoURI);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCurrentPhotoURI);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<Uri> list = Matisse.obtainResult(data);
            List<Uri> uris = new ArrayList<>();
            for (Uri uri : list) {
                uris.add(Uri.parse(ImagePath.getRealPathFromUri(mContext, uri)));
            }
            mListener.result(uris.toArray(new Uri[] {}));
            dismiss();
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if (!needCrop) {
                mListener.result(Uri.fromFile(mTempFile));
                dismiss();
            } else {
                crop(null);
            }
        } else if (requestCode == REQUEST_CODE_CHOOSE_SYSTEM && resultCode == RESULT_OK) {
            crop(data.getData());
        } else if (requestCode == REQUEST_CODE_CORP && resultCode == RESULT_OK) {
            mListener.result(Uri.fromFile(mTempFile));
            dismiss();
        } else if (requestCode == CameraActivity.REQUEST_CODE
          && resultCode == CameraActivity.RESULT_CODE) {
            final String path = CameraActivity.getImagePath(data);
            Log.i(TAG, "id card path: " + path);
            mListener.result(Uri.parse(path));
            dismiss();
        }
    }

    private void crop(Uri uri) {
        Log.i(TAG, "crop uri: " + uri);
        try {
            mTempFile = createImageFile();
        } catch (IOException ignored) {
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri == null ? mCurrentPhotoURI : uri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // 该参数可以不设定用来规定裁剪区的宽高比
        if (!needFree) {
            intent.putExtra("aspectX", aspect[0]);
            intent.putExtra("aspectY", aspect[1]);
        }
        // 该参数设定为你的imageView的大小
        if (!needFree) {
            intent.putExtra("outputX", 600);
            intent.putExtra("outputY", 600);
        }
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempFile));
        // 输出图片的格式
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        // 头像识别
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, REQUEST_CODE_CORP);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            boolean ok = true;
            for (int grantResult : grantResults) {
                if (grantResult != PermissionChecker.PERMISSION_GRANTED) {
                    ok = false;
                }
            }
            if (ok) {
                if (type == 1) {
                    selectImage();
                } else {
                    dispatchTakePictureIntent();
                }
            } else {
                dismiss();
                Toast.makeText(getContext(), "没有权限，请到设置-应用程序管理，给予权限。", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public interface ImageChooseListener {
        void result(Uri... uris);
    }

    public static class Builder {
        private int maxCount = 1;
        private int[] aspect = new int[] { 1, 1 };
        private ImageChooseListener mListener;
        private boolean needCrop = false;
        private boolean needFree = false;
        private boolean isIdCardFront = false;
        private boolean isIdCardBack = false;

        /**
         * 最大图片数量
         *
         * @param count 默认 1
         * @return this
         */
        public Builder setMaxCount(int count) {
            maxCount = count;
            return this;
        }

        /**
         * 是否需要裁剪 默认不裁剪
         *
         * @return this
         */
        public Builder needCrop() {
            needCrop = true;
            return this;
        }

        /**
         * 是否自由裁剪 默认不自由裁剪
         *
         * @return this
         */
        public Builder needFreeCrop() {
            this.needCrop = true;
            this.needFree = true;
            return this;
        }

        /**
         * 设置图片裁剪比例
         * <p>
         * 需要调用 {@link #needCrop()} 才会生效
         * 于 {@link #needFreeCrop()} 互斥
         *
         * @param aspect 默认 1：1
         * @return this
         */
        public Builder setAspect(int[] aspect) {
            this.aspect = aspect;
            return this;
        }

        /**
         * 拍身份证正面照（照片）
         *
         * @return this
         */
        public Builder isIdCardFront() {
            this.isIdCardFront = true;
            return this;
        }

        /**
         * 拍身份证反面照（国徽）
         *
         * @return this
         */
        public Builder isIdCardBack() {
            this.isIdCardBack = true;
            return this;
        }

        public Builder setListener(ImageChooseListener listener) {
            mListener = listener;
            return this;
        }

        public ImageChoose create() {
            return new ImageChoose(maxCount, isIdCardFront, isIdCardBack, needFree, needCrop,
              aspect, mListener);
        }

        public void show(FragmentManager manager) {
            create().show(manager);
        }
    }
}
