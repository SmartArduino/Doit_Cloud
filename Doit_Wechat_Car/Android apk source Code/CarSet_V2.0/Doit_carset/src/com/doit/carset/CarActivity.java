package com.doit.carset;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CarActivity extends Activity implements OnClickListener {
	private static final String ACTION = "com.doit.carset.action.NEW_FILE";
	private static final String ACTION_FINISH = "com.doit.carset.action.UPLOAD_FINISH";
	private Button but_center;
	private Button but_left;
	private Button but_right;
	private Button but_up;
	private Button but_below;
  
	private TextView title;
	public static String id,key,name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		  requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.car);

		Intent intent = getIntent(); 
		name = intent.getStringExtra("NAME");
		id = intent.getStringExtra("ID");
		key = intent.getStringExtra("KEY");
		
		initView();
 
		IntentFilter filter = new IntentFilter(ACTION_FINISH);
		registerReceiver(this.UploadList, filter);
 
	}

	 
	
	private final BroadcastReceiver UploadList = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String dolres = intent.getStringExtra("DOLRES");
			String str = intent.getStringExtra("RESULT");
			 
//			
//			if (str.equals("1")) {
//				title.setText("发送成功");
//			}
//
//			if (str.equals("-1")) {
//				title.setText("发送失败");
//			}
//
//			if (str.equals("-2")) {
//				finish();
//			}
			
		}
	};

	public void initView() {
		but_center = (Button) findViewById(R.id.but_center);
		but_left = (Button) findViewById(R.id.but_left);
		but_right = (Button) findViewById(R.id.but_right);
		but_up = (Button) findViewById(R.id.but_up);
		but_below = (Button) findViewById(R.id.but_below); 
		title = (TextView) findViewById(R.id.car_title);
		title.setText(name);
		
		but_center.setOnClickListener(this);
		but_left.setOnClickListener(this);
		but_right.setOnClickListener(this);
		but_up.setOnClickListener(this);
		but_below.setOnClickListener(this); 
 
	}

	@Override
	public void onClick(View v) { 
			switch (v.getId()) {
			case R.id.but_center:
				send(id,key,"0");
				break;
			case R.id.but_left:
				send(id,key,"3");
				break;
			case R.id.but_right:
				send(id,key,"4");
				break;
			case R.id.but_up:
				send(id,key,"1");
				break;
			case R.id.but_below:
				send(id,key,"2");
				break;
			 
			default:
				break;
			} 
	}

	private void send(String id, String key, String str) {
		Intent intentAddFile = new Intent(ACTION);
		intentAddFile.putExtra("ID", id);
		intentAddFile.putExtra("KEY", key);
		intentAddFile.putExtra("TYPE", str);
		sendBroadcast(intentAddFile);
	}

	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(UploadList);
 

	}
 
 
	 
} 

