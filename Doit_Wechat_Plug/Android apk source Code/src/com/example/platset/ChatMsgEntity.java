
package com.example.platset;

public class ChatMsgEntity {
    private static final String TAG = ChatMsgEntity.class.getSimpleName();

    private String text,desc,url;
 
    private int layoutID, who;
 

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    public String getDesc() {
        return desc;
    }

    public void setDesc(String text) {
        this.desc = text;
    }
    public String getUrl() {
        return url;
    }

    public void setUrl(String text) {
        this.url = text;
    }
    
    public int getLayoutID() {
        return layoutID;
    }

    public void setLayoutID(int layoutID) {
        this.layoutID = layoutID;
    }
    
    public int getWhoID() {
        return who;
    }

    public void setWhoID(int whoId) {
        this.who = whoId;
    }
    

    public ChatMsgEntity() {
    }

    public ChatMsgEntity(String text, String desc,String url,int layoutID,int who) {
        super(); 
        this.text = text;
        this.desc = desc;
        this.url = url;
        this.layoutID = layoutID;
        this.who = who;
    }

}
