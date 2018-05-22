package com.pictorytale.messenger.android.photo.picker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ProgressBar;

public class Utils {

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    private static void setDecodedBitmapFactoryOptions(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int widthSampleSize = Math.max(1, Math.round((float) width / (float) reqWidth));
        int heightSampleSize = Math.max(1, Math.round((float) height / (float) reqHeight));
        int inSampleSize = Math.max(widthSampleSize, heightSampleSize);
        Log.e("BitmapOptions", "inSampleSize="+inSampleSize);
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
    }

    private static BitmapFactory.Options getBitmapFactoryOptions(InputStream fileInputStream, int reqWidth, int reqHeight)
    {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(fileInputStream, null, options);
        Utils.setDecodedBitmapFactoryOptions(options, reqWidth, reqHeight);
        return  options;
    }

    private static BitmapFactory.Options getBitmapFactoryOptions(String filePath, int reqWidth, int reqHeight)
    {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        Utils.setDecodedBitmapFactoryOptions(options, reqWidth, reqHeight);
        return  options;
    }

    public static Bitmap decodeSampledBitmapFromInputStream(InputStream inputStream, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = getBitmapFactoryOptions(inputStream, reqWidth, reqHeight);
        return BitmapFactory.decodeStream(inputStream, null, options);

    }

    public static Bitmap decodeSampledBitmapFromFilePath(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = getBitmapFactoryOptions(path, reqWidth, reqHeight);
        return BitmapFactory.decodeFile(path, options);
    }

    public static String getExternalFilePath(Context c, String fileName) {
        String path = /*"file://" + */c.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator + fileName;
        return path;
    }

    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri uri, int reqWidth, int reqHeight)
    {
        String picturePath = Utils.getPath(context, uri);

        Log.e("decodeSBitmapFromUri", "pictureUri=" + uri);
        Log.e("decodeSBitmapFromUri", "picturePath=" + picturePath);
        Bitmap bitmap = null;
        //        if (picturePath != null) {
        //            bitmap = Utils.decodeSampledBitmapFromFilePath(picturePath, reqWidth, reqHeight);
        //            try {
        //                ExifInterface exif = new ExifInterface(picturePath);
        //                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        //                bitmap = Utils.rotateBitmapWithExifOrientation(bitmap, orientation);
        //            } catch (IOException e) {
        //                e.printStackTrace();
        //            }
        //        } else {
        //        }
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            bitmap = Utils.decodeSampledBitmapFromInputStream(inputStream, reqWidth, reqHeight);
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap decodeBitmapFromFile(Context context, File file) {
        Bitmap bitmap = null;
        try {
            InputStream input = context.getContentResolver().openInputStream(Uri.fromFile(file));
            bitmap = BitmapFactory.decodeStream(input);
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap rotateBitmapWithAngleOrientation(Bitmap bitmap, int angle) {

        Matrix matrix = new Matrix();
        matrix.setRotate(angle);
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap rotateBitmapWithExifOrientation(Bitmap bitmap, int exifOrientation) {

        Matrix matrix = new Matrix();
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File copyImageFileToExternalFilesDir(Context context, Uri srcUri, String imageFileName)
    {
        File outFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageFileName);
        try {
            outFile.createNewFile();

            InputStream in = context.getContentResolver().openInputStream(srcUri);
            FileOutputStream out = new FileOutputStream(outFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            // write the output file
            out.flush();
            out.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outFile;
    }

    public static File saveBitmapToExternalFilesDir(Context context, byte[] bytes, String imageFileName) {
        File imageFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageFileName);
        FileOutputStream outputStream;
        try {
            imageFile.createNewFile();
            outputStream = new FileOutputStream(imageFile);
            outputStream.write(bytes);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile;
    }

    public static byte[] compressBitmapToJPEGByteArray(Bitmap bitmap) {
        byte[] bytes = null;
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            bytes = stream.toByteArray();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static Bitmap scaleBitmapToSize(Bitmap bitmap, int reqWidth, int reqHeight, boolean keepAspectRatio) {

        if (keepAspectRatio) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float ratio = (float)width/(float)height;
            float reqRatio = (float)reqWidth/(float)reqHeight;

            if (ratio > reqRatio) {
                width = reqWidth;
                height = (int)(width/ratio);
            } else {
                height = reqHeight;
                width = (int)(height*ratio);
            }
            return Bitmap.createScaledBitmap(bitmap, width, height, true);
        } else {
            return Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true);
        }
    }

    public static Bitmap scaleBitmapToScreenSize(Context context, Bitmap bitmap, boolean keepAspectRatio) {

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int displayWidth = metrics.widthPixels;
        int displayHeight = metrics.heightPixels;

        return scaleBitmapToSize(bitmap, displayWidth, displayHeight, keepAspectRatio);
    }

    public static Bitmap cropMiddleSquare(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int r = Math.min(w, h);
        return Bitmap.createBitmap(bitmap, (w - r) / 2, (h - r) / 2, r, r);
    }

    public static Bitmap cropMiddleRect(Bitmap bitmap, int cropWidth, int cropHeight, boolean scaleToFill) {

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (scaleToFill) {
            float ratio = (float)width/(float)height;
            float cropRatio = (float)cropWidth/(float)cropHeight;
            if (cropRatio > ratio) {
                cropWidth = width;
                cropHeight = (int)(cropWidth/cropRatio);
            } else {
                cropHeight = height;
                cropWidth = (int)(cropHeight*cropRatio);
            }
        }
        return Bitmap.createBitmap(bitmap,
                Math.max((width - cropWidth) / 2, 0), Math.max((height - cropHeight) / 2, 0),
                Math.min(width, cropWidth), Math.min(height, cropHeight));
    }


    private static ProgressDialog progressDialog;
    private ProgressBar progressBar;

    public static void showProgressIndicator(Context context) {
        //        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);
        progressDialog = ProgressDialog.show(context, "Loading", "Wait while loading...");
        //        progressDialog = new ProgressDialog(context);
        //        progressDialog.setTitle("Loading");
        //        progressDialog.setMessage("Wait while loading...");
        //        progressDialog.setCancelable(false); // disable dismiss by tapping outside of the dialog
        //        progressDialog.show();
    }
    public static  void hideProgressIndicator() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


}
