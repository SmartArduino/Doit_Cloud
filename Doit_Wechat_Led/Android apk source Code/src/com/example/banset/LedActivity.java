package com.example.banset;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;
import com.larswerkman.holocolorpicker.ColorPicker.OnColorSelectedListener;

import am.doit.ledmanager.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class LedActivity extends Activity implements OnColorSelectedListener {

	ColorPicker picker;
	//SVBar svBar;
	//SaturationBar saturationBar;
	//ValueBar valueBar;
	ImageView bt1;
	SeekBar sk1;
	TextView tx1;
	
	String device_id, device_key;
	String url;
	boolean flag1 = false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		device_id = intent.getStringExtra("ID");
        device_key = intent.getStringExtra("KEY");
        String name = intent.getStringExtra("NAME");
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        
		setContentView(R.layout.led);
		tx1 = (TextView) findViewById(R.id.ledtitle);
		tx1.setText(name);
        bt1 = (ImageView) findViewById(R.id.bt1);
        sk1 = (SeekBar) findViewById(R.id.sk1);
        
		picker = (ColorPicker) findViewById(R.id.picker);
		//svBar = (SVBar) findViewById(R.id.svbar);
		//OpacityBar opacityBar = (OpacityBar) findViewById(R.id.opacitybar);
		//saturationBar = (SaturationBar) findViewById(R.id.saturationbar);
		//valueBar = (ValueBar) findViewById(R.id.valuebar);

		//picker.addSVBar(svBar);
		//picker.addOpacityBar(opacityBar);
		//picker.addSaturationBar(saturationBar);
		//picker.addValueBar(valueBar);

		//To set the old selected color u can do it like this
		//picker.setOldCenterColor(picker.getColor());
		// adds listener to the colorpicker which is implemented
		//in the activity
		
		//to turn of showing the old color
		picker.setShowOldCenterColor(false);

		//adding onChangeListeners to bars
//		opacitybar.setOnOpacityChangeListener(new OnOpacityChangeListener …)
//		valuebar.setOnValueChangeListener(new OnValueChangeListener …)
//		saturationBar.setOnSaturationChangeListener(new OnSaturationChangeListener …)

		
		picker.setOnColorSelectedListener(this);
//		picker.setOnColorChangedListener(new OnColorChangedListener() {
//			
//			@Override
//			public void onColorChanged(int color) {
//				// TODO Auto-generated method stub
//				send(1);
//			}
//		});
		
		
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
				System.out.println(sk1.getProgress());
				send(1);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				//send(1);
			}
		});
	}
	public void send(int i) {
		switch (i) {
		case 0:
			url = "http://wechat.doit.am/cloud_api/publish.php?cmd=publish&device_id="+device_id+"&device_key="+device_key+"&message=0%7C0%7C0"; //关灯
			httpGet();
			bt1.setBackgroundResource(R.drawable.guan);
			break;
		case 1:
			int c = picker.getColor();
			int r = Color.red(c);
			int g = Color.green(c);
			int b = Color.blue(c);
			int l = sk1.getProgress() + 1;
			System.out.println("old | " + r + " | " + g + " | " + b);
			r = r*l/255;
			g = g*l/255;
			b = b*l/255;
			System.out.println("new | " + r + " | " + g + " | " + b);
			url = "http://wechat.doit.am/cloud_api/publish.php?cmd=publish&device_id="+device_id+"&device_key="+device_key+"&message="+r+"%7C"+g+"%7C"+b; 
			httpGet();
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
	@Override
	public void onColorSelected(int color) {
		// TODO Auto-generated method stub
		System.out.println("selected");
		send(1);
	}
	
	public void httpGet() {

		new Thread() {

			/* (non-Javadoc)
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				HttpClient httpCient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);
				try {
					HttpResponse httpResponse = httpCient.execute(httpGet);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						HttpEntity entity = httpResponse.getEntity();
						 String response = EntityUtils.toString(entity,"utf-8");
						 System.out.println(response);
					}
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}.start();
	}
	
}
