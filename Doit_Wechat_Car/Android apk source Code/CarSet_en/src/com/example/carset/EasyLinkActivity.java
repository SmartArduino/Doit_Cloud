package com.example.carset;
 
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
 
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
 
import com.mxchip.easylink.EasyLinkAPI;
import com.mxchip.easylink.FTCListener;
import com.mxchip.wifiman.EasyLinkWifiManager;

 
public class EasyLinkActivity extends Activity {

    private String PATH;
    private String FAVOSSIDPATH ="/EMW3165ssid.xml";
    private String FAVOPASSPATH ="/EMW3165pass.xml";
    
    private List<String> ssid_list = new ArrayList<String>(); 
    private List<String> pass_list = new ArrayList<String>(); 
	private String str_ssid="",str_pass="";
	private String str_ssid_FLAG="",str_pass_FLAG="";
	private int index;
    
	private TextView wifissid;
	private EditText wifipsw;
	public EasyLinkAPI elapi;
	private Context ctx = null;
	private EasyLinkWifiManager mWifiManager = null;
    private ProgressDialog pd;  
    RelativeLayout b_ss;
    boolean flag=false;
    ProgressBar pb;
    TextView b_text;
    TcpServer thread;
    
    @SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
        public void handleMessage(Message msg) {
       
        	switch(msg.what){
        	case 0:     
	            try {  
	       	  	  	  elapi.stopFTC();
	  				  elapi.stopEasyLink();
  

					  flag=false;
					  pb.setAlpha(0);
					  b_text.setText(R.string.str_start);
					    
	          	    if(str_ssid_FLAG.equals(str_ssid)){
	          	    	if(!str_pass_FLAG.equals(str_pass)){
	          		    	if(str_ssid.length()>0 && str_pass.length()>0 && index!=-1){
	    	              	    ssid_list.set(index, str_ssid);
	    	              	    pass_list.set(index, str_pass);
	    	  				    writeSSID(FAVOSSIDPATH);
	    	  				    writePASS(FAVOPASSPATH);
	              	    	}
	          	    	}
	          	    }else{
	          	    	if(str_ssid.length()>0 && str_pass.length()>0){
		              	    ssid_list.add(str_ssid);
		              	    pass_list.add(str_pass);
		  				    writeSSID(FAVOSSIDPATH);
		  				    writePASS(FAVOPASSPATH);
	          	    	}
	          	    }
	            } catch (Exception e) {  
	                // TODO Auto-generated catch block  
	                e.printStackTrace();  
	            } 	
          	    
          	    
          	    
				  new AlertDialog.Builder(EasyLinkActivity.this)
		          .setIcon(android.R.drawable.ic_dialog_info)
		          .setTitle(R.string.tip)
		          .setMessage(R.string.str_set_s) 
		          .setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
		              public void onClick(DialogInterface dialog, int whichButton) {
  
						    dialog.dismiss();    
						    EasyLinkActivity.this.finish();
		              }
		          }).show();
				  
        		 break; 
        	case 1:       
	            break; 	
        	case -1:       
	            break;  
    		default:
    			break;
        	}
        }
	};
	
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		   requestWindowFeature(Window.FEATURE_NO_TITLE); 
		   
		setContentView(R.layout.wasylink);

		readSSID(FAVOSSIDPATH);
	    readPASS(FAVOPASSPATH);
	    
		wifissid = (TextView) findViewById(R.id.wifissid);
		wifipsw = (EditText) findViewById(R.id.wifipsw); 
		b_ss = (RelativeLayout) findViewById(R.id.sssearch);
		pb = (ProgressBar) findViewById(R.id.pb);
		b_text = (TextView) findViewById(R.id.b_text);
		
		ctx = EasyLinkActivity.this;
		elapi = new EasyLinkAPI(ctx);

		mWifiManager = new EasyLinkWifiManager(ctx);
		wifissid.setText(mWifiManager.getCurrentSSID());
 
	 
		String ssid = mWifiManager.getCurrentSSID();
		index=-1;
		int len = ssid_list.size();
		for(int i=0; i<len; i++){
			if(ssid.equals(ssid_list.get(i))){  
				wifipsw.setText(pass_list.get(i)); 
				str_ssid_FLAG = ssid;
				str_pass_FLAG = pass_list.get(i);
				index = i;
				i=len;
			}
		}
		
		
		b_ss.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(flag){ 
					 elapi.stopFTC();
					 elapi.stopEasyLink();
					 flag=false;
					 pb.setAlpha(0);
					 b_text.setText(R.string.str_start);
					 str_ssid = null;
					 str_pass = null;
					 
				}else{
					if(wifissid.getText().toString().length()>0 && wifipsw.getText().toString().length()>0){
						flag=true;
						pb.setAlpha(1);
						b_text.setText(R.string.str_stop);
						str_ssid = wifissid.getText().toString();
						str_pass = wifipsw.getText().toString();
						 
	 		    		    
						elapi.startFTC(wifissid.getText().toString().trim(),wifipsw.getText().toString(), new FTCListener() {
							@Override
							public void onFTCfinished(String ip, String jsonString) {
//								  Log.d("FTCEnd", ip + " " + jsonString);
//					      	  	  elapi.stopFTC();
//								  elapi.stopEasyLink();
//								  flag=false;
//								  pb.setAlpha(0);
//								  b_text.setText("开始");
//								   
//				          	    if( !str_ssid_FLAG.equals(str_ssid) || !str_pass_FLAG.equals(str_pass)){
//				            	    ssid_list.add(str_ssid);
//				            	    pass_list.add(str_pass);
//								    writeSSID(FAVOSSIDPATH);
//								    writePASS(FAVOPASSPATH);
//				        	    }
//				          	    
//								  new AlertDialog.Builder(EasyLinkActivity.this)
//						          .setIcon(android.R.drawable.ic_dialog_info)
//						          .setTitle("提示")
//						          .setMessage("配置成功!") 
//						          .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//						              public void onClick(DialogInterface dialog, int whichButton) {
//
//
//										    dialog.dismiss();    
//										    EasyLinkActivity.this.finish();
//						              }
//						          }).show();
//								  
//								  
							}
					
							@Override
							public void isSmallMTU(int MTU) {
							}
						}); 
						thread = new TcpServer();
						thread.start();
						
					}else{
						new AlertDialog.Builder(EasyLinkActivity.this) 
			    	    .setTitle(R.string.str_tip_name_pass) 
			    	    .setNegativeButton(R.string.str_ok, new DialogInterface.OnClickListener() {
			    	        public void onClick(DialogInterface dialog, int which) {
			    	        	 
			    	        }
			    	    }).show();
 
					}
				}
			 
				 
			}
		}); 
		
		
		thread = new TcpServer();
		thread.start();
	}
	
	
	
	
	  private class TcpServer extends Thread{ 
			
	    	 public void run() {
	    		   
//	    		    ServerSocket serverSocket = null;  
//	    	        // 声明一个socket来接受客户端连接  
//	    	        Socket socket = null;  
//	    	        try {  
//	    	            int temp;  
//	    	            // 定义服务器端socket并指定监听端口  
//	    	            serverSocket = new ServerSocket(8001);  
//	    	            // 调用阻塞式方法来获取客户端连接的socket  
//	    	            socket = serverSocket.accept();  
//	    	            // 获取客户端socket的输入流  
//	    	            InputStream inputStream = socket.getInputStream();  
//	    	            
//	    	            PrintWriter out = new PrintWriter(socket.getOutputStream()); 
//	    	            // 读取客户端socket的输入流的内容并输出  
//	    	            byte[] buffer = new byte[1024];  
//	    	            while ((temp = inputStream.read(buffer)) != -1) {  
//	    	            	String res = (new String(buffer, 0, temp)); 
//	    	            	if(res.equals("ok") || res.length()>0){
//	    	            		
//		 				       Message msg1=new Message();
//		 		    		   msg1.what=0;
//		 		    		   handler.sendMessage(msg1); 
//		 		    		   
//		 		    		    out.println("ok");  
//		 		    		    out.flush(); 
//		 		                out.close(); 
//		 		    		    inputStream.close();                
//		 		    		    socket.close();  
//		    	                serverSocket.close();  
//		 		    		    return;
//	    	            	}
//	    	            }  
//	    	        } catch (IOException e) {  
//	    	            // TODO Auto-generated catch block  
//	    	            e.printStackTrace();  
//	    	        } finally {  
//	    	        	
//	    	            try {  
//	    	            	if(socket!=null){
//		    	                socket.close();   
//	    	            	}
//	    	            	if(serverSocket!=null){ 
//		    	                serverSocket.close();
//	    	            	}
//	    	            } catch (IOException e) {  
//	    	                // TODO Auto-generated catch block  
//	    	                e.printStackTrace();  
//	    	            }  
//	    	        }  
	    		 
	    		 
	    		 
	    		 
	    		 //创建一个DatagramSocket对象，并指定监听端口。（UDP使用DatagramSocket）    
	    	        DatagramSocket socket=null;  
	    	        try {  
	    	            socket = new DatagramSocket(8001);  
	    	            //创建一个byte类型的数组，用于存放接收到得数据    
	    	            byte data[] = new byte[4*1024];    
	    	            //创建一个DatagramPacket对象，并指定DatagramPacket对象的大小    
	    	            DatagramPacket packet = new DatagramPacket(data,data.length);    
	    	            //读取接收到得数据    
	    	            socket.receive(packet);    
	    	            //把客户端发送的数据转换为字符串。    
	    	            //使用三个参数的String方法。参数一：数据包 参数二：起始位置 参数三：数据包长    
	    	            String res = new String(packet.getData(),packet.getOffset() ,packet.getLength());    
	    	         
    	            	if(res.equals("ok") || res.length()>0){
//    	            		byte data1[] = "ok".getBytes();
//    	    	            DatagramPacket packet1 = new DatagramPacket(data1,data1.length);  
//    	            		socket.send(packet1);
    	            		 
	 				        Message msg1=new Message();
	 		    		    msg1.what=0;
	 		    		    handler.sendMessage(msg1);
	 		    		     
	 		    		    socket.close();   
	 		    		    return;
    	            	}
    	    	    } catch (IOException e) {  
	    	            // TODO Auto-generated catch block  
	    	            e.printStackTrace();  
	    	        } finally {  
	    	        	
	    	            try {  
	    	            	if(socket!=null){
		    	                socket.close();   
	    	            	} 
	    	            } catch (Exception e) {  
	    	                // TODO Auto-generated catch block  
	    	                e.printStackTrace();  
	    	            }  
	    	        	
	    	        }

	        } 
	 
	    }

	  
	  
	
	
	 public boolean onKeyDown(int keyCode, KeyEvent event)   
	    {  
	                 if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)  
	                 {  
	                           
	                		  elapi.stopFTC();
	    					  elapi.stopEasyLink();
	    					  flag=false;
	    					  pb.setAlpha(0);
	    					  b_text.setText(R.string.str_start);
	    					    
	                          finish();   
	                                   
	                          return true;  
	                 }  
	                 return super.onKeyDown(keyCode, event);  
	    }  
	 

		private void readSSID(String res){  
			PATH =this.getFilesDir().getPath();
			String path =PATH + res;
			
			
	    	File dirFile=new File(PATH);
	    	if(!dirFile.exists()){
	    		dirFile.mkdir();
	    	}
		
			String str=null;
			
			if(!ssid_list.isEmpty()){
				ssid_list.clear();
			}
	        try {
	        	FileInputStream fis =new  FileInputStream (path);  
	        	BufferedReader in = new BufferedReader(new InputStreamReader(fis));  
		        while( (str=in.readLine()) != null ){
		        	ssid_list.add(Uri.decode(str)); 
		        }
		        in.close();
	        }
	        catch(FileNotFoundException e){   
	        }
	        catch(Exception e){   
	        }
	         
		}

		private void readPASS(String res){  
			
			PATH =this.getFilesDir().getPath();
			String path =PATH + res;
			
	    	File dirFile=new File(PATH);
	    	if(!dirFile.exists()){
	    		dirFile.mkdir();
	    	}
		
			String str=null;
			
			if(!pass_list.isEmpty()){
				pass_list.clear();
			}
	        try {
	        	FileInputStream fis =new  FileInputStream (path);  
	        	BufferedReader in = new BufferedReader(new InputStreamReader(fis));  
		        while( (str=in.readLine()) != null ){
		        	pass_list.add(Uri.decode(str)); 
		        }
		        in.close();
	        }
	        catch(FileNotFoundException e){   
	        }
	        catch(Exception e){   
	        }
	         
		}	
		
		
	 private void writeSSID(String res){   
			PATH =this.getFilesDir().getPath();
			String path =PATH + res;
			
	    	File dirFile=new File(PATH);
	    	if(!dirFile.exists()){
	    		dirFile.mkdir();
	    	}
	    	
			int len = ssid_list.size();
			File write = new File(path);
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(write));
				for(int i=0; i<len; i++){ 
					bw.write(Uri.encode(ssid_list.get(i))+"\r\n");
				} 
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		 
		}
	 private void writePASS(String res){   
			PATH =this.getFilesDir().getPath();
			String path =PATH + res;
			
	    	File dirFile=new File(PATH);
	    	if(!dirFile.exists()){
	    		dirFile.mkdir();
	    	}
	    	
			int len = pass_list.size();
			File write = new File(path);
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(write));
				for(int i=0; i<len; i++){ 
					bw.write(Uri.encode(pass_list.get(i))+"\r\n");
				} 
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		 
		}
}
