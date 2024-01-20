package com.CustomerData.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.CustomerData.Model.PathData;
import com.CustomerData.Service.CsvProcessorService;

@Controller
@RequestMapping(path = { "/csv" })
public class CsvProcessingController {
	
	@Autowired
	private CsvProcessorService csvProcessorService;
	
	@GetMapping(path = { "/generate-pdf" })
	public ResponseEntity<String> processData(@RequestBody(required = false) PathData pathData) throws Exception{
		
		csvProcessorService.generatePdf(pathData);
		return ResponseEntity.status(HttpStatus.OK).body("Started");
		
	}
	
}
