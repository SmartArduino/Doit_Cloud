package com.example.myzxingtest;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import com.doit.carset.ListCarActivity;
import com.doit.carset.LoginActivity;
import com.doit.carset.MyHttp;
import com.doit.carset.R; 
import com.example.zxing.BeepManager;
import com.example.zxing.FinishListener;
import com.example.zxing.InactivityTimer;
import com.example.zxing.ResultHandler;
import com.example.zxing.ResultHandlerFactory;
import com.example.zxing.camera.CameraManager;
import com.example.zxing.view.ViewfinderView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 已修改扫描界面为竖屏，扫描线上线移动，
 * @desc 
 * @date 2014-4-23 下午3:33:25
 * @author ljh
 */
public class MainActivity extends MyMainActivity implements SurfaceHolder.Callback {
	private static final Set<ResultMetadataType> DISPLAYABLE_METADATA_TYPES = EnumSet.of(ResultMetadataType.ISSUE_NUMBER, ResultMetadataType.SUGGESTED_PRICE,
			ResultMetadataType.ERROR_CORRECTION_LEVEL, ResultMetadataType.POSSIBLE_COUNTRY);
	private CameraManager cameraManager;// 相机管理者
	private CaptureActivityHandler handler;// ��activity��handler
	private Result savedResultToShow;// core���еĽ����
	private Result lastResult;// ���Ľ��
	private boolean hasSurface;
	private IntentSource source;// intent����Դ
	private Collection<BarcodeFormat> decodeFormats;// �����ʽ������
	private String characterSet;
	private InactivityTimer inactivityTimer;// ���ڵ����㣬activity�Զ��رյĹ���
	private BeepManager beepManager;// ���������
	private ViewfinderView viewfinderView;// ����view
	private TextView statusView;// ״̬text
	private View resultView;// ���view

	private String url;
	
	// ��ȡview
	ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	// ��ȡhandler
	public Handler getHandler() {
		return handler;
	}

	// ��ȡ��������
	public CameraManager getCameraManager() {
		return cameraManager;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Window window = getWindow();
		// ������Ļ����
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		hasSurface = false;
		// ��ȡ��������
		inactivityTimer = new InactivityTimer(this);
		// ��ȡ���������
		beepManager = new BeepManager(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// ��ȡ��������
		cameraManager = new CameraManager(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);
		resultView = findViewById(R.id.result_view);
	 
		statusView = (TextView) findViewById(R.id.status_view);
		handler = null;
		lastResult = null;
	 
		resetStatusView();
	 
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		 
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		 
		if (hasSurface) {
			 
			initCamera(surfaceHolder);
		} else {
			 
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		beepManager.updatePrefs();
		inactivityTimer.onResume();
		source = IntentSource.NONE;
		decodeFormats = null;
		characterSet = null;
	}

	@Override
	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:// ���ؼ�
				if (source == IntentSource.NONE && lastResult != null) {
					restartPreviewAfterDelay(0L);
					return true;
				}
				break;
			case KeyEvent.KEYCODE_CAMERA:// ����
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:// ��
				cameraManager.setTorch(false);
				return true;
			case KeyEvent.KEYCODE_VOLUME_UP:// ��
				cameraManager.setTorch(true);
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	 
	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
		if (handler == null) {
			savedResultToShow = result;
		} else {
			if (result != null) {
				savedResultToShow = result;
			}
			if (savedResultToShow != null) {
				Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
				handler.sendMessage(message);
			}
			savedResultToShow = null;
		}
	}

	 
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	 
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	//  
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	 
	private void drawResultPoints(Bitmap barcode, Result rawResult) {
		ResultPoint[] points = rawResult.getResultPoints();
		if (points != null && points.length > 0) {
			Canvas canvas = new Canvas(barcode);
			Paint paint = new Paint();
			paint.setColor(getResources().getColor(R.color.result_points));
			if (points.length == 2) {
				paint.setStrokeWidth(4.0f);
				drawLine(canvas, paint, points[0], points[1]);
			} else if (points.length == 4 && (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
				drawLine(canvas, paint, points[0], points[1]);
				drawLine(canvas, paint, points[2], points[3]);
			} else {
				paint.setStrokeWidth(10.0f);
				for (ResultPoint point : points) {
					canvas.drawPoint(point.getX(), point.getY(), paint);
				}
			}
		}
	}

	// ����
	private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b) {
		canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(), paint);
	}

	// �������1
	public void handleDecode(Result rawResult, Bitmap barcode) {
		inactivityTimer.onActivity();
		lastResult = rawResult;
		ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);
		// �ж��Ƿ�����ն�4
		boolean fromLiveScan = barcode != null;
		if (fromLiveScan) {
			beepManager.playBeepSoundAndVibrate();
			drawResultPoints(barcode, rawResult);
		}
		handleDecodeInternally(rawResult, resultHandler, barcode);
	}

	
	private void handleDecodeInternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
		statusView.setVisibility(View.GONE);
		viewfinderView.setVisibility(View.GONE); 
		resultView.setVisibility(View.VISIBLE);
		 
		ImageView barcodeImageView = (ImageView) findViewById(R.id.barcode_image_view);
		if (barcode == null) {
//			barcodeImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.launcher_icon));
		} else {
//			barcodeImageView.setImageBitmap(barcode);
		}
		 
		TextView formatTextView = (TextView) findViewById(R.id.format_text_view);
//		formatTextView.setText(rawResult.getBarcodeFormat().toString());
	 
		TextView typeTextView = (TextView) findViewById(R.id.type_text_view);
//		typeTextView.setText(resultHandler.getType().toString());
		  
//		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
//		String formattedTime = formatter.format(new Date(rawResult.getTimestamp()));
		String formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ", Locale.CHINA).format(new Date(rawResult.getTimestamp()));
		TextView timeTextView = (TextView) findViewById(R.id.time_text_view);
//		timeTextView.setText(formattedTime);
		//  
		TextView metaTextView = (TextView) findViewById(R.id.meta_text_view);
		View metaTextViewLabel = findViewById(R.id.meta_text_view_label);
		metaTextView.setVisibility(View.GONE);
		metaTextViewLabel.setVisibility(View.GONE);
		Map<ResultMetadataType, Object> metadata = rawResult.getResultMetadata();
		if (metadata != null) {
			StringBuilder metadataText = new StringBuilder(20);
			for (Map.Entry<ResultMetadataType, Object> entry : metadata.entrySet()) {
				//  
				if (DISPLAYABLE_METADATA_TYPES.contains(entry.getKey())) {
					metadataText.append(entry.getValue()).append('\n');
				}
			}
			if (metadataText.length() > 0) {
				metadataText.setLength(metadataText.length() - 1);
//				metaTextView.setText(metadataText);
				metaTextView.setVisibility(View.VISIBLE);
				metaTextViewLabel.setVisibility(View.VISIBLE);
			}
		}
		// 处理条码内容
		TextView contentsTextView = (TextView) findViewById(R.id.contents_text_view);
		CharSequence displayContents = resultHandler.getDisplayContents();
//		contentsTextView.setText(displayContents);
		int scaledSize = Math.max(22, 32 - displayContents.length() / 4);
		contentsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize);
		TextView supplementTextView = (TextView) findViewById(R.id.contents_supplement_text_view);
		supplementTextView.setText("");
		  
//		CharSequence displayContents = resultHandler.getDisplayContents();
		url = displayContents.toString();
		new setdata().start();
//	Intent intent = new Intent(Intent.ACTION_VIEW); 
//	intent.setData(Uri.parse(displayContents.toString())); 
//	startActivity(intent);
	}

	 
	public void restartPreviewAfterDelay(long delayMS) {
		if (handler != null) {
			handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
		}
		resetStatusView();
	}

	 
	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		 
		if (cameraManager.isOpen()) {
			return;
		}
		try {
		 
			cameraManager.openDriver(surfaceHolder);
			if (handler == null) {
				 
				handler = new CaptureActivityHandler(this, decodeFormats, characterSet, cameraManager);
			}
			decodeOrStoreSavedBitmap(null, null);
		} catch (IOException ioe) {
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			displayFrameworkBugMessageAndExit();
		}
	}

 
	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}

	 
	private void resetStatusView() {
		resultView.setVisibility(View.GONE);
		statusView.setText(R.string.msg_default_status);
		statusView.setVisibility(View.VISIBLE);
		viewfinderView.setVisibility(View.VISIBLE);
		lastResult = null;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}
	
	
	
	  private String encode(String src){
			String des=Uri.encode(src);
			return des;
		}
	
	 private class setdata extends Thread{
    	 public void run() { 
    		 boolean flag =false;
    		 String res;
    		 
    	       try {
	  	    	 	 String uri = "http://wechat.doit.am/iot_api/bind.php?open_id="+encode(LoginActivity.open_id)+
		        			 "&wx_url="+encode(url);
		        			  
		    	   	 MyHttp myGet = new MyHttp(uri);
		    	     String des = myGet.httpPost();
		    	     
		    	     if(des.length()>0){
		    	 		  JSONObject jsonEvents =  new JSONObject(des);
		    	 		  res= jsonEvents.getString("ret"); 
		    	 		  if(res.equals("1")){ 
		    	 			 flag=true;
		    	 		  }else{ 
		    	 			 flag=false;
		    	 		  }
		    	     }
//	     	 		   Toast.makeText(MainActivity.this,"扫描成功！",Toast.LENGTH_LONG).show();
		    	     
    	          } catch (Exception e) {   
      	 		      flag=false;
//      	 		     Toast.makeText(MainActivity.this,"扫描失败，请重试！",Toast.LENGTH_LONG).show();
      	 		  }
    
    	          if(flag){
    	        	  Message msg2=new Message();
    	 		      msg2.what=-111;
    	 		      handler.sendMessage(msg2);
    	          }else{
    	        	  Message msg2=new Message();
    	 		      msg2.what=-112;
    	 		      handler.sendMessage(msg2);
    	          }

        } 
 
    }
	 

}
