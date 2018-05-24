package com.pictorytale.messenger.android.photo.picker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
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

			if (requestCode == Plugin.PICK_IMAGE_REQUEST_CODE && !Plugin.cropActivityIsLaunching) {
				Utils.showProgressIndicator(this);
				Uri imageUri = data.getData();
				String imagePath = Utils.getPath(this, imageUri);
				imagePath = TextUtils.isEmpty(imagePath) ? "" : imagePath;
				String imageExtension = Utils.getFileExtensionFromUrl(imagePath);
				Log.e("GalleryActivity", "imagePath=" + imagePath);
				Log.e("GalleryActivity", "imageExtension=" + imageExtension);
				String copiedFileName = TextUtils.concat(Plugin.copiedFileName, ".", imageExtension).toString();
				File copiedFile = Utils.copyImageFileToExternalFilesDir(this, imageUri, copiedFileName);
				Log.e("GalleryActivity", "copiedFileExists=" + copiedFile.exists());
				if (copiedFile.exists()) {
					Uri copiedFileUri = Uri.fromFile(copiedFile);
					Log.e("GalleryActivity", "copiedFileUri=" + copiedFileUri);
					boolean cropLaunched = Plugin.instance.launchCropActivity(this, copiedFileUri, true);
					Utils.hideProgressIndicator();
					if (!cropLaunched) {
						Bitmap bitmap = Utils.decodeSampledBitmapFromUri(this, copiedFileUri, Plugin.cropWidth, Plugin.cropHeight);
						if (bitmap != null) {
							bitmap = Utils.cropMiddleRect(bitmap, Plugin.cropWidth, Plugin.cropHeight, true);
							bitmap = Utils.scaleBitmapToSize(bitmap, Plugin.cropWidth, Plugin.cropHeight, false);
							Plugin.instance.backToUnity(this, Plugin.GALLERY_CALLBACK, bitmap, null);
						} else {
							Plugin.instance.backToUnityWithCancel(this);
						}
					}
				} else {
					Plugin.instance.backToUnityWithCancel(this);
				}
			}

		} else if (resultCode == RESULT_CANCELED) {
			super.onActivityResult(requestCode, resultCode, data);
			Plugin.instance.backToUnityWithCancel(this);
		} else {
			super.onActivityResult(requestCode, resultCode, data);
			Plugin.instance.backToUnityWithCancel(this);
		}
	}


}
