package com.CustomerData.Model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerData {
	
	private String ssn;
	
	private String customerName;
	
	private String careOfAddress;
	
	private String streetName;
	
	private String city;
	
	private String pincode;
	
	private List<AccountData> accountData;
	
	private SummaryData summaryData;

}
