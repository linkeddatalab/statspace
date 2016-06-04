package tuwien.ldlab.statspace.codelist;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import tuwien.ldlab.statspace.model.mediator.StringTriple;
import tuwien.ldlab.statspace.model.util.QB;

public class CL_Area {	
	private ArrayList<StringTriple> arrCL = new ArrayList<StringTriple>();	

	public CL_Area(){		
		BufferedReader br = null;
		String line, iso2Code, iso3Code, name;
		
		try{			
			br = new BufferedReader(new InputStreamReader(new FileInputStream("data/countries.csv"), "UTF-8"));		
			while ((line = br.readLine()) != null) {
				name = line.substring(0, line.indexOf("\t"));
				name = name.replace(" ", "");
				line = line.substring(line.indexOf("\t")+1);	
				iso2Code = line.substring(0, line.indexOf("\t"));
				line = line.substring(line.indexOf("\t")+1);	
				iso3Code = line.trim();														
				arrCL.add(new StringTriple(iso2Code, iso3Code, name));								
			}		
		}catch(Exception e){				
		}			
	}
	
	public CL_Area(String sPath){		
		BufferedReader br = null;
		String line, iso2Code, iso3Code, name;
		
		try{			
			br = new BufferedReader(new InputStreamReader(new FileInputStream(sPath), "UTF-8"));		
			while ((line = br.readLine()) != null) {
				name = line.substring(0, line.indexOf("\t"));
				name = name.replace(" ", "");
				line = line.substring(line.indexOf("\t")+1);	
				iso2Code = line.substring(0, line.indexOf("\t"));
				line = line.substring(line.indexOf("\t")+1);	
				iso3Code = line.trim();														
				arrCL.add(new StringTriple(iso2Code, iso3Code, name));								
			}		
		}catch(Exception e){				
		}			
	}
	

	public CL_Area(boolean bStatus){		
		BufferedReader br = null;
		String line, iso2Code, iso3Code, name, file;
		if(bStatus) file = "data/WBcountries.csv";
		else file = "data/ISOcountries.csv";
		try{			
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));		
			while ((line = br.readLine()) != null) {
				name = line.substring(0, line.indexOf("\t"));	
				line = line.substring(line.indexOf("\t")+1);	
				iso2Code = line.substring(0, line.indexOf("\t"));
				line = line.substring(line.indexOf("\t")+1);	
				iso3Code = line.trim();														
				arrCL.add(new StringTriple(iso2Code, iso3Code, name));								
			}		
		}catch(Exception e){				
		}			
	}
	
	public StringTriple getCountry(int i){
		return arrCL.get(i);
	}	
		
	public int getSize(){
		return arrCL.size();
	}	
	
	public void display(){		
		for(int i=0; i<arrCL.size(); i++)
			arrCL.get(i).display();
	}
	
	public String getIso2Code(int i){
		return arrCL.get(i).getFirstString();
	}
	
	public String getIso3Code(int i){
		return arrCL.get(i).getSecondString();
	}
	
	public String getCountryName(int i){		
		return arrCL.get(i).getThirdString();
	}
	
	public String getUri(int i){
		return "http://statspace.linkedwidgets.org/codelist/cl_area/"+arrCL.get(i).getThirdString();
	}
	
	public String getLabel(int i){		
		return arrCL.get(i).getThirdString();
	}
	
	public String getCountryNameByISO(String iso){
		int i;
		for(i=0; i<arrCL.size(); i++)
			if(arrCL.get(i).getFirstString().equalsIgnoreCase(iso) || arrCL.get(i).getSecondString().equalsIgnoreCase(iso))
				return arrCL.get(i).getThirdString();
		return null;
	}
	public String getCountryName(String s){		
		if(isCountry(s))
			return s;
		
		String[] list = s.split(" ");
		int i;
		for(i=0; i<list.length; i++)
			if(isCountry(list[i]))
				return list[i];
		return "";
	}
	
	public boolean isCountry(String s){
		int i;
		String c;
		for(i=0; i<arrCL.size(); i++){			
			c = arrCL.get(i).getThirdString();			
			if(c.equalsIgnoreCase(s))
				return true;
			else if(c.contains(",")){
				c = c.substring(0, c.indexOf(","));
				if(c.equalsIgnoreCase(s))
					return true;
			}else if(c.contains("(")){
				c = c.substring(c.indexOf("(")+1, c.indexOf(")"));
				if(c.equalsIgnoreCase(s))
					return true;
			}			
		}
		return false;
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

		Resource dimension  = mOutput.createResource("http://purl.org/linked-data/sdmx/2009/dimension#refArea");
		Resource conceptScheme  = mOutput.createResource("http://statspace.linkedwidgets.org/codelist/cl_area");
		Resource cls  = mOutput.createResource("http://statspace.linkedwidgets.org/codelist/cl_Area");
    	
		dimension.addProperty(RDF.type, QB.DimensionProperty);
		dimension.addProperty(RDF.type, RDF.Property);
		literal = mOutput.createLiteral("Reference Area", "en");
		dimension.addProperty(label, literal);
		literal = mOutput.createLiteral("The country or geographic area to which the measured statistical phenomenon relates", "en");
		dimension.addProperty(comment, literal);
		dimension.addProperty(RDFS.isDefinedBy, resource);
		dimension.addProperty(codelist, conceptScheme);
		
		//Class
		cls.addProperty(RDF.type, rdfsClass);
		cls.addProperty(RDF.type, owlClass);
		cls.addProperty(subClassOf, skosConcept);
		literal = mOutput.createLiteral("Code list for Reference Area (AREA) - codelist class", "en");
		cls.addProperty(label, literal);
		literal = mOutput.createLiteral("This code list provides code values for geographical areas, defined as areas included within the borders of a country, region, group of countries, etc.", "en");
		cls.addProperty(comment, literal);
		cls.addProperty(seeAlso, conceptScheme);
		
		//ConceptScheme
		conceptScheme.addProperty(RDF.type, skosConceptScheme);
		literal = mOutput.createLiteral("Code list for Reference Area (AREA) - codelist class", "en");
		conceptScheme.addProperty(prefLabel, literal);		
		conceptScheme.addProperty(label, literal);
		conceptScheme.addProperty(notation, "CL_AREA");
		literal = mOutput.createLiteral("This code list provides code values for geographical areas, defined as areas included within the borders of a country, region, group of countries, etc.", "en");
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
		}
		
		FileOutputStream out;
		try {
			out = new FileOutputStream("data/codelist/cl_area.ttl");
			mOutput.write(out, "Turtle", null);		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}


	public String getIso2CodeByCountryName(String sCountry) {
		int i;
		for(i=0; i<arrCL.size(); i++)
			if(arrCL.get(i).getThirdString().replaceAll("\\s", "").equalsIgnoreCase(sCountry))
				return arrCL.get(i).getFirstString();	
		return "";
	}	
}
