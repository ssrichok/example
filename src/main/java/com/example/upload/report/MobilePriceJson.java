package com.example.upload.report;

import java.util.List;

public class MobilePriceJson {

    private List<MobilePrice> mpList;

    public MobilePriceJson(List<MobilePrice> mpList) {
	super();
	this.mpList = mpList;
    }

    public List<MobilePrice> getMpList() {
	return mpList;
    }

    public void setMpList(List<MobilePrice> mpList) {
	this.mpList = mpList;
    }
}
