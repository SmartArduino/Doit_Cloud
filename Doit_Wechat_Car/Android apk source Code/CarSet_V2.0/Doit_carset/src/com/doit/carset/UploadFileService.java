package com.doit.carset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

@SuppressLint("HandlerLeak")
public class UploadFileService extends Service {
	private static final String ACTION = "com.doit.carset.action.NEW_FILE";
	private static final String ACTION_FINISH = "com.doit.carset.action.UPLOAD_FINISH";
	private HandleThread thread;
	static public List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
	private String type = null;String id = null;String key = null;

	public static final String IP = "http://wechat.doit.am/cloud_api/publish.php?cmd=publish&device_id=";
//	public static final String IP2 = "http://182.92.178.210/num.php?topic=";

	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		IntentFilter filter = new IntentFilter(ACTION);
		registerReceiver(this.UploadReceiver, filter);

		thread = new HandleThread();
		thread.start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		unregisterReceiver(this.UploadReceiver);

		thread.requestExit();

	}

	private final BroadcastReceiver UploadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String id = intent.getStringExtra("ID");
			String key = intent.getStringExtra("KEY");
			String type = intent.getStringExtra("TYPE");
			
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("TYPE", type);
			item.put("ID", id);
			item.put("KEY", key);
			
			synchronized (data) {
				data.add(item);
				data.notify();
			}
		}
	};

	private void initFileInfo(Map<String, Object> cache) {
		type = (String) cache.get("TYPE");
		id = (String) cache.get("ID");
		key = (String) cache.get("KEY");
	}

	private void noticeUploadList(String str, String dolres) {

		Intent intent1 = new Intent(ACTION_FINISH);
		intent1.putExtra("RESULT", str);
		intent1.putExtra("DOLRES", dolres);
		sendBroadcast(intent1);

	}

	private class HandleThread extends Thread {
		private String res;
		private String dolres; // 查询设备连接状态结果
		private Map<String, Object> cache = null;
		private boolean bRun = true;

		public void requestExit() {
			bRun = false;
			synchronized (data) {
				data.notify();
			}
		}


		public String send(String id, String key, String type) {
			String res = null;
			try {
				MyHttp myAds = new MyHttp(IP + id + "&device_key=" + key + "&message=" + type);
				res = myAds.httpPost();
			} catch (Exception e) {
				res = null;
			}
			return res;

		}

		public void run() {

			while (bRun) {

				synchronized (data) {
					if (data.size() > 0) {
						cache = data.get(0);
					} else {
						try {
							data.wait();
						} catch (InterruptedException e) {
							Log.d("data", "data.wait");
						}
						continue;
					}
				}
				if (cache != null) {
					initFileInfo(cache);
					cache = null;

					res = send(id, key, type);
					data.remove(0);
					// 这里注意返回值看是“ok”还是“ok\r\n”
//					if (res.equals("ok\r\n")) {
//						data.remove(0);
//						noticeUploadList("1", dolres);
//					} else {
//						data.remove(0);
//						noticeUploadList("-1", dolres);
//					}
				}
			}
		}
	}

}
