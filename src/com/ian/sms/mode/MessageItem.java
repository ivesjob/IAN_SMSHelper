package com.ian.sms.mode;

import java.util.Vector;

/**
 * 
 * @author ives
 * @date 2013-3-1上午11:46:36
 * @version 1.0
 * @comment^\s*\n
 */
public class MessageItem implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	private int id;
	private int type;
	private int protocol;
	private String phone;
	private String body;
	private long date;
	private int read;
	private String thread_id;
	private boolean isTemp;
	
	
	
	public MessageItem() {
	}

	
	public boolean isTemp() {
		return isTemp;
	}


	public void setTemp(boolean isTemp) {
		this.isTemp = isTemp;
	}


	public String getThread_id() {
		return thread_id;
	}

	public void setThread_id(String thread_id) {
		this.thread_id = thread_id;
	}

	public int getRead() {
		return read;
	}

	public void setRead(int read) {
		this.read = read;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getProtocol() {
		return protocol;
	}

	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String toString() {
		return "id = " + id + ";" + "type = " + type + ";" + "protocol = "
				+ protocol + ";" + "phone = " + phone + ";" + "body = " + body;
	}

}
