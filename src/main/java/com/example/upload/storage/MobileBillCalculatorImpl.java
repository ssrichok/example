package com.example.upload.storage;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.example.upload.MobileBillCalculator;

@Component(value="MobileBillCalculatorImpl")
public class MobileBillCalculatorImpl implements MobileBillCalculator {

	float tariffUnitP1 = 1f;
	float tariffUnitSecP1 = 1f/60f;
	
	private TreeMap<String, Float> paymentReportMap = new TreeMap<String, Float>();
	/**
	 * [Date (dd/MM/yyyy)]|[Start time (HH:mm:ss)]|[End time (HH:mm:ss)]|[MobileNo]|[Promotion]
	 */
	
	
	boolean isStartCal = false;
	@Override
	public void  addCalculateBill(String txt) {
		
		System.out.println(txt);
		
			
		try {
			MobileUsage m = new MobileUsage(txt);
			float tariff = getTariff(m);
			System.out.println("price "+tariff);
			if(paymentReportMap.get(m.getMobileMo()) != null){
				
				float p = paymentReportMap.get(m.getMobileMo());
				
				paymentReportMap.put(m.getMobileMo(), p+tariff);
				
			}else{
				paymentReportMap.put(m.getMobileMo(), tariff);
			}
			isStartCal = true;
		} catch (ParseException e) {
			e.printStackTrace();
		}
	
		
	}
	
	@Override
	public Map<String, Float> getPaymentReport() {
		if(isStartCal){
			return paymentReportMap;
		}else{
			return null;
		}
	}

	private float getTariff(MobileUsage m){
		float f = 0f;
		
		switch (m.promotion) {
		case "P1":			
				
				long diff = m.getEndTime().getTime() - m.getStartTime().getTime();
				if(diff < 0l){
					System.out.println(m.getMobileMo()+" invalid time");
				}else{
					long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
					long minutes = TimeUnit.MILLISECONDS.toMinutes(diff); 
					
					long remainSec = seconds - (minutes*60);
					
					System.out.println("total second "+seconds+" > minute "+minutes+ " sec = "+remainSec);
					System.out.println("tariffUnitSec ="+tariffUnitSecP1);
					System.out.println("price sec ="+tariffUnitSecP1*remainSec);
					System.out.println("price min ="+tariffUnitP1*minutes);
					BigDecimal price = new BigDecimal((tariffUnitSecP1*remainSec)+ (tariffUnitP1*minutes));
					price =  price.setScale(2, BigDecimal.ROUND_HALF_UP);
					f = price.floatValue();
				}
			break;

		default:
			break;
		}
		
		return f;
	}

	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyyHH:mm:ss" , Locale.ENGLISH);
	
	class MobileUsage{
		
		String date;
		Date startTime;
		Date endTime;
		String mobileMo;
		String promotion;
		
		
		MobileUsage(String txt) throws ParseException{
			String [] t = txt.split("[|]");
			date = t[0];
			startTime = dateFormat.parse(t[0]+t[1]);
			endTime =  dateFormat.parse(t[0]+t[2]);
			mobileMo = t[3];
			promotion = t[4];
		}

		public String getDate() {
			return date;
		}

		public Date getStartTime() {
			return startTime;
		}

		public Date getEndTime() {
			return endTime;
		}

		public String getMobileMo() {
			return mobileMo;
		}

		public String getPromotion() {
			return promotion;
		}

	}
}
