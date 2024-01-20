package com.CustomerData.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import com.CustomerData.Model.CustomerData;
import com.CustomerData.Model.PathData;
import com.CustomerData.Model.SummaryData;
import com.itextpdf.html2pdf.HtmlConverter;

@Component
public class PdfService {
	
    private SpringTemplateEngine templateEngine;

    @Autowired
    public PdfService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String generatePdf(PathData pathData, CustomerData customerData) throws Exception {
    	
        Context context = getContext(pathData, customerData);
        String html = loadAndFillTemplate(context);
        String pdfName=String.format("%s_%s.pdf", customerData.getSsn(),customerData.getSummaryData().getSummaryYear());
        
        return renderPdfUsingIText(html, pathData.getDestPdfPath()+pdfName);
        
    }
    
    private String renderPdfUsingIText(String html, String destPath) throws IOException {
    	
    	File pdfFile = new File(destPath);

        // Convert HTML to PDF
        try (OutputStream pdfOutputStream = new FileOutputStream(pdfFile)) {
        	
//        	ConverterProperties converterProperties=new ConverterProperties();
        	
            HtmlConverter.convertToPdf(html, pdfOutputStream);
            
        }
	      
	    return Files.exists(Paths.get(destPath))?destPath:"Error processing pdf :(";
    	
    }

    private Context getContext(PathData pathData, CustomerData customerData) throws Exception {
    	
        Context context = new Context();
        context.setVariable("customerDatas", customerData.getAccountData());
       
        SummaryData summaryData=customerData.getSummaryData();
		
		context.setVariable("customerName", customerData.getCustomerName());
		context.setVariable("summaryYear", summaryData.getSummaryYear());
		context.setVariable("customerCareOfAddress", customerData.getCareOfAddress());
		context.setVariable("customerStreetName", customerData.getStreetName());
		context.setVariable("customerCity", customerData.getCity());
		context.setVariable("customerPincode", customerData.getPincode());
		context.setVariable("customerContent", "Lorem ipsum dolor sit amet consectetur");
		context.setVariable("sumReceivedInterest", summaryData.getSumReceivedInterest());
		context.setVariable("sumPreliminaryTax", summaryData.getSumPreliminaryTax());
		context.setVariable("sumPaidInterest", summaryData.getSumPaidInterest());
		
        return context;
        
    }

    private String loadAndFillTemplate(Context context) {
        return templateEngine.process("customer_data", context);
    }
	
}
