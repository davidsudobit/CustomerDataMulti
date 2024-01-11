package com.CustomerData.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

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

    public String generatePdf() throws Exception {
        Context context = getContext();
        String html = loadAndFillTemplate(context);
        return renderPdf(html);
    }


    private String renderPdf(String html) throws IOException, DocumentException {
//        File file = Files.createFile(Paths.get("C:\\Users\\sr73\\OneDrive - Capgemini\\Documents\\workspace-spring-tool-suite\\CustomerData\\src\\main\\resources\\outputFiles"));
//        OutputStream outputStream = new FileOutputStream(file);
        
        OutputStream outputStream=Files.newOutputStream(Paths.get("C:\\Users\\sr73\\OneDrive - Capgemini\\Documents\\workspace-spring-tool-suite\\CustomerData\\src\\main\\resources\\outputFiles\\Output.pdf"));
        
        ITextRenderer renderer = new ITextRenderer(20f * 4f / 3f, 20);
        renderer.setDocumentFromString(html, new ClassPathResource(PDF_RESOURCES).getURL().toExternalForm());
        renderer.layout();
        renderer.createPDF(outputStream);
        outputStream.close();
        
        return Files.exists(Paths.get("C:\\Users\\sr73\\OneDrive - Capgemini\\Documents\\workspace-spring-tool-suite\\CustomerData\\src\\main\\resources\\outputFiles\\Output.pdf"))?"C:\\Users\\sr73\\OneDrive - Capgemini\\Documents\\workspace-spring-tool-suite\\CustomerData\\src\\main\\resources\\outputFiles\\Output.pdf":"Error processing pdf :(";
        
    }

    private Context getContext() throws Exception {
        Context context = new Context();
        context.setVariable("customerDatas", csvProcessorService.processCsv(null, null));
        return context;
    }

    private String loadAndFillTemplate(Context context) {
        return templateEngine.process("customer_data", context);
    }
	
}
