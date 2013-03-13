package com.ian.sms.diyview;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ian.sms.view.R;
/**
 * 字母滚动条
 * @author ives
 * @date Feb 23, 20133:09:49 PM
 * @version 1.0
 * @comment
 */
public class AlphaSeachBar extends ImageButton {
	private TextView mDialogText;//用于做屏幕中央显示当前字母的控件
	private Handler mHandler;//用于操作中央显示的字母
	private ListView mList;//列表控件
	private float mHight;//控件高度（用于计算当前触摸到的字母）
	private String[] letters = new String[] { "#", "A", "B", "C", "D", "E",
			"F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
			"S", "T", "U", "V", "W", "X", "Y", "Z" };
	private HashMap<String, Integer> alphaIndexer;//字母在list控件中的位置索引

	public AlphaSeachBar(Context context) {
		super(context);
	}

	public AlphaSeachBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AlphaSeachBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	/**
	 * init
	 * @date Feb 23, 20133:53:32 PM
	 * @comment 
	 * @param ctx
	 */
	public void init(Activity ctx) {
		mDialogText = (TextView) ctx.findViewById(R.id.fast_position);
		mDialogText.setVisibility(View.INVISIBLE);
		mHandler = new Handler();
	}
	
	public void setListView(ListView mList) {
		this.mList = mList;
	}
	/**
	 * 设置字母在list控件中的位置索引
	 * @date Feb 23, 20133:38:45 PM
	 * @comment 
	 * @param alphaIndexer
	 */
	public void setAlphaIndexer(HashMap<String, Integer> alphaIndexer) {
		this.alphaIndexer = alphaIndexer;
	}
	
	public void setHight(float mHight) {
		System.out.println("height:"+mHight);
		this.mHight = mHight;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int act = event.getAction();
		float y = event.getY();//触摸的y坐标
		int selectIndex = (int) (y / (mHight / 27));// 计算手指位置，找到对应的段，让mList移动段开头的位置上
		if(alphaIndexer==null)return super.onTouchEvent(event);
		if (selectIndex < 27) {// 防止越界
			if(selectIndex<0)selectIndex =0;
			String key = letters[selectIndex];//取得触摸的字母
			if (alphaIndexer.containsKey(key)) {//如果当前字母索引中存在此字母
				int pos = alphaIndexer.get(key);//取得该字母做listview中的位置
				if (mList.getHeaderViewsCount() > 0) {// 防止ListView有标题栏，本例中没有。
					this.mList.setSelectionFromTop(pos + mList.getHeaderViewsCount(), 0);
				} else {
					this.mList.setSelectionFromTop(pos, 0);//操作list控件滑动到指定位置
				}
				mDialogText.setText(letters[selectIndex]);
			}
		}
		if (act == MotionEvent.ACTION_DOWN) {
			if (mHandler != null) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (mDialogText != null && mDialogText.getVisibility() == View.INVISIBLE) {
							mDialogText.setVisibility(VISIBLE);
						}
					}
				});
			}
		} else if (act == MotionEvent.ACTION_UP) {
			if (mHandler != null) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (mDialogText != null && mDialogText.getVisibility() == View.VISIBLE) {
							mDialogText.setVisibility(INVISIBLE);
						}
					}
				});
			}
		}
		return super.onTouchEvent(event);
	}
}
