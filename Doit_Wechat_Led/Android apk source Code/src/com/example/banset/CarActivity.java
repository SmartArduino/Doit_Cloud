package com.example.banset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject; 
 
 

import am.doit.ledmanager.R;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CarActivity extends Activity {
 
	private Button on_led,off_led;
	private Button but_send;
	private ListView 	text_View; 
	private EditText 	edit_Text;
	private TextView title;
	
	
	private ChatMsgViewAdapter mAdapter;
    private ArrayList<ChatMsgEntity> list = new ArrayList<ChatMsgEntity>();
	private String 		me_text,he_text,tip_text; 
	

    private static HandleThread keepThread; 
	public static String id,key,name;
	private static final int ME_ID = 1;
	private static final int HE_ID = -1; 
	private static final int TIP_ID = 0; 	

	
	Handler myHandler = new Handler(){
		public void handleMessage(Message msg) {	 
			switch(msg.what){ 
			case -1:	 
				Toast.makeText(CarActivity.this,"登录失败！",Toast.LENGTH_LONG).show();	            	
				break; 
			case 1:	  
				 ConnectSuccess();
				 break; 
			case 2:	  
				 updateHeText();
				 break; 
			case 3:	  
				 updateMeText();
				 break; 				 
			case 4:	  
				 updateTipText();
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
		
		title = (TextView) findViewById(R.id.ledtitle);
		title.setText(name);
		
		 text_View = (ListView )findViewById(R.id.mycontent);
	     edit_Text = (EditText)findViewById(R.id.edit);
	     
	        
	     on_led = (Button)findViewById(R.id.led_on);
	     on_led.setOnClickListener(new OnClickListener(){ 
	        	public void onClick(View v){ 
	        
	        		new Thread(new Runnable() {  
	                    @Override  
	                    public void run() {  
	                    	try {
	    		        		MyHttp myGet = new MyHttp("http://wechat.doit.am/cloud_api/publish.php?cmd=publish&topic="+id+"_chat&device_id="+id+"&device_key="+key+"&message="+Uri.encode("led|1"));
	    		        		String des = myGet.httpPost();
	    		        	 
	    		        		
//	    					    if(des.equals("ok")){
//	    					        me_text = str;
//	    	    		            Message msg2=new Message();
//	        			        	msg2.what=3;
//	        			        	myHandler.sendMessage(msg2);
//	    					    }else{
//	    					        tip_text = "\""+str+"\""+" 发送失败.";
//	    	    		            Message msg2=new Message();
//	        			        	msg2.what=4;
//	        			        	myHandler.sendMessage(msg2);
//	    					    }
	    	        		}catch (Exception e) {
	    	        			e.printStackTrace(); 
	    	        		} 
	                    }  
	                }).start();  
	        		
	        	}
	        });
	     
	     off_led = (Button)findViewById(R.id.led_off);
	     off_led.setOnClickListener(new OnClickListener(){ 
	        	public void onClick(View v){
	        		new Thread(new Runnable() {  
	                    @Override  
	                    public void run() {  
	                    	try {
	    		        		MyHttp myGet = new MyHttp("http://wechat.doit.am/cloud_api/publish.php?cmd=publish&topic="+id+"_chat&device_id="+id+"&device_key="+key+"&message="+Uri.encode("led|0"));
	    		        		String des = myGet.httpPost();
	    		        		 
	    	        		}catch (Exception e) {
	    	        			e.printStackTrace(); 
	    	        		} 
	                    }  
	                }).start();  
	 
	        	}
	        });
	     
	     
	     but_send = (Button)findViewById(R.id.send);
	     but_send.setOnClickListener(new OnClickListener(){ 
	        	public void onClick(View v){
	        		String str = edit_Text.getText().toString();
	            	if(str.length()>0){
	            		SendTEXT msg =   new SendTEXT();
	            		msg.str = str;
	            		msg.start();
	            		edit_Text.setText("");
//	            		me_text=str; 
//	            		sendAnMessage(19);  
//	                   	edit_Text.setText("");
	            	} 
	        	}
	        });
//		 but_send.setEnabled(false);
	     text_View.setDividerHeight(0); 
	         
		 keepThread = new HandleThread();
		 keepThread.requestOn(); 
		 keepThread.start(); 
	}

  
    private void ConnectSuccess(){ 
        int RId= R.layout.list_say_tip_item; 
        
        ChatMsgEntity newMessage = new ChatMsgEntity("连接成功",null,null, RId, TIP_ID);
        list.add(newMessage);
		mAdapter = new ChatMsgViewAdapter(this, list);
		text_View.setAdapter(mAdapter);
		 
    }
	   
    private void updateTipText(){ 
        int RId= R.layout.list_say_tip_item; 
        
        ChatMsgEntity newMessage = new ChatMsgEntity(tip_text,null,null, RId, TIP_ID);
        list.add(newMessage); 
        mAdapter.notifyDataSetChanged();
        text_View.setSelection(text_View.getCount());  
		 
    }
    
    private void updateHeText(){   
            int RId= R.layout.list_say_he_item; 
            ChatMsgEntity newMessage = new ChatMsgEntity(he_text,null,null, RId,HE_ID);
            list.add(newMessage);
            mAdapter.notifyDataSetChanged();
            text_View.setSelection(text_View.getCount());  

    }
 
    
    private void updateMeText(){   
            int RId= R.layout.list_say_me_item; 
            ChatMsgEntity newMessage = new ChatMsgEntity(me_text,null,null, RId,ME_ID);
            list.add(newMessage);
            mAdapter.notifyDataSetChanged();               
            text_View.setSelection(text_View.getCount());
     
    }
    
    
    
	  private class SendTEXT extends Thread{
		     public String str;
		     
	    	 public void run() {

	        		try {
		        		MyHttp myGet = new MyHttp("http://wechat.doit.am/cloud_api/publish.php?cmd=m2m_chat&topic="+id+"_chat&device_id="+id+"&device_key="+key+"&message="+str);
		        		String des = myGet.httpPost();
		        		
					    if(des.equals("ok")){
					        me_text = str;
	    		            Message msg2=new Message();
    			        	msg2.what=3;
    			        	myHandler.sendMessage(msg2);
					    }else{
					        tip_text = "\""+str+"\""+" 发送失败.";
	    		            Message msg2=new Message();
    			        	msg2.what=4;
    			        	myHandler.sendMessage(msg2);
					    }
	        		}catch (Exception e) {
	        			e.printStackTrace(); 
	        		} 
	        }
	  } 
    
	   public void onDestroy()  {
	        super.onDestroy();   
			keepThread.requestExit();
	    }  
    
	   public boolean onKeyDown(int keyCode, KeyEvent event)   
	    {  
	                 if(keyCode == KeyEvent.KEYCODE_BACK)  
	                 {  
	                         finish();      
	                         return true;  
	                 }  
	                 return super.onKeyDown(keyCode, event);  
	    }  
	 //////////////////////////////////////////
    private class HandleThread extends Thread
    { 
        private boolean bRun = true;
        private boolean keep_run=false;
        
        InputStream is;
        BufferedReader br;
        OutputStream os;
        PrintWriter pw;
    	Socket socket;;
        
        public void requestExit(){
        	bRun = false;  
        	
    		try{
	            br.close();  
	            is.close();  
	            pw.close();  
	            os.close(); 
	            socket.close(); 
 			}
 			catch(Exception e){
 				e.printStackTrace();
 			}
        }        
        
        public void requestOn(){
        	bRun = true;  
        }  
          
        
    	public void run() {

    			 try {
    		            //1.建立客户端socket连接，指定服务器位置及端口  
    		            socket =new Socket("s.doit.am",8810);  
    		            socket.setSoTimeout(5000);
    		            //2.得到socket读写流  
    		            OutputStream os=socket.getOutputStream();  
    		            PrintWriter pw=new PrintWriter(os);  
    		            //输入流  
    		            is=socket.getInputStream();  
    		            br=new BufferedReader(new InputStreamReader(is));  
    		            //3.利用流按照一定的操作，对socket进行读写操作  
    		           
    		            String info="cmd=m2m_chat&device_id="+id+"&device_key="+key+"&topic="+id+"_chat\r\n";  
    		            pw.write(info);  
    		            pw.flush();  
//    		            socket.shutdownOutput();  
    		         
    		            String reply=null;  
    		            while(!((reply=br.readLine())==null)){
    		            	int index = reply.indexOf("res=");
    		                if(index != -1){
    		                	String res = reply.substring(index+4,index+5);
    		                	if(res.equals("1")){
 		    		            
     		                	       Message msg=new Message();
     					    		   msg.what=1;
     					    		   myHandler.sendMessage(msg);
     					    		   
     					    		   keep_run=true;
     					    		   
    					    		   break;
    		                	}else{
    		                	       Message msg=new Message();
    					    		   msg.what=-1;
    					    		   myHandler.sendMessage(msg);
    					    		   
    					    		   keep_run=false;
    		                	}
    		                }
    		            }  
    		          

//    		            socket.close();  
    			    }catch(InterruptedIOException e){  
    		            e.printStackTrace();  
	     			}catch (IOException e) {  
    		        	keep_run=false;
    		            e.printStackTrace();  
    		        }  
    			 
    			 
    			 if(keep_run){
    		    	    while(bRun){
    		     			try{
    		     				Thread.sleep(1000);
    		     			}
    		     			catch(Exception e){
    		     				e.printStackTrace();
    		     			}
    		     			
      		     			try{
//      		     			    InputStream is=socket.getInputStream();  
//      	    		            BufferedReader br=new BufferedReader(new InputStreamReader(is));  
      	    		            String reply=null;
      	    		            while(!((reply=br.readLine())==null)){ 
      	    		            	int index = reply.indexOf("message=");
      	    		                if(index != -1){
	      	    		                he_text = reply.substring(index+8);
	      	    		               	Message msg2=new Message();
	    	    			        	msg2.what=2;
	    	    			        	myHandler.sendMessage(msg2);
      	    		                }
      	    		            }
    		     			}catch(InterruptedIOException e){  
    		     				e.printStackTrace();
    		     			}catch(Exception e){
    		     				e.printStackTrace();
    		     			}
    		     			
    		    		} 	
    			 }
 
       }     
    	
    	
    	
   }

    
} 

