package com.CustomerData.Model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Configuration("headings")
public class Headings {
	
	@Value("${csv.processor.default.account_csv_source_path}")
	private String accountCSVDefaultPath;
	
	@Value("${csv.processor.default.summary_csv_source_path}")
	private String summaryCSVDefaultPath;
	
	@Value("${csv.processor.default.pdf_dest_path}")
	private String pdfDestPath;
	
//	@Value("${target.headings.savings_account}")
//	private String savingsAccount;
	
	@Value("${target.headings.neglect.data}")
	private String neglectData;
	
	@Value("${target.headings.loan_account}")
	private String loanAccount;
	
	@Value("${target.heading.ssn}")
	private String ssn;
	
	@Value("${target.heading.engagement_number}")
	private String engagementNumber;
	
	@Value("${target.heading.product_text}")
	private String productText;

	@Value("${target.heading.capital_share_percentage}")
	private String capitalSharePercentage;
	
	@Value("${target.heading.received_interest_share_percentage}")
	private String receivedInterestSharePercentage;
	
	@Value("${target.heading.balance_share}")
	private String balanceShare;
	
	@Value("${target.heading.received_interest_share}")
	private String receivedInterestShare;
	
	@Value("${target.heading.year_concerned}")
	private String yearConcerned;
	
	@Value("${target.heading.sum_received_interest}")
	private String sumReceivedInterest;
	
	@Value("${target.heading.sum_preliminary_tax}")
	private String sumPreliminaryTax;
	
	@Value("${target.heading.sum_paid_interest}")
	private String sumPaidInterest;
	
	@Value("${target.heading.name}")
	private String name;
	
	@Value("${target.heading.care_of_address}")
	private String careOfAddress;
	
	@Value("${target.heading.street_address}")
	private String streetAddress;
	
	@Value("${target.heading.postal_code}")
	private String postalCode;
	
	@Value("${target.heading.city}")
	private String city;
	
}
