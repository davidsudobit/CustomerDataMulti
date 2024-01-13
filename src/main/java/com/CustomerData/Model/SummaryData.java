package com.CustomerData.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SummaryData {
	
	private String summaryYear;
	
	private String sumReceivedInterest;
	
	private String sumPreliminaryTax;
	
	private String sumPaidInterest;

}
