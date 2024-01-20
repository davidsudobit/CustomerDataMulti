package com.CustomerData.Model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountData {
	
	private String accountName;
	
	private List<DataHolder> dataHolder;
	
	private String balanceShare;
	
	private String interestShare;

}
