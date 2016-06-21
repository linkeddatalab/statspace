package at.tuwien.ldlab.statspace.codelist;

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

import at.tuwien.ldlab.statspace.metadata.StringTriple;
import at.tuwien.ldlab.statspace.util.QB;

public class CL_Freq {
	private ArrayList<StringTriple> arrCL;
	
	public CL_Freq(){
		arrCL = new ArrayList<StringTriple>();			
		arrCL.add(new StringTriple("http://purl.org/linked-data/sdmx/2009/code#freq-A","Annual","1"));
		arrCL.add(new StringTriple("http://purl.org/linked-data/sdmx/2009/code#freq-S","Half yearly, semester","1"));
		arrCL.add(new StringTriple("http://purl.org/linked-data/sdmx/2009/code#freq-Q","Quarterly","1"));
		arrCL.add(new StringTriple("http://purl.org/linked-data/sdmx/2009/code#freq-M","Monthly","1"));
		arrCL.add(new StringTriple("http://purl.org/linked-data/sdmx/2009/code#freq-W","Weekly","1"));
		arrCL.add(new StringTriple("http://purl.org/linked-data/sdmx/2009/code#freq-D","Daily","1"));
		arrCL.add(new StringTriple("http://purl.org/linked-data/sdmx/2009/code#freq-H","Hourly","1"));
		arrCL.add(new StringTriple("http://purl.org/linked-data/sdmx/2009/code#freq-B","Daily-business week","1"));
		arrCL.add(new StringTriple("http://purl.org/linked-data/sdmx/2009/code#freq-N","Minutely","1"));		
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
		
		//compare label
		if(label!=null && !label.isEmpty()){
			label = label.toLowerCase();
			if(label.contains("annual"))
				return "http://purl.org/linked-data/sdmx/2009/code#freq-A";
			if(label.contains("year")||label.contains("semester"))
				return "http://purl.org/linked-data/sdmx/2009/code#freq-S";
			if(label.contains("quarter"))
				return "http://purl.org/linked-data/sdmx/2009/code#freq-Q";
			if(label.contains("month"))
				return "http://purl.org/linked-data/sdmx/2009/code#freq-M";
			if(label.contains("week"))
				return "http://purl.org/linked-data/sdmx/2009/code#freq-W";
			if(label.contains("bussiness"))
				return "http://purl.org/linked-data/sdmx/2009/code#freq-B";
			if(label.contains("daily"))
				return "http://purl.org/linked-data/sdmx/2009/code#freq-D";
			if(label.contains("hour"))
				return "http://purl.org/linked-data/sdmx/2009/code#freq-H";
			if(label.contains("minute"))
				return "http://purl.org/linked-data/sdmx/2009/code#freq-N";	
		}
			
		
		//compare code
		if(uri!=null && !uri.isEmpty()){			
			String code="";
			code = extractFreqCode(uri);			
			if(!code.isEmpty()){		
				for(i=0; i<arrCL.size(); i++)
					if(code.equalsIgnoreCase(extractFreqCode(arrCL.get(i).getFirstString())))
						return arrCL.get(i).getFirstString();
			}
		}
		
		//not found
		return null;		
	}
	
	public String extractFreqCode(String s){
		int i = s.length()-1;
		while(i>0 && s.charAt(i)!='/') i--;
		if(i>0){
			s = s.substring(i+1);
			if(s.contains("#"))
				s = s.substring(s.indexOf('#')+1);
			s = s.replace("freq", "").replace("-", "").replace("frequency", "").trim();
			return s;
		}
		else
			return "";
	}
	
	//create RDF triples
	public void generateCodeList(){
		String qb = "http://purl.org/linked-data/cube#";	
		String skos = "http://www.w3.org/2004/02/skos/core#";
		String owl = "http://www.w3.org/2002/07/owl#";
		String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
		String sdmx_code = "http://purl.org/linked-data/sdmx/2009/code#";
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
		mOutput.setNsPrefix("sdmx-code", sdmx_code);		
		
		
		int i;
		Literal literal;			

		Resource dimension  = mOutput.createResource("http://purl.org/linked-data/sdmx/2009/dimension#freq");
		Resource conceptScheme  = mOutput.createResource(sdmx_code + "freq");
		Resource cls  = mOutput.createResource(sdmx_code+"Freq");
    	
		dimension.addProperty(RDF.type, QB.DimensionProperty);
		dimension.addProperty(RDF.type, RDF.Property);
		literal = mOutput.createLiteral("Frequency", "en");
		dimension.addProperty(label, literal);
		literal = mOutput.createLiteral("The time interval at which observations occur over a given time period", "en");
		dimension.addProperty(comment, literal);
		dimension.addProperty(codelist, conceptScheme);
		dimension.addProperty(RDFS.isDefinedBy, resource);
		
		//Class
		cls.addProperty(RDF.type, rdfsClass);
		cls.addProperty(RDF.type, owlClass);
		cls.addProperty(subClassOf, skosConcept);
		literal = mOutput.createLiteral("Code list for Frequency (FREQUENCY) - codelist class", "en");
		cls.addProperty(label, literal);
		literal = mOutput.createLiteral("It provides a list of values indicating the frequency of the data (e.g. monthly) and, thus, indirectly, also implying the type of time reference that could be used for identifying the data with respect time", "en");
		cls.addProperty(comment, literal);
		cls.addProperty(seeAlso, conceptScheme);
		
		//ConceptScheme
		conceptScheme.addProperty(RDF.type, skosConceptScheme);
		literal = mOutput.createLiteral("Code list for Frequency (FREQUENCY) - codelist scheme", "en");
		conceptScheme.addProperty(prefLabel, literal);		
		conceptScheme.addProperty(label, literal);
		conceptScheme.addProperty(notation, "CL_FREQ");
		literal = mOutput.createLiteral("It provides a list of values indicating the frequency of the data (e.g. monthly) and, thus, indirectly, also implying the type of time reference that could be used for identifying the data with respect time", "en");
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
			
			String s = extractFreqCode(getUri(i));
			if(!s.isEmpty()){
				literal = mOutput.createLiteral(s);
				uri.addProperty(notation, literal);
			}
		}
		
		FileOutputStream out;
		try {
			out = new FileOutputStream("data/code/cl_freq.ttl");
			mOutput.write(out, "Turtle", null);		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
