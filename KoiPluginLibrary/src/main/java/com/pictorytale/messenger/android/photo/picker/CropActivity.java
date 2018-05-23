package com.pictorytale.messenger.android.photo.picker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class CropActivity extends Activity {

    private File cropFile;

    private String originalContextClassName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Intent intent = getIntent();
        if (intent.hasExtra(Plugin.ORIGINAL_CONTEXT_CLASS_NAME)) {
            originalContextClassName = intent.getStringExtra(Plugin.ORIGINAL_CONTEXT_CLASS_NAME);
        }
        Log.e("CropActivity.onCreate", "originalContextClassName=" + originalContextClassName);
        CropImageFromUri(intent.getData());
        Plugin.cropActivityIsLaunching = false;
    }

    private void CropImageFromUri(Uri uri) {
        Log.e("CropImageFromUri", uri.getPath());
        // Image Crop Code
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(uri, "image/*");
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("outputX", Plugin.cropWidth);
        cropIntent.putExtra("outputY", Plugin.cropHeight);
        cropIntent.putExtra("aspectX", Plugin.cropWidth);
        cropIntent.putExtra("aspectY", Plugin.cropHeight);
        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("scaleUpIfNeeded", true);
        //cropIntent.putExtra("return-data", true);

        cropFile = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), Plugin.cropFileName);
        try {
            cropFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cropFile));
        startActivityForResult(cropIntent, Plugin.CROP_IMAGE_REQUEST_CODE);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK /*&& data != null*/) {

            if (requestCode == Plugin.CROP_IMAGE_REQUEST_CODE) {
                //                Bundle extras = data.getExtras();
                //                Bitmap bitmap = extras.getParcelable("data");
                if (cropFile != null && cropFile.exists()) {
                    Bitmap bitmap = Utils.decodeBitmapFromFile(this, cropFile);
                    if (bitmap != null) {
                        Plugin.instance.backToUnity(this, Plugin.CROP_CALLBACK, bitmap, Uri.fromFile(cropFile));
                    } else {
                        Plugin.instance.backToUnity(this);
                    }
                }
            }
        } else {
            Class destClass;
            if (originalContextClassName != null && originalContextClassName.equals(CameraActivity.class.getName()))
                destClass = CameraActivity.class;
            else
                destClass = GalleryActivity.class;
            if (resultCode == Activity.RESULT_CANCELED) {
                //User cancelled the picker. We don't have anything to crop
                super.onActivityResult(requestCode, resultCode, data);
                Plugin.instance.launchActivityWithIntent(this, destClass);
            } else {
                super.onActivityResult(requestCode, resultCode, data);
                Plugin.instance.launchActivityWithIntent(this, destClass);
            }
        }
    }


}
