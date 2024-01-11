package com.CustomerData.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.CustomerData.Model.CustomerData;
import com.CustomerData.Service.CsvProcessorService;
import com.CustomerData.Service.PdfService;

@Controller
@RequestMapping(path = { "/csv" })
public class CsvProcessingController {
	
	@Autowired
	private CsvProcessorService csvProcessorService;
	
	@Autowired
	private PdfService pdfService;
	
	@GetMapping(path = { "/processCSV" })
	public ResponseEntity<List<CustomerData>> processCsv(@RequestParam(name="sourcePath", required = false) String sourcePath, @RequestParam(name="destPath", required = false) String destPath) throws Exception{
		
		return ResponseEntity.status(HttpStatus.OK).body(csvProcessorService.processCsv(sourcePath, destPath));
		
	}
	
	@GetMapping(path = { "/customer_data" })
	public ModelAndView getCustomerData(ModelAndView modelAndView) throws Exception {
		
		modelAndView.addObject("customerDatas", csvProcessorService.processCsv(null, null));
		modelAndView.setViewName("customer_data");
		
		return modelAndView;
		
	}
	
	@GetMapping(path = { "/generate-pdf" })
	public ResponseEntity<String> generatePdf(@RequestParam(name="sourcePath", required=false) String sourcePath, @RequestParam(name="destPath", required=false) String destPath) throws Exception{
		
		return ResponseEntity.status(HttpStatus.OK).body(pdfService.generatePdf());
		
//		return pdfService.generatePdf()
//				? ResponseEntity.status(HttpStatus.OK).body("Output PDF generated!")
//						: ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Some problem occured :(");
		
	}

}
