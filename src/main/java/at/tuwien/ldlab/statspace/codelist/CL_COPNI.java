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

public class CL_COPNI {
	private ArrayList<StringTriple> arrCL;
	
	public CL_COPNI(){
		String fileName = "data/sdmx/CL_COPNI_1999_1.0_1-7-2014.xlsx";
		arrCL = new ArrayList<StringTriple>();
		
		try{
			InputStream inStream = new FileInputStream(fileName);
			org.apache.poi.ss.usermodel.Workbook wb = WorkbookFactory.create(inStream);
			org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
			String code="", label="", level="";
			int count;
			Iterator<Row> rowIterator = sheet.iterator(); 
			Row row = rowIterator.next(); //skip the title
			while (rowIterator.hasNext()) { 
				
				row = rowIterator.next();
	        	Iterator<Cell> cellIterator = row.cellIterator(); 
	        	count=0;
	        	while (cellIterator.hasNext()) {
	        		count++;
	        		if(count>3) {
	        			arrCL.add(new StringTriple(code, label, level));
	        			break;
	        		}
	        		Cell cell = cellIterator.next();
	        		if(count==1) code = "http://statspace.linkedwidgets.org/codelist/cl_copni/"+row.getCell(0).getStringCellValue();
	        		else if(count==2) label = cell.getStringCellValue();
	        		else {
	        			level = cell.getStringCellValue();
	        			level = level.replace("Hierarchical level", "").trim();
	        		}	
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
			System.out.println(arrCL.get(i).getFirstString() + "\t" + arrCL.get(i).getSecondString()+ "\t" + arrCL.get(i).getThirdString());
	}
	
	public String getUpperLevel(int i){	
		int level = Integer.parseInt(arrCL.get(i).getThirdString());
		String uri, uriUpper="";
		if(level>1){
			uri = arrCL.get(i).getFirstString();
			uriUpper = uri.substring(0, uri.length()-1);
			for(int j=0; j<arrCL.size(); j++){
				if(arrCL.get(j).getFirstString().equalsIgnoreCase(uriUpper))
					return uriUpper;
			}				
		}			
		return null;
	}
	
	public void displayLevels(){
		for(int i=0; i<arrCL.size()/3; i++){
			String uri = arrCL.get(i).getFirstString();
			System.out.println(uri + "\t" + getUpperLevel(i));						
		}
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
		Property narrower = mOutput.createProperty(skos+"narrower");
		Property broader = mOutput.createProperty(skos+"broader");
		Property label = mOutput.createProperty(rdfs+"label");	
		Property subClassOf = mOutput.createProperty(rdfs+"subClassOf");	
		Property seeAlso = mOutput.createProperty(rdfs+"seeAlso");		
		Property comment = mOutput.createProperty(rdfs+"comment");
		
		mOutput.setNsPrefix("skos", skos);	
		mOutput.setNsPrefix("qb", qb);	
		mOutput.setNsPrefix("rdfs", rdfs);
		
		
		int i;
		Literal literal;
		
		Resource dimension  = mOutput.createResource("http://statspace.linkedwidgets.org/dimension/expenditure");
		Resource conceptScheme  = mOutput.createResource("http://statspace.linkedwidgets.org/codelist/cl_copni");
		Resource cls  = mOutput.createResource("http://statspace.linkedwidgets.org/codelist/cl_Copni");
    	
		dimension.addProperty(RDF.type, QB.DimensionProperty);
		dimension.addProperty(RDF.type, RDF.Property);
		literal = mOutput.createLiteral("Expenditure", "en");
		dimension.addProperty(label, literal);
		literal = mOutput.createLiteral("Expenditure", "en");
		dimension.addProperty(comment, literal);
		dimension.addProperty(RDFS.isDefinedBy, resource);
		dimension.addProperty(codelist, conceptScheme);
		
		//Class
		cls.addProperty(RDF.type, rdfsClass);
		cls.addProperty(RDF.type, owlClass);
		cls.addProperty(subClassOf, skosConcept);
		literal = mOutput.createLiteral("Code list for Purposes of Non-Profit Institutions Serving Households (COPNI) - codelist class", "en");
		cls.addProperty(label, literal);
		literal = mOutput.createLiteral("This code list provides a set of building blocks to be used for classifying individual outlays of Non-Profit Institutions Serving Households  (NPISHs) according to the purpose they serve", "en");
		cls.addProperty(comment, literal);
		cls.addProperty(seeAlso, conceptScheme);
		
		//ConceptScheme
		conceptScheme.addProperty(RDF.type, skosConceptScheme);
		literal = mOutput.createLiteral("Code list for Purposes of Non-Profit Institutions Serving Households (COPNI) - codelist class", "en");
		conceptScheme.addProperty(prefLabel, literal);		
		conceptScheme.addProperty(label, literal);
		conceptScheme.addProperty(notation, "CL_COPNI");
		literal = mOutput.createLiteral("This code list provides a set of building blocks to be used for classifying individual outlays of Non-Profit Institutions Serving Households  (NPISHs) according to the purpose they serve", "en");
		conceptScheme.addProperty(note, literal);
		conceptScheme.addProperty(definition, resource);
		conceptScheme.addProperty(seeAlso, cls);
		for(i=0; i<arrCL.size(); i++){
			Resource uri = mOutput.createResource(getUri(i));			
			uri.addProperty(RDF.type, skosConcept);			
			uri.addProperty(RDF.type, cls);
			String upperUri = getUpperLevel(i);
			if(upperUri==null){
				uri.addProperty(topConceptOf, conceptScheme);
				conceptScheme.addProperty(hasTopConceptOf, uri);	
			}else{
				Resource upperLevel = mOutput.createResource(upperUri);
				upperLevel.addProperty(narrower, uri);
				uri.addProperty(broader, upperLevel);
			}
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
			out = new FileOutputStream("data/code/cl_copni.ttl");
			mOutput.write(out, "Turtle", null);		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}	
}
