package com.example.upload.report;

public class MobilePrice {

    private String mobileNo;
    private float price;

    public MobilePrice(String mobileNo, float price) {
	super();
	this.mobileNo = mobileNo;
	this.price = price;
    }

    public String getMobileNo() {
	return mobileNo;
    }

    public float getPrice() {
	return price;
    }

    public void setMobileNo(String mobileNo) {
	this.mobileNo = mobileNo;
    }

    public void setPrice(float price) {
	this.price = price;
    }
}
