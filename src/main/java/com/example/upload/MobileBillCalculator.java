package com.example.upload;

import java.util.Map;

public interface MobileBillCalculator {

	public void  addCalculateBill(String txt);
	
	public Map<String, Float> getPaymentReport();
}
