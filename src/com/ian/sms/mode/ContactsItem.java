package com.ian.sms.mode;


/**
 * 
 * @author ives
 * @date 2013-3-3下午3:43:45
 * @version 1.0
 * @comment
 */
public class ContactsItem implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	private String phone;
	private String name;
	
	
	
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public boolean equals(Object o) {
		if(o instanceof ContactsItem){
			ContactsItem item = (ContactsItem) o;
			return item.phone.equals(this.phone);
		}
		return false;
		
	}
	@Override
	public int hashCode() {
		return phone.hashCode();
	}
	
	
	

}
