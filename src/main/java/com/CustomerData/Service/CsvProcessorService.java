package com.CustomerData.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

import org.springframework.stereotype.Service;

import com.CustomerData.Model.CustomerData;
import com.CustomerData.Model.DataHolder;
import com.CustomerData.Model.SummaryData;

@Service
public class CsvProcessorService {
	
	private static final String SOURCE_FILE_PATH="C:\\Users\\sr73\\OneDrive - Capgemini\\Documents\\workspace-spring-tool-suite\\CustomerData\\src\\main\\resources\\inputFiles\\organizations-100.csv";
	
//	private static final String DEST_FILE_PATH="C:\\Users\\sr73\\OneDrive - Capgemini\\Documents\\workspace-spring-tool-suite\\CustomerData\\src\\main\\resources\\outputFiles\\organization-100.csv";
	
	public List<CustomerData> processCustomerData(String sourcePath) throws Exception {
		
		sourcePath=sourcePath==null?SOURCE_FILE_PATH:sourcePath;
//		destPath=destPath==null?DEST_FILE_PATH:destPath;
		
		if(Files.exists(Paths.get(sourcePath))){
			
			List<String> inputLines=Files.readAllLines(Paths.get(sourcePath));
			List<String[]> inputData=parseAndSplitInputData(inputLines);
//			inputData=dataProcessor(inputData);
			List<CustomerData> listOfCustomerData=loadCustomerDataCustom(inputData);
			
			return listOfCustomerData;
//			inputLines=makeCsv(inputData);
//			Path destFilePath=makeCsvFile(inputLines, destPath);
			
//			return destFilePath.toAbsolutePath();
			
		}
		
		throw new Exception("Source file not found : [ "+sourcePath+" ]");
		
	}
	
	public SummaryData processSummaryData(String sourcePath) throws IOException, DataFormatException {
		
		sourcePath=sourcePath==null?"C:\\Users\\sr73\\OneDrive - Capgemini\\Documents\\workspace-spring-tool-suite\\CustomerData\\src\\main\\resources\\inputFiles\\secondcsv.csv":sourcePath;
		
		List<String> fileData=getFileData(sourcePath);
		
		List<String[]> linesData=parseAndSplitInputData(fileData);
		
		return loadSummaryData(linesData);
		
	}
	
	private List<String[]> parseAndSplitInputData(List<String> inputLines) {
		
		return inputLines.stream().map(line->{
			
			String datas[]=line.split(";");
			
			return datas;
			
		}).collect(Collectors.toList());
		
	}
	
	private List<String[]> dataProcessor(List<String[]> inputData) {
		
		return inputData.stream().map(datas->{
			
			for(int i=0;i<datas.length;i++) {
				
				String regexPattern = "\\+|\\b0(?:\\.0+)?\\b|N/A|n/a|\\s";

		        // Create a Pattern object
		        Pattern pattern = Pattern.compile(regexPattern);

		        // Create a Matcher object
		        Matcher matcher = pattern.matcher(datas[i]);
		        
		        if(matcher.matches()&&i!=7) {
		        
//		        	System.out.println();
//		        	System.out.printf("Data : %s, Index: %d\n", datas[i], i);
//		        	System.out.println();
		        	
		        	String result = matcher.replaceAll("");
		        	datas[i]=result;
		        	
		        }
		        
//		        if(i==10) {
//		        	System.out.println();
//		        	System.out.printf("Data : %s, Index: %d\n", datas[i], i);
//		        	System.out.println();
//		        }
				
			}
			
			return datas;
			
		}).collect(Collectors.toList());
		
	}
	
	private List<String> makeCsv(List<String[]> inputData) {

		return inputData.stream().map(datas->{
			
			StringJoiner stringJoiner=new StringJoiner(",");
			
			for(String data : datas) {
				
				stringJoiner.add(data);
				
			}
			
			return stringJoiner.toString();
			
		}).collect(Collectors.toList());
		
	}
	
	private List<CustomerData> loadCustomerData(List<String[]> inputData){
		
		String heading[]=inputData.remove(0);
		
		return inputData.stream().map(datas->{
			
			List<DataHolder> listOfData=new LinkedList<>();
			
			CustomerData customerData=new CustomerData();
			customerData.setOrganizationName(String.join(" ", datas[3], datas[0].substring(6)));
			customerData.setDataHolder(listOfData);
			
			for(int i=4; i<datas.length; i++) {
				
				if(!(datas[i].isEmpty()||datas[i].isBlank())) {
					
					try {
						
						Double doubleData=Double.parseDouble(datas[i]);
						
						datas[i]=String.format("%.2f", doubleData);
						
						datas[i]=(i==12)?datas[i]+"%":datas[i];
						
					}
					catch(NumberFormatException e) {
						
					}
					
//					datas[i].format("%.2s", heading)
					
					listOfData.add(new DataHolder(heading[i], datas[i]));
					
				}
				
			}
			
			return customerData;
			
		}).collect(Collectors.toList());
		
	}
	
	private List<CustomerData> loadCustomerDataCustom(List<String[]> inputData){
		
		String heading[]=inputData.remove(0);
		
		Integer savingsAccountIndexes[]=loadIndexesForSavingsAccount(heading);
		Integer loanAccountIndexes[]=loadIndexesForLoanAccount(heading);
		
		return inputData.stream().map(datas->{
			
			List<DataHolder> listOfData=new LinkedList<>();
			
			String accountId=preceddingZeroRemover(datas[0].toCharArray());
			
			CustomerData customerData=new CustomerData();
			customerData.setOrganizationName(String.join(" ", datas[3], accountId));
			customerData.setDataHolder(listOfData);
			
			if(datas[3].toLowerCase().contains("SparKonto".toLowerCase())) {
				
				for(int i=0;i<savingsAccountIndexes.length-2;i++) {
					
					Integer indexToFetch=savingsAccountIndexes[i];
					
					if(!(heading[indexToFetch].equalsIgnoreCase("CapitalSharePercentage")||heading[indexToFetch].equalsIgnoreCase("ReceivedInterestSharePercentage"))) {
						
						datas[indexToFetch]=formatData(datas[indexToFetch]);
						
					}else {
						datas[indexToFetch]=String.format("%.0f", Double.parseDouble(datas[indexToFetch]));
					}
					
					listOfData.add(new DataHolder(heading[indexToFetch], (i==0)?datas[indexToFetch]+"%":datas[indexToFetch]));
					
				}
				
				Integer indexToFetch=savingsAccountIndexes.length;
				
				customerData.setBalanceShare(formatData(datas[savingsAccountIndexes[indexToFetch-2]]));
				customerData.setInterestShare(formatData(datas[savingsAccountIndexes[indexToFetch-1]]));
				
			}
			else {
				
				for(int i=0;i<loanAccountIndexes.length-2;i++) {
					
					Integer indexToFetch=loanAccountIndexes[i];
					
					if(!(heading[indexToFetch].equalsIgnoreCase("CapitalSharePercentage")||heading[indexToFetch].equalsIgnoreCase("ReceivedInterestSharePercentage"))) {
						
						datas[indexToFetch]=formatData(datas[indexToFetch]);
						
					}else {
						datas[indexToFetch]=String.format("%.0f", Double.parseDouble(datas[indexToFetch]));
					}
					
					listOfData.add(new DataHolder(heading[indexToFetch], datas[indexToFetch]));
					
				}
				
				Integer indexToFetch=loanAccountIndexes.length;
				
				customerData.setBalanceShare(formatData(datas[loanAccountIndexes[indexToFetch-2]]));
				customerData.setInterestShare(formatData(datas[loanAccountIndexes[indexToFetch-1]]));
				
			}
			
//			for(int i=4; i<datas.length; i++) {
//				
//				if(!(datas[i].isEmpty()||datas[i].isBlank())) {
//					
//					try {
//						
//						Double doubleData=Double.parseDouble(datas[i]);
//						
//						datas[i]=String.format("%.2f", doubleData);
//						
//						datas[i]=(i==12)?datas[i]+"%":datas[i];
//						
//					}
//					catch(NumberFormatException e) {
//						
//					}
//					
////					datas[i].format("%.2s", heading)
//					
//					listOfData.add(new DataHolder(heading[i], datas[i]));
//					
//				}
//				
//			}
			
			return customerData;
			
		}).collect(Collectors.toList());
		
	}
	
	private SummaryData loadSummaryData(List<String[]> linesData) throws DataFormatException {
		
		String headings[]=linesData.remove(0);
		
		Integer summaryDataIndexes[]=loadIndexesForSummaryData(headings, null);
		
		SummaryData summaryDataOut= linesData.stream().map(datas->{
			
			SummaryData summaryData=new SummaryData();
			
			summaryData.setSummaryYear(datas[summaryDataIndexes[0]]);
			summaryData.setSumReceivedInterest(formatData(datas[summaryDataIndexes[1]]));
			summaryData.setSumPreliminaryTax(formatData(datas[summaryDataIndexes[2]]));
			summaryData.setSumPaidInterest(formatData(datas[summaryDataIndexes[3]]));
			
			return summaryData;
			
		}).findFirst().orElseThrow(()->new DataFormatException("Summary data loading failed :("));
		
		return summaryDataOut;
		
	}
	
	private Integer[] loadIndexesForSavingsAccount(String headings[]) {
		
		String savingsAccountHeaderings="Interest1,ReceivedInterest,Balance,PreliminaryTax,CapitalSharePercentage,ReceivedInterestSharePercentage,BalanceShare,ReceivedInterestShare";
		
		String savingsAccountHeaders[]= savingsAccountHeaderings.split(",");
		
		Integer savingsAccountIndexes[]= new Integer[savingsAccountHeaders.length];
		int indexOfIndexes=0;
		
		for(String header : savingsAccountHeaders) {
			
			for(int i=0;i<headings.length;i++) {
				
				if(headings[i].equalsIgnoreCase(header)) {
					
					savingsAccountIndexes[indexOfIndexes++]=i;
					break;
					
				}
				
			}
			
		}
		
		return savingsAccountIndexes;
		
	}
	
	private Integer[] loadIndexesForLoanAccount(String headings[]) {
		
		String loanAccountHeaderings="PaidInterest,Debth,CapitalSharePercentage,ReceivedInterestSharePercentage,BalanceShare,PaidInterestShare";
		
		String loanAccountHeaders[]= loanAccountHeaderings.split(",");
		
		Integer loanAccountIndexes[]= new Integer[loanAccountHeaders.length];
		int indexOfIndexes=0;
		
		for(String header : loanAccountHeaders) {
			
			for(int i=0;i<headings.length;i++) {
				
				if(headings[i].equalsIgnoreCase(header)) {
					
					loanAccountIndexes[indexOfIndexes++]=i;
					break;
					
				}
				
			}
			
		}
		
		return loanAccountIndexes;
		
	}
	
	private Integer[] loadIndexesForSummaryData(String[] headings, String target) {
		
		String targetHeadings[]=target!=null?target.split(","):"YearConcerned,SumReceivedInterest,SumPreliminaryTax,SumPaidInterest".split(",");
		
		Integer summaryDataIndexes[]= new Integer[targetHeadings.length];
		Integer indexOfIndexes=0;
		
		for(String targetHeading : targetHeadings) {
			
			for(int i=0;i<headings.length;i++) {
				
				if(targetHeading.equalsIgnoreCase(headings[i])) {
					
					summaryDataIndexes[indexOfIndexes++]=i;
					break;
					
				}
				
			}
			
		}
		
		return summaryDataIndexes;
		
	}
	
	private String formatData(String data) {
		
		try {
			
			Double doubleData=Double.parseDouble(data);
			
			return String.format("%,.2f", doubleData);
			
		}
		catch(NumberFormatException e) {
			
			return data;
			
		}
		
	}
	
	private String preceddingZeroRemover(char dataArray[]) {
		
		for(int i=0;i<dataArray.length;i++) {
			
			if(dataArray[i]!='0') {
				break;
			}
			
			dataArray[i]=' ';
			
		}
		
		return String.valueOf(dataArray).stripLeading();
		
	}
	
	private List<String> getFileData(String filePath) throws IOException{
		
		if(isFileExists(filePath)) {
			
			return Files.readAllLines(Paths.get(filePath));
			
		}
		
		throw new FileNotFoundException("File not found : [ "+filePath+" ]");
		
	}
	
	private Boolean isFileExists(String filePath) {
		
		return Files.exists(Paths.get(filePath));
		
	}
	
	private Path makeCsvFile(List<String> inputLines, String destPath) throws IOException {
		
		return Files.write(Paths.get(destPath), inputLines, Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		
	}

}
