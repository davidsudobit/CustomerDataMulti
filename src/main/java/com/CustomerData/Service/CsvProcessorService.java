package com.CustomerData.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import javax.management.openmbean.InvalidKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.CustomerData.Model.AccountData;
import com.CustomerData.Model.CustomerData;
import com.CustomerData.Model.DataHolder;
import com.CustomerData.Model.Headings;
import com.CustomerData.Model.PathData;
import com.CustomerData.Model.SummaryData;

@Service
public class CsvProcessorService {
	
	@Autowired
	@Lazy
	private PdfService pdfService;
	
	@Autowired
	private Headings headingsConfig;
	
	private static List<String> accountDataHeadings;
	
	private static List<String> summaryDataHeadings;
	
	private static LinkedHashMap<String, List<String[]>> accountData;
	
	private static LinkedHashMap<String,List<String[]>> summaryData;
		
	@Async
	public void generatePdf(PathData pathData) throws Exception {
		
		validatePathdata(pathData);
		pathData.setDestPdfPath(pathData.getDestPdfPath()!=null?pathData.getDestPdfPath():headingsConfig.getPdfDestPath());
		
		processCustomerDataCustom(pathData.getAccountDataCsv());
		processSummaryDataCustom(pathData.getSummaryDataCsv());
		
		List<CustomerData> customerDatas=null;
		
		try {
			
			customerDatas=loadAllData();
			
		}
		catch(DataFormatException e) {
			
			System.out.println("Error in processing CSV :( ; Error Details : [ "+e.getMessage()+" ]");
			return;
			
		}
		finally {
			
			accountDataHeadings.clear();
			summaryDataHeadings.clear();
			accountData.clear();
			summaryData.clear();
			
		}
		
		System.out.print("\n<<<<< PDF Generation Started >>>>>\n");
		
//		System.out.println("\n"+pdfService.generatePdf(pathData, customerDatas.get(0))+"\n");
		
		customerDatas.forEach(customerData->{
			
			try {
				
				System.out.print("\n"+pdfService.generatePdf(pathData, customerData)+"\n");
				
			} catch (Exception e) {
				
				System.out.printf("Error Generating PDF : [ %s ]", pathData.getDestPdfPath()+String.format("%s_%s.pdf", customerData.getSsn(),customerData.getSummaryData().getSummaryYear()));
				
			}
			
		});
		
		System.out.print("\n<<<<< PDF Generation Completed >>>>>\n");
		
	}
	
	public void processCustomerDataCustom(String sourcePath) throws Exception {
		
		sourcePath=sourcePath==null?headingsConfig.getAccountCSVDefaultPath():sourcePath;
		
		List<String> inputLines=getFileData(sourcePath);
		List<String[]> inputData=parseAndSplitInputData(inputLines);
	
		accountDataHeadings=new LinkedList<>(Arrays.asList(inputData.remove(0)));
		accountData=removeDuplicates(inputData, accountDataHeadings);
		
	}
		
	public void processSummaryDataCustom(String sourcePath) throws IOException, DataFormatException {
		
		sourcePath=sourcePath==null?headingsConfig.getSummaryCSVDefaultPath():sourcePath;
		
		List<String> fileData=getFileData(sourcePath);
		List<String[]> linesData=parseAndSplitInputData(fileData);
		
		summaryDataHeadings=new LinkedList<>(Arrays.asList(linesData.remove(0)));
		summaryData=removeDuplicates(linesData, summaryDataHeadings);
		
	}
	
	private List<String[]> parseAndSplitInputData(List<String> inputLines) {
		
		return inputLines.stream().map(line->{
			
			String datas[]=line.split(";");
			
			for(int i=0;i<datas.length; i++) {
				
				datas[i]=datas[i].replace("\"", "").replace("\uFEFF","").trim();
				
			}
			
			return datas;
			
		}).collect(Collectors.toList());
		
	}
	
	private LinkedHashMap<String, List<String[]>> removeDuplicates(List<String[]> datas, List<String> headings){
		
		Map<String,List<String[]>> datasMap = datas.stream().collect(Collectors.groupingBy(inputDatas->inputDatas[getIndexOfHeading(headings, headingsConfig.getSsn())],LinkedHashMap<String, List<String[]>>::new, Collectors.mapping(Function.identity(), Collectors.toList())));
		
		return datasMap.entrySet().stream().map(entry->{
			
			List<String[]> dataList=entry.getValue();
			
			return dataList.stream().map(inputDatas->{
				
				StringJoiner stringJoiner=new StringJoiner(";");
				
				for(String inputData: inputDatas) {
					
					stringJoiner.add(inputData);
					
				}
				
				return stringJoiner.toString();
				
			}).distinct()
					.map(uniqueData->{
				
				return uniqueData.split(";");
				
			});
			
		}).flatMap(Function.identity()).collect(Collectors.groupingBy(processedData->processedData[getIndexOfHeading(headings, headingsConfig.getSsn())], LinkedHashMap<String,List<String[]>>::new, Collectors.mapping(Function.<String[]>identity(), Collectors.toList())));
		
	}
	
	private List<AccountData> loadAccountDataCustom(List<String[]> inputData){
		
		Integer savingsAccountIndexes[]=loadIndexesForAccount(accountDataHeadings, headingsConfig.getSavingsAccount());
		Integer loanAccountIndexes[]=loadIndexesForAccount(accountDataHeadings, headingsConfig.getLoanAccount());
		
		return inputData.stream().map(datas->{
			
			List<DataHolder> listOfData=new LinkedList<>();
			
			String accountId=preceddingZeroRemover(datas[getIndexOfHeading(accountDataHeadings, headingsConfig.getEngagementNumber())].toCharArray());
			
			AccountData accountData=new AccountData();
			
			accountData.setAccountName(String.join(" ", datas[getIndexOfHeading(accountDataHeadings, headingsConfig.getProductText())], accountId));
			accountData.setDataHolder(listOfData);
			
			String customerAccountType=
					datas[getIndexOfHeading(accountDataHeadings,headingsConfig.getProductText())].toLowerCase().contains("SparKonto".toLowerCase())?"sparkonto":
						datas[getIndexOfHeading(accountDataHeadings,headingsConfig.getProductText())].toLowerCase().contains("Banklån".toLowerCase())?"banklån":
							datas[getIndexOfHeading(accountDataHeadings,headingsConfig.getProductText())];
			
			switch(customerAccountType) {
				
				case "sparkonto" -> {
					
					for(int i=0;i<savingsAccountIndexes.length-2;i++) {
						
						Integer indexToFetch=savingsAccountIndexes[i];
						
						if(!(accountDataHeadings.get(indexToFetch).equalsIgnoreCase(headingsConfig.getCapitalSharePercentage())||accountDataHeadings.get(indexToFetch).equalsIgnoreCase(headingsConfig.getReceivedInterestSharePercentage()))) {
							
							datas[indexToFetch]=formatData(datas[indexToFetch]);
							
						}else {
							datas[indexToFetch]=String.format("%.0f", Double.parseDouble(datas[indexToFetch]));
						}
						
						listOfData.add(new DataHolder(accountDataHeadings.get(indexToFetch), (i==0)?datas[indexToFetch]+"%":datas[indexToFetch]));
						
					}
					
					Integer indexToFetch=savingsAccountIndexes.length;
					
					accountData.setBalanceShare(formatData(datas[savingsAccountIndexes[indexToFetch-2]]));
					accountData.setInterestShare(formatData(datas[savingsAccountIndexes[indexToFetch-1]]));
					
					break;
					
				}
				
				case "banklån" -> {
					
					for(int i=0;i<loanAccountIndexes.length-2;i++) {
						
						Integer indexToFetch=loanAccountIndexes[i];
						
						if(!(accountDataHeadings.get(indexToFetch).equalsIgnoreCase(headingsConfig.getCapitalSharePercentage())||accountDataHeadings.get(indexToFetch).equalsIgnoreCase(headingsConfig.getReceivedInterestSharePercentage()))) {
							
							datas[indexToFetch]=formatData(datas[indexToFetch]);
							
						}else {
							datas[indexToFetch]=String.format("%.0f", Double.parseDouble(datas[indexToFetch]));
						}
						
						listOfData.add(new DataHolder(accountDataHeadings.get(indexToFetch), datas[indexToFetch]));
						
					}
					
					Integer indexToFetch=loanAccountIndexes.length;
					
					accountData.setBalanceShare(formatData(datas[loanAccountIndexes[indexToFetch-2]]));
					accountData.setInterestShare(formatData(datas[loanAccountIndexes[indexToFetch-1]]));
					
					break;
					
				}
				
				default ->{
					
					for(int i=0;i<loanAccountIndexes.length-2;i++) {
						
						Integer indexToFetch=loanAccountIndexes[i];
						
						if(!(accountDataHeadings.get(indexToFetch).equalsIgnoreCase(headingsConfig.getCapitalSharePercentage())||accountDataHeadings.get(indexToFetch).equalsIgnoreCase(headingsConfig.getReceivedInterestSharePercentage()))) {
							
							datas[indexToFetch]=formatData(datas[indexToFetch]);
							
						}else {
							datas[indexToFetch]=String.format("%.0f", Double.parseDouble(datas[indexToFetch]));
						}
						
						listOfData.add(new DataHolder(accountDataHeadings.get(indexToFetch), datas[indexToFetch]));
						
					}
					
					Integer indexToFetch=loanAccountIndexes.length;
					
					accountData.setBalanceShare(formatData(datas[loanAccountIndexes[indexToFetch-2]]));
					accountData.setInterestShare(formatData(datas[loanAccountIndexes[indexToFetch-1]]));
					
				}
					
			
			};
			
//			if(datas[getIndexOfHeading(accountDataHeadings,headingsConfig.getProductText())].toLowerCase().contains("SparKonto".toLowerCase())) {
//				
//				for(int i=0;i<savingsAccountIndexes.length-2;i++) {
//					
//					Integer indexToFetch=savingsAccountIndexes[i];
//					
//					if(!(accountDataHeadings.get(indexToFetch).equalsIgnoreCase(headingsConfig.getCapitalSharePercentage())||accountDataHeadings.get(indexToFetch).equalsIgnoreCase(headingsConfig.getReceivedInterestSharePercentage()))) {
//						
//						datas[indexToFetch]=formatData(datas[indexToFetch]);
//						
//					}else {
//						datas[indexToFetch]=String.format("%.0f", Double.parseDouble(datas[indexToFetch]));
//					}
//					
//					listOfData.add(new DataHolder(accountDataHeadings.get(indexToFetch), (i==0)?datas[indexToFetch]+"%":datas[indexToFetch]));
//					
//				}
//				
//				Integer indexToFetch=savingsAccountIndexes.length;
//				
//				customerData.setBalanceShare(formatData(datas[savingsAccountIndexes[indexToFetch-2]]));
//				customerData.setInterestShare(formatData(datas[savingsAccountIndexes[indexToFetch-1]]));
//				
//			}
//			else {
//				
//				for(int i=0;i<loanAccountIndexes.length-2;i++) {
//					
//					Integer indexToFetch=loanAccountIndexes[i];
//					
//					if(!(accountDataHeadings.get(indexToFetch).equalsIgnoreCase(headingsConfig.getCapitalSharePercentage())||accountDataHeadings.get(indexToFetch).equalsIgnoreCase(headingsConfig.getReceivedInterestSharePercentage()))) {
//						
//						datas[indexToFetch]=formatData(datas[indexToFetch]);
//						
//					}else {
//						datas[indexToFetch]=String.format("%.0f", Double.parseDouble(datas[indexToFetch]));
//					}
//					
//					listOfData.add(new DataHolder(accountDataHeadings.get(indexToFetch), datas[indexToFetch]));
//					
//				}
//				
//				Integer indexToFetch=loanAccountIndexes.length;
//				
//				customerData.setBalanceShare(formatData(datas[loanAccountIndexes[indexToFetch-2]]));
//				customerData.setInterestShare(formatData(datas[loanAccountIndexes[indexToFetch-1]]));
//				
//			}
			
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
			
			return accountData;
			
		}).collect(Collectors.toList());
		
	}
	
	private SummaryData loadSummaryData(List<String[]> linesData) throws DataFormatException {
		
		SummaryData summaryDataOut= linesData.stream().map(datas->{
			
			SummaryData summaryData=new SummaryData();
			
			summaryData.setSummaryYear(datas[getIndexOfHeading(summaryDataHeadings, headingsConfig.getYearConcerned())]);
			summaryData.setSumReceivedInterest(formatData(datas[getIndexOfHeading(summaryDataHeadings, headingsConfig.getSumReceivedInterest())]));
			summaryData.setSumPreliminaryTax(formatData(datas[getIndexOfHeading(summaryDataHeadings, headingsConfig.getSumPreliminaryTax())]));
			summaryData.setSumPaidInterest(formatData(datas[getIndexOfHeading(summaryDataHeadings, headingsConfig.getSumPaidInterest())]));
			
			return summaryData;
			
		}).findFirst().orElseThrow(()->new DataFormatException("Summary data loading failed :("));
		
		return summaryDataOut;
		
	}
	
	private CustomerData loadCustomerData(List<String[]> inputDatas, CustomerData customerData) {
		
		inputDatas.forEach(datas->{
			
			customerData.setCustomerName(datas[getIndexOfHeading(summaryDataHeadings, headingsConfig.getName())]);
			customerData.setCareOfAddress(datas[getIndexOfHeading(summaryDataHeadings, headingsConfig.getCareOfAddress())]);
			customerData.setStreetName(datas[getIndexOfHeading(summaryDataHeadings, headingsConfig.getStreetAddress())]);
			customerData.setPincode(datas[getIndexOfHeading(summaryDataHeadings, headingsConfig.getPostalCode())]);
			customerData.setCity(datas[getIndexOfHeading(summaryDataHeadings, headingsConfig.getCity())]);
			
		});
		
		return customerData;
		
	}
	
	private List<CustomerData> loadAllData() throws DataFormatException{
		
		return accountData.entrySet().stream().map(entry->{
			
			CustomerData customerData=new CustomerData();
			
			String ssn=entry.getKey();
			List<AccountData> customerAccountData=loadAccountDataCustom(entry.getValue());
			SummaryData customerSummaryData=null;
			try {
				
				if(summaryData.containsKey(ssn)) {
					
					customerSummaryData=loadSummaryData(summaryData.get(ssn));
					customerData=loadCustomerData(summaryData.get(ssn), customerData);
					
				}
				
			} catch (DataFormatException e) {
				
			}
			
			customerData.setSsn(ssn);
			customerData.setAccountData(customerAccountData);
			customerData.setSummaryData(customerSummaryData);
			
			return customerData;
			
		}).collect(Collectors.toList());
		
	}
	
	private Integer[] loadIndexesForAccount(List<String> headings, String targetHeadings) {
		
		String accountHeaders[]= targetHeadings.split(",");
		
		Integer accountIndexes[]= new Integer[accountHeaders.length];
		int indexOfIndexes=0;
		
		for(String header : accountHeaders) {
			
			for(int i=0;i<headings.size();i++) {
				
				if(headings.get(i).equalsIgnoreCase(header)) {
					
					accountIndexes[indexOfIndexes++]=i;
					break;
					
				}
				
			}
			
		}
		
		return accountIndexes;
		
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
	
	private Integer getIndexOfHeading(List<String> headings, String heading) {
		
		if(headings.contains(heading)) {
			
			return headings.indexOf(heading);
			
		}
		
		throw new InvalidKeyException("Heading not found : "+heading);
		
	}
	
	private List<String> getFileData(String filePath) throws IOException{
		
		if(isFileExists(filePath)) {
			
			return Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
			
		}
		
		throw new FileNotFoundException("File not found : [ "+filePath+" ]");
		
	}
	
	private Boolean isFileExists(String filePath) {
		
		return Files.exists(Paths.get(filePath));
		
	}
	
	private PathData validatePathdata(PathData pathData) throws FileNotFoundException, IllegalArgumentException, NullPointerException {
    	
    	if(pathData!=null) {
    		
    		if(pathData.getAccountDataCsv()!=null&&pathData.getSummaryDataCsv()!=null&&pathData.getDestPdfPath()!=null) {
    			
    			if(Files.exists(Paths.get(pathData.getAccountDataCsv()))&&Files.exists(Paths.get(pathData.getSummaryDataCsv()))) {
    				
    				return pathData;
    				
    			}
    			
    			throw new FileNotFoundException("Some of the input files not found : [ "+ String.join(", ", pathData.getAccountDataCsv(), pathData.getSummaryDataCsv()) +" ]");
    			
    		}
    		
    		throw new IllegalArgumentException("Some of the input files paths are null : [ "+ String.join(", ", pathData.getAccountDataCsv(), pathData.getSummaryDataCsv()) +" ]");
    		
    	}
    	
    	throw new NullPointerException("Pathdata is null");
    	
    }
	
//	private Path makeCsvFile(List<String> inputLines, String destPath) throws IOException {
//		
//		return Files.write(Paths.get(destPath), inputLines, Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
//		
//	}

}
