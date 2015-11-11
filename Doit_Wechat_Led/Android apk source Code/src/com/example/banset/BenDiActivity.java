package com.example.banset;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
 
 
import am.doit.ledmanager.R;
import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

public class BenDiActivity extends Activity implements OnClickListener {

	private boolean wifi_connected;
	private String wifi_ssid;
	private TextView text1;

	/* 发送广播端的socket */
	private MulticastSocket ms;
	private MulticastSocket ms1;
	private MulticastSocket ms2;
	// 定义每个数据报的最大大小为1KB
	private static final int DATA_LEN = 1024;
	// 定义接收网络数据的字节数组
	byte[] inBuff = new byte[DATA_LEN];
	byte[] inBuff1 = new byte[DATA_LEN];
	byte[] inBuff2 = new byte[DATA_LEN];
	// 以指定的字节数组创建准备接收数据的DatagramPacket对象
	private DatagramPacket inPacket = new DatagramPacket(inBuff, inBuff.length);
	private DatagramPacket inPacket1 = new DatagramPacket(inBuff1,inBuff1.length);
	private DatagramPacket inPacket2 = new DatagramPacket(inBuff2,inBuff2.length);
	DatagramPacket dataPacket = null;
	DatagramPacket dataPacket1 = null;
	DatagramPacket dataPacket2 = null;
	InetAddress address;
	InetAddress address1;
	InetAddress address2;

	private String str_udp1;
	private String str_udp2;
	private String str_udp3;
	private String str_ip;
	private String str_speed; 
	private RelativeLayout mainlayout;
	private String str_instr;
	private Button but1;
	private SeekBar seek1,seek2;
	private Switch switch1,switch2;
	
	Handler handler = new Handler() { 
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				text1.setText("设备不在线，请确认连接到Doit_ESP_xxxxxx的wifi或者局域网");
				but1.setVisibility(View.VISIBLE);
				break;
			case 1:
				text1.setText("连接成功");
				but1.setVisibility(View.GONE);
				str_ip = getip(str_udp1);
//				System.out.println(str_ip);
				mainlayout.setVisibility(View.VISIBLE);
//				getspeed("5");
				break;
			case 2:
				//text1.setText(str_instr + "OK");
				break;
			case 3:
				//text1.setText(str_instr + "指令发送失败");
				break;
			case 4:
				//text1.setText("获取速度失败");
				break;
			case 5:
				//text1.setText("当前速度: " + str_speed);
//				System.out.println(Integer.parseInt(str_speed));
//				seek.setProgress(Integer.parseInt(str_speed));
				break;
			default:
				break;
			}
		}

	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		   this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.bendi);

		initView();
//		mainlayout.setVisibility(View.VISIBLE);
		wifi_connected = isWifiConnected(); 
		if (!wifi_connected) {
			text1.setText("请连接到无线路由器或者小车的soft ap");
			Intent intent = new Intent();
			intent.setAction("android.net.wifi.PICK_WIFI_NETWORK"); 
			startActivity(intent);
			but1.setVisibility(View.VISIBLE);
		} else {
			connectcar();
		}

		wifi_ssid = getConnectWifiSsid(); 
	}

	public void initView() {
		text1 = (TextView) findViewById(R.id.text1);
		mainlayout = (RelativeLayout) findViewById(R.id.mainlayout);  
		but1 = (Button) findViewById(R.id.but1);
		seek1 = (SeekBar) findViewById(R.id.seek1);
		seek1.setOnSeekBarChangeListener(new OnSeekBarChangeListener()  
	        {  
	            public void onProgressChanged(SeekBar arg0,int progress,boolean fromUser)  
	            {  
	             	send("2",String.valueOf(progress));
	            }  
	              
	            @Override  
	            public void onStartTrackingTouch(SeekBar seekBar) {  
	                // TODO Auto-generated method stub  
	                  
	            }  
	            @Override  
	            public void onStopTrackingTouch(SeekBar seekBar) {  
	                // TODO Auto-generated method stub  
	                  
	  
	            }  
	        });  
		 
		seek2 = (SeekBar) findViewById(R.id.seek2);
		seek2.setOnSeekBarChangeListener(new OnSeekBarChangeListener()  
        {  
            public void onProgressChanged(SeekBar arg0,int progress,boolean fromUser)  
            {  
            	send("4",String.valueOf(progress));
            }  
              
            @Override  
            public void onStartTrackingTouch(SeekBar seekBar) {  
                // TODO Auto-generated method stub  
                  
            }  
            @Override  
            public void onStopTrackingTouch(SeekBar seekBar) {  
                // TODO Auto-generated method stub  
                  
  
            }  
        });  
		
		
		but1.setOnClickListener(this);
		
		
		switch1 = (Switch) findViewById(R.id.switch1);
		switch1.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
	        
            @Override  
            public void onCheckedChanged(CompoundButton buttonView,  
                    boolean isChecked) {  
                // TODO Auto-generated method stub  
                if (isChecked) {  
                	send("0","1");
                } else {  
                	send("0","0");
                } 
            }  
        });  
		
		switch2 = (Switch) findViewById(R.id.switch2);
		switch2.setOnCheckedChangeListener(new OnCheckedChangeListener() {  
	        
            @Override  
            public void onCheckedChanged(CompoundButton buttonView,  
                    boolean isChecked) {  
                // TODO Auto-generated method stub  
                if (isChecked) {  
                	send("1","1");
                } else {  
                	send("1","0");
                } 
            }  
        });  
	}

	public boolean isWifiConnected() {

		ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWiFiNetworkInfo = mConnectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (mWiFiNetworkInfo != null) {
			return mWiFiNetworkInfo.isAvailable();
		}

		return false;
	}

	private String getConnectWifiSsid() {
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo(); 
		return wifiInfo.getSSID();
	}



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		  
		case R.id.but1:
			connectcar();
			break;

		default:
			break;
		}
	}

	public void send(String id, String str) {
		String str1 = "cmd=gpio&id=" + id +"&v="+ str;
		byte[] data = str1.getBytes();
		try {
			address1 = InetAddress.getByName(str_ip);
			dataPacket1 = new DatagramPacket(data, data.length, address1, 8089);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				try {
					/* 创建socket实例 */
					ms1 = new MulticastSocket();
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					ms1.setSoTimeout(4);
					ms1.send(dataPacket1);
					// 读取Socket中的数据，读到的数据放在inPacket所封装的字节数组中
					ms1.receive(inPacket1);
					System.out.println(new String(inBuff1, 0, inPacket1
							.getLength()));
					str_udp2 = new String(inBuff1, 0, inPacket1.getLength());
					System.out.println(str_udp2);

				} catch (Exception e) {
					e.printStackTrace();
				}

				ms1.close();
				if (str_udp2 == "ok") {
					Message msg = new Message();
					msg.what = 2;
					handler.sendMessage(msg);
				} else {
					Message msg = new Message();
					msg.what = 3;
					handler.sendMessage(msg);
				}
			}

		}.start();
	}

//	public void getspeed(String str) {
//		String str1 = "cmd=control&d=" + str;
//		byte[] data = str1.getBytes();
//		try {
//			address2 = InetAddress.getByName(str_ip);
//			dataPacket2 = new DatagramPacket(data, data.length, address2, 8089);
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		new Thread() {
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				super.run();
//				try {
//					/* 创建socket实例 */
//					ms2 = new MulticastSocket();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//				int i = 0;
//				while(i<5) {
//					try {
//						sleep(200);
//					} catch (InterruptedException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//				
//				try {
//					ms2.setSoTimeout(4);
//					ms2.send(dataPacket2);
//					// 读取Socket中的数据，读到的数据放在inPacket所封装的字节数组中
//					ms2.receive(inPacket2);
//					System.out.println(new String(inBuff2, 0, inPacket2
//							.getLength()));
//					
//					if (inPacket2.getLength() > 0) {
//						str_udp3 = new String(inBuff2, 0, inPacket2.getLength());
//						break;
//					}
//
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				i++;
//			}
//
//				ms2.close();
//				if(str_udp3 == "") {
//					Message msg = new Message();
//					msg.what = 4;
//					handler.sendMessage(msg);
//				} else {
//					str_speed = str_udp3.substring(str_udp3.lastIndexOf("=")+1);
//					Message msg = new Message();
//					msg.what = 5;
//					handler.sendMessage(msg);
//				}
//			}
//
//		}.start();
//	}
	
	public String getip(String str) {
		int a = str.indexOf("=");
		int b = str.indexOf("=", a + 1);
		int c = str.indexOf("&", b + 1);
		String str1 = str.substring(b + 1, c);

		if (str1 == "") {
			int d = str.lastIndexOf("=");
			str1 = str.substring(d + 1);
		}
		return str1;
	}
	
	public void connectcar() {

		text1.setText("正在连接小车");
		byte[] data = "cmd=ping".getBytes();
		try {
			address = InetAddress.getByName("255.255.255.255");
			dataPacket = new DatagramPacket(data, data.length, address,
					8089);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 发送的数据包，局网内的所有地址都可以收到该数据包
		new Thread() { 
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				int i = 0;
				str_udp1 = null;
				try {
					/* 创建socket实例 */
					ms = new MulticastSocket();
				} catch (Exception e) {
					e.printStackTrace();
				}
				while (i < 6) {
					try {
						sleep(300);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					try {
						// ms.setTimeToLive(1);
						ms.setSoTimeout(1);
						ms.send(dataPacket);
						System.out.println(i);
						// 读取Socket中的数据，读到的数据放在inPacket所封装的字节数组中
						ms.receive(inPacket);
						System.out.println(new String(inBuff, 0, inPacket
								.getLength()));
						if (inPacket.getLength() > 0) {
							str_udp1 = new String(inBuff, 0,
									inPacket.getLength());
							break;
						}

					} catch (Exception e) {
						e.printStackTrace();
						System.err.println("error");
					}
					i++;
				}
				//////////////////////////////////
				System.out.println("end");
				ms.close();
				if (str_udp1 == null) {
					Message msg = new Message();
					msg.what = 0;
					handler.sendMessage(msg);
				} else {
					Message msg = new Message();
					msg.what = 1;
					handler.sendMessage(msg);
				}
			}

		}.start();
	
	}

}
