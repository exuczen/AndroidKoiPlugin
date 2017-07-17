package com.pictorytale.messenger.android.photo.picker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import android.os.Bundle;
import android.util.Log;

public class CameraActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		openCamera();
	}

	private void openCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, Plugin.TAKE_PHOTO_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK && data != null) {

			if (requestCode == Plugin.TAKE_PHOTO_REQUEST_CODE) {
				Bundle bundle = data.getExtras();
				Bitmap bitmap = (Bitmap)bundle.get("data");
				if (bitmap != null) {
					Uri photoUri = data.getData();
					Log.e("capturedPhotoUri", photoUri.toString());
					boolean cropLaunched = Plugin.instance.launchCropActivity(null, photoUri);
					if (!cropLaunched) {
						bitmap = Utils.scaleBitmapToSize(bitmap, Plugin.cropWidth, Plugin.cropHeight, true);
						bitmap = Utils.cropMiddleRect(bitmap, Plugin.cropWidth, Plugin.cropHeight, true);
						Plugin.instance.backToUnity(this, Plugin.CAMERA_CALLBACK, bitmap, null);
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
