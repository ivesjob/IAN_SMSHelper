package com.ian.sms.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ian.sms.diyview.AlphaSeachBar;
import com.ian.sms.mode.ContactsItem;

/**
 * 字母滑动条操作联系人
 * @author ives
 * @date Feb 23, 20133:12:10 PM
 * @version 1.0
 * @comment
 */
public class ContactChoiceActivity extends Activity {
	private ContactListAdapter adapter;//自定义adapter
	private ListView personList;//联系人显示控件
	private List<ContentValues> persons;//联系人集合
	private AsyncQueryHandler asyncQuery;//数据库异步查询类
	private AlphaSeachBar alphabar;//字母滑动条
	public static final String NAME = "name", NUMBER = "number",SORT_KEY = "sort_key";
	public static final int OPEN_CONTACTCHOICEACTIVITY_RESULTCODE = 1;
	ImageButton ibtn_back;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_contact_list);
		personList = (ListView) findViewById(R.id.listViews);
		alphabar = (AlphaSeachBar) findViewById(R.id.fast_scroller);
		ibtn_back = (ImageButton) findViewById(R.id.backbutton);
		//实例化数据库操作类
		asyncQuery = new MyAsyncQueryHandler(getContentResolver());
		ibtn_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(adapter==null){
					finish();
					return;
				}
				ArrayList<ContactsItem> ps = adapter.getChoicePersons();
				if(ps==null || ps.size()<1)finish();
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putSerializable("persons", ps);
				intent.putExtras(bundle);
				setResult(OPEN_CONTACTCHOICEACTIVITY_RESULTCODE, intent);
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		Uri uri = Uri.parse("content://com.android.contacts/data/phones"); // 联系人的Uri
		String[] projection = { "_id", "display_name", "data1", "sort_key" }; // 查询的列
		asyncQuery.startQuery(0, null, uri, projection, null, null,"sort_key COLLATE LOCALIZED asc"); // 按照sort_key升序查询
	}
	/**
	 * 数据库异步查询类AsyncQueryHandler
	 * @author ives
	 * @date Feb 23, 20133:23:32 PM
	 * @version 1.0
	 * @comment
	 */
	private class MyAsyncQueryHandler extends AsyncQueryHandler {
		public MyAsyncQueryHandler(ContentResolver cr) {
			super(cr);

		}
		/**
		 * 查询结束的回调函数
		 */
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			if (cursor != null && cursor.getCount() > 0) {
				persons = new ArrayList<ContentValues>();
				cursor.moveToFirst();//将结果光标移动到第一个
				for (int i = 0; i < cursor.getCount(); i++) {
					ContentValues cv = new ContentValues();
					cursor.moveToPosition(i);
					String name = cursor.getString(1);
					String number = cursor.getString(2);
					String sortKey = cursor.getString(3);

					if (number.startsWith("+86")) {// 去除多余的中国地区号码标志，对这个程序没有影响。
						cv.put(NAME, name);
						cv.put(NUMBER, number.substring(3));
						cv.put(SORT_KEY, sortKey);
					} else {
						cv.put(NAME, name);
						cv.put(NUMBER, number);
						cv.put(SORT_KEY, sortKey);
					}
					persons.add(cv);
				}
				if (persons.size() > 0) {
					setAdapter(persons);
				}
				if(cursor!=null)
					if(!cursor.isClosed())cursor.close();
			}
		}

	}
	/**
	 * 设置adaapter
	 * @date Feb 23, 20133:25:43 PM
	 * @comment 
	 * @param list
	 */
	private void setAdapter(List<ContentValues> list) {
		//new联系人adapter
		adapter = new ContactListAdapter(this);
		personList.setAdapter(adapter);
		//设置字母滑动条的属性
		alphabar.init(ContactChoiceActivity.this);
		alphabar.setListView(personList);
		alphabar.setHight(alphabar.getHeight());
		alphabar.setVisibility(View.VISIBLE);
	}

	private static class ViewHolder {
		TextView alpha;
		TextView name;
		TextView number;
		CheckBox check;
	}

	
	/**
	 * 自定义的联系人adapter
	 * @author ives
	 * @date Feb 23, 20133:58:47 PM
	 * @version 1.0
	 * @comment
	 */
	private class ContactListAdapter extends BaseAdapter{
		private LayoutInflater inflater;
		private HashMap<String, Integer> alphaIndexer;//字母在list控件中的位置索引
		private String[] sections;//所有出现做list控件中的首字母
		HashMap<Integer, Boolean> selecteds;//被选中的checkbox
		

		public ContactListAdapter(Context context) {
			this.inflater = LayoutInflater.from(context);
			this.alphaIndexer = new HashMap<String, Integer>();
			selecteds = new HashMap<Integer, Boolean>();
			for (int i =0; i <persons.size(); i++) {
				String name = getAlpha(persons.get(i).getAsString(SORT_KEY));
				if(!alphaIndexer.containsKey(name)){//只记录在list中首次出现的位置
					alphaIndexer.put(name, i);
				}
				selecteds.put(i, false);
			}
			/**可省略**/
			Set<String> sectionLetters = alphaIndexer.keySet();
			ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);//所有出现的字母索引
			Collections.sort(sectionList);
			sections = new String[sectionList.size()];
			sectionList.toArray(sections);
			
			alphabar.setAlphaIndexer(alphaIndexer);
			
			
			
		}

		@Override
		public int getCount() {
			return persons.size();
		}

		@Override
		public Object getItem(int position) {
			return persons.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		/**
		 * 取得选中的联系人信息
		 * @date 2013-3-3下午3:35:11
		 * @comment 
		 * @return
		 */
		public ArrayList<ContactsItem> getChoicePersons(){
			ArrayList<ContactsItem> results = new ArrayList<ContactsItem>();
			if(persons==null )return results;
			for (int i = 0; i < persons.size(); i++) {
				if(selecteds.get(i)){
					ContactsItem item = new ContactsItem();
					item.setName(persons.get(i).getAsString(NAME));
					item.setPhone(persons.get(i).getAsString(NUMBER));
					results.add(item);
				}
			}
			return results;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.contact_list_item, null);
				holder = new ViewHolder();
				holder.alpha = (TextView) convertView.findViewById(R.id.alpha);
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.number = (TextView) convertView.findViewById(R.id.number);
				holder.check =  (CheckBox) convertView.findViewById(R.id.choice);
				//添加选中事件
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final int index = position;
			holder.check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					selecteds.put(index, isChecked);
				}
			});
			holder.check.setChecked(selecteds.get(position));
			ContentValues cv = persons.get(position);
			String name = cv.getAsString(NAME);
			String number = cv.getAsString(NUMBER);
			holder.name.setText(name);
			holder.number.setText(number);

			// 当前联系人的sortKey
			String currentStr = getAlpha(persons.get(position).getAsString(SORT_KEY));
			// 上一个联系人的sortKey
			String previewStr = (position - 1) >= 0 ? getAlpha(persons.get(position - 1).getAsString(SORT_KEY)) : " ";
			/**
			 * 判断显示#、A-Z的TextView隐藏与可见
			 */
			if (!previewStr.equals(currentStr)) { // 当前联系人的sortKey！=上一个联系人的sortKey，说明当前联系人是新组。
				holder.alpha.setVisibility(View.VISIBLE);
				holder.alpha.setText(currentStr);
			} else {
				holder.alpha.setVisibility(View.GONE);
			}
			return convertView;
		}
	}

	/**
	 * 提取英文的首字母，非英文字母用#代替。
	 * 
	 * @param str
	 * @return
	 */
	private String getAlpha(String str) {
		if (str == null) {
			return "#";
		}

		if (str.trim().length() == 0) {
			return "#";
		}

		char c = str.trim().substring(0, 1).charAt(0);
		// 正则表达式，判断首字母是否是英文字母
		Pattern pattern = Pattern.compile("^[A-Za-z]+$");
		if (pattern.matcher(c + "").matches()) {
			return (c + "").toUpperCase(); // 大写输出
		} else {
			return "#";
		}
	}

	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(adapter==null)return super.onKeyUp(keyCode, event);
			ArrayList<ContactsItem> ps = adapter.getChoicePersons();
			if(ps==null || ps.size()<1)return super.onKeyUp(keyCode, event);
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putSerializable("persons", ps);
			intent.putExtras(bundle);
			setResult(OPEN_CONTACTCHOICEACTIVITY_RESULTCODE, intent);
			finish();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onStop() {
		super.onStop();
		alphabar = null;
		this.finish();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		alphabar.setHight(alphabar.getHeight());
	}
	
	
}