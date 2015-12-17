package com.example.carset;

     
 
import org.json.JSONObject;
 
 

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;

 
@SuppressLint("HandlerLeak")
public class LoginActivity extends Activity implements OnClickListener{
	public static final int REQUSET = 1; 
	private Button login_Button,reg_Button,bendi_Button;
	private EditText edit_uid,edit_pass; 
	private String uid,pass;
	public static String open_id;
	  
 
    static public final String SET_UID = "UID";   
    private boolean check_flag;
    private ProgressDialog pd;  
	CheckBox cb;
    
    
	Handler handler = new Handler() {
        public void handleMessage(Message msg) {
       
        	switch(msg.what){
        	case 0:     
        		 pd = ProgressDialog.show(LoginActivity.this, getText(R.string.str_logining), getText(R.string.str_wait)); 
    			break; 
        	case 1:      
        	 
  	        	  SharedPreferences settings = getSharedPreferences(SET_UID, 0);  
  	        	  Editor editor = settings.edit();
  	        	  editor.putBoolean("CHECK", check_flag);
  	        	  editor.putString("OPEN_ID", open_id);
  	        	  editor.putString("UID", uid);
  	        	  editor.putString("PASS", pass); 
  	        	  editor.commit();
  	        	  
          		 pd.dismiss(); 
          		  
          		 Intent intent = new Intent(LoginActivity.this, ListCarActivity.class);
      		     startActivity(intent); 
      		      
//          		 finish();
        	 
	            break; 	
        	case -1:      
        		 Toast.makeText(LoginActivity.this,R.string.str_login_fail,Toast.LENGTH_LONG).show();
            	
        		 pd.dismiss();
	            break; 
        	case -2:      
 
	            break;  
    		default:
    			break;
        	}
        }
	};
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		
		initView();
		  
	}

	private void initView() {
		bendi_Button=(Button) findViewById(R.id.buton2);
		bendi_Button.setOnClickListener(this); 
		
		login_Button=(Button) findViewById(R.id.buton1);
		login_Button.setOnClickListener(this); 
		
		
		reg_Button=(Button) findViewById(R.id.regist);
		reg_Button.setOnClickListener(this);

		
		edit_uid =(EditText) findViewById(R.id.uid);
		edit_pass=(EditText) findViewById(R.id.password);
		 
		cb = (CheckBox)this.findViewById(R.id.auto_save_password); 
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		      @Override
			 public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		    	  check_flag=arg1;
			 }
		  });
		
		
		SharedPreferences settings = getSharedPreferences(SET_UID, 0);  
		String uid = settings.getString("UID", null);  
		String pass = settings.getString("PASS", null); 
		boolean check = settings.getBoolean("CHECK", true); 
		if(uid!=null && pass!=null && check==true){
			edit_uid.setText(uid);
			edit_pass.setText(pass);
		}
		 
		if(check==true){
			check_flag=true;
			cb.setChecked(true);
		}else{
			check_flag=false;
			cb.setChecked(false);
		} 
	}

    @Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
        // TODO Auto-generated method stub  
        super.onActivityResult(requestCode, resultCode, data);   
        if (requestCode == REQUSET && resultCode == RESULT_OK) {  
        	 open_id = data.getStringExtra("OPEN_ID");
        	 uid = data.getStringExtra("UID");
        	 pass = data.getStringExtra("PASS");
 			 edit_uid.setText(uid);
 			 edit_pass.setText(pass);
 			 check_flag=true;
 			 cb.setChecked(true);
 			 
    		 Intent intent = new Intent(LoginActivity.this, ListCarActivity.class);
  		     startActivity(intent); 
        }   
    }  
    
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.regist:
	    	 Intent intent = new Intent(LoginActivity.this, RegActivity.class);
	    	 startActivityForResult(intent, REQUSET);  
//		     finish();
		break;
		
		case R.id.buton1:
			 Login(edit_uid.getText().toString(),edit_pass.getText().toString());
		 	
		break;
		case R.id.buton2:
	    	 Intent intent1 = new Intent(LoginActivity.this, BenDiActivity.class);
		     startActivity(intent1); 
//		     finish();
		break;
		default:
		break;
		}
	}

	
	private void Login(String uid, String pass){
		if(uid.length()==0 || pass.length()==0){
			new AlertDialog.Builder(this) 
    	    .setTitle(R.string.str_tip_name_pass) 
    	    .setNegativeButton(R.string.str_ok, new DialogInterface.OnClickListener() {
    	        public void onClick(DialogInterface dialog, int which) {
    	        	 
    	        }
    	    }).show();
		}else{
			this.uid=uid;
			this.pass=pass;
			new setdata().start();
		}
	}
	
	  private String encode(String src){
			String des=Uri.encode(src);
			return des;
		}
 
	  private class setdata extends Thread{
	    	 public void run() {
	    		 
	    	       Message msg1=new Message();
	    		   msg1.what=0;
	    		   handler.sendMessage(msg1);
	    		    
	    		   String res = null;

	    		   //获取token值
	    	       try {
		  	    	 	 String uri = "http://wechat.doit.am/mobile_app/get_open_id.php?uid="+encode(uid)+
			        			 "&password="+encode(pass);
			        			  
			    	   	 MyHttp myGet = new MyHttp(uri);
			    	     String des =  myGet.httpPost();
			    	     
			    	 	 if(des.length()>0){
			    	 		  JSONObject jsonEvents =  new JSONObject(des);
			    	 		  res= jsonEvents.getString("ret"); 
			    	 		  if(res.equals("1")){
			    	 			 LoginActivity.open_id = jsonEvents.getString("open_id"); 
			    	 		   
//			    	 			 Message msg2=new Message();
//			    	 		     msg2.what=1;
//			    	 		     handler.sendMessage(msg2);
			    	 		     
			    	 		     handler.sendEmptyMessageDelayed(1,2000);
			    	 		    
			    	 		  }else{
					    	       Message msg=new Message();
					    		   msg.what=-1;
					    		   handler.sendMessage(msg);
					    		   
				    	    	   return;
			    	 		  }
			    	     }else{ 
				    	       Message msg=new Message();
				    		   msg.what=-1;
				    		   handler.sendMessage(msg);
				    		   
			    	    	   return;
			    	     }
	    	    	  
	    	          } catch (Exception e) {
	    	           // TODO Auto-generated catch block
		    	           e.printStackTrace();
		    	           
		    	           Message msg=new Message();
			    		   msg.what=-1;
			    		   handler.sendMessage(msg);
			    		   
		    	    	   return;
	    	        }
	    

	        } 
	 
	    }

	  
	
	  private long exitTime = 0;
		@Override  
	    public boolean onKeyDown(int keyCode, KeyEvent event)   
	    {  
	                 if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)  
	                 {  
	                           
	                         if((System.currentTimeMillis()-exitTime) > 2000)   
	                         {  
	                          Toast.makeText(getApplicationContext(), R.string.str_exit,Toast.LENGTH_SHORT).show();                                  
	                          exitTime = System.currentTimeMillis();  
	                         }  
	                         else  
	                         {  
	                             finish();  
	                             System.exit(0);  

	                         }  
	                                   
	                         return true;  
	                 }  
	                 return super.onKeyDown(keyCode, event);  
	    }  
	  
}