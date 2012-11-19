package com.alloyteam.weibo.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alloyteam.weibo.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

public class ImageLoader {

	MemoryCache memoryCache = new MemoryCache();
	FileCache fileCache;
	private Map<ImageView, String> imageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	// 线程池
	ExecutorService executorService;

	public ImageLoader(Context context) {
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);
	}

	// 当进入listview时默认的图片，可换成你自己的默认图片
	final int stub_id = R.drawable.avatar;

	// 最主要的方法
	public void displayImage(String url, ImageView imageView,
			ImageCallback callback) {
		imageViews.put(imageView, url);
		// 先从内存缓存中查找

		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null) {
			if(callback!=null){
				imageView.setImageBitmap(callback.imageLoaded(bitmap, url));
			}
			else{
				imageView.setImageBitmap(bitmap);
			}
		} else {
			// 若没有的话则开启新线程加载图片
			queuePhoto(url, imageView, callback);
			//imageView.setImageResource(stub_id);
		}
	}

	private void queuePhoto(String url, ImageView imageView,
			ImageCallback callback) {
		PhotoToLoad p = new PhotoToLoad(url, imageView, callback);
		executorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(String url) {
		/*
		 * File f = fileCache.getFile(url);
		 * 
		 * // 先从文件缓存中查找是否有 Bitmap b = decodeFile(f); if (b != null) return b;
		 */

		// 最后从指定的url中下载图片
		try {
			Bitmap bitmap = null;
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl
					.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			// OutputStream os = new FileOutputStream(f);
			// CopyStream(is, os);
			// os.close();
			bitmap = decodeFile(is);
			Log.d("my", "bitmap decode");
			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	// decode这个图片并且按比例缩放以减少内存消耗，虚拟机对每张图片的缓存大小也是有限制的
	private Bitmap decodeFile(InputStream f) {

		// decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		// o.inJustDecodeBounds = true;
		return BitmapFactory.decodeStream(f, null, o);

		// Find the correct scale value. It should be the power of 2.
		/*
		 * final int REQUIRED_SIZE = 70; int width_tmp = o.outWidth, height_tmp
		 * = o.outHeight; int scale = 1; while (true) { if (width_tmp / 2 <
		 * REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) break; width_tmp /=
		 * 2; height_tmp /= 2; scale *= 2; }
		 * 
		 * // decode with inSampleSize BitmapFactory.Options o2 = new
		 * BitmapFactory.Options(); o2.inSampleSize = scale; return
		 * BitmapFactory.decodeStream(f, null, o2);
		 */

	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;
		public ImageCallback callback;

		public PhotoToLoad(String u, ImageView i, ImageCallback c) {
			url = u;
			imageView = i;
			callback = c;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		// @Override
		public void run() {
			Log.d("my", "run");
			if (imageViewReused(photoToLoad))
				return;
			Bitmap bmp = getBitmap(photoToLoad.url);
			memoryCache.put(photoToLoad.url, bmp);
			if (imageViewReused(photoToLoad))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bmp,photoToLoad);			
			// 更新的操作放在UI线程中
			Activity a = (Activity) photoToLoad.imageView.getContext();
			a.runOnUiThread(bd);
			// bd.run();
		}
	}

	/**
	 * 防止图片错位
	 * 
	 * @param photoToLoad
	 * @return
	 */
	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// 用于在UI线程中更新界面
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			Log.d("my", "set");
			if (bitmap != null) {
				if(photoToLoad.callback!=null){
					photoToLoad.imageView.setImageBitmap(photoToLoad.callback.imageLoaded(bitmap, photoToLoad.url));
				}
				else{
					photoToLoad.imageView.setImageBitmap(bitmap);
				}
			} else{
				//photoToLoad.imageView.setImageResource(stub_id);
			}
		}
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	public interface ImageCallback {

		public Bitmap imageLoaded(Bitmap bm, String imageUrl);

	}

}
