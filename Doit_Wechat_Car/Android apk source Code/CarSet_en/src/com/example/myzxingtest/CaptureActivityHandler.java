/*
 * Copyright (C) 2008 ZXing authors
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
package com.example.myzxingtest;

import java.util.Collection;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.carset.ListCarActivity;
import com.example.carset.R;
import com.example.zxing.DecodeThread;
import com.example.zxing.camera.CameraManager;
import com.example.zxing.view.ViewfinderResultPointCallback;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

//主类handler
public final class CaptureActivityHandler extends Handler {
	private static final String TAG = CaptureActivityHandler.class.getSimpleName();
	private final MainActivity activity;
	private final DecodeThread decodeThread;// 解码线程
	private State state;
	private final CameraManager cameraManager;// 相机管理者

	// 枚举当前的状态类型
	private enum State {
		PREVIEW, // 预览
		SUCCESS, // 成功
		DONE// 完成
	}

	// 实例化时所需参数，activity，
	CaptureActivityHandler(MainActivity activity, Collection<BarcodeFormat> decodeFormats, String characterSet, CameraManager cameraManager) {
		this.activity = activity;
		decodeThread = new DecodeThread(activity, decodeFormats, characterSet, new ViewfinderResultPointCallback(activity.getViewfinderView()));
		decodeThread.start();
		state = State.SUCCESS;
		this.cameraManager = cameraManager;
		cameraManager.startPreview();
		restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
			case R.id.restart_preview:// 重新开始
				Log.d(TAG, "Got restart preview message");
				restartPreviewAndDecode();
				break;
			case R.id.decode_succeeded:// 成功
				Log.d(TAG, "Got decode succeeded message");
				state = State.SUCCESS;
				Bundle bundle = message.getData();
				Bitmap barcode = bundle == null ? null : (Bitmap) bundle.getParcelable(DecodeThread.BARCODE_BITMAP);
				activity.handleDecode((Result) message.obj, barcode);
				break;
			case R.id.decode_failed:// 失败
				state = State.PREVIEW;
				cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
				break;
			case R.id.return_scan_result:
				Log.d(TAG, "Got return scan result message");
				activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
				activity.finish();
				break;
			case -111: 
				dialog();
//				activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
//				activity.finish();
//				 Toast.makeText(activity,"扫描成功！",Toast.LENGTH_LONG).show();
				break;
			case -112: 
				dialog2();
//				activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
//				activity.finish();
//				Toast.makeText(activity,"扫描失败，请重试！！",Toast.LENGTH_LONG).show();
				break;
		}
	}

	
	protected void dialog() {
//		  AlertDialog.Builder builder = new Builder(activity);
//		  builder.setMessage("设备绑定成功!");  
//		  builder.setIcon(android.R.drawable.ic_dialog_info);
//		  builder.setTitle("提示");  
//		  builder.setPositiveButton("确认", new OnClickListener() {   
//			  @Override
//		   public void onClick(DialogInterface dialog, int which) {
//				    dialog.dismiss();    
////					activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
//					activity.finish();
//		   }
//		  });  
//		  builder.create().show();
		  
		  new AlertDialog.Builder(activity)
          .setIcon(android.R.drawable.ic_dialog_info)
          .setTitle(R.string.tip)
          .setMessage(R.string.str_device_bind_s) 
          .setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int whichButton) {
				    dialog.dismiss();    
				    Intent intent=new Intent();  
	                intent.putExtra("RESULT", "1");    
	                activity.setResult(Activity.RESULT_OK, intent); 
					activity.finish();
              }
          }).show();
		}
	
	protected void dialog2() {
		  AlertDialog.Builder builder = new Builder(activity);
		  builder.setMessage(R.string.str_device_bind_f);
		  builder.setIcon(android.R.drawable.ic_dialog_info);
		  builder.setTitle(R.string.tip);  
		  builder.setPositiveButton(R.string.str_ok, new OnClickListener() {   
			  @Override
		   public void onClick(DialogInterface dialog, int which) {
				    dialog.dismiss();    
				    Intent intent=new Intent();  
	                intent.putExtra("RESULT", "0");    
	                activity.setResult(Activity.RESULT_OK, intent); 
					activity.finish();
		   }
		  });  
		  builder.create().show();
		}
	
	// 退出同步
	public void quitSynchronously() {
		state = State.DONE;
		cameraManager.stopPreview();
		Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
		quit.sendToTarget();
		try {
			decodeThread.join(500L);
		} catch (InterruptedException e) {
		}
		removeMessages(R.id.decode_succeeded);
		removeMessages(R.id.decode_failed);
	}

	private void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;
			cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
			activity.drawViewfinder();
		}
	}
}
