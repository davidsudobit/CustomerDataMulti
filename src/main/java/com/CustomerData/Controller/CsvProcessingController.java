package com.CustomerData.Controller;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.CustomerData.Model.CustomerData;
import com.CustomerData.Model.PathData;
import com.CustomerData.Model.SummaryData;
import com.CustomerData.Service.CsvProcessorService;
import com.CustomerData.Service.PdfService;

@Controller
@RequestMapping(path = { "/csv" })
public class CsvProcessingController {
	
	@Autowired
	private CsvProcessorService csvProcessorService;
	
	@Autowired
	private PdfService pdfService;
	
//	@GetMapping(path = { "/processCSV" })
//	public ResponseEntity<List<CustomerData>> processCsv(@RequestParam(name="sourcePath", required = false) String sourcePath, @RequestParam(name="destPath", required = false) String destPath) throws Exception{
//		
//		return ResponseEntity.status(HttpStatus.OK).body(csvProcessorService.processCustomerData(sourcePath));
//		
//	}
	
//	@GetMapping(path = { "/process-summary-data" })
//	public ResponseEntity<SummaryData> processSummaryData(@RequestParam(name = "sourcePath", required = false) String sourcePath, @RequestParam(name = "destPath", required = false) String destPath) throws IOException, DataFormatException{
//	
//		return ResponseEntity.status(HttpStatus.OK).body(csvProcessorService.processSummaryData(sourcePath));
//		
//	}
	
//	@GetMapping(path = { "/customer_data" })
//	public ModelAndView getCustomerData(ModelAndView modelAndView) throws Exception {
//		
//		modelAndView.addObject("customerDatas", csvProcessorService.processCustomerData(null));
//		
//		SummaryData summaryData=csvProcessorService.processSummaryData(null);
//		
//		modelAndView.addObject("customerName", "BroKoi");
//		modelAndView.addObject("summaryYear", summaryData.getSummaryYear());
//		modelAndView.addObject("customerAddress", "Lorem ipsum dolor sit amet consectetur\r\n"
//				+ "				adipisicing elit. Iusto magnam labore cum fugit tenetur amet quisquam\r\n"
//				+ "				id unde maiores consequatur");
//		modelAndView.addObject("customerContent", "Lorem ipsum dolor sit amet consectetur");
//		modelAndView.addObject("sumReceivedInterest", summaryData.getSumReceivedInterest());
//		modelAndView.addObject("sumPreliminaryTax", summaryData.getSumPreliminaryTax());
//		modelAndView.addObject("sumPaidInterest", summaryData.getSumPaidInterest());
//		
//		modelAndView.setViewName("customer_data");
//		
//		return modelAndView;
//		
//	}
	
	@GetMapping(path = { "/generate-pdf" })
	public ResponseEntity<String> generatePdf(@RequestBody(required = true) PathData pathData) throws Exception{
		
		return ResponseEntity.status(HttpStatus.OK).body(pdfService.generatePdf(pathData));
		
//		return pdfService.generatePdf()
//				? ResponseEntity.status(HttpStatus.OK).body("Output PDF generated!")
//						: ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Some problem occured :(");
		
	}

}
