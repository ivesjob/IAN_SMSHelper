package com.ian.sms.view;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ian.sms.db.DatabaseHelper;
import com.ian.sms.mode.AutoReplyItem;
import com.ian.sms.mode.ContactsItem;
import com.ian.sms.tool.ListTool;
import com.ian.sms.tool.SmsTool;
import com.ian.sms.tool.StringTool;
import com.ian.sms.view.FSendActivity.ReceiveUsersAdapter;
import com.ian.sms.view.FSendActivity.ReceiveUsersViewHolder;

public class AddReplyItemActivity extends BaseActivity{
	ListView spechalList;//
	ListView lv_contacts;
	RelativeLayout layout_spechalset;
	private ArrayList<ContactsItem> persons;
	ReceiveUsersAdapter receiveUsersAdapter;
	
	CheckBox cb_isIncludeStranger;
	CheckBox cb_isAllContacts;
	CheckBox cb_isNoAnswerCall;
	CheckBox cb_isAcceptSMS;
	
	ImageButton ibtn_back;
	ImageButton ibtn_save;
	ImageButton ibtn_insert;
	TextView tv_body;
	AutoReplyItem item;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_add_reply);
		init();
		
		Object object = getIntent().getSerializableExtra("item");
		if(object==null){
			item = new AutoReplyItem();
			item.setIncludeStranger(true);
			item.setAllContacts(true);
			item.setNoAnswerCall(true);
			item.setAcceptSMS(true);
		}else{
			this.item = (AutoReplyItem) object;
			ArrayList<String> result = StringTool.getListToString(item.getContacts(), ",");
			for (int i = 0; i < result.size(); i++) {
				ContactsItem item = new ContactsItem();
				item.setName(SmsTool.getNameFromPhone(result.get(i), this));
				item.setPhone(result.get(i));
				persons.add(item);
			}
		}
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		cb_isIncludeStranger.setChecked(item.isIncludeStranger());
		cb_isAllContacts.setChecked(item.isAllContacts());
		cb_isNoAnswerCall.setChecked(item.isNoAnswerCall());
		cb_isAcceptSMS.setChecked(item.isAcceptSMS());
		
		tv_body.setText(item.getBody());
		if(!item.isAllContacts()){
			layout_spechalset.setVisibility(View.VISIBLE);
		}
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
	    switch (resultCode) {
		case ContactChoiceActivity.OPEN_CONTACTCHOICEACTIVITY_RESULTCODE:
			ArrayList<ContactsItem> persons = (ArrayList<ContactsItem>) data.getExtras().getSerializable("persons");
			if(persons==null || persons.size()<1)return;
			cb_isAllContacts.setChecked(false);
			
			this.persons.addAll(persons);
			ListTool.removeDuplicate(this.persons);
			receiveUsersAdapter.notifyDataSetChanged();
			break;

		default:
			break;
		}
	}
	
	private void init(){
		layout_spechalset = (RelativeLayout) findViewById(R.id.layout_spechalset);
		spechalList = (ListView) findViewById(R.id.spechalList);
		lv_contacts = (ListView) findViewById(R.id.lv_contacts);
		ibtn_back = (ImageButton) findViewById(R.id.btn_back);
		ibtn_save = (ImageButton) findViewById(R.id.savebutton);
		ibtn_insert= (ImageButton) findViewById(R.id.btn_input_shrase);
		tv_body = (TextView) findViewById(R.id.et_sendmessage);
		
		cb_isIncludeStranger = (CheckBox) findViewById(R.id.cb_isIncludeStranger);
		cb_isAllContacts = (CheckBox) findViewById(R.id.cb_isAllContacts);
		cb_isNoAnswerCall = (CheckBox) findViewById(R.id.cb_isNoAnswerCall);
		cb_isAcceptSMS = (CheckBox) findViewById(R.id.cb_isAcceptSMS);
		ibtn_save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				save();
			}
		});
		ibtn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		OnCheckedChangeListener onCheckedChangeListener=new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				switch (buttonView.getId()) {
				case R.id.cb_isIncludeStranger:
					item.setIncludeStranger(isChecked);
					break;
				case R.id.cb_isAllContacts:
					item.setAllContacts(isChecked);
					if(isChecked){
						layout_spechalset.setVisibility(View.GONE);
					}else{
						layout_spechalset.setVisibility(View.VISIBLE);	
					}
					
					break;
				case R.id.cb_isNoAnswerCall:
					item.setNoAnswerCall(isChecked);
					break;
				case R.id.cb_isAcceptSMS:
					item.setAcceptSMS(isChecked);
					break;
				default:
					break;
				}
			}
		};
		cb_isIncludeStranger.setOnCheckedChangeListener(onCheckedChangeListener);
		cb_isAllContacts.setOnCheckedChangeListener(onCheckedChangeListener);
		cb_isNoAnswerCall.setOnCheckedChangeListener(onCheckedChangeListener);
		cb_isAcceptSMS.setOnCheckedChangeListener(onCheckedChangeListener);
		
		String[] data = new String[]{"自己勾选联系人(默认全部)"};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,data);
		spechalList.setAdapter(adapter);
		spechalList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					startActivityForResult(new Intent(AddReplyItemActivity.this,ContactChoiceActivity.class), ContactChoiceActivity.OPEN_CONTACTCHOICEACTIVITY_RESULTCODE);
					break;

				default:
					break;
				}
			}
		});
		persons = new ArrayList<ContactsItem>();
		receiveUsersAdapter = new ReceiveUsersAdapter(this);
		lv_contacts.setAdapter(receiveUsersAdapter);
	}
	
	private void save(){
		if(TextUtils.isEmpty(tv_body.getText())){
			return;
		}
		if(!item.isAllContacts()){//如果不包含全部联系人，就相当于自定义接收的人
			StringBuilder contacts = new StringBuilder();
			for (int i = 0; i < persons.size(); i++) {
				contacts.append(persons.get(i).getPhone()+",");
			}
			item.setContacts(contacts.toString());	
		}else{
			item.setContacts("");	
		}
		
		
		String body = tv_body.getText().toString().trim();
		item.setBody(body);
		if(item.getId()==0){
			saveAutoReplyItemToDB(item);
		}else{
			updateAutoReplyItemToDB(item);
		}
		
		
	}
	private void updateAutoReplyItemToDB(AutoReplyItem item){
		ContentValues values = new ContentValues();
		values.put(AutoReplyItem.BODY, item.getBody());
		values.put(AutoReplyItem.CONTACTS, item.getContacts());
		values.put(AutoReplyItem.DAY_STARTTIME, item.getDay_startTime());
		values.put(AutoReplyItem.DAY_STOPTIME, item.getDay_stopTime());
		values.put(AutoReplyItem.ISACCEPTSMS, item.isAcceptSMS());
		values.put(AutoReplyItem.ISALLCONTACTS, item.isAllContacts());
		values.put(AutoReplyItem.ISCUSTOMTIME, item.isCustomTime());
		values.put(AutoReplyItem.ISHANGUPCALL, item.isHangUpCall());
		values.put(AutoReplyItem.ISINCLUDESTRANGER, item.isIncludeStranger());
		values.put(AutoReplyItem.ISNOANSWERCALL, item.isNoAnswerCall());
		values.put(AutoReplyItem.ISONLYREPLY, item.isOnlyReply());
		values.put(AutoReplyItem.ISSTART, item.isStart());
		
		long result = DatabaseHelper.update(this, DatabaseHelper.TABLE_NAME_REPLY, values, AutoReplyItem.ID+"="+item.getId(), null);
		if(result>0){
			Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(this, "修改失败", Toast.LENGTH_SHORT).show();
		}
	}
	private void saveAutoReplyItemToDB(AutoReplyItem item){
		ContentValues values = new ContentValues();
		values.put(AutoReplyItem.BODY, item.getBody());
		values.put(AutoReplyItem.CONTACTS, item.getContacts());
		values.put(AutoReplyItem.DAY_STARTTIME, item.getDay_startTime());
		values.put(AutoReplyItem.DAY_STOPTIME, item.getDay_stopTime());
		values.put(AutoReplyItem.ISACCEPTSMS, item.isAcceptSMS());
		values.put(AutoReplyItem.ISALLCONTACTS, item.isAllContacts());
		values.put(AutoReplyItem.ISCUSTOMTIME, item.isCustomTime());
		values.put(AutoReplyItem.ISHANGUPCALL, item.isHangUpCall());
		values.put(AutoReplyItem.ISINCLUDESTRANGER, item.isIncludeStranger());
		values.put(AutoReplyItem.ISNOANSWERCALL, item.isNoAnswerCall());
		values.put(AutoReplyItem.ISONLYREPLY, item.isOnlyReply());
		values.put(AutoReplyItem.ISSTART, item.isStart());
		
		long result = DatabaseHelper.insert(this, DatabaseHelper.TABLE_NAME_REPLY, values);
		if(result>0){
			Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show();
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
					receiveUsersAdapter.notifyDataSetChanged();
				}
			});
			return convertView;
		}
	}
}
