package com.ian.sms.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ian.sms.mode.MessageItem;
import com.ian.sms.mode.SMS;
import com.ian.sms.tool.DateTool;
import com.ian.sms.tool.ILog;
import com.ian.sms.tool.SmsTool;
import com.ian.sms.tool.Threads;

/**
 * 对话界面
 * @author ives
 * @date 2013-2-18下午5:42:32
 * @version 1.0
 * @comment 对话界面实现
 */
public class TalkActivity extends BaseActivity implements OnClickListener{
	private ImageButton mBtnSend;
	private ImageButton mBtnBack;
	private EditText edit_body;
	private ListView mListView;
	private TextView tv_title;
	private ChatMsgViewAdapter mAdapter;
	private List<MessageItem> mDataArrays ;
	public static  String thread_id;
	private  Set<String> phones;//记录当前的address
	UpDateUIReceiver upDateUIReceiver;
	public static final String RECEIVE_TalkMessageItemAddReceiver_ACTION = "com.ian.sms.receive.updateuireceiver.action";
	Handler handler;
	
	MessageItem draft;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);
        thread_id = getIntent().getStringExtra("threadid");
        
        initView();
        initData();
        handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
			}
        };
        handler.obtainMessage();
        draft = SmsTool.getDraftByThreadID(this, thread_id);
        if(draft!=null){
        	edit_body.setText(draft.getBody());
        }
    }
    

    
    @Override
	protected void onResume() {
		super.onResume();
		//刷新
		registerTalkMessageItemAddReceiver();
	}


	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(upDateUIReceiver);
	}


	public void initView(){
    	mListView = (ListView) findViewById(R.id.listview);
    	mBtnSend = (ImageButton) findViewById(R.id.btn_send);
    	mBtnSend.setOnClickListener(this);
    	mBtnBack = (ImageButton) findViewById(R.id.btn_back);
    	mBtnBack.setOnClickListener(this);
    	tv_title = (TextView) findViewById(R.id.tv_title);
    	
    	edit_body = (EditText) findViewById(R.id.et_sendmessage);
    }
    public void initData(){
    	
    	//取得所有对话
    	mDataArrays = SmsTool.getSmsDataListByThreadId(this, thread_id);
    	//将所有对话里的未读消息改成已读消息
    	ContentValues values = new ContentValues();
    	values.put("read", 1);
    	SmsTool.update_sys_group(TalkActivity.this, thread_id, values);
    	//
    	if(mDataArrays==null){
    		ILog.e(this, "mDataArrays is null!!!");
    		return;
    	}
    	mAdapter = new ChatMsgViewAdapter(this, mDataArrays);
		mListView.setAdapter(mAdapter);
		mListView.setSelection(mListView.getAdapter().getCount()-1);
		
		phones = new HashSet<String>();
		for (int i = 0; i < mDataArrays.size(); i++) {
			phones.add(mDataArrays.get(i).getPhone());
		}
		phones.remove(null);
		//设置标题
    	StringBuilder title =new StringBuilder();
    	for (String temp:phones) {
    		String name =SmsTool.getNameFromPhone(temp, TalkActivity.this);
    		if(name==null){
    			title.append(temp+",");
    		}else{
    			title.append(name+",");
    		}
		}
    	if(title.toString().endsWith(",")){
    		tv_title.setText(title.toString().substring(0,title.length()-1));
    	}else{
    		tv_title.setText(title.toString());
    	}
    	
    }


	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.btn_send:
			sendSMS();
			break;
		case R.id.btn_back:
			draftCheck();
			finish();
			break;
		}
	}
	/**
	 * 发送消息
	 * @date Feb 27, 20136:06:02 PM
	 * @comment
	 */
	private void sendSMS(){
		if (TextUtils.isEmpty(edit_body.getText())) {
            return;
        }
		String body = edit_body.getText().toString();
		try{
			for (String phone:phones) {
				if(phone!=null){
					SmsTool.sendMessage(this, phone, body,thread_id,mAdapter.getCount());
					addTempSendItem(body,phone);	
				}
			}	
		}catch(Exception e){
			e.printStackTrace();
			Toast.makeText(this, "错误！请检查收件人地址", Toast.LENGTH_SHORT).show();
		}
		
		
		edit_body.setText("");
	}
	/**
	 * 添加临时的发送消息
	 * @date 2013-2-28下午11:09:22
	 * @comment 
	 * @param body
	 */
	private void addTempSendItem(String body,String address){
		MessageItem info = new MessageItem();
		info.setType(2);
		info.setDate(System.currentTimeMillis());
		info.setPhone(address);
		info.setBody(body);
		info.setRead(2);
		info.setTemp(true);
		
		mDataArrays.add(info);
		mAdapter.notifyDataSetChanged();
		mListView.setSelection(mListView.getAdapter().getCount()-1);
	}
	/**
	 * 草稿检查
	 * @date 2013-3-9下午5:24:38
	 * @comment
	 */
	private void draftCheck(){
		//如果edit_body内容为空，清空草稿
		if (TextUtils.isEmpty(edit_body.getText())) {
			if(draft!=null){
				SmsTool.delete_sms_item(this, draft.getId());
			}
		}else{//保存草稿
			if(draft==null){
				draft = new MessageItem();
				draft.setBody(edit_body.getText().toString().trim());
				draft.setDate(System.currentTimeMillis());
				draft.setThread_id(thread_id);
				draft.setType(SMS.MESSAGE_TYPE_DRAFT);
				
				ContentValues values  = new ContentValues();
				values.put(SMS.BODY, draft.getBody());
				values.put(SMS.DATE, System.currentTimeMillis());
				values.put(SMS.THREAD_ID,thread_id );
				values.put(SMS.TYPE, SMS.MESSAGE_TYPE_DRAFT);
				values.put(SMS.READ,SMS.SMS_READ_YES );
				
				SmsTool.insert_sms_item(this, values);
			}
			draft.setBody(edit_body.getText().toString().trim());
			draft.setDate(System.currentTimeMillis());
			draft.setThread_id(thread_id);
			draft.setType(SMS.MESSAGE_TYPE_DRAFT);
			
			//进行保存
			ContentValues values  = new ContentValues();
			values.put(SMS.BODY, draft.getBody());
			values.put(SMS.DATE, System.currentTimeMillis());
			values.put(SMS.READ,SMS.SMS_READ_YES );
			SmsTool.update_sms_item(this, draft.getId(), values);
		}
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK){
			draftCheck();
		}
		return super.onKeyUp(keyCode, event);
	}



	/**
	 * 注册广播
	 * @date 2013-3-1下午3:25:19
	 * @comment
	 */
	private void registerTalkMessageItemAddReceiver(){
		upDateUIReceiver = new UpDateUIReceiver();
		registerReceiver(upDateUIReceiver, new IntentFilter(RECEIVE_TalkMessageItemAddReceiver_ACTION));
	}
	/**
	 * 
	 * @author ives
	 * @date 2013-3-1下午4:40:38
	 * @version 1.0
	 * @comment
	 */
	class UpDateUIReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getStringExtra("type").equals("in")){//有消息进入
				Bundle bundle= intent.getExtras();
				final MessageItem item = (MessageItem) bundle.getSerializable("messageItem");
				handler.post(new Runnable() {
					@Override
					public void run() {
						//将此消息改成已读
						//将消息在数据库中的状态改为已读
						ContentValues values = new ContentValues();
						values.put("read", 1);
						SmsTool.update_sms_item(TalkActivity.this, item.getId(), values);
						//添加到界面
						mDataArrays.add(item);
						mAdapter.notifyDataSetChanged();
						mListView.setSelection(mListView.getAdapter().getCount()-1);
						
					}
				});	
			}else if(intent.getStringExtra("type").equals("out")){//有消息发出
				final int viewIndex = intent.getIntExtra("viewIndex", 0);
				if(viewIndex==0)return;
				handler.post(new Runnable() {
					@Override
					public void run() {
						MessageItem item = (MessageItem) mAdapter.getItem(viewIndex);
						item.setTemp(false);
						mAdapter.notifyDataSetChanged();
						mListView.setSelection(mListView.getAdapter().getCount()-1);
						
					}
				});
				
			}
			
		} 
	}
	/**
	 * adapter
	 * @author ives
	 * @date 2013-3-1下午4:06:50
	 * @version 1.0
	 * @comment
	 */
	public class ChatMsgViewAdapter extends BaseAdapter {
		private List<MessageItem> coll;// 所有的消息
		private LayoutInflater mInflater;// xml装载器

		public ChatMsgViewAdapter(Context context, List<MessageItem> coll) {
			this.coll = coll;
			mInflater = LayoutInflater.from(context);
			
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
			MessageItem entity = coll.get(position);
			return entity.getType();

		}

		public int getViewTypeCount() {
			if(coll==null)return 0;
			return coll.size();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MessageItem entity = coll.get(position);
			ViewHolder viewHolder = null;
			if (entity.getType()==SMS.SMS_TYPE_IN) {//接受的消息
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.chatting_item_msg_text_left, null);
					viewHolder = new ViewHolder();
					viewHolder.tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime_l);
					viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_chatcontent_l);
					convertView.setTag(R.id.tag_first,viewHolder);	
				}else{
					viewHolder = (ViewHolder) convertView.getTag(R.id.tag_first);
				}
			} else {//发送的消息
				if (convertView == null) {
					convertView = mInflater.inflate(R.layout.chatting_item_msg_text_right, null);
					viewHolder = new ViewHolder();
					viewHolder.tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime_r);
					viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_chatcontent_r);
					viewHolder.progressBar = (ProgressBar) convertView.findViewById(R.id.sendProgress);
					viewHolder.error = convertView.findViewById(R.id.ibtn_error);
					if(entity.isTemp()){
						viewHolder.progressBar.setVisibility(View.VISIBLE);
					}else{
						viewHolder.progressBar.setVisibility(View.GONE);
					}
					if(entity.getType()==SMS.MESSAGE_TYPE_FAILED){
						viewHolder.error.setVisibility(View.VISIBLE);
					}else{
						viewHolder.error.setVisibility(View.GONE);
					}
					convertView.setTag(R.id.tag_second,viewHolder);	
				}else{
					viewHolder = (ViewHolder) convertView.getTag(R.id.tag_second);
					if(viewHolder.progressBar!=null){
						if(entity.isTemp()){
							viewHolder.progressBar.setVisibility(View.VISIBLE);
						}else{
							viewHolder.progressBar.setVisibility(View.GONE);
						}
					}
					if(viewHolder.error!=null){
						if(entity.getType()==SMS.MESSAGE_TYPE_FAILED){
							viewHolder.error.setVisibility(View.VISIBLE);
						}else{
							viewHolder.error.setVisibility(View.GONE);
						}
					}
				}
			}
			viewHolder.tvSendTime.setText(DateTool.formatTimeStampString(convertView.getContext(), entity.getDate(), true));
			viewHolder.tvContent.setText(entity.getBody());

			return convertView;
		}
		
		 class ViewHolder {
			public TextView tvSendTime;
			public TextView tvContent;
			public View error;
			public ProgressBar progressBar;
		}
	}
}
