package tuwien.ldlab.statspace.codelist;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import tuwien.ldlab.statspace.model.mediator.StringTetrad;
import tuwien.ldlab.statspace.model.util.QB;

public class CL_Period {
	
	String uk_time = "http://reference.data.gov.uk/id/";	
	String sYear   = "[1-9][0-9]{3}";
	String sMonth  = "[1-9][0-9]{3}-[0-1][0-9]";
	String sQuarter = "[1-9][0-9]{3}-Q[1-4]";
	String sDay   = "[1-9][0-9]{3}-[0-1][0-9]-[0-3][0-9]";
	Pattern pYear = Pattern.compile(sYear);
	Pattern pQuarter = Pattern.compile(sQuarter);
	Pattern pMonth = Pattern.compile(sMonth);
	Pattern pDay = Pattern.compile(sDay);
	
	private ArrayList<StringTetrad> arrCL = new ArrayList<StringTetrad>();	
	
	public CL_Period(){
		arrCL = new ArrayList<StringTetrad>();
		/* year: 1900 - 2100: 201 years
		 * months: * 12
		 * quarter: * 4
		 * day: 1/1 to 31/12 
		 */
		
		int i, j, k, t;
		String year, quarter, month;	
		
		for(i=1800; i<=2100; i++){
			year = uk_time + "gregorian-year/" + Integer.toString(i);
			//tetrad: URI - Label - Type - Upper Level
			arrCL.add(new StringTetrad(year, "Gregorian Year:" + Integer.toString(i), "http://referecnce.data.gov.uk/def/intervals/CalendarYear", null));
			
			for(j=1; j<=4; j++){
				quarter = uk_time + "gregorian-quarter/" + Integer.toString(i) + "-" + "Q" + Integer.toString(j);
				arrCL.add(new StringTetrad(quarter, "Gregorian Quarter:" + Integer.toString(i) + "-" + "Q" + Integer.toString(j), "http://reference.data.gov.uk/def/intervals/CalendarQuarter", year));
				
				for(k=1; k<=3; k++){
					t = (j-1) * 3 + k;
					month = uk_time + "gregorian-month/" + Integer.toString(i) + "-" + Integer.toString(t);
					arrCL.add(new StringTetrad(month, "Gregorian Month:" + Integer.toString(i) + "-" + Integer.toString(t), "http://reference.data.gov.uk/def/intervals/CalendarMonth", quarter));
					arrCL.add(new StringTetrad(month, "Gregorian Month:" + Integer.toString(i) + "-" + Integer.toString(t), "http://reference.data.gov.uk/def/intervals/CalendarMonth", year));
					
//					if(t==1||t==3||t==5||t==7||t==8||t==10||t==12) count=31;
//					else if(t==4||t==6||t==9||t==11) count=30;
//					else if(i%4==0) count=29;
//					else count=28;
//					for(m=1; m<=count; m++){
//						day = uk_time + "gregorian-day/" + Integer.toString(i) + "-" + Integer.toString(t) + "-" + Integer.toString(m);
//						arrCL.add(new StringTetrad(day, "Gregorian Day:" + Integer.toString(i) + "-" + Integer.toString(t) + "-" + Integer.toString(m), "http://reference.data.gov.uk/def/intervals/CalendarDay", month));
//						
//					}					
				}
			}
		}		
	}
	
	public int getSize(){return arrCL.size();}	
	public String getUri(int i){return arrCL.get(i).getFirstString();}	
	public String getLabel(int i){return arrCL.get(i).getSecondString();}	
	public String getType(int i){return arrCL.get(i).getThirdString();}	
	public String getUpperLevel(int i){return arrCL.get(i).getFourthString();}
	
	public void display(){
		for(int i=0; i<arrCL.size(); i++)
			System.out.println(arrCL.get(i).getFirstString() + "\t" + arrCL.get(i).getSecondString()+ "\t" + arrCL.get(i).getThirdString()+"\t" + arrCL.get(i).getFourthString());
	}
	
	public void displayLevels(){
		for(int i=0; i<arrCL.size()/3; i++){
			String uri = arrCL.get(i).getFirstString();
			System.out.println(uri + "\t" + getUpperLevel(i));						
		}
	}
	
	/* Input:  URI represents a temporal value
	 * Output: co-reference URI in the vocabulary
	 */
	public String identifyReference(String uri){
		Matcher m;
		int i;
		String time_value, duration;
		String uri_lowercase = uri.toLowerCase();
			
		if(uri_lowercase.contains("interval")){
			i=uri.length()-1;
			while(i>=0 && uri.charAt(i)!='/') i--;
			if(i>0){
				duration = uri.substring(i+1);
				if(duration.startsWith("P")){
					m = pYear.matcher(uri);
					if(m.find()){
						time_value = uri.substring(m.start(), m.end());	
						return uk_time+"gregorian-interval/"+time_value+"/"+duration;
					}
					return null;
				}
				return null;
			}
			return null;
		}	
		
		if(uri_lowercase.contains("year")){
			m = pYear.matcher(uri);
			if(m.find()){
				time_value = uri.substring(m.start(), m.end());	
				return uk_time+"gregorian-year/"+time_value;
			}
			return null;
		}
		
		if(uri_lowercase.contains("quarter")){
			m = pQuarter.matcher(uri);
			if(m.find()){	
				time_value = uri.substring(m.start(), m.end());
				return uk_time+"gregorian-quarter/"+time_value;
			}
			return null;
		}
		
		if(uri_lowercase.contains("month")){
			m = pMonth.matcher(uri);
			if(m.find()){
				time_value = uri.substring(m.start(), m.end());	
				return uk_time+"gregorian-month/"+time_value;
			}
			return null;
		}
		
		if(uri_lowercase.contains("day")){					
			m = pDay.matcher(uri);
			if(m.find()){		
				time_value = uri.substring(m.start(), m.end());	
				return uk_time+"gregorian-day/"+time_value;
				
			}
			return null;
		 }
				
		m = pDay.matcher(uri);
		if(m.find()){
			time_value = uri.substring(m.start(), m.end());	
			return uk_time+"gregorian-day/"+time_value;
		}
		
		m = pQuarter.matcher(uri);
		if(m.find()){	
			time_value = uri.substring(m.start(), m.end());
			return uk_time+"gregorian-quarter/"+time_value;
		}
			
		m = pMonth.matcher(uri);
		if(m.find()){
			time_value = uri.substring(m.start(), m.end());	
			return uk_time+"gregorian-month/"+time_value;
		}
		
		m = pYear.matcher(uri);
		if(m.find()){
			time_value = uri.substring(m.start(), m.end());	
			return uk_time+"gregorian-year/"+time_value;
		}
		
		return null;
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

		Resource dimension  = mOutput.createResource("http://purl.org/linked-data/sdmx/2009/dimension#refPeriod");
		Resource conceptScheme  = mOutput.createResource("http://statspace.linkedwidgets.org/codelist/cl_period");
		Resource cls  = mOutput.createResource("http://statspace.linkedwidgets.org/codelist/cl_Period");
    	
		dimension.addProperty(RDF.type, QB.DimensionProperty);
		dimension.addProperty(RDF.type, RDF.Property);
		literal = mOutput.createLiteral("Reference Period", "en");
		dimension.addProperty(label, literal);
		literal = mOutput.createLiteral("The period of time or point in time to which the measured observation is intended to refer", "en");
		dimension.addProperty(comment, literal);
		dimension.addProperty(RDFS.isDefinedBy, resource);
		dimension.addProperty(codelist, conceptScheme);
		
		//Class
		cls.addProperty(RDF.type, rdfsClass);
		cls.addProperty(RDF.type, owlClass);
		cls.addProperty(subClassOf, skosConcept);
		literal = mOutput.createLiteral("Code list for Reference Period (PERIOD) - codelist class", "en");
		cls.addProperty(label, literal);
		literal = mOutput.createLiteral("This code list provides code values for periods of time which the measured observation is intended to refer", "en");
		cls.addProperty(comment, literal);
		cls.addProperty(seeAlso, conceptScheme);
		
		//ConceptScheme
		conceptScheme.addProperty(RDF.type, skosConceptScheme);
		literal = mOutput.createLiteral("Code list for Reference Period (PERIOD) - codelist class", "en");
		conceptScheme.addProperty(prefLabel, literal);		
		conceptScheme.addProperty(label, literal);
		conceptScheme.addProperty(notation, "CL_PERIOD");
		literal = mOutput.createLiteral("This code list provides code values for periods of time which the measured observation is intended to refer", "en");
		conceptScheme.addProperty(note, literal);
		conceptScheme.addProperty(definition, resource);
		conceptScheme.addProperty(seeAlso, cls);
		for(i=0; i<arrCL.size(); i++){
			Resource uri = mOutput.createResource(getUri(i));
			Resource type = mOutput.createResource(getType(i));
			uri.addProperty(RDF.type, skosConcept);			
			uri.addProperty(RDF.type, cls);
			uri.addProperty(RDF.type, type);
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
		}
		
		FileOutputStream out;
		try {
			out = new FileOutputStream("data/codelist/cl_period.ttl");
			mOutput.write(out, "Turtle", null);		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
