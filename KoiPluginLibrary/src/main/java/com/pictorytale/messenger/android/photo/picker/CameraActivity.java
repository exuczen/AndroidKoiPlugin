package com.pictorytale.messenger.android.photo.picker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import java.io.File;
import java.io.IOException;

public class CameraActivity extends Activity {

	private File photoFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());
		openCamera();
	}

	private void openCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		photoFile = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), Plugin.photoFileName);
		try {
			photoFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
		startActivityForResult(intent, Plugin.TAKE_PHOTO_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK /*&& data != null*/) {

			if (requestCode == Plugin.TAKE_PHOTO_REQUEST_CODE && !Plugin.cropActivityIsLaunching) {
				//				Bundle bundle = data.getExtras();
				//				Bitmap bitmap = (Bitmap)bundle.get("data");
				if (photoFile != null && photoFile.exists()) {
					Uri photoUri = Uri.fromFile(photoFile); /*data.getData();*/
					Log.e("CameraActivity", "capturedPhotoUri:" + (photoUri != null ? photoUri.toString() : ""));
					boolean cropLaunched = Plugin.instance.launchCropActivity(this, photoUri, true);
					if (!cropLaunched) {
						Bitmap bitmap = Utils.decodeSampledBitmapFromUri(this, photoUri, Plugin.cropWidth, Plugin.cropHeight);
						if (bitmap != null) {
							bitmap = Utils.cropMiddleRect(bitmap, Plugin.cropWidth, Plugin.cropHeight, true);
							bitmap = Utils.scaleBitmapToSize(bitmap, Plugin.cropWidth, Plugin.cropHeight, false);
							Plugin.instance.backToUnity(this, Plugin.CAMERA_CALLBACK, bitmap, null);
						} else {
							Plugin.instance.backToUnity(this);
						}
					}
				} else {
					Plugin.instance.backToUnity(this);
				}
			}
		} else if (resultCode == RESULT_CANCELED) {
			super.onActivityResult(requestCode, resultCode, data);
			Plugin.instance.backToUnity(this);
		} else {
			super.onActivityResult(requestCode, resultCode, data);
			Plugin.instance.backToUnity(this);
		}


	}

}
