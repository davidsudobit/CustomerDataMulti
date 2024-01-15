package com.CustomerData.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.CustomerData.Model.CustomerData;
import com.CustomerData.Model.PathData;
import com.CustomerData.Model.SummaryData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

@Component
public class ITextPdfService {
	
	@Autowired
	private CsvProcessorService csvProcessorService;
	
	private static String destPath="C:\\Users\\sr73\\OneDrive - Capgemini\\Documents\\workspace-spring-tool-suite\\CustomerData\\src\\main\\resources\\outputFiles\\output-itext.pdf";
	
	private List<CustomerData> customerData;
	private SummaryData summaryData;
	
	public String createPdf(PathData pathData) throws Exception {
		
		validatePathdata(pathData);
		loadData(pathData);
		
		PdfWriter pdfWriter=new PdfWriter(Files.newOutputStream(Paths.get(destPath)));
		
		PdfDocument pdfDocument=new PdfDocument(pdfWriter);
		
		Document document=new Document(pdfDocument);
		document.setFont(PdfFontFactory.createFont("C:\\Users\\sr73\\Downloads\\times.ttf"));
		
		addHeaders(document);
		setSubject(document);
		setAllCustomerData(document);
		setSummaryData(document);
		
		document.close();
		
		return Files.exists(Paths.get(destPath))?destPath:"Error in creating PDF :(";

	}
	
	private void addHeaders(Document document) throws IOException {
		
		Table table=new Table(2);
		
		Style cellStyle=new Style();
		cellStyle.setBorder(Border.NO_BORDER);
		
		table.setBorder(Border.NO_BORDER);
		table.setWidth(UnitValue.createPercentValue(100));
		
		Cell imageCell=new Cell();
		Image image=new Image(ImageDataFactory.create("C:\\Users\\sr73\\OneDrive - Capgemini\\Documents\\workspace-spring-tool-suite\\CustomerData\\src\\main\\resources\\static\\images\\logo.png"));
		image.scaleAbsolute(45, 45);
		imageCell.add(image);
		imageCell.addStyle(cellStyle);
		table.addCell(imageCell);
		
		Cell headerContentCell=new Cell();
		String headerContent="Antonio Hoglund\n"+
								"Gustavslundsv 340\n"+
								"44837 Floda";
		headerContentCell.add(new Paragraph(new Text(headerContent)).setWidth(UnitValue.createPercentValue(50)).setHorizontalAlignment(HorizontalAlignment.RIGHT).setFontSize(12));
		headerContentCell.addStyle(cellStyle);
		headerContentCell.setVerticalAlignment(VerticalAlignment.TOP);
		headerContentCell.setTextAlignment(TextAlignment.RIGHT);
//		headerContentCell.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		table.addCell(headerContentCell);
		
		document.add(table);
		
	}
	
	private void setSubject(Document document) {
		
		Paragraph customerName=new Paragraph("BoiKoi "+summaryData.getSummaryYear());
		customerName.setFontSize(16);
		customerName.setBold();
		customerName.setMarginTop(25);
		
		document.add(customerName);

		Style subjectStyle=new Style();
		subjectStyle.setFontSize(10);
		
		document.add(new Paragraph("This post describes how to generate PDF documents").addStyle(subjectStyle));
		
		document.add(new Paragraph("Flying Saucer is a pure-Java library for rendering arbitrary well-formed XML (or XHTML) using CSS 2.1 for layout and formatting").addStyle(subjectStyle));
		
	}
	
	private void setAllCustomerData(Document document) {
		
//		System.out.println(customerData.get(0));
		
		addLines(document, 1);
		
		customerData.forEach(data->{
			
			Table customerDataTable=new Table(2);
			
			customerDataTable.setBorder(Border.NO_BORDER);
			customerDataTable.setWidth(UnitValue.createPercentValue(100));
			
			Style cellStyle=new Style();
			cellStyle.setBorder(Border.NO_BORDER);
			cellStyle.setFontSize(10);
			
			Cell accountName=new Cell();
			accountName.add(new Paragraph(data.getOrganizationName()).setFontSize(12).setBold());
			accountName.setWidth(UnitValue.createPercentValue(50));
			accountName.addStyle(cellStyle);
			
			customerDataTable.addHeaderCell(accountName);
			customerDataTable.addHeaderCell(new Cell().setTextAlignment(TextAlignment.RIGHT).setWidth(UnitValue.createPercentValue(50)).addStyle(cellStyle));
			
			data.getDataHolder().forEach(fieldData->{
				
				if(fieldData.getDataHeader().equalsIgnoreCase("CapitalSharePercentage")) {
					
					Cell dataHeader=new Cell();
					dataHeader.add(new Paragraph(fieldData.getDataHeader()+": "+fieldData.getDataContent()+"%")).addStyle(cellStyle);
					customerDataTable.addCell(dataHeader);
					
					Cell dataContent=new Cell();
					dataContent.add(new Paragraph(data.getBalanceShare()));
					dataContent.setTextAlignment(TextAlignment.RIGHT).addStyle(cellStyle);
					customerDataTable.addCell(dataContent);
					
					return;
					
				}
				else if(fieldData.getDataHeader().equalsIgnoreCase("ReceivedInterestSharePercentage")) {
					
					Cell dataHeader=new Cell();
					dataHeader.add(new Paragraph(fieldData.getDataHeader()+": "+fieldData.getDataContent()+"%")).addStyle(cellStyle);
					dataHeader.setBackgroundColor(ColorConstants.LIGHT_GRAY);
					dataHeader.setItalic();
					dataHeader.setBold();
					dataHeader.setCharacterSpacing(0.5f);
					customerDataTable.addCell(dataHeader);
					
					Cell dataContent=new Cell();
					dataContent.add(new Paragraph(data.getInterestShare()));
					dataContent.setTextAlignment(TextAlignment.RIGHT).addStyle(cellStyle);
					dataContent.setBackgroundColor(ColorConstants.LIGHT_GRAY);
					dataContent.setItalic();
					dataContent.setBold();
//					dataContent.setCharacterSpacing(1);
					customerDataTable.addCell(dataContent);
					
					return;
					
				}
				
				Cell dataHeader=new Cell();
				dataHeader.add(new Paragraph(fieldData.getDataHeader())).addStyle(cellStyle);
				customerDataTable.addCell(dataHeader);
				
				Cell dataContent=new Cell();
				dataContent.add(new Paragraph(fieldData.getDataContent()));
				dataContent.setTextAlignment(TextAlignment.RIGHT).addStyle(cellStyle);
				customerDataTable.addCell(dataContent);
				
			});
			
			customerDataTable.setKeepTogether(true);
			customerDataTable.setMarginBottom(20);
			
			document.add(customerDataTable);
			
		});
		
	}
	
	private void setSummaryData(Document document) {
		
		Table summaryDataTable=new Table(2);
		
		summaryDataTable.setMarginTop(5);
		summaryDataTable.setBorder(Border.NO_BORDER);
		summaryDataTable.setWidth(UnitValue.createPercentValue(100));
		
		Style cellStyle=new Style();
		cellStyle.setBorder(Border.NO_BORDER);
		cellStyle.setFontSize(10);
		
		Cell dataHeader=new Cell();
		dataHeader.add(new Paragraph("SumReceivedInterest")).addStyle(cellStyle);
		dataHeader.setBackgroundColor(ColorConstants.LIGHT_GRAY);
		dataHeader.setItalic();
		dataHeader.setBold();
		dataHeader.setCharacterSpacing(0.5f);
		summaryDataTable.addCell(dataHeader);
		
		Cell dataContent=new Cell();
		dataContent.add(new Paragraph(summaryData.getSumReceivedInterest()));
		dataContent.setTextAlignment(TextAlignment.RIGHT).addStyle(cellStyle);
		dataContent.setBackgroundColor(ColorConstants.LIGHT_GRAY);
		dataContent.setItalic();
		dataContent.setBold();
//		dataContent.setCharacterSpacing(1);
		summaryDataTable.addCell(dataContent);
		
		dataHeader=new Cell();
		dataHeader.add(new Paragraph("SumPreliminaryTax")).addStyle(cellStyle);
		dataHeader.setBackgroundColor(ColorConstants.LIGHT_GRAY);
		dataHeader.setItalic();
		dataHeader.setBold();
		dataHeader.setCharacterSpacing(0.5f);
		summaryDataTable.addCell(dataHeader);
		
		dataContent=new Cell();
		dataContent.add(new Paragraph(summaryData.getSumPreliminaryTax()));
		dataContent.setTextAlignment(TextAlignment.RIGHT).addStyle(cellStyle);
		dataContent.setBackgroundColor(ColorConstants.LIGHT_GRAY);
		dataContent.setItalic();
		dataContent.setBold();
//		dataContent.setCharacterSpacing(1);
		summaryDataTable.addCell(dataContent);
		
		dataHeader=new Cell();
		dataHeader.add(new Paragraph("SumPaidInterest")).addStyle(cellStyle);
		dataHeader.setBackgroundColor(ColorConstants.LIGHT_GRAY);
		dataHeader.setItalic();
		dataHeader.setBold();
		dataHeader.setCharacterSpacing(0.5f);
		summaryDataTable.addCell(dataHeader);
		
		dataContent=new Cell();
		dataContent.add(new Paragraph(summaryData.getSumPaidInterest()));
		dataContent.setTextAlignment(TextAlignment.RIGHT).addStyle(cellStyle);
		dataContent.setBackgroundColor(ColorConstants.LIGHT_GRAY);
		dataContent.setItalic();
		dataContent.setBold();
//		dataContent.setCharacterSpacing(1);
		summaryDataTable.addCell(dataContent);
		
		summaryDataTable.setKeepTogether(true);
		summaryDataTable.setMarginBottom(20);
		
		document.add(summaryDataTable);
		
	}
	
	private void addLines(Document document, int noOfLines) {
		
		for(int i=0;i<noOfLines;i++) {
			
			Paragraph gap=new Paragraph("\n");
			document.add(gap);
			
		}
		
	}
	
	private void loadData(PathData pathData) throws Exception {
		
		this.customerData=csvProcessorService.processCustomerData(pathData.getCustomerDataCsv());
		this.summaryData=csvProcessorService.processSummaryData(pathData.getSummaryDataCsv());
		destPath=pathData.getDestPdfPath();
		
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
