package com.CustomerData.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.CustomerData.Model.PathData;
import com.CustomerData.Model.SummaryData;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.lowagie.text.DocumentException;

@Component
public class PdfService {
	
	private static final String PDF_RESOURCES = "/pdf-resources/";
    private CsvProcessorService csvProcessorService;
    private SpringTemplateEngine templateEngine;

    @Autowired
    public PdfService(CsvProcessorService csvProcessorService, SpringTemplateEngine templateEngine) {
        this.csvProcessorService = csvProcessorService;
        this.templateEngine = templateEngine;
    }

    public String generatePdf(PathData pathData) throws Exception {
    	
    	validatePathdata(pathData);
    	
        Context context = getContext(pathData);
        String html = loadAndFillTemplate(context);
        
        return renderPdfUsingIText(html, pathData.getDestPdfPath());
//        return renderPdf(html, pathData.getDestPdfPath());
    }
    
    private String renderPdfUsingIText(String html, String destPath) throws IOException {
    	
    	destPath=destPath==null?"C:\\Users\\sr73\\OneDrive - Capgemini\\Documents\\workspace-spring-tool-suite\\CustomerData\\src\\main\\resources\\outputFiles\\Output.pdf":destPath;
    	
    	File pdfFile = new File(destPath);
//        pdfFile.getParentFile().mkdirs();

        // Convert HTML to PDF
        try (OutputStream pdfOutputStream = new FileOutputStream(pdfFile)) {
        	
        	ConverterProperties converterProperties=new ConverterProperties();
        	
            HtmlConverter.convertToPdf(html, pdfOutputStream);
        }
	      
	    return Files.exists(Paths.get(destPath))?destPath:"Error processing pdf :(";
    	
    }

    private String renderPdf(String html, String destPath) throws IOException, DocumentException {
//        File file = Files.createFile(Paths.get("C:\\Users\\sr73\\OneDrive - Capgemini\\Documents\\workspace-spring-tool-suite\\CustomerData\\src\\main\\resources\\outputFiles"));
//        OutputStream outputStream = new FileOutputStream(file);
    	
    	destPath=destPath==null?"C:\\Users\\sr73\\OneDrive - Capgemini\\Documents\\workspace-spring-tool-suite\\CustomerData\\src\\main\\resources\\outputFiles\\Output.pdf":destPath;
        
        OutputStream outputStream=Files.newOutputStream(Paths.get(destPath));
        
        ITextRenderer renderer = new ITextRenderer(20f * 4f / 3f, 20);
        renderer.setDocumentFromString(html, new ClassPathResource(PDF_RESOURCES).getURL().toExternalForm());
        renderer.layout();
        renderer.createPDF(outputStream);
        outputStream.close();
        
        return Files.exists(Paths.get(destPath))?destPath:"Error processing pdf :(";
        
    }

    private Context getContext(PathData pathData) throws Exception {
    	
        Context context = new Context();
        context.setVariable("customerDatas", csvProcessorService.processCustomerData(pathData.getCustomerDataCsv()));
       
        SummaryData summaryData=csvProcessorService.processSummaryData(pathData.getSummaryDataCsv());
		
		context.setVariable("customerName", "BroKoi");
		context.setVariable("summaryYear", summaryData.getSummaryYear());
		context.setVariable("customerAddress", "Lorem ipsum dolor sit amet consectetur\r\n"
				+ "				adipisicing elit. Iusto magnam labore cum fugit tenetur amet quisquam\r\n"
				+ "				id unde maiores consequatur");
		context.setVariable("customerContent", "Lorem ipsum dolor sit amet consectetur");
		context.setVariable("sumReceivedInterest", summaryData.getSumReceivedInterest());
		context.setVariable("sumPreliminaryTax", summaryData.getSumPreliminaryTax());
		context.setVariable("sumPaidInterest", summaryData.getSumPaidInterest());
		
        return context;
        
    }

    private String loadAndFillTemplate(Context context) {
        return templateEngine.process("customer_data", context);
    }
    
    private PathData validatePathdata(PathData pathData) throws FileNotFoundException, IllegalArgumentException, NullPointerException {
    	
    	if(pathData!=null) {
    		
    		if(pathData.getCustomerDataCsv()!=null&&pathData.getSummaryDataCsv()!=null&&pathData.getDestPdfPath()!=null) {
    			
    			if(Files.exists(Paths.get(pathData.getCustomerDataCsv()))&&Files.exists(Paths.get(pathData.getSummaryDataCsv()))) {
    				
    				return pathData;
    				
    			}
    			
    			throw new FileNotFoundException("Some of the input files not found : [ "+ String.join(", ", pathData.getCustomerDataCsv(), pathData.getSummaryDataCsv()) +" ]");
    			
    		}
    		
    		throw new IllegalArgumentException("Some of the input files paths are null : [ "+ String.join(", ", pathData.getCustomerDataCsv(), pathData.getSummaryDataCsv()) +" ]");
    		
    	}
    	
    	throw new NullPointerException("Pathdata is null");
    	
    }
	
}
