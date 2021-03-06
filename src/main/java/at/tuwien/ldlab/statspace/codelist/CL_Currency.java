package at.tuwien.ldlab.statspace.codelist;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import at.tuwien.ldlab.statspace.metadata.StringTriple;
import at.tuwien.ldlab.statspace.util.QB;

public class CL_Currency {
	private ArrayList<StringTriple> arrCL;
	
	public CL_Currency(){		
		String fileName = "data/sdmx/ISOCurrencyCodes081507.xls";
		arrCL = new ArrayList<StringTriple>();
		
		try{
			InputStream inStream = new FileInputStream(fileName);
			org.apache.poi.ss.usermodel.Workbook wb = WorkbookFactory.create(inStream);
			org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
			String code="", label="";
			int count;
			Iterator<Row> rowIterator = sheet.iterator(); 
			Row row = rowIterator.next(); //skip the title
			while (rowIterator.hasNext()) { 
				row = rowIterator.next();
	        	Iterator<Cell> cellIterator = row.cellIterator(); 
	        	count=0;
	        	while (cellIterator.hasNext()) {
	        		count++;
	        		if(count==3) {
	        			arrCL.add(new StringTriple(code, label,"1"));
	        			break;
	        		}
	        		Cell cell = cellIterator.next();
	        		if(count==1) code = "http://statspace.linkedwidgets.org/codelist/cl_currency/"+row.getCell(0).getStringCellValue();
	        		else label = cell.getStringCellValue();
	        		
	        	}         
		    }
			wb.close();
		} catch (IOException | EncryptedDocumentException | InvalidFormatException e) {
			e.printStackTrace();
		}				
		
	}
	
	public String getUri(int i){return arrCL.get(i).getFirstString();}
	public String getLabel(int i){return arrCL.get(i).getSecondString();}
	public String getLevel(int i){return arrCL.get(i).getThirdString();}
	public int getSize(){return arrCL.size();}	
	public void display(){
		for(int i=0; i<arrCL.size(); i++)
			System.out.println(arrCL.get(i).getFirstString() + "\t" + arrCL.get(i).getSecondString());
	}
	
	/* Input:  (URI, label) represents a value
	 * Output: co-reference URI in the vocabulary
	 */
	
	public String identifyReference(String uri, String label){
		int i;
		
		//compare uri
		if(uri!=null && !uri.isEmpty())
			for(i=0; i<arrCL.size(); i++)
				if(uri.equalsIgnoreCase(arrCL.get(i).getFirstString()))
					return arrCL.get(i).getFirstString();
					
		//compare label
		if(label!=null && !label.isEmpty())
			for(i=0; i<arrCL.size(); i++)
				if(label.equalsIgnoreCase(arrCL.get(i).getSecondString()))
					return arrCL.get(i).getFirstString();
		
		//compare code
		if(uri!=null && !uri.isEmpty()){			
			String code="";
			code = extractCode(uri);
			if(!code.isEmpty()){		
				for(i=0; i<arrCL.size(); i++)
					if(code.equalsIgnoreCase(extractCode(arrCL.get(i).getFirstString())))
						return arrCL.get(i).getFirstString();
			}
		}
		
		//not found
		return null;	
	}
	
	public String extractCode(String s){
		int i = s.length()-1;
		while(i>0 && s.charAt(i)!='/') i--;
		if(i>0)
			return s.substring(i+1);
		else
			return "";
	}
	
	//create RDF triples
	public void generateCodeList(){
		String qb = "http://purl.org/linked-data/cube#";	
		String skos = "http://www.w3.org/2004/02/skos/core#";
		String owl = "http://www.w3.org/2002/07/owl#";
		String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
		Model mOutput = ModelFactory.createDefaultModel();	
		
		//Resources
		Resource skosConceptScheme  = mOutput.createResource(skos+"ConceptScheme");
		Resource skosConcept  = mOutput.createResource(skos+"Concept");
		Resource owlClass  = mOutput.createResource(owl+"Class");
		Resource rdfsClass  = mOutput.createResource(rdfs+"Class");
		Resource resource  = mOutput.createResource("https://sdmx.org/wp-content/uploads/SDMX_Glossary_Version_1_0_February_2016.pdf");
		
		//Properties
		Property codelist = mOutput.createProperty(qb+"codeList");
		Property prefLabel = mOutput.createProperty(skos+"prefLabel");
		Property notation = mOutput.createProperty(skos+"notation");
		Property note = mOutput.createProperty(skos+"note");
		Property definition = mOutput.createProperty(skos+"definition");
		Property hasTopConceptOf = mOutput.createProperty(skos+"hasTopConcept");
		Property topConceptOf = mOutput.createProperty(skos+"topConceptOf");
		Property inScheme = mOutput.createProperty(skos+"inScheme");	
		Property label = mOutput.createProperty(rdfs+"label");	
		Property subClassOf = mOutput.createProperty(rdfs+"subClassOf");	
		Property seeAlso = mOutput.createProperty(rdfs+"seeAlso");		
		Property comment = mOutput.createProperty(rdfs+"comment");
		
		mOutput.setNsPrefix("skos", skos);	
		mOutput.setNsPrefix("qb", qb);	
		mOutput.setNsPrefix("rdfs", rdfs);
		
		
		int i;
		Literal literal;			

		Resource dimension  = mOutput.createResource("http://purl.org/linked-data/sdmx/2009/dimension#currency");
		Resource conceptScheme  = mOutput.createResource("http://statspace.linkedwidgets.org/codelist/cl_currency");
		Resource cls  = mOutput.createResource("http://statspace.linkedwidgets.org/codelist/cl_Currency");
    	
		dimension.addProperty(RDF.type, QB.DimensionProperty);
		dimension.addProperty(RDF.type, RDF.Property);
		literal = mOutput.createLiteral("Currency", "en");
		dimension.addProperty(label, literal);
		literal = mOutput.createLiteral("Currency", "en");
		dimension.addProperty(comment, literal);
		dimension.addProperty(codelist, conceptScheme);
		dimension.addProperty(RDFS.isDefinedBy, resource);
		
		//Class
		cls.addProperty(RDF.type, rdfsClass);
		cls.addProperty(RDF.type, owlClass);
		cls.addProperty(subClassOf, skosConcept);
		literal = mOutput.createLiteral("Code list for Currency (CURRENCY) - codelist class", "en");
		cls.addProperty(label, literal);
		literal = mOutput.createLiteral("This code list provides values for currencies", "en");
		cls.addProperty(comment, literal);
		cls.addProperty(seeAlso, conceptScheme);
		
		//ConceptScheme
		conceptScheme.addProperty(RDF.type, skosConceptScheme);
		literal = mOutput.createLiteral("Code list for Currency (CURRENCY) - codelist class", "en");
		conceptScheme.addProperty(prefLabel, literal);		
		conceptScheme.addProperty(label, literal);
		conceptScheme.addProperty(notation, "CL_CURRENCY");
		literal = mOutput.createLiteral("This code list provides values for currencies", "en");
		conceptScheme.addProperty(note, literal);
		conceptScheme.addProperty(definition, resource);
		conceptScheme.addProperty(seeAlso, cls);
		for(i=0; i<arrCL.size(); i++){
			Resource uri = mOutput.createResource(getUri(i));			
			uri.addProperty(RDF.type, skosConcept);			
			uri.addProperty(RDF.type, cls);
			uri.addProperty(topConceptOf, conceptScheme);
			conceptScheme.addProperty(hasTopConceptOf, uri);	
			uri.addProperty(inScheme, conceptScheme);			
			literal = mOutput.createLiteral(getLabel(i), "en");
			uri.addProperty(prefLabel, literal);		
			String s = extractCode(getUri(i));
			if(!s.isEmpty()){
				literal = mOutput.createLiteral(s);
				uri.addProperty(notation, literal);
			}
		}
		
		FileOutputStream out;
		try {
			out = new FileOutputStream("data/code/cl_currency.ttl");
			mOutput.write(out, "Turtle", null);		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}	
}
