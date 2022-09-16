package ir.behrooz.loan.common;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;

import static ir.behrooz.loan.common.Constants.IMAGE_PATH;


public final class FileUtil {
    public static double read_double(FileInputStream fp) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(8);
        fp.read(buf.array(), 0, 8);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf.getDouble();
    }

    public static float read_float(FileInputStream fp) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(4);
        fp.read(buf.array(), 0, 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf.getFloat();
    }

    public static int read_int(FileInputStream fp) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(4);
        fp.read(buf.array(), 0, 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf.getInt();
    }

    public static short read_short(FileInputStream fp) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(2);
        fp.read(buf.array(), 0, 2);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf.getShort();
    }

    public static void copyFile(File inputFile, File outputFile) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs();
            }
            outputFile.createNewFile();


            in = new FileInputStream(inputFile);
            out = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());

        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    public static void deleteFile(String src) {
        File srcFile = new File(src);
        srcFile.delete();
    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    public static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), IMAGE_PATH);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "CROP_" + timeStamp + ".jpg");
    }

    public static String saveToStorage(Bitmap bitmapImage, Context context) {
        ContextWrapper cw = new ContextWrapper(context);
        File imgDir = getOutputMediaFile();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imgDir);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return imgDir.getPath();
    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static void copyAssets(Context context) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            if (filename.toLowerCase().endsWith(".mcs")) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = assetManager.open(filename);
                    File dir = context.getExternalFilesDir(null);
                    File outFile = new File(dir, filename);
                    if (!outFile.exists()) {
                        out = new FileOutputStream(outFile);
                        copyFile(in, out);
                    }
                } catch (IOException e) {
                    Log.e("tag", "Failed to copy asset file: " + filename, e);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                }
            }
        }
    }
}
