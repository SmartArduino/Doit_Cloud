/*
 * Copyright (C) 2010 ZXing authors
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
import android.graphics.Point;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.example.myzxingtest.MyMainActivity;

/**
 * A class which deals with reading, parsing, and setting the camera parameters
 * which are used to configure the camera hardware.
 */
// 照相机配置管理者
final class CameraConfigurationManager {
	private static final String TAG = "CameraConfiguration";
	// This is bigger than the size of a small screen, which is still supported.
	// The routine
	// below will still select the default (presumably 320x240) size for these.
	// This prevents
	// accidental selection of very low resolution on some devices.
	private static final int MIN_PREVIEW_PIXELS = 470 * 320; // normal screen
	private static final int MAX_PREVIEW_PIXELS = 1280 * 720;// 最大预览像素
	private final Context context;
	private Point screenResolution;// 屏幕分辨率
	private Point cameraResolution;// 相机分辨率

	CameraConfigurationManager(Context context) {
		this.context = context;
	}

	/**
	 * Reads, one time, values from the camera that are needed by the app.
	 */

	/**
	 * 修改竖屏替换方法
	 * @author ljh @desc 
	 * @param camera
	 */
	void initFromCameraParameters(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		if (width < height) {
			int temp = width;
			width = height;
			height = temp;
		}
		screenResolution = new Point(height, width);
		cameraResolution = findBestPreviewSizeValue(parameters, new Point(width, height));
	}
	// 初始化一次相机的参数
	void initFromCameraParameters1(Camera camera) {
		// 获取相机的参数
		Camera.Parameters parameters = camera.getParameters();
		// 获取屏幕的宽高------------------------------------------------
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		display.getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		// ------------------------------------------------------------
		if (width < height) {
			Log.i(TAG, "Display reports portrait orientation; assuming this is incorrect");
			int temp = width;
			width = height;
			height = temp;
		}
		// 屏幕分辨率
		screenResolution = new Point(width, height);
		Log.i(TAG, "Screen resolution: " + screenResolution);
		// 相机分辨率
		cameraResolution = findBestPreviewSizeValue(parameters, screenResolution);
		Log.i(TAG, "Camera resolution: " + cameraResolution);
	}

	// 设置渴望相机的参数
	void setDesiredCameraParameters(Camera camera, boolean safeMode) {
		Camera.Parameters parameters = camera.getParameters();
		if (parameters == null) {
			Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
			return;
		}
		Log.i(TAG, "Initial camera parameters: " + parameters.flatten());
		if (safeMode) {
			Log.w(TAG, "In camera config safe mode -- most settings will not be honored");
		}
		SharedPreferences prefs = MyMainActivity.preferences(context);
		initializeTorch(parameters, prefs, safeMode);
		String focusMode = null;
		if (prefs.getBoolean(MyMainActivity.KEY_AUTO_FOCUS, true)) {
			if (safeMode || prefs.getBoolean(MyMainActivity.KEY_DISABLE_CONTINUOUS_FOCUS, false)) {
				focusMode = findSettableValue(parameters.getSupportedFocusModes(), Camera.Parameters.FOCUS_MODE_AUTO);
			} else {
				focusMode = findSettableValue(parameters.getSupportedFocusModes(), "continuous-picture", // Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
																											// in
																											// 4.0+
						"continuous-video", // Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
											// in 4.0+
						Camera.Parameters.FOCUS_MODE_AUTO);
			}
		}
		if (!safeMode && focusMode == null) {
			focusMode = findSettableValue(parameters.getSupportedFocusModes(), Camera.Parameters.FOCUS_MODE_MACRO, "edof"); // Camera.Parameters.FOCUS_MODE_EDOF
																															// in
																															// 2.2+
		}
		if (focusMode != null) {
			parameters.setFocusMode(focusMode);
		}
		
		camera.setDisplayOrientation(90);	//修改竖屏添加代码
		parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
		camera.setParameters(parameters);
	}

	// 获取相机分辨lv
	Point getCameraResolution() {
		return cameraResolution;
	}

	// 获取屏幕分辨率
	Point getScreenResolution() {
		return screenResolution;
	}

	// 设置手电筒
	void setTorch(Camera camera, boolean newSetting) {
		Camera.Parameters parameters = camera.getParameters();
		doSetTorch(parameters, newSetting, false);
		camera.setParameters(parameters);
		SharedPreferences prefs = MyMainActivity.preferences(context);
		boolean currentSetting = prefs.getBoolean(MyMainActivity.KEY_FRONT_LIGHT, false);
		if (currentSetting != newSetting) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(MyMainActivity.KEY_FRONT_LIGHT, newSetting);
			editor.commit();
		}
	}

	// 初始化手电筒
	private void initializeTorch(Camera.Parameters parameters, SharedPreferences prefs, boolean safeMode) {
		boolean currentSetting = prefs.getBoolean(MyMainActivity.KEY_FRONT_LIGHT, false);
		doSetTorch(parameters, currentSetting, safeMode);
	}

	// 设置手电筒
	private void doSetTorch(Camera.Parameters parameters, boolean newSetting, boolean safeMode) {
		String flashMode;
		if (newSetting) {
			flashMode = findSettableValue(parameters.getSupportedFlashModes(), Camera.Parameters.FLASH_MODE_TORCH, Camera.Parameters.FLASH_MODE_ON);
		} else {
			flashMode = findSettableValue(parameters.getSupportedFlashModes(), Camera.Parameters.FLASH_MODE_OFF);
		}
		if (flashMode != null) {
			parameters.setFlashMode(flashMode);
		}
	}

	// 找到最佳的相机预览分辨率
	private Point findBestPreviewSizeValue(Camera.Parameters parameters, Point screenResolution) {
		// 获取支持的相机预览分辨率
		List<Camera.Size> rawSupportedSizes = parameters.getSupportedPreviewSizes();
		if (rawSupportedSizes == null) {
			Log.w(TAG, "Device returned no supported preview sizes; using default");
			// 如果没有，就直接返回相机的预览分辨率
			Camera.Size defaultSize = parameters.getPreviewSize();
			return new Point(defaultSize.width, defaultSize.height);
		}
		// Sort by size, descending
		List<Camera.Size> supportedPreviewSizes = new ArrayList<Camera.Size>(rawSupportedSizes);
		// 根据size的大小排序
		Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
			@Override
			public int compare(Camera.Size a, Camera.Size b) {
				int aPixels = a.height * a.width;
				int bPixels = b.height * b.width;
				if (bPixels < aPixels) {
					return -1;
				}
				if (bPixels > aPixels) {
					return 1;
				}
				return 0;
			}
		});
		// 打印
		if (Log.isLoggable(TAG, Log.INFO)) {
			StringBuilder previewSizesString = new StringBuilder();
			for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
				previewSizesString.append(supportedPreviewSize.width).append('x').append(supportedPreviewSize.height).append(' ');
			}
			Log.i(TAG, "Supported preview sizes: " + previewSizesString);
		}
		// 获取bestsize
		Point bestSize = null;
		// 屏幕长宽比
		float screenAspectRatio = (float) screenResolution.x / (float) screenResolution.y;
		float diff = Float.POSITIVE_INFINITY;
		for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
			int realWidth = supportedPreviewSize.width;
			int realHeight = supportedPreviewSize.height;
			int pixels = realWidth * realHeight;
			// 如果小于要求的最小值就继续
			if (pixels < MIN_PREVIEW_PIXELS || pixels > MAX_PREVIEW_PIXELS) {
				continue;
			}
			boolean isCandidatePortrait = realWidth < realHeight;
			int maybeFlippedWidth = isCandidatePortrait ? realHeight : realWidth;
			int maybeFlippedHeight = isCandidatePortrait ? realWidth : realHeight;
			if (maybeFlippedWidth == screenResolution.x && maybeFlippedHeight == screenResolution.y) {
				Point exactPoint = new Point(realWidth, realHeight);
				Log.i(TAG, "Found preview size exactly matching screen size: " + exactPoint);
				return exactPoint;
			}
			float aspectRatio = (float) maybeFlippedWidth / (float) maybeFlippedHeight;
			float newDiff = Math.abs(aspectRatio - screenAspectRatio);
			if (newDiff < diff) {
				bestSize = new Point(realWidth, realHeight);
				diff = newDiff;
			}
		}
		if (bestSize == null) {
			Camera.Size defaultSize = parameters.getPreviewSize();
			bestSize = new Point(defaultSize.width, defaultSize.height);
			Log.i(TAG, "No suitable preview sizes, using default: " + bestSize);
		}
		Log.i(TAG, "Found best approximate preview size: " + bestSize);
		return bestSize;
	}

	// 获取设置的值(获取相机手电支持的类型，有就返回那个值)
	private static String findSettableValue(Collection<String> supportedValues, String... desiredValues) {
		Log.i(TAG, "Supported values: " + supportedValues);
		String result = null;
		if (supportedValues != null) {
			for (String desiredValue : desiredValues) {
				if (supportedValues.contains(desiredValue)) {
					result = desiredValue;
					break;
				}
			}
		}
		Log.i(TAG, "Settable value: " + result);
		return result;
	}
}
