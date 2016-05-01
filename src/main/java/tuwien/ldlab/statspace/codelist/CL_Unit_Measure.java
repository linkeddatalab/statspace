package tuwien.ldlab.statspace.codelist;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import tuwien.ldlab.statspace.model.mediator.StringCouple;
import tuwien.ldlab.statspace.model.util.QB;

public class CL_Unit_Measure {
	private ArrayList<StringCouple> arrCL;
	
	public CL_Unit_Measure(){
		arrCL = new ArrayList<StringCouple>();		
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/USD","USD"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/EUR","EUR"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/CN","Local currency"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/KN","Constant Local currency"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/FE","Female"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/MA","Male"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/P1","Person"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/P2","People, 100"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/P3","People, 1000"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/P5","People, 100000"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/P6","People, 1000000"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/ZS","Percentage %"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/HA","Hectares"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/K1","Kilometers"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/K2","Square Kilometers"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/K3","Cubic Kilometers"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/K6","Million Tonne/Passenger-Kilometers"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/MM","Milimeters"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/M1","Meters"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/M2","Square Meters"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/M3","Cubic Meters"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/K6","Million Ton-Kilometer"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/MC","Micrograms"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/GR","Gram"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/KG","Kilograms"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/MT","Metricton"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/KT","Kiloton"));		
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/NO","Number"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/KWH","Kilowatt hour"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/W1","Watt"));		
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/W3","Kilowatt"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/W6","Megawatt"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/W9","Gigawatt"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/DD","Days"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/YY","Years"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/WW","Weeks"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/MM","Months"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/HH","Hours"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/TU","Twenty-Foot Equivalent Unit"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/PPS","Purchasing Power Standard"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/J1","Joule"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/J9","Gigajoule"));
		arrCL.add(new StringCouple("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/J12","Terajoule"));
	}
	
	public String getUri(int i){return arrCL.get(i).getFirstString();}
	public String getLabel(int i){return arrCL.get(i).getSecondString();}
	public int getSize(){return arrCL.size();}	
	public void display(){
		for(int i=0; i<arrCL.size(); i++)
			System.out.println(arrCL.get(i).getFirstString() + "\t" + arrCL.get(i).getSecondString());
	}
	
	public String getDefaultUnit(){
		return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/NO";
	}
	
	
	public String identifyReference(String uri, String label){
		String unit="", scale;
		String part1, part2, unit1, unit2;
		int i, j;
		
		if(label.contains("per ")){
			i = label.indexOf("per ");
			if(label.contains("per capita")|| 
					label.contains("per worker") || 
					label.contains("per person") || 
					label.contains("per labor")|| 
					label.contains("per inhabitant")){
				unit2=null;
			}else{				
				part2 = label.substring(i+4).trim();
				unit2 = identifyReference("", part2);				
			}
			j = label.indexOf("(", i);
			if(j!=-1)
				part1 = label.substring(0, i).trim() + label.substring(j+1);
			else
				part1 = label.substring(0, i).trim() ;
			unit1 = identifyReference("", part1);
			if(!unit1.endsWith("ZS")){				
				if(unit2==null) return unit1;
				else return unit1+"/"+getAttributeCode(unit2);
			}	
		}else {
			if(label.contains("/")){		
				i = label.indexOf("/");
				part1 = label.substring(0, i).trim();
				part2 = label.substring(i+1).trim();
				unit1 = identifyReference("", part1);
				unit2 = identifyReference("", part2);
				if(!unit1.endsWith("NO") && !unit1.endsWith("ZS") && !unit2.endsWith("NO") && !unit2.endsWith("ZS")){
					unit = unit1+"/"+getAttributeCode(unit2);
				}
			}	
			else if(label.contains("-")&& !label.contains(" -")){		
				i = label.indexOf("-");
				part1 = label.substring(0, i).trim();
				part2 = label.substring(i+1).trim();
				unit1 = identifyUnit("", part1);
				unit2 = identifyUnit("", part2);
				if(!unit1.endsWith("NO") && !unit2.endsWith("NO") && !unit2.endsWith("ZS")){
					scale = identifyScale(uri, label);
					if(scale!=null) unit = unit1+"-"+getAttributeCode(unit2)+"."+scale;
					else unit = unit1+"-"+getAttributeCode(unit2);
					if(unit.endsWith("MT-K1.6"))
						return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/K6";
				}
			}			
		}
		if(unit.isEmpty()){
			unit = identifyUnit(uri, label);
			
			if(unit.endsWith("ZS")||unit.endsWith("NO")||unit.endsWith("K6"))
				scale=null;
			else
				scale = identifyScale(uri, label);
			
			if(scale!=null){
				if(unit.endsWith("P1")){
					if(scale.equals("2")) unit = unit.substring(0,  unit.length()-2) +  "P2";
					else if(scale.equals("3")) unit = unit.substring(0,  unit.length()-2) +  "P3";
					else if(scale.equals("5")) unit = unit.substring(0,  unit.length()-2) +  "P5";
					else if(scale.equals("6")) unit = unit.substring(0,  unit.length()-2) +  "P6";
				}
				else 
					unit = unit + "." + scale;
			}
		}
		return unit;
	}
	
	
	public String identifyUnit(String uri, String label){
		/* special unit  .CD => USD		  				
		  				 .KM => K1
		*/
		if(uri.endsWith(".CD"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/USD";
		if(uri.endsWith(".KM"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/K1";
		
		label = label.toLowerCase();
		if(label.contains("percentage")||label.contains("%"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/ZS";	
		else if(label.contains("$"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/USD";
		else if(label.contains("€")||(label.contains("euro")&&!label.contains("europ")))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/EUR";
		else if(label.contains("constant local currency")||label.contains("constant lcu"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/KN";
		else if(label.contains("local currency")||label.contains("national currency")||label.contains("current lcu"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/CN";
		else if(label.contains("years"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/YY";
		else if(label.contains("female"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/FE";
		else if(label.contains("male"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/MA";
		else if(label.contains("people") || label.contains("person") || label.contains("worker") || label.contains("population") || label.contains("migration") || label.contains("migrant") || label.contains("labor")|| label.contains("births")|| label.contains("adults"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/P1";
		else if(label.contains("hectare"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/HA";
		else if(label.contains("million") && label.contains("tonne") && (label.contains("kilometre")||label.contains("kilometer")))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/K6";
		else if(label.contains("cubic kilometer")||label.contains("cubic kilometre"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/K3";
		else if(label.contains("square kilometer")||label.contains("square kilometre")||label.contains("sq. km"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/K2";
		else if(label.contains("kilometer")||label.contains("kilometre"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/K1";
		else if(label.contains("kilogram")||label.contains("kg"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/KG";
		else if(label.contains("microgram")||label.contains("ug"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/MC";
		else if(label.contains("gram"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/GR";
		else if(label.contains("metric ton")||label.contains("tonne"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/MT";
		else if(label.contains("kiloton")||label.contains("kt"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/KT";
		else if(label.contains("milimeter")||label.contains("mm"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/MM";
		else if(label.contains("cubic meter")||label.contains("cubic metre"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/M3";
		else if(label.contains("square meter")||label.contains("square metre"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/M2";
		else if(label.contains(" meter")||label.contains(" metre"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/M1";
		else if(label.contains("gigawatt"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/W9";	
		else if(label.contains("megawatt"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/W6";
		else if((label.contains("kilowatt") && label.contains("hour")) || label.contains("kwh"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/KWH";
		else if(label.contains("kilowatt")||label.contains("kw"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/W3";	
		else if(label.contains("watt"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/W1";	
		else if(label.contains("twienty-foot"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/TU";		
		else if(label.contains("purchasing power standard"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/PPS";		
		else if(label.contains("terajoule"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/J12";
		else if(label.contains("gigajoule"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/J9";
		else if(label.contains("joule"))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/J1";		
		else if(label.contains("years")||(label.contains("year") && label.length()<6))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/YY";
		else if(label.contains("months")||(label.contains("month") && label.length()<7))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/MM";
		else if(label.contains("weeks")||(label.contains("week") && label.length()<6))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/WW";
		else if(label.contains("days")||(label.contains("day") && label.length()<5))
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/DD";
		else if(label.contains("hours")||(label.contains("hour") && label.length()<6)) //mm per hour) 
			return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/HH";
		
		int i=uri.length()-1;
		while(i>0 && uri.charAt(i)!='/') i--;
		if(i>0){
			uri = uri.substring(i+1);
			uri = uri.replace("unit#", "");
			i=0;
			while(i<uri.length() && uri.charAt(i)>='0' && uri.charAt(i)<='9') i++;
			uri = uri.substring(i);
			if(uri.equalsIgnoreCase("T"))
				return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/MT";
			else if(uri.equalsIgnoreCase("KT")) 
				return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/KT";
			else if(uri.equalsIgnoreCase("KG")) 
				return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/KG";
			else if(uri.equalsIgnoreCase("GR")) 
				return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/GR";
		}
	
		return "http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/NO";
	}
	
	public String identifyScale(String uri, String label){
		
		if(label.isEmpty()){
			int i=uri.length()-1;
			while(i>0 && uri.charAt(i)!='/') i--;
			if(i>0){
				uri = uri.substring(i+1);
				uri = uri.replace("unit#", "");
				label = uri;
			}
		}
	
		label = label.toLowerCase();	
		if(label.contains("more than"))
			return null;
		
		if(label.contains("1000000000") || label.contains("1,000,000,000") || label.contains("1.000.000.000") || label.contains("billion"))
			return "9";
		if(label.contains("100000000") || label.contains("100,000,000") || label.contains("100.000.000"))
			return "8";
		if(label.contains("10000000") || label.contains("10,000,000") || label.contains("10.000.000"))
			return "7";
		if(label.contains("1000000") || label.contains("1,000,000") || label.contains("1.000.000") || label.contains("million"))
			return "6";
		if(label.contains("100000") || label.contains("100,000") || label.contains("100.000"))
			return "5";
		if(label.contains("10000") || label.contains("10,000") || label.contains("10.000"))
			return "4";
		if(label.contains("1000") || label.contains("1,000") || label.contains("1.000") ||  label.equals("1000") || label.contains("thousand"))
			return "3";
		if((label.contains("100 ") || label.contains(" 100") ||  label.equals("100")|| label.contains("hundred")) && !label.contains("=100"))
			return "2";
		if((label.contains(" 10") || label.contains(" ten ")) && !label.contains("=10"))
			return "1";
		
		return null;
	}
	
	public String getAttributeCode(String uri){
		int i;
		i = uri.length()-1;
		while(i>=0 && uri.charAt(i)!='/') i--;
		if(i>=0)
			return uri.substring(i+1);
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

		Resource dimension  = mOutput.createResource("http://purl.org/linked-data/sdmx/2009/attribute#unitMeasure");
		Resource conceptScheme  = mOutput.createResource("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure");
		Resource cls  = mOutput.createResource("http://statspace.linkedwidgets.org/codelist/cl_UnitMeasure");
    	
		dimension.addProperty(RDF.type, QB.AttributeProperty);
		dimension.addProperty(RDF.type, RDF.Property);
		literal = mOutput.createLiteral("Unit of Measure", "en");
		dimension.addProperty(label, literal);
		literal = mOutput.createLiteral("The unit in which the data values are measured", "en");
		dimension.addProperty(comment, literal);
		dimension.addProperty(RDFS.isDefinedBy, resource);
		dimension.addProperty(codelist, conceptScheme);
		
		//Class
		cls.addProperty(RDF.type, rdfsClass);
		cls.addProperty(RDF.type, owlClass);
		cls.addProperty(subClassOf, skosConcept);
		literal = mOutput.createLiteral("Code list for unit of Measure (UNIT MEASURE) - codelist class", "en");
		cls.addProperty(label, literal);
		literal = mOutput.createLiteral("This code list provides a set of values to be used for identifying unit of observed measure", "en");
		cls.addProperty(comment, literal);
		cls.addProperty(seeAlso, conceptScheme);
		
		//ConceptScheme
		conceptScheme.addProperty(RDF.type, skosConceptScheme);
		literal = mOutput.createLiteral("Code list for unit of Measure (UNIT MEASURE) - codelist class", "en");
		conceptScheme.addProperty(prefLabel, literal);		
		conceptScheme.addProperty(label, literal);
		conceptScheme.addProperty(notation, "CL_UNIT_MEASURE");
		literal = mOutput.createLiteral("This code list provides a set of values to be used for identifying unit of observed measure", "en");
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
			out = new FileOutputStream("data/codelist/cl_unitMeasure.ttl");
			mOutput.write(out, "Turtle", null);		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
