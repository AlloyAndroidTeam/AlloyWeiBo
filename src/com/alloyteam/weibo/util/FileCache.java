package com.alloyteam.weibo.util;

import java.io.File;

import android.content.Context;


public class FileCache {

        private File cacheDir;

        public FileCache(Context context) {
                // �����SD������SD���н�һ��LazyList��Ŀ¼��Ż����ͼƬ
                // û��SD���ͷ���ϵͳ�Ļ���Ŀ¼��
                if (android.os.Environment.getExternalStorageState().equals(
                                android.os.Environment.MEDIA_MOUNTED))
                        cacheDir = new File(
                                        android.os.Environment.getExternalStorageDirectory(),
                                        "LazyList");
                else
                        cacheDir = context.getCacheDir();
                if (!cacheDir.exists())
                        cacheDir.mkdirs();
        }

        public File getFile(String url) {
                // ��url��hashCode��Ϊ������ļ���
                String filename = String.valueOf(url.hashCode());
                // Another possible solution
                // String filename = URLEncoder.encode(url);
                File f = new File(cacheDir, filename);
                return f;

        }

        public void clear() {
                File[] files = cacheDir.listFiles();
                if (files == null)
                        return;
                for (File f : files)
                        f.delete();
        }

}