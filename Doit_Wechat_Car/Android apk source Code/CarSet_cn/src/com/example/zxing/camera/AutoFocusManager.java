/*
 * Copyright (C) 2012 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.zxing.camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

import com.example.myzxingtest.MyMainActivity;
import com.example.zxing.AsyncTaskExecInterface;
import com.example.zxing.AsyncTaskExecManager;

//自动对焦管理者
final class AutoFocusManager implements Camera.AutoFocusCallback {
	private static final String TAG = AutoFocusManager.class.getSimpleName();
	// 自动对焦时间
	private static final long AUTO_FOCUS_INTERVAL_MS = 2000L;
	private static final Collection<String> FOCUS_MODES_CALLING_AF;
	static {
		FOCUS_MODES_CALLING_AF = new ArrayList<String>(2);
		FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO);
		FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO);
	}
	private boolean active;
	private final boolean useAutoFocus;
	private final Camera camera;
	private AutoFocusTask outstandingTask;// 自动对焦task
	private final AsyncTaskExecInterface taskExec;// 获取task

	AutoFocusManager(Context context, Camera camera) {
		this.camera = camera;
		taskExec = new AsyncTaskExecManager().build();// 获取对应的task
		SharedPreferences sharedPrefs = MyMainActivity.preferences(context);
		// 获取当前相机对焦的模式
		String currentFocusMode = camera.getParameters().getFocusMode();
		useAutoFocus = sharedPrefs.getBoolean(MyMainActivity.KEY_AUTO_FOCUS, true) && FOCUS_MODES_CALLING_AF.contains(currentFocusMode);
		Log.i(TAG, "Current focus mode '" + currentFocusMode + "'; use auto focus? " + useAutoFocus);
		start();
	}

	@Override
	public synchronized void onAutoFocus(boolean success, Camera theCamera) {
		if (active) {
			// 启动task
			outstandingTask = new AutoFocusTask();
			taskExec.execute(outstandingTask);
		}
	}

	// 开始自动对焦
	synchronized void start() {
		if (useAutoFocus) {
			active = true;
			try {
				// 设置camera的callback
				camera.autoFocus(this);
			} catch (RuntimeException re) {
				// Have heard RuntimeException reported in Android 4.0.x+;
				// continue?
				Log.w(TAG, "Unexpected exception while focusing", re);
			}
		}
	}

	// 停止自动对焦
	synchronized void stop() {
		if (useAutoFocus) {
			try {
				// 相机退出自动对焦
				camera.cancelAutoFocus();
			} catch (RuntimeException re) {
				// Have heard RuntimeException reported in Android 4.0.x+;
				// continue?
				Log.w(TAG, "Unexpected exception while cancelling focusing", re);
			}
		}
		if (outstandingTask != null) {
			// 停止task
			outstandingTask.cancel(true);
			outstandingTask = null;
		}
		active = false;
	}

	// 自动对焦的task
	private final class AutoFocusTask extends AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... voids) {
			try {
				// 每隔对应时间自动对焦一次
				Thread.sleep(AUTO_FOCUS_INTERVAL_MS);
			} catch (InterruptedException e) {
				// continue
			}
			synchronized (AutoFocusManager.this) {
				if (active) {
					start();
				}
			}
			return null;
		}
	}
}
