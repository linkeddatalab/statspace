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

import tuwien.ldlab.statspace.model.mediator.StringTriple;
import tuwien.ldlab.statspace.model.util.QB;
public class CL_Civil_Status {
	private ArrayList<StringTriple> arrCL;
	
	public CL_Civil_Status(){
		arrCL = new ArrayList<StringTriple>();
		arrCL.add(new StringTriple("http://statspace.linkedwidgets.org/codelist/cl_civilStatus/S","Single person", "1"));
		arrCL.add(new StringTriple("http://statspace.linkedwidgets.org/codelist/cl_civilStatus/M","Married person", "1"));
		arrCL.add(new StringTriple("http://statspace.linkedwidgets.org/codelist/cl_civilStatus/W","Widowed person", "1"));
		arrCL.add(new StringTriple("http://statspace.linkedwidgets.org/codelist/cl_civilStatus/D","Divorced person", "1"));
		arrCL.add(new StringTriple("http://statspace.linkedwidgets.org/codelist/cl_civilStatus/L","Leggaly separated person", "1"));
		arrCL.add(new StringTriple("http://statspace.linkedwidgets.org/codelist/cl_civilStatus/P","Person in Registerd partnership", "1"));
		arrCL.add(new StringTriple("http://statspace.linkedwidgets.org/codelist/cl_civilStatus/Q","Person whose registered partnership ended with the death of the partner", "1"));
		arrCL.add(new StringTriple("http://statspace.linkedwidgets.org/codelist/cl_civilStatus/E","Person whose registered partnership was legally dissolved", "1"));
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
		
		Resource dimension  = mOutput.createResource("http://purl.org/linked-data/sdmx/2009/dimension#civilStatus");
		Resource conceptScheme  = mOutput.createResource("http://statspace.linkedwidgets.org/codelist/cl_civilStatus");
		Resource cls  = mOutput.createResource("http://statspace.linkedwidgets.org/codelist/cl_CivilStatus");
    	
		dimension.addProperty(RDF.type, QB.DimensionProperty);
		dimension.addProperty(RDF.type, RDF.Property);
		literal = mOutput.createLiteral("Civil Status", "en");
		dimension.addProperty(label, literal);
		literal = mOutput.createLiteral("Legal, conjugal status of each individual in relation to the marriage laws or customs of the country", "en");
		dimension.addProperty(comment, literal);
		dimension.addProperty(codelist, conceptScheme);
		dimension.addProperty(RDFS.isDefinedBy, resource);
		
		//Class
		cls.addProperty(RDF.type, rdfsClass);
		cls.addProperty(RDF.type, owlClass);
		cls.addProperty(subClassOf, skosConcept);
		literal = mOutput.createLiteral("Code list for Civil Status (CIVIL STATUS) - codelist class", "en");
		cls.addProperty(label, literal);
		literal = mOutput.createLiteral("This code list provides a list of values for describing the civil (or marital) status of an individual, i.e. the legal, conjugal status of an individual in relation to the marriage laws or customs of the country", "en");
		cls.addProperty(comment, literal);
		cls.addProperty(seeAlso, conceptScheme);
		
		//ConceptScheme
		conceptScheme.addProperty(RDF.type, skosConceptScheme);
		literal = mOutput.createLiteral("Code list for Civil Status (CIVIL STATUS) - codelist class", "en");
		conceptScheme.addProperty(prefLabel, literal);		
		conceptScheme.addProperty(label, literal);
		conceptScheme.addProperty(notation, "CL_CIVIL_STATUS");
		literal = mOutput.createLiteral("This code list provides a list of values for describing the civil (or marital) status of an individual, i.e. the legal, conjugal status of an individual in relation to the marriage laws or customs of the country", "en");
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
			String s = extractCode(arrCL.get(i).getFirstString());
			if(!s.isEmpty()){
				literal = mOutput.createLiteral(s);
				uri.addProperty(notation, literal);
			}
		}
		
		FileOutputStream out;
		try {
			out = new FileOutputStream("data/codelist/cl_civilstatus.ttl");
			mOutput.write(out, "Turtle", null);		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
