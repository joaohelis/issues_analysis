package br.ufrn.deliverydelay.util;

import java.sql.Date;

public class DateInterval{

	private Date startDate,
				 endDate;
	
	public DateInterval(Date startDate, Date endDate) {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public String toString() {
		return "DateInterval [startDate=" + startDate + ", endDate=" + endDate
				+ "]";
	}
}
