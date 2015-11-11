
package com.example.banset;
 
import am.doit.ledmanager.R;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;

import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
 
 
 
public class ChatMsgViewAdapter extends BaseAdapter {
    private static final String TAG = ChatMsgViewAdapter.class.getSimpleName();

    private ArrayList<ChatMsgEntity> coll;

    private Context ctx;

    public ChatMsgViewAdapter(Context context, ArrayList<ChatMsgEntity> coll) {
        ctx = context;
        this.coll = coll;
    }

    public boolean areAllItemsEnabled() {
        return false;
    }

    public boolean isEnabled(int arg0) {
        return false;
    }

    public int getCount() {
        return coll.size();
    }

    public Object getItem(int position) {
        return coll.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public int getItemViewType(int position) {
        return position;
    }  

    public View getView(int position, View convertView, ViewGroup parent) {
       
        ChatMsgEntity entity = coll.get(position);
        int itemLayout = entity.getLayoutID();

        LinearLayout layout = new LinearLayout(ctx);
        LayoutInflater vi = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        vi.inflate(itemLayout, layout, true);
        
        ImageView headImage = (ImageView) layout.findViewById(R.id.row_head);
        headImage.setTag(position);
    	headImage.setImageResource(R.drawable.mytou_defaul);
//    	if(entity.getWhoID() == 1){
//            if(ChatActivity.mytou_flag){
//            	headImage.setImageBitmap(ChatActivity.mytou_image);
//            }else{
//            	headImage.setImageResource(R.drawable.mytou_defaul);
//            }
//    	}else if (entity.getWhoID() == -1){
//            if(ChatActivity.hetou_flag){
//            	headImage.setImageBitmap(ChatActivity.hetou_image);
//            }else{
//            	headImage.setImageResource(R.drawable.mytou_defaul);
//            }
//    	}
        

        
        ImageView tvImage = (ImageView) layout.findViewById(R.id.row_image);
        tvImage.setTag(position);
        
        TextView tvText = (TextView) layout.findViewById(R.id.row_text);
        tvText.setTag(position);
    
//        SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(ctx, entity.getText());   
        tvText.setText(entity.getText());
        
        
//        String str = entity.getText();
//        if(str.equals("PICTURE")){
//        	try {
//        		 //tvImage.setImageBitmap(BitmapFactory.decodeStream(new FileInputStream((entity.getDesc()))));
//			 
//        		 BitmapFactory.Options opts = new BitmapFactory.Options();
//        		 opts.inJustDecodeBounds = true;
//        		 BitmapFactory.decodeFile(entity.getDesc(), opts);
//  
//        		 
//        		 double w = opts.outWidth;
//        		 double h = opts.outHeight; 
//    			 
//    		     if(w<100){
//    		    	 opts.inSampleSize = 1; 
//    		     }else if(w<400){
//    		    	 opts.inSampleSize = 2;
//    		     }else if(w<800){
//    		    	 opts.inSampleSize = 4;
//    		     }else if(w<1600){
//    		    	 opts.inSampleSize = 8;
//    		     }else if(w<3200){
//    		    	 opts.inSampleSize = 16;
//    		     }else if(w<6400){
//    		    	 opts.inSampleSize = 32;
//    		     }else if(w<12800){
//    		    	 opts.inSampleSize = 64;
//    		     }else if(w<25600){
//    		    	 opts.inSampleSize = 128;
//    		     }else{
////    		    	 opts.inSampleSize = (ComputeSize.computeSampleSize(opts, -1, 128*128))/5; 
//    		     }
//    		     //opts.inSampleSize = ComputeSize.computeSampleSize(opts, -1, 128*128); 
//    		     opts.inJustDecodeBounds = false;
//          		 opts.inPreferredConfig = Bitmap.Config.RGB_565; 
//    		     Bitmap bmp =  BitmapFactory.decodeFile(entity.getDesc(), opts);
//    		     if(bmp!=null)
//    		    	 tvImage.setImageBitmap(bmp);
//    		     
//    		     
//        	} catch (Exception e) {
//				e.printStackTrace();
//			}  
//        }else if(str.equals("AUDIO")){ 
////        	if(entity.getWhoID() == 1){
////             	tvImage.setImageResource(R.drawable.audio_me_icon);
////        	}else if (entity.getWhoID() == -1){
////             	tvImage.setImageResource(R.drawable.audio_he_icon);
////        	} 
//        }else if(str.equals("MARKET")){
//        	
//        	tvText.setText(entity.getDesc());
//        }else if(str.equals("BROWSER")){
//        	
//        	tvText.setText(entity.getDesc());
//        }else{
////        	SpannableString spannableString = FaceConversionUtil.getInstace().getExpressionString(ctx, entity.getText());   
////            tvText.setText(spannableString);
//            //tvText.setText(str);
//        }
        
        tvImage.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) { 
//				   int index = Integer.parseInt(v.getTag().toString());
//			       ChatMsgEntity entity = coll.get(index);
//			       String str = entity.getText();
//			       
//			        if(str.equals("PICTURE")){
//			        	 
//						Intent intent = new Intent(ctx, PicActivity.class);
//						intent.putExtra("str", entity.getDesc());
//						ctx.startActivity(intent); 
//			        }else if(str.equals("AUDIO")){
//			        	
//			        	if(ChatActivity.mPlayer!=null){
//			        		ChatActivity.mPlayer.release();
//			        		ChatActivity.mPlayer = null;
//			               
//			        	}
//				        ChatActivity.mPlayer = new MediaPlayer();
//				        try {
//				        	ChatActivity.mPlayer.setDataSource(entity.getDesc());
//				        	ChatActivity.mPlayer.prepare();
//				        	ChatActivity.mPlayer.start();
//				        } catch (IOException e) {
//				            
//				        }
//			        }else if(str.equals("EMOICONS")){
//			        	 
//			        }else if(str.equals("MARKET")){
//			        	 
//			        }else if(str.equals("BROWSER")){
//			        	 
//			        }else{
//			          
//			        }
			}
		});

        tvText.setOnClickListener(new View.OnClickListener() { 
			public void onClick(View v) { 
//				   int index = Integer.parseInt(v.getTag().toString());
//			       ChatMsgEntity entity = coll.get(index);
//			       String str = entity.getText();
//			        
//			       
//			        if(str.equals("PICTURE")){
//			         
//			        }else if(str.equals("AUDIO")){
//			         
//			        }else if(str.equals("EMOICONS")){
//			        	 
//			        }else if(str.equals("MARKET")){
//	    	            try{
//	           				Uri u = Uri.parse(entity.getUrl()); 
//	           				Intent it = new Intent(Intent.ACTION_VIEW, u); 
//	           				it.setClassName("com.android.vending", "com.android.vending.AssetBrowserActivity");
//	           				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//	           				ctx.startActivity(it); 
//	    	                 
//	    	            }catch(Exception e){ 
//	     		    	        
//	    	            }
//			        }else if(str.equals("BROWSER")){
//	    	            try{
//	           				Uri u = Uri.parse(entity.getUrl()); 
//	           				Intent it = new Intent(Intent.ACTION_VIEW, u);  
//	           				ctx.startActivity(it); 
//	    	                 
//	    	            }catch(Exception e){ 
//	     		    	        
//	    	            }
//			        }else{
//			          
//			        }
			}
		});
        
        return layout;
    }

    public int getViewTypeCount() {
        return coll.size();
    }

    public boolean hasStableIds() {
        return false;
    }

    public boolean isEmpty() {
        return false;
    }

    public void registerDataSetObserver(DataSetObserver observer) {
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
    }
    

  
}
