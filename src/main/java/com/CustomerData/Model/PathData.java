package com.CustomerData.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PathData {
	
	private String customerDataCsv;
	
	private String summaryDataCsv;
	
	private String destPdfPath;

}
