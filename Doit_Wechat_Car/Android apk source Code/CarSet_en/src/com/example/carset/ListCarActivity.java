package com.example.carset;

   
import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList; 
import java.util.List; 
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;
 
import com.example.carset.TitlePopup.OnItemOnClickListener;
import com.example.esptouch.demo_activity.EsptouchDemoActivity;
import com.example.esptouch.demo_activity.TipActivity;
import com.example.myzxingtest.MainActivity;   
 
import android.support.v4.widget.SwipeRefreshLayout; 
      
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context; 
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager; 
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class ListCarActivity extends Activity  implements SwipeRefreshLayout.OnRefreshListener { 
//    private static final String PATH = "/data/data/com.example.carset/files/";
    
//	private static final String ACTION = "com.example.carset.action.NEW_FILE";
//	private static final String ACTION_FINISH = "com.example.carset.action.UPLOAD_FINISH";
	
	  public static final int REQUSET = 1; 
	  
	  
    public static List<String> img_list = new ArrayList<String>(); 
    public static List<String> id_list = new ArrayList<String>();
    public static List<String> key_list = new ArrayList<String>();
    public static List<String> name_list = new ArrayList<String>();
    public static List<String> stat_list = new ArrayList<String>();
    
    
    private ListView listView = null;  
    BookItemAdapter adapter;
	private ProgressDialog pd;   
	private Button button = null;
	private TitlePopup titlePopup;
	private SwipeRefreshLayout swipeRefreshLayout;
	Timer timer = new Timer();  
    public static boolean pause_flag=true;
	
	@SuppressLint("HandlerLeak")
	Handler myHandler = new Handler(){
		public void handleMessage(Message msg) {	 
			switch(msg.what){
			case -11:	  
				 pd.dismiss();   
				 break;
			case -1:	
				pd.dismiss(); 
				Toast.makeText(ListCarActivity.this,R.string.str_get_device,Toast.LENGTH_LONG).show();	            	
//				adapter.notifyDataSetChanged();
				break;
			case 0:		   
				pd = ProgressDialog.show(ListCarActivity.this, "", getText(R.string.str_load_list));
			    break;
			case 1:	 
        	    reload();
        	    adapter.notifyDataSetChanged(); 
				pd.dismiss();  
				UpdateDevice();
				 break;
			case 2:	 
//        	    reload();
        	    adapter.notifyDataSetChanged();  
        	    break;
			case -2:	
//				Toast.makeText(ListCarActivity.this,"更新失败，请重新尝试！",Toast.LENGTH_LONG).show();	            	
        	    break;
			case 3:	 
        	    reload();
        	    adapter.notifyDataSetChanged();  
        	    swipeRefreshLayout.setRefreshing(false);
        	    break;
			case -3:	
			    swipeRefreshLayout.setRefreshing(false);
//				Toast.makeText(ListCarActivity.this,"更新失败，请重新尝试！",Toast.LENGTH_LONG).show();	            	
        	    break;
			case 4:	 
        	    reload();
        	    adapter.notifyDataSetChanged();   
        	    break;
			case -4:	
			    break;
			}
		}		
	};
	
	
	private void UpdateDevice(){

		 timer.schedule(new TimerTask() {
			 
			@Override
			public void run() {
				if(!pause_flag){
					String res = null;
					int len  = stat_list.size();
					int num = 0;
					for(int i=0;i<len;i++){
						try {
							MyHttp myDol = new MyHttp("http://wechat.myembed.com/cloud_api/num.php?device_id=" +id_list.get(i)+ "&device_key="+key_list.get(i));
							res = myDol.httpPost();
							if(!stat_list.get(i).equals(res)){
								stat_list.set(i,res);
								adapter.setModel(img_list.get(i),id_list.get(i),key_list.get(i),name_list.get(i),stat_list.get(i),i);
								num++;
							}
						} catch (Exception e) {
							i=len;
							num=0;
							break;
						}  
					}
					
					if(num>0){
					       Message msg=new Message();
			    		   msg.what=2;
			    		   myHandler.sendMessage(msg);
					}
				}
				
			}
		}, 0,2000);
	}
	
	private void reload(){ 
		adapter.clean();
		
        int len= img_list.size();
    	for(int i=0;i<len;i++){
    		adapter.addBook(img_list.get(i),id_list.get(i),key_list.get(i),name_list.get(i),stat_list.get(i));
    	} 
	}
	
	  @Override  
	    protected void onResume() {  
	        super.onResume();  
	        
		   	 String ssid = getConnectWifiSsid();
			 if(ssid.indexOf("Doit_ESP") == -1 && isNetworkAvailable(ListCarActivity.this)){
				 pause_flag=false;
			 }
		  
	    }  
	  
	  
		private String getConnectWifiSsid() {
			WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo(); 
			return wifiInfo.getSSID();
		}
	     
	      public boolean isNetworkAvailable(Activity activity)
	      {
	          Context context = activity.getApplicationContext(); 
	          ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	          
	          if (connectivityManager == null)
	          {
	              return false;
	          }
	          else
	          {
	              // 获取NetworkInfo对象
	              NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
	              
	              if (networkInfo != null && networkInfo.length > 0)
	              {
	                  for (int i = 0; i < networkInfo.length; i++)
	                  { 
	                      // 判断当前网络状态是否为连接状态
	                      if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
	                      {
	                          return true;
	                      }
	                  }
	              }
	          }
	          return false;
	      }
	 

    @SuppressLint("ResourceAsColor")
	public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);         		 
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.listlocalapp);
        
    	new setdata().start();
    	
        listView = (ListView)findViewById(R.id.device_list); 
     	adapter = new BookItemAdapter(ListCarActivity.this,listView);
		listView.setAdapter(adapter);
	    listView.setOnItemClickListener(new OnItemClickListener(){ 
	         public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
	        	 if(!stat_list.get(arg2).equals("0")){
	        		    Intent intent = new Intent(); 
						intent.putExtra("ID", id_list.get(arg2)); 
						intent.putExtra("KEY", key_list.get(arg2));
						intent.putExtra("NAME", name_list.get(arg2));
						intent.setClass(ListCarActivity.this, CarActivity.class);
						ListCarActivity.this.startActivity(intent);
	        	 }else{
	        		 Toast.makeText(ListCarActivity.this,"The target device is offline！",Toast.LENGTH_LONG).show();	            	
	        	 }
	            	
	         } 
	    });
	       
	    swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.id_swipe_ly);
	    swipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright, android.R.color.holo_green_light,  
                android.R.color.holo_orange_light, android.R.color.holo_red_light);  
	    swipeRefreshLayout.setOnRefreshListener(this);    
	 
	     
	    
        button = ((Button)findViewById(R.id.add_btn));
        button.setOnClickListener(new Button.OnClickListener() 
		{
    		public void onClick(View v) {
    			titlePopup.show(v);
    		}
    	});
        
        initData();
        
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("com.example.carset.service");
		startService(serviceIntent);
 
    }
     
   
    @Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        // TODO Auto-generated method stub  
        super.onActivityResult(requestCode, resultCode, data);  
        //requestCode标示请求的标示   resultCode表示有数据  
        if (requestCode == REQUSET && resultCode == RESULT_OK) {  
        	if(data.getStringExtra("RESULT").equals("1")){
        		new setdataThree().start();	
        	}else{
        		
        	}
        }   
    }  
    
    
    private void initData(){
    	titlePopup = new TitlePopup(this, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		titlePopup.addAction(new ActionItem(this, "Scan QR Code", R.drawable.qr));
		titlePopup.addAction(new ActionItem(this, "Configure(ESP8266)", R.drawable.set));
		titlePopup.addAction(new ActionItem(this, "Configure(EMW3165)", R.drawable.set));
		titlePopup.addAction(new ActionItem(this, "Manual Configure", R.drawable.set1));
	    titlePopup.setItemOnClickListener(new OnItemOnClickListener() {
			
			public void onItemClick(ActionItem item, int position) {
//				Toast.makeText(getApplication(), "This is : " + position, Toast.LENGTH_SHORT).show();
				switch(position){ 
				default:break;
				
				case 0:
			  		 Intent intent = new Intent(ListCarActivity.this, MainActivity.class);
			  		 startActivityForResult(intent, REQUSET); 
					break;
					
				case 1:
			  		 Intent intent1 = new Intent(ListCarActivity.this, TipActivity.class);
	    		     startActivity(intent1); 
					break;
					
				case 2:
			  		 Intent intent3 = new Intent(ListCarActivity.this, TipTwoActivity.class);
	    		     startActivity(intent3); 
					break;
					
				case 3:
					 String ssid = getConnectWifiSsid();
					 if(ssid.indexOf("Doit_ESP") != -1){
						 Intent intent2 = new Intent(ListCarActivity.this, WifiListActivity.class);
		    		     startActivity(intent2); 
					 }else{ 
						 new AlertDialog.Builder(ListCarActivity.this)
			                .setIcon(android.R.drawable.ic_dialog_info)
			                .setTitle(R.string.tip)
			                .setMessage(R.string.str_tip_ap)
			                .setNegativeButton(R.string.str_cancel, new DialogInterface.OnClickListener() {
			                    public void onClick(DialogInterface dialog, int which) {
			                    	  dialog.dismiss(); 
			                    }
			                })
			                .setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
			                    public void onClick(DialogInterface dialog, int whichButton) {
		                         
			                    	if(android.os.Build.VERSION.SDK_INT > 10 ){ 
			                    	       startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
			                    	   } else {
			                    	       startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
			                    	   }
			                    	
			                    	  dialog.dismiss(); 
			                    	  
			                    	  pause_flag=true;
			                    }
			                }).show();

					 }

					break;
				
				}
			}
		});
	}
	
	
	  private String encode(String src){
			String des=Uri.encode(src);
			return des;
		}
	 
    private class setdata extends Thread{
   	 public void run() {
   		 
   	       Message msg1=new Message();
   		   msg1.what=0;
   		   myHandler.sendMessage(msg1);
   		    
	   	   img_list.clear();
	       id_list.clear(); 
	       key_list.clear();
	       name_list.clear();
	       stat_list.clear();
    	 
   	       try {
	  	    	 	 String uri = "http://wechat.doit.am/iot_api/list.php?open_id="+encode(LoginActivity.open_id)+
		        			 "&app_id=40";
		        			  
		    	   	 MyHttp myGet = new MyHttp(uri);
		    	     String des =  myGet.httpPost();
		    	     
		    	     if(!des.equals("null")){
			    	   	 JSONArray array = new JSONArray(des);
	                     int num = array.length();
	                       
		                 for(int j=0;j<num;j++){
							JSONObject jsonEvents = array.getJSONObject(j); 
		                	
		                	img_list.add(jsonEvents.getString("device_img"));
		                	id_list.add(jsonEvents.getString("device_id"));
		                	key_list.add(jsonEvents.getString("device_key"));
		                	name_list.add(jsonEvents.getString("device_name"));
		                	stat_list.add(jsonEvents.getString("device_stat"));
		                 } 
	   	    	  
		                 myHandler.sendEmptyMessageDelayed(1,2000);
		    	     }else{
		    	           Message msg=new Message();
			    		   msg.what=-11;
			    		   myHandler.sendMessage(msg);
		    	     }

   	          } catch (Exception e) {
   	           // TODO Auto-generated catch block
	    	           e.printStackTrace();
	    	           
	    	           Message msg=new Message();
		    		   msg.what=-1;
		    		   myHandler.sendMessage(msg);
		    		    
   	        }
   

       }  
   }

    private class setdataTwo extends Thread{
      	 public void run() {
      		  
      		    
   	   	   img_list.clear();
   	       id_list.clear(); 
   	       key_list.clear();
   	       name_list.clear();
   	       stat_list.clear();
       	 
      	       try {
   	  	    	 	 String uri = "http://wechat.doit.am/iot_api/list.php?open_id="+encode(LoginActivity.open_id)+
   		        			 "&app_id=40";
   		        			  
   		    	   	 MyHttp myGet = new MyHttp(uri);
   		    	     String des =  myGet.httpPost();
   		    	     
   		    	    if(!des.equals("null")){
			    	   	 JSONArray array = new JSONArray(des);
	                     int num = array.length();
	                       
		                 for(int j=0;j<num;j++){
							JSONObject jsonEvents = array.getJSONObject(j); 
		                	
		                	img_list.add(jsonEvents.getString("device_img"));
		                	id_list.add(jsonEvents.getString("device_id"));
		                	key_list.add(jsonEvents.getString("device_key"));
		                	name_list.add(jsonEvents.getString("device_name"));
		                	stat_list.add(jsonEvents.getString("device_stat"));
		                 } 
	   	    	  
		                 myHandler.sendEmptyMessageDelayed(3,2000);
		    	     }else{
	 	                   Message msg=new Message();
	  		    		   msg.what=-3;
	  		    		   myHandler.sendMessage(msg); 
		    	     }
		    	     
   		    	   	 
      	          } catch (Exception e) {
      	           // TODO Auto-generated catch block
   	    	           e.printStackTrace();
   	    	          myHandler.sendEmptyMessageDelayed(-3,2000);
//   	    	           Message msg=new Message();
//   		    		   msg.what=-3;
//   		    		   myHandler.sendMessage(msg);
   		    		    
      	        }
      

          }  
      }

    private class setdataThree extends Thread{
     	 public void run() {
     		   
  	   	   img_list.clear();
  	       id_list.clear(); 
  	       key_list.clear();
  	       name_list.clear();
  	       stat_list.clear();
      	 
     	       try {
  	  	    	 	 String uri = "http://wechat.doit.am/iot_api/list.php?open_id="+encode(LoginActivity.open_id)+
  		        			 "&app_id=40";
  		        			  
  		    	   	 MyHttp myGet = new MyHttp(uri);
  		    	     String des =  myGet.httpPost();
  		    	      
  		    	    if(!des.equals("null")){
			    	   	 JSONArray array = new JSONArray(des);
	                     int num = array.length();
	                       
		                 for(int j=0;j<num;j++){
							JSONObject jsonEvents = array.getJSONObject(j); 
		                	
		                	img_list.add(jsonEvents.getString("device_img"));
		                	id_list.add(jsonEvents.getString("device_id"));
		                	key_list.add(jsonEvents.getString("device_key"));
		                	name_list.add(jsonEvents.getString("device_name"));
		                	stat_list.add(jsonEvents.getString("device_stat"));
		                 } 
	   	    	  
	 	                   Message msg=new Message();
	  		    		   msg.what=4;
	  		    		   myHandler.sendMessage(msg); 
		    	     }else{
	 	                   Message msg=new Message();
	  		    		   msg.what=-3;
	  		    		   myHandler.sendMessage(msg); 
		    	     }
		    	       
     	          } catch (Exception e) {
     	           // TODO Auto-generated catch block
  	    	           e.printStackTrace(); 
  	    	           Message msg=new Message();
  		    		   msg.what=-4;
  		    		   myHandler.sendMessage(msg);
  		    		    
     	        }
     

         }  
     }

    
	


	public void onDestroy() {
		super.onDestroy(); 
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("com.example.carset.service");
		stopService(serviceIntent);

	}

	@Override
	public void onRefresh() { 
		new setdataTwo().start();
	}

}
