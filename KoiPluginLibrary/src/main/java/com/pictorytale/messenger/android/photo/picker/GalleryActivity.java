package com.pictorytale.messenger.android.photo.picker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import android.os.Bundle;
import android.util.Log;

import java.io.File;

public class GalleryActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
						bitmap = Utils.cropMiddleRect(bitmap, Plugin.cropWidth, Plugin.cropHeight, true);
						Plugin.instance.backToUnity(this, Plugin.GALLERY_CALLBACK, bitmap, null);
					}
				} else {
					Plugin.instance.backToUnity(this);
				}

				//				String decodedImageName = "decodedImage.jpeg";
				//				Bitmap bitmap = Utils.decodeSampledBitmapFromUri(this, copiedFileUri, Plugin.reqImageWidth, Plugin.reqImageWidth);
				//				copiedFile.delete();
				//				byte[] byteArray = Utils.compressBitmapToJPEGByteArray(bitmap);
				//				File imageFile = Utils.saveBitmapToExternalFilesDir(this, byteArray, decodedImageName);
				//				Plugin.instance.backToUnity(this, Plugin.GALLERY_CALLBACK, bitmap, byteArray);


				//				File file = new File(imageUri.getPath());
				//				Log.e("file.absolutePath",file.getAbsolutePath());
				//				try {
				//					InputStream inputStream = this.getContentResolver().openInputStream(imageUri);
				//					Bitmap bitmap = Utils.decodeSampledBitmapFromInputStream(inputStream, Plugin.reqImageWidth, Plugin.reqImageWidth);
				//					inputStream.close();
				//					if (bitmap != null) {
				//						String fileName = "selectedFile.jpeg";
				//
				//						byte[] byteArray = Utils.compressBitmapToJPEGByteArray(bitmap);
				//						File imageFile = Utils.saveBitmapToExternalFilesDir(this, byteArray, fileName);
				//
				//						String createdFilePath = Utils.getPath(this, Uri.fromFile(imageFile));
				//						Log.e("createFilePath", createdFilePath);
				//
				//						ExifInterface exif = new ExifInterface(createdFilePath);
				//						int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
				//
				//						bitmap = Utils.rotateBitmapWithExifOrientation(bitmap, orientation);
				//						byteArray = Utils.compressBitmapToJPEGByteArray(bitmap);
				//						imageFile = Utils.saveBitmapToExternalFilesDir(this, byteArray, fileName);
				//
				//						Log.e("createdFileUri", Uri.fromFile(imageFile).toString());
				//						boolean cropLaunched = Plugin.instance.launchCropActivity(null, Uri.fromFile(imageFile));
				//						if (!cropLaunched) {
				//							Plugin.instance.backToUnity(this, Plugin.GALLERY_CALLBACK, bitmap, byteArray);
				//						}
				//					} else {
				//						Plugin.instance.backToUnity(this);
				//					}
				//				} catch (FileNotFoundException e) {
				//					e.printStackTrace();
				//					Plugin.instance.backToUnity(this);
				//				} catch (IOException e) {
				//					e.printStackTrace();
				//					Plugin.instance.backToUnity(this);
				//				}
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
