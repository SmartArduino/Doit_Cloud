package com.doit.carset;
  
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.annotation.SuppressLint;
import android.app.Activity; 
import android.content.Intent;
import android.os.Bundle; 
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button; 
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SendWifiKeyActivity extends Activity {

	Button Button_send;
	EditText pass;
	TextView ssid;
	String  ssid_str, pass_str;
	
	 public static final String IP = "255.255.255.255"; 
	 public static final int PORT = 8089;
 
	 private String msg=null;
	
	 
	 @SuppressLint("HandlerLeak")
	private Handler handler =new Handler(){  
	        @Override   
	        public void handleMessage(Message msg){
	            super.handleMessage(msg);   
	            if(msg.what == 0){
	            	Toast.makeText(getApplicationContext(), R.string.str_send_success,Toast.LENGTH_LONG).show();                         
	            	finish();
	            }else if(msg.what == 1){
    				Toast.makeText(getApplicationContext(), R.string.str_send_fail,Toast.LENGTH_LONG).show(); 
    				finish();
	            }
	        }
	    };  
	    
	    
	    
	    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//	       getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);         		 
	        requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.send_key);
		
		Intent intent = getIntent(); 
		ssid_str = intent.getStringExtra("SSID");
		
		
		ssid = (TextView) findViewById(R.id.wifissid);
		ssid.setText(ssid_str);
		pass = (EditText) findViewById(R.id.wifipass);
		Button_send = (Button) findViewById(R.id.b_send);
		Button_send.setOnClickListener(new Button.OnClickListener() 
		{
    		public void onClick(View v) {
    			 pass_str =  pass.getText().toString();
    			 if(pass_str.length()>0 && pass_str!=null){
    				 send(ssid_str, pass_str);
    			 }else{
    				Toast.makeText(getApplicationContext(), R.string.str_input_pass,Toast.LENGTH_SHORT).show();                         
    					 
    			 }
    			 
    		}
    	});
		
		 
	}
		 
	
	
	private void send(String ssid, String pass) { 
			msg="cmd=config&ssid="+ssid+"&ps="+pass;
			sendMsg(); 
		 
	}
	 
	
	public void sendMsg() { 
		
	      Thread thread=new Thread(new Runnable()  
	        {  
	            @Override  
	            public void run() {  
	            	boolean flag = true;
	            	DatagramSocket dSocket = null; 
	        		InetAddress local = null;
	        		try {
	        			local = InetAddress.getByName(IP);   
	        		} catch (UnknownHostException e) { 
	        			e.printStackTrace();
	        			flag=false;
	        		}
	        		try {
	        			dSocket = new DatagramSocket();   
	        		} catch (SocketException e) {
	        			e.printStackTrace(); 
	        			flag=false;
	        		}
	        		 
	        		byte[] buf = msg.getBytes();
	        		DatagramPacket dPacket = new DatagramPacket(buf, buf.length, local, PORT);
	        		try {
	        			dSocket.send(dPacket); 
	        		} catch (IOException e) {
	        			e.printStackTrace(); 
	        			flag=false;
	        		}
	        		dSocket.close();
	        		
	        		if(flag){
	        			   Message msg=new Message();
			    		   msg.what=1;
			    		   handler.sendMessage(msg);
	        		}else{
	        			   Message msg=new Message();
			    		   msg.what=0;
			    		   handler.sendMessage(msg);
	        		}
	            }  
	        });  
	        thread.start(); 
	          
	}
	 

}
