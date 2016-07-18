package br.ufrn.deliverydelay.util;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DateIntervalUtil {
	
	public static List<DateInterval> genenateDateIntervalPerMonth(Date startDate, Date endDate){
		
		Calendar start = Calendar.getInstance();
		start.setTime(startDate);
		
		Calendar end = Calendar.getInstance();
		end.setTime(endDate);

		List<DateInterval> datesBetween = new ArrayList<DateInterval>();

		while (start.compareTo(end) < 0){
			Calendar startDateIntervalAux = (Calendar) start.clone();
			startDateIntervalAux.add(Calendar.DAY_OF_MONTH, start.getActualMaximum(Calendar.DAY_OF_MONTH) -1);
			
			datesBetween.add(new DateInterval(new Date(start.getTimeInMillis()), 
					new Date(startDateIntervalAux.getTimeInMillis())));
		
			start.add(Calendar.DAY_OF_MONTH, start.getActualMaximum(Calendar.DAY_OF_MONTH));
		}
		
		return datesBetween;
	}
}
