package com.ian.sms.view;

import java.util.ArrayList;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ian.sms.mode.ContactsItem;
import com.ian.sms.tool.ListTool;
import com.ian.sms.tool.SmsTool;
import com.ian.sms.tool.StringTool;

/**
 * 消息群发界面
 * 
 * @author ives
 * @date Feb 18, 20139:47:13 PM
 * @version 1.0
 * @comment
 */
public class FSendActivity extends BaseActivity {
	ImageButton ibtn_savetoDraft;
	ImageButton ibtn_back;
	ImageButton ibtn_contexts_add;
	ImageButton ibtn_send;
	ImageButton ibtn_insert;
	EditText edit_body;
	EditText edit_person;
	private ListView receiveUsers;
	private ArrayList<ContactsItem> persons;
	ReceiveUsersAdapter adapter;
	ProgressBar progressBar_send;
	UpDateUIReceiver upDateUIReceiver;
	public static final String RECEIVE_FSEND_ACTION = "com.ian.sms.receive.fsend.action";
	Handler handler;
	int person_count = 0;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fsend);
		persons =  new ArrayList<ContactsItem>();
		initView();
		//检查是否需要隐藏back按钮
		if(getIntent().getBooleanExtra("hideback", false)){
			ibtn_back.setVisibility(View.VISIBLE);
		}else{
			ibtn_back.setVisibility(View.GONE);
		}
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
			}
			
		};
		handler.obtainMessage();
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		progressBar_send.setVisibility(View.GONE);
		upDateUIReceiver = new UpDateUIReceiver();
		registerReceiver(upDateUIReceiver, new IntentFilter(RECEIVE_FSEND_ACTION));
	}


	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(upDateUIReceiver);
	}


	private void initView(){
		receiveUsers = (ListView) findViewById(R.id.receiveusers);
		adapter = new ReceiveUsersAdapter(this);
		receiveUsers.setAdapter(adapter);
		receiveUsers.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				Toast.makeText(FSendActivity.this, "你点击了:"+position, Toast.LENGTH_SHORT).show();
			}
		});
		ibtn_savetoDraft = (ImageButton) findViewById(R.id.ibtn_savetoDraft);
		ibtn_back = (ImageButton) findViewById(R.id.ibtn_back);
		ibtn_contexts_add = (ImageButton) findViewById(R.id.btn_contexts_add);
		ibtn_send = (ImageButton) findViewById(R.id.sendbutton);
		ibtn_insert = (ImageButton) findViewById(R.id.insertdiy);
		edit_body = (EditText) findViewById(R.id.edit_body);
		edit_person = (EditText) findViewById(R.id.edit_contexts_add);
		progressBar_send = (ProgressBar) findViewById(R.id.group_send_progress);
		
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.ibtn_savetoDraft:
					//将内容保存为草稿
					saveDraft();
					break;
				case R.id.ibtn_back:
					//返回
					finish();
					break;
				case R.id.btn_contexts_add:
					startActivityForResult(new Intent(FSendActivity.this,ContactChoiceActivity.class), ContactChoiceActivity.OPEN_CONTACTCHOICEACTIVITY_RESULTCODE);
					break;
				case R.id.sendbutton:
					//发送短信
					sendSMS();
					break;
				case R.id.insertdiy:
					//插入内容
					insert();
					break;

				default:
					break;
				}
			}
		};
		 ibtn_savetoDraft.setOnClickListener(clickListener);
		 ibtn_back.setOnClickListener(clickListener);
		 ibtn_contexts_add.setOnClickListener(clickListener);
		 ibtn_send.setOnClickListener(clickListener);
		 ibtn_insert.setOnClickListener(clickListener);
	}
	private void saveDraft(){

	}
	private void sendSMS(){
		if (TextUtils.isEmpty(edit_body.getText())) {//检查内容是否为空
            return;
        }
		//检查收信人是否为空
		if(persons.size()<1 && TextUtils.isEmpty(edit_person.getText())){
			return;
		}
		//检查收信人文本格式
		//填入的号码
		StringBuilder phones_str = new StringBuilder(edit_person.getText().toString().trim());
		//将所有接收人收集起来
		String body = edit_body.getText().toString();
		for (int i = 0; i < persons.size(); i++) {
			phones_str.append(","+persons.get(i).getPhone());
		}
		Set<String>address = StringTool.getSetToString(phones_str.toString(), ",");
		try{
			//发送
			SmsTool.sendGroupMessage(FSendActivity.this, address,body);
			Toast.makeText(this, "发送完毕", Toast.LENGTH_SHORT).show();
		}catch(Exception e){
			e.printStackTrace();
			Toast.makeText(this, "错误！请检查收件人地址", Toast.LENGTH_SHORT).show();
			return;
		}
		//刷新界面
		edit_body.setText("");
		edit_person.setText("");
		person_count = address.size();
		progressBar_send.setMax(person_count);
		progressBar_send.setSecondaryProgress(person_count);
		progressBar_send.setProgress(0);
//progressBar_send.setVisibility(View.VISIBLE);
		progressBar_send.setVisibility(View.GONE);
		persons.clear();
		adapter.notifyDataSetChanged();
	}
	private void insert(){
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
	    switch (resultCode) {
		case ContactChoiceActivity.OPEN_CONTACTCHOICEACTIVITY_RESULTCODE:
			ArrayList<ContactsItem> persons = (ArrayList<ContactsItem>) data.getExtras().getSerializable("persons");
			if(persons==null || persons.size()<1)return;
			this.persons.addAll(persons);
			ListTool.removeDuplicate(this.persons);
			adapter.notifyDataSetChanged();
			break;

		default:
			break;
		}
	}


	public final class ReceiveUsersViewHolder {
		public TextView info;
		public ImageButton btn_cancel;
	}

	class ReceiveUsersAdapter extends BaseAdapter {
		LayoutInflater inflater;

		public ReceiveUsersAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return persons.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// 得到自定义的单项数据结构对象
			ReceiveUsersViewHolder holder = null;
			if (convertView == null) {
				holder = new ReceiveUsersViewHolder();
				convertView = inflater.inflate(R.layout.view_receiveuser_item, null);
				holder.info = (TextView) convertView.findViewById(R.id.usermessage);
				holder.btn_cancel = (ImageButton) convertView.findViewById(R.id.delete_button);
				convertView.setTag(holder);
			} else {
				holder = (ReceiveUsersViewHolder) convertView.getTag();
			}
			if(persons.get(position).getName()==null || persons.get(position).getName().equals("")){
				holder.info.setText(persons.get(position).getPhone());
			}else{
				holder.info.setText(persons.get(position).getName()+" ("+persons.get(position).getPhone()+")");	
			}
			
			final int index = position;
			holder.btn_cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					persons.remove(index);
					adapter.notifyDataSetChanged();
				}
			});
			return convertView;
		}
	}
	class UpDateUIReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(RECEIVE_FSEND_ACTION)){//确认接收到的是正确的消息
				//取得数据
				String result = intent.getStringExtra("result");
				String address = intent.getStringExtra("address");
				handler.post(new Runnable() {
					@Override
					public void run() {
						//更新UI
						if(progressBar_send.getMax()<1)return;
						int now_progress = progressBar_send.getProgress();
System.out.println("now:"+now_progress);
						progressBar_send.setProgress(++now_progress);
						//检查是否完毕
						if(now_progress==person_count){
System.out.println("ok:"+now_progress   +"    count:"+person_count);
							Toast.makeText(FSendActivity.this, "已经发送完毕"+now_progress, Toast.LENGTH_SHORT).show();
							progressBar_send.setVisibility(View.GONE);
							
						}						
					}
				});
			}
		} 
	}
}
