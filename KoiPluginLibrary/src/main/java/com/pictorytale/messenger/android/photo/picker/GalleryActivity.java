package com.pictorytale.messenger.android.photo.picker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import java.io.File;

public class GalleryActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());
		openGallery();
	}

	private void openGallery() {
		//		Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
		//		startActivityForResult(intent, LOAD_IMAGE);

		//		Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		//		intent.setType("image/*");
		//		startActivityForResult(intent, Plugin.PICK_IMAGE_REQUEST_CODE);

		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select File"), Plugin.PICK_IMAGE_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK && data != null) {

			if (requestCode == Plugin.PICK_IMAGE_REQUEST_CODE) {
				Utils.showProgressIndicator(this);
				Uri imageUri = data.getData();

				File copiedFile = Utils.copyImageFileToExternalFilesDir(this, imageUri, Plugin.copiedFileName);
				Log.e("copiedFile","copiedFileExists="+copiedFile.exists());
				if (copiedFile.exists()) {
					Uri copiedFileUri = Uri.fromFile(copiedFile);
					Log.e("copiedFile","copiedFileUri="+copiedFileUri);
					boolean cropLaunched = Plugin.instance.launchCropActivity(null, copiedFileUri);
					Utils.hideProgressIndicator();
					if (!cropLaunched) {
						Bitmap bitmap = Utils.decodeSampledBitmapFromUri(this, copiedFileUri, Plugin.cropWidth, Plugin.cropHeight);
						if (bitmap != null) {
							bitmap = Utils.cropMiddleRect(bitmap, Plugin.cropWidth, Plugin.cropHeight, true);
							Plugin.instance.backToUnity(this, Plugin.GALLERY_CALLBACK, bitmap, null);
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
