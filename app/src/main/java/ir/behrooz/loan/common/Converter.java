package ir.behrooz.loan.common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Converter {

    public static byte[] toByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap toBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static String toBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static String toBase64(String path, Context context) {
        return toBase64(toByteArray(path, context));
    }

    public static byte[] toByteArray(String path, Context context) {
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            bufferedInputStream.read(bytes, 0, bytes.length);
            bufferedInputStream.close();
        } catch (FileNotFoundException ex) {
            Toast.makeText(context, ex.getStackTrace().toString(), Toast.LENGTH_SHORT).show();
        } catch (IOException ex) {
            Toast.makeText(context, ex.getStackTrace().toString(), Toast.LENGTH_SHORT).show();
        }
        return bytes;
    }
}
