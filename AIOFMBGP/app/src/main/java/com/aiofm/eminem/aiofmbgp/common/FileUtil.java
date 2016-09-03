package com.aiofm.eminem.aiofmbgp.common;

/**
 * 文件工具类
 * Created by eminem on 2016/4/10
 */
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;

public class FileUtil {
    /**
     * 将Bitmap 图片保存到本地路径，并返回路径
     * @param c 上下文
     * @param fileName 文件名称
     * @param bitmap 图片资源
     * @return
     * Added by Bao guangpu on 2016/4/10
     */
    public static String saveFile(Context c, String fileName, Bitmap bitmap) {
        return saveFile(c, "", fileName, bitmap);
    }

    /**
     * 保存位图文件
     * @param c 上下文
     * @param filePath 保存文件路径
     * @param fileName 文件名称
     * @param bitmap 图片资源
     * @return
     * Added by Bao guangpu on 2016/4/10
     */
    public static String saveFile(Context c, String filePath, String fileName, Bitmap bitmap) {
        byte[] bytes = bitmapToBytes(bitmap);
        return saveFile(c, filePath, fileName, bytes);
    }

    /**
     * 将位图转换为字节数组
     * @param bm 图片资源
     * @return
     * Added by Bao guangpu on 2016/4/10
     */
    public static byte[] bitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * 保存字节数组文件
     * @param c 上下文
     * @param filePath 保存文件路径
     * @param fileName 文件名称
     * @param bytes 字节数组
     * @return
     * Added by Bao guangpu on 2016/4/10
     */
    public static String saveFile(Context c, String filePath, String fileName, byte[] bytes) {
        String fileFullName = "";
        FileOutputStream fos = null;
        String dateFolder = new SimpleDateFormat("yyyyMMdd", Locale.CHINA)
                .format(new Date());
        try {
            String suffix = "";
            if (filePath == null || filePath.trim().length() == 0) {
                filePath = Environment.getExternalStorageDirectory() + "/BaoGuangpu/" + dateFolder + "/";
            }
            File file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            File fullFile = new File(filePath, fileName + suffix);
            fileFullName = fullFile.getAbsolutePath();
            fos = new FileOutputStream(new File(filePath, fileName + suffix));
            fos.write(bytes);
        } catch (Exception e) {
            fileFullName = "";
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    fileFullName = "";
                }
            }
        }
        return fileFullName;
    }
}

