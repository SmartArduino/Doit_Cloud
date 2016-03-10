package com.doit.carset;

import java.util.List;
import java.util.zip.Inflater;
 

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class WifiListActivity extends Activity {

	private WifiManager wifiManager;
	List<ScanResult> list;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);         		 
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.wife_list);
		init();
	}

	private void init() {
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		list = wifiManager.getScanResults();
		ListView listView = (ListView) findViewById(R.id.listView);
		if (list == null) {
			Toast.makeText(this, R.string.str_no_wifi, Toast.LENGTH_LONG).show();
		}else {
			listView.setAdapter(new MyAdapter(this,list));
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			
			@SuppressLint("ShowToast")
			@Override
			    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
			                   long arg3) {
				               Toast.makeText(getApplicationContext(),  list.get(arg2).SSID, 3000).show();
				               
				           	Intent intent = new Intent(); 
			    			intent.putExtra("SSID", list.get(arg2).SSID); 
			    			intent.setClass(WifiListActivity.this, SendWifiKeyActivity.class);
			    		  	WifiListActivity.this.startActivity(intent);
			    		  	finish();
				           }
			});
		}
	}

	public class MyAdapter extends BaseAdapter {

		LayoutInflater inflater;
		List<ScanResult> list;
		public MyAdapter(Context context, List<ScanResult> list) {
			// TODO Auto-generated constructor stub
			this.inflater = LayoutInflater.from(context);
			this.list = list;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view = null;
			view = inflater.inflate(R.layout.item_wifi_list, null);
			ScanResult scanResult = list.get(position);
			TextView textView = (TextView) view.findViewById(R.id.textView);
			textView.setText(scanResult.SSID);
//			TextView signalStrenth = (TextView) view.findViewById(R.id.signal_strenth);
//			signalStrenth.setText(String.valueOf(Math.abs(scanResult.level)));
			ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
		 
			if (Math.abs(scanResult.level) > 100) {
				imageView.setImageDrawable(getResources().getDrawable(R.drawable.stat_sys_wifi_signal_0));
			} else if (Math.abs(scanResult.level) > 80) {
				imageView.setImageDrawable(getResources().getDrawable(R.drawable.stat_sys_wifi_signal_1));
			} else if (Math.abs(scanResult.level) > 70) {
				imageView.setImageDrawable(getResources().getDrawable(R.drawable.stat_sys_wifi_signal_1));
			} else if (Math.abs(scanResult.level) > 60) {
				imageView.setImageDrawable(getResources().getDrawable(R.drawable.stat_sys_wifi_signal_2));
			} else if (Math.abs(scanResult.level) > 50) {
				imageView.setImageDrawable(getResources().getDrawable(R.drawable.stat_sys_wifi_signal_3));
			} else {
				imageView.setImageDrawable(getResources().getDrawable(R.drawable.stat_sys_wifi_signal_4));
			}
			return view;
		}

	}

}
