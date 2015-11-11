package com.example.platset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject; 

import com.example.platset.R;
 
 

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window; 
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class CarActivity extends Activity {
   
	private TextView title;
	private String id,key,name,stat;
	private Switch switch1,switch2,switch3,switch4;

	
	Handler myHandler = new Handler(){
		public void handleMessage(Message msg) {	 
			switch(msg.what){ 
			case 0:	 
	          	try { 
						String[] stats = stat.split("\\|");
						if(stats[0].equals("1")){
							switch1.setChecked(true);
						}
						if(stats[1].equals("1")){
							switch2.setChecked(true);
						}
						if(stats[2].equals("1")){
							switch3.setChecked(true);
						}
						if(stats[3].equals("1")){
							switch4.setChecked(true);
						}
	        		}catch (Exception e) {
	        			e.printStackTrace(); 
	        		} 

//				Toast.makeText(CarActivity.this,"登录失败！",Toast.LENGTH_LONG).show();	            	
				break; 
			case 1:	   
				 break;  
			}
		}		
	};
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.car);
 
		Intent intent = getIntent(); 
		name = intent.getStringExtra("NAME");
		id = intent.getStringExtra("ID");
		key = intent.getStringExtra("KEY");
		
		title = (TextView) findViewById(R.id.porttitle);
		title.setText(name);
		  
		switch1 = (Switch) findViewById(R.id.port_switch1);
		switch1.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
	        
            @Override  
            public void onCheckedChanged(CompoundButton buttonView,  
                    boolean isChecked) {  
                if (isChecked) {  
                	switch1.setChecked(true);
                	send("p_port1","1");
                } else {  
                 	switch1.setChecked(false);
                	send("p_port1","0");
                } 
            }  
        });  
		
		switch4 = (Switch) findViewById(R.id.port_switch4);
		switch4.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
	        
            @Override  
            public void onCheckedChanged(CompoundButton buttonView,  
                    boolean isChecked) {  
                if (isChecked) {  
                	switch4.setChecked(true);
                	send("p_port4","1");
                } else {  
                 	switch4.setChecked(false);
                	send("p_port4","0");
                } 
            }  
        });  
		
		switch2 = (Switch) findViewById(R.id.port_switch2);
		switch2.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
	        
            @Override  
            public void onCheckedChanged(CompoundButton buttonView,  
                    boolean isChecked) {  
                if (isChecked) {  
                	switch2.setChecked(true);
                	send("p_port2","1");
                } else {  
                 	switch2.setChecked(false);
                	send("p_port2","0");
                } 
            }  
        });  
		
		switch3 = (Switch) findViewById(R.id.port_switch3);
		switch3.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
	        
            @Override  
            public void onCheckedChanged(CompoundButton buttonView,  
                    boolean isChecked) {  
                if (isChecked) {  
                	switch3.setChecked(true);
                	send("p_port3","1");
                } else {  
                 	switch3.setChecked(false);
                	send("p_port3","0");
                } 
            }  
        });  
 

		new Thread(new Runnable() {  
            @Override  
            public void run() {  
            	try {
	        		MyHttp myGet = new MyHttp("http://wechat.myembed.com/cloud_api/tpipe.php?device_id="+id+"&device_key="+key+"&message=get_stat");
	        		String des = myGet.httpPost();
	        		if(des != null){
	        			stat=des;
	        			Message msg = new Message();
						msg.what = 0;
						myHandler.sendMessage(msg);
	        		}else{

	        		}
        		}catch (Exception e) {
        			e.printStackTrace(); 
        		} 
            }  
        }).start();
	}

   
	public void send(final String port, final String str) {
 
		new Thread() { 
			@Override
			public void run() { 
				super.run();
				try {
	        		MyHttp myGet = new MyHttp("http://wechat.doit.am/cloud_api/publish.php?cmd=publish&device_id="+id+"&device_key="+key+"&message="+port+Uri.encode("|")+str);
	        		String des = myGet.httpPost();
	        		  
        		}catch (Exception e) {
        			e.printStackTrace(); 
        		} 
			}

		}.start();
	
	}
     
} 

