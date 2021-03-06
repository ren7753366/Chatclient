package com.bochatclient.bean;

public class GiftBean {
	
	private long giftId;
	private String giftUid;
	private boolean comboAutoSwitch;//连击开关
	private int comboGroupNum;//连击组数
	private int comboNum;//连击次数
	private int giftCount;//礼物数量
	private String giftName;//礼物名称
	private boolean isComboFlag;//是否连击
	private int isFree;//是否免费礼物 0-不是/1-是
	private String giftIcon;//礼物图片
	private int version;//礼物版本号
	private long giftTime;//送礼时间
	
	public String getGiftUid() {
		return giftUid;
	}
	public void setGiftUid(String giftUid) {
		this.giftUid = giftUid;
	}
	public long getGiftId() {
		return giftId;
	}
	public void setGiftId(long giftId) {
		this.giftId = giftId;
	}
	public long getGiftTime() {
		return giftTime;
	}
	public void setGiftTime(long giftTime) {
		this.giftTime = giftTime;
	}
	public boolean isComboAutoSwitch() {
		return comboAutoSwitch;
	}
	public void setComboAutoSwitch(boolean comboAutoSwitch) {
		this.comboAutoSwitch = comboAutoSwitch;
	}
	
	public int getComboNum() {
		return comboNum;
	}
	public void setComboNum(int comboNum) {
		this.comboNum = comboNum;
	}
	public int getComboGroupNum() {
		return comboGroupNum;
	}
	public void setComboGroupNum(int comboGroupNum) {
		this.comboGroupNum = comboGroupNum;
	}
	public int getGiftCount() {
		return giftCount;
	}
	public void setGiftCount(int giftCount) {
		this.giftCount = giftCount;
	}
	public String getGiftName() {
		return giftName;
	}
	public void setGiftName(String giftName) {
		this.giftName = giftName;
	}
	public boolean isComboFlag() {
		return isComboFlag;
	}
	public void setComboFlag(boolean isComboFlag) {
		this.isComboFlag = isComboFlag;
	}
	public int getIsFree() {
		return isFree;
	}
	public void setIsFree(int isFree) {
		this.isFree = isFree;
	}
	public String getGiftIcon() {
		return giftIcon;
	}
	public void setGiftIcon(String giftIcon) {
		this.giftIcon = giftIcon;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
}
