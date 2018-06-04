package com.pictorytale.messenger.android.photo.picker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.unity3d.player.UnityPlayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


public class Plugin {
	/**
	 * The request code used to start pick image activity to be used on result to identify this specific request.
	 */
	public static final int PICK_IMAGE_REQUEST_CODE = 200;
	/**
	 * The request code used to start pick image activity to be used on result to identify this specific request.
	 */
	public static final int CROP_IMAGE_REQUEST_CODE = 1;
	/**
	 * The request code used to start camera activity to be used on result to identify this specific request.
	 */
	public static final int TAKE_PHOTO_REQUEST_CODE = 1337;

	public static final String CROP_CALLBACK = "OnCropDone";
	public static final String CAMERA_CALLBACK = "OnPickDoneFromCamera";
	public static final String GALLERY_CALLBACK = "OnPickDoneFromGallery";
	public static final String CANCEL_CALLBACK = "OnCancel";

	public static final String ORIGINAL_CONTEXT_CLASS_NAME = "originalContextClassName";

	//The tag for debugging in LogCat
	public static final String TAG = "PictoryTalePlugin";
	
	//The result returned from this plugin
	private byte[] pluginData = null;
	
	//The name of the GameObject in Unity's Scene which is used to communicate with this Plugin
	private static String unityGameObjectName = "PhotoPicker";

	public static String copiedFileName = "copiedFileFromGallery";

	public static String cropFileName = "cropFileFromGalleryOrCamera.jpeg";

	public static String photoFileName = "photoFileTakenWithCamera.jpeg";

	public static int cropWidth = 256;
	public static int cropHeight = 256;

	public static Plugin instance = new Plugin();

	public static boolean cropActivityIsLaunching;

	private static Context unityActivity;

	private static Class unityActivityClass;

	private static String unityActivityClassName;

	/**
	 * this method is called in Unity
	 * @param name
	 */
	public static void setUnityObjectName(String name) {
		unityGameObjectName = name;
	}

	/**
	 * this method is called in Unity
	 * @param name
	 */

	public static void setCopiedFileName(String name) { copiedFileName = name; };

	/**
	 * this method is called in Unity
	 * @param name
	 */
	public static void setCropFileName(String name) { cropFileName = name; };

	/**
	 * Return the result to Unity, this method is called in Unity
	 */
	public byte[] getPluginData(){
		return pluginData;
	}

	/**
	 * Clear all data, this method is called in Unity
	 */
	public void cleanUp(){
		pluginData = null;
	}

	/**
	 *
	 * @param width
	 * @param height
	 */
	public static void setCropSize(int width, int height) {
		Plugin.cropWidth = width;
		Plugin.cropHeight = height;
	}

	/**
	 * Open phone's gallery, this method is called in Unity
	 */
	public void launchGallery(Context context){
		launchAndroidActivity(context, GalleryActivity.class);
	}

	/**
	 * Start phone's camera, this method is called in Unity
	 */
	public void launchCamera(Context context){
		launchAndroidActivity(context, CameraActivity.class);
	}

	/**
	 * Set the data result to return later
	 */
	private void setPluginData(byte[] data){
		pluginData = data;
	}

	public boolean launchActivityWithIntent(Context context, Class<?> activityClass) {
		if (context == null && unityActivity != null)
			context = unityActivity;
		try {
			Intent intent = new Intent(context, activityClass);
			context.startActivity(intent);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void launchAndroidActivity(Context context, Class<?> activityClass) {
		unityActivity = context;
		unityActivityClass = unityActivity.getClass();
		unityActivityClassName = unityActivityClass.getName();
		Intent myIntent = new Intent(context, activityClass);
		context.startActivity(myIntent);
	}

	public boolean launchCropActivity(Context context, Uri uri, boolean startOnUnityActivityContext) {
		cropActivityIsLaunching = true;
		String originalContextClassName = context != null ? context.getClass().getName() : "";
		if ((context == null || startOnUnityActivityContext) && unityActivity != null) {
			context = unityActivity;
		}
		try {
			Intent intent = new Intent(context, CropActivity.class);
			intent.setData(uri);
			intent.putExtra(Plugin.ORIGINAL_CONTEXT_CLASS_NAME, originalContextClassName);
			context.startActivity(intent);
			return true;
		} catch (Exception e) {
			cropActivityIsLaunching = false;
			e.printStackTrace();
			return false;
		}
	}

	public void backToUnity(Activity context, String method, Bitmap bitmap, Uri fileUri) {
		if (bitmap == null)
		{
			backToUnityWithCancel(context);
		}
		else
		{
			byte[] bitmapByteArray = Utils.compressBitmapToJPEGByteArray(bitmap);
			this.setPluginData(bitmapByteArray);
			JSONArray jsa = getJSONArrayWithBitmapData(context, bitmap, fileUri);
			Plugin.sendMessageToUnityObject(method, jsa);

			backToUnity(context);
		}
	}

	private JSONArray getJSONArrayWithBitmapData(Context context, Bitmap bitmap, Uri uri) {
		JSONObject jso = getJSONObjectWithBitmapData(context, bitmap, uri);
		JSONArray jsa = new JSONArray();
		jsa.put(jso);
		return jsa;
	}

	private JSONObject getJSONObjectWithBitmapData(Context context, Bitmap bitmap, Uri uri) {
		JSONObject jso = new JSONObject();
		if (bitmap != null) {
			try {
				jso.put("width", bitmap.getWidth());
				jso.put("height", bitmap.getHeight());
				if (uri != null) {
					jso.put("uri", uri.toString());
					jso.put("uriPath", Utils.getPath(context, uri));
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return jso;
	}

	/**
	 * Go back to Unity (from Android Activity) with cancel action
	 */
	public void backToUnityWithCancel(Activity androidActivity) {
		Plugin.sendMessageToUnityObject(CANCEL_CALLBACK, androidActivity.getClass().getName());
		backToUnity(androidActivity);
	}

	/**
	* Go back to Unity (from Android Activity)
	*/
	private void backToUnity(Activity androidActivity) {
		Utils.hideProgressIndicator();
		Log.e(TAG, "backToUnity: unityActivityClass: " + unityActivityClass);
		Log.e(TAG, "backToUnity: unityActivityClassName: " + unityActivityClassName);
		if (unityActivityClass == null) {
			if (TextUtils.isEmpty(unityActivityClassName)) {
				unityActivityClassName = "com.pictorytale.messenger.android.PictoryTaleUnityPlayerActivity";
			}
			try {
				unityActivityClass = Class.forName(unityActivityClassName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		Log.e(TAG, "backToUnity: unityActivityClass: " + unityActivityClass);
		Intent myIntent = new Intent(androidActivity, unityActivityClass);
		androidActivity.startActivity(myIntent);
		unityActivity = null;
	}
	
	/**
	 * Send message to Unity's GameObject (named as Plugin.unityGameObjectName)
	 * @param method name of the method in GameObject's script
	 * @param jsonParams the actual message
	 */
	private static void sendMessageToUnityObject(String method, String jsonParams){
		UnityPlayer.UnitySendMessage(unityGameObjectName, method, jsonParams);
	}
	
	private static void sendMessageToUnityObject(String method, JSONArray jsonParams){
		sendMessageToUnityObject(method, jsonParams.toString());
	}

	private static void sendMessageToUnityObject(String method, JSONObject jsonParams){
		sendMessageToUnityObject(method, jsonParams.toString());
	}


}
