package com.example.banset;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorSelectedListener;

import am.doit.ledmanager.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class LedUdpActivity extends Activity {

	private boolean wifi_connected;
	private String wifi_ssid;
	private TextView text1;

	/* 发送广播端的socket */
	private MulticastSocket ms;
	private MulticastSocket ms1;
	// 定义每个数据报的最大大小为1KB
	private static final int DATA_LEN = 1024;
	// 定义接收网络数据的字节数组
	byte[] inBuff = new byte[DATA_LEN];
	byte[] inBuff1 = new byte[DATA_LEN];
	byte[] inBuff2 = new byte[DATA_LEN];
	// 以指定的字节数组创建准备接收数据的DatagramPacket对象
	private DatagramPacket inPacket = new DatagramPacket(inBuff, inBuff.length);
	private DatagramPacket inPacket1 = new DatagramPacket(inBuff1,
			inBuff1.length);
	DatagramPacket dataPacket = null;
	DatagramPacket dataPacket1 = null;
	DatagramPacket dataPacket2 = null;
	InetAddress address;
	InetAddress address1;
	InetAddress address2;

	private String str_udp1;
	private String str_udp2;
	private String str_ip;
	
	private Button but1;
	private RelativeLayout mainlayout;

	ColorPicker picker;
	ImageView bt1;
	SeekBar sk1;
	
	String device_id, device_key;
	String url;
	boolean flag1 = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ledudp);

		initView();

		wifi_connected = isWifiConnected();
		// System.out.println(wifi_connected);
		if (!wifi_connected) {
			text1.setText("请连接到无线路由器或者智能灯的soft ap");
			Intent intent = new Intent();
			intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
			startActivity(intent);
			but1.setVisibility(View.VISIBLE);
		} else {
			connectcar();
		}

		wifi_ssid = getConnectWifiSsid();
		System.out.println(wifi_ssid);
	}

	public void initView() {
		mainlayout = (RelativeLayout) findViewById(R.id.mainlayout);
		text1 = (TextView) findViewById(R.id.text1);

		but1 = (Button) findViewById(R.id.but1);
		
		bt1 = (ImageView) findViewById(R.id.bt1);
        sk1 = (SeekBar) findViewById(R.id.sk1);
        
		picker = (ColorPicker) findViewById(R.id.picker);
		picker.setShowOldCenterColor(false);

//		picker.setOnColorSelectedListener(new OnColorSelectedListener() {
//			
//			@Override
//			public void onColorSelected(int color) {
//				// TODO Auto-generated method stub
//				send(1);
//			}
//		});
		
		picker.setOnColorChangedListener(new OnColorChangedListener() {
			
			@Override
			public void onColorChanged(int color) {
				// TODO Auto-generated method stub
				send(1);
			}
		});
		
		bt1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(flag1) {
					send(0);
					flag1 = false;
				}else {
					send(1);
					flag1 = true;
				}
			}
		});
		
		sk1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				//System.out.println(sk1.getProgress());
				//send(1);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				send(1);
			}
		});
		
		but1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				but1.setVisibility(View.GONE);
				connectcar();
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

	Handler handler = new Handler() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
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
				//text1.setText("连接成功");
				but1.setVisibility(View.GONE);
				text1.setVisibility(View.GONE);
				mainlayout.setVisibility(View.VISIBLE);
				str_ip = getip(str_udp1);
				System.out.println(str_ip);
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
				//System.out.println(Integer.parseInt(str_speed));
				
				break;
			default:
				break;
			}
		}

	};

	
	public void sendUDP(int r, int g, int b) { 
		String str1 = "cmd=light&v=" + r + "|" +g + "|" + b;
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

		text1.setText("正在连接LED");
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

	public void send(int i) {
		switch (i) {
		case 0:
			sendUDP(0, 0, 0);
			bt1.setBackgroundResource(R.drawable.guan);
			break;
		case 1:
			int c = picker.getColor();
			int r = Color.red(c);
			int g = Color.green(c);
			int b = Color.blue(c);
			int l = sk1.getProgress() + 1;
			r = r*l/255;
			g = g*l/255;
			b = b*l/255;
			sendUDP(r, g, b);
			if(r+g+b == 0) {
				bt1.setBackgroundResource(R.drawable.guan);
				flag1=false;
			}else {
				if(!flag1) {
					bt1.setBackgroundResource(R.drawable.kai);
					flag1=true;
				}
			}
			break;
		default:
			break;
		}
		
	}

}
