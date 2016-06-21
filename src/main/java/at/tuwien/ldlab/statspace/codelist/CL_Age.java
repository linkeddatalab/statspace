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

import at.tuwien.ldlab.statspace.metadata.StringTetrad;
import at.tuwien.ldlab.statspace.util.QB;


public class CL_Age {
	private ArrayList<StringTetrad> arrCL;
	
	public CL_Age(){
		arrCL = new ArrayList<StringTetrad>();
		int i, j;	
		String total = "http://statspace.linkedwidgets.org/codelist/cl_age/TOTAL";
		
		//URI - Label - Type - UpperLevel
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/TOTAL", "Total", "", null));
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/UNK", "Unknown", "", total));
		
		
		//Y0-4 to Y105-109
		for(i=0; i<=105; i=i+5){
			arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y"+i+"T"+(i+4), "From "+ Integer.toString(i)+" to " + Integer.toString(i+4) + " years", "http://statspace.linkedwidgets.org/terms/Age5Year", total));
			for(j=i; j<=i+4; j++){
				if(j!=0)
					arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y"+j, Integer.toString(j)+ " years", "http://statspace.linkedwidgets.org/terms/AgeIndvYear", "http://statspace.linkedwidgets.org/codelist/cl_age/Y"+i+"T"+(i+4)));
				else
					arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y"+j, Integer.toString(j)+ " year", "http://statspace.linkedwidgets.org/terms/AgeIndvYear", "http://statspace.linkedwidgets.org/codelist/cl_age/Y"+i+"T"+(i+4)));
			}
		}
		
		//25-34, 75-84..,95-104		
		for(i=25; i<=95; i=i+10){
			arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y"+i+"T"+(i+9), "From "+ Integer.toString(i)+" to " + Integer.toString(i+9) + " years", "http://statspace.linkedwidgets.org/terms/Age10Year", total));
			for(j=i; j<=i+5; j=j+5)				
				arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y"+j+"T"+(j+4), "From "+ Integer.toString(j)+" to " + Integer.toString(j+4)+ " years", "http://statspace.linkedwidgets.org/terms/Age5Year", "http://statspace.linkedwidgets.org/codelist/cl_age/Y"+i+"T"+(i+9)));
		}	
		
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_LE15", "Age under 15", "http://statspace.linkedwidgets.org/terms/AgeGroup", total));
		for(j=0; j<15; j=j+5)
			arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y"+j+"T"+(j+4), "From "+ Integer.toString(j)+" to " + Integer.toString(j+4)+ " years", "http://statspace.linkedwidgets.org/terms/Age5Year", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_LE15"));
		
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_LE25", "Age under 25", "http://statspace.linkedwidgets.org/terms/AgeGroup", total));
		for(j=0; j<25; j=j+5)
			arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y"+j+"T"+(j+4), "From "+ Integer.toString(j)+" to " + Integer.toString(j+4)+ " years", "http://statspace.linkedwidgets.org/terms/Age5Year", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_LE25"));
		
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE65", "Age 65 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", total));
		for(j=65; j<=105; j=j+5)
			arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y"+j+"T"+(j+4), "From "+ Integer.toString(j)+" to " + Integer.toString(j+4)+ " years", "http://statspace.linkedwidgets.org/terms/Age5Year", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE70"));
		
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE70", "Age 70 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", total));
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE70", "Age 70 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE65"));
		for(j=70; j<=105; j=j+5)
			arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y"+j+"T"+(j+4), "From "+ Integer.toString(j)+" to " + Integer.toString(j+4)+ " years", "http://statspace.linkedwidgets.org/terms/Age5Year", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE70"));
		
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE75", "Age 75 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", total));
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE75", "Age 75 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE65"));
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE75", "Age 75 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE70"));
		for(j=75; j<=105; j=j+5)
			arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y"+j+"T"+(j+4), "From "+ Integer.toString(j)+" to " + Integer.toString(j+4)+ " years", "http://statspace.linkedwidgets.org/terms/Age5Year", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE75"));
		
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE80", "Age 80 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", total));
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE80", "Age 80 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE65"));
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE80", "Age 80 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE70"));
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE80", "Age 80 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE75"));
		for(j=80; j<=105; j=j+5)
			arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y"+j+"T"+(j+4), "From "+ Integer.toString(j)+" to " + Integer.toString(j+4)+ " years", "http://statspace.linkedwidgets.org/terms/Age5Year", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE80"));
		
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE85", "Age 85 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", total));
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE85", "Age 85 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE65"));
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE85", "Age 85 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE70"));
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE85", "Age 85 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE75"));
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE85", "Age 85 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE80"));
		for(j=85; j<=105; j=j+5)
			arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y"+j+"T"+(j+4), "From "+ Integer.toString(j)+" to " + Integer.toString(j+4)+ " years", "http://statspace.linkedwidgets.org/terms/Age5Year", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE85"));
		
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE90", "Age 90 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", total));
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE90", "Age 90 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE65"));
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE90", "Age 90 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE70"));
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE90", "Age 90 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE75"));
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE90", "Age 90 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE80"));
		arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE90", "Age 90 and above", "http://statspace.linkedwidgets.org/terms/AgeGroup", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE85"));		
		for(j=90; j<=105; j=j+5)
			arrCL.add(new StringTetrad("http://statspace.linkedwidgets.org/codelist/cl_age/Y"+j+"T"+(j+4), "From "+ Integer.toString(j)+" to " + Integer.toString(j+4)+ " years", "http://statspace.linkedwidgets.org/terms/Age5Year", "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE90"));		
	}
	
	public String getUri(int i){return arrCL.get(i).getFirstString();}
	public String getLabel(int i){return arrCL.get(i).getSecondString();}
	public String getType(int i){return arrCL.get(i).getThirdString();}	
	public String getUpperLevel(int i){return arrCL.get(i).getFourthString();}
	public int getSize(){return arrCL.size();}	
	public void display(){
		for(int i=0; i<arrCL.size(); i++)
			System.out.println(arrCL.get(i).getFirstString() + "\t" + arrCL.get(i).getSecondString() + "\t" + arrCL.get(i).getThirdString() + "\t" + arrCL.get(i).getFourthString());
	}
	
	public void display2(){
		int i, j;
		for(i=0; i<arrCL.size(); i++){			
			for(j=i-1; j>=0; j--)
				if(arrCL.get(j).getFirstString().equals(arrCL.get(i).getFirstString()))
					break;
			if(j==-1){
				System.out.print(arrCL.get(i).getFirstString() + "\t" + arrCL.get(i).getSecondString() + "\t" + arrCL.get(i).getThirdString() + "\t" + arrCL.get(i).getFourthString());
				for(j=i+1; j<arrCL.size(); j++)
					if(arrCL.get(j).getFirstString().equals(arrCL.get(i).getFirstString())){
						if(!arrCL.get(j).getSecondString().equals(arrCL.get(i).getSecondString())||
						   !arrCL.get(j).getThirdString().equals(arrCL.get(i).getThirdString()))
							System.out.print("\n!!!!!!!!" + arrCL.get(i).getFirstString());
						else
							System.out.print("\t" + arrCL.get(j).getFourthString());
					}
			}
			System.out.println();
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
		
		//data type
		if(uri.contains("^^http")){
			i = uri.indexOf("^^http");
			uri = uri.substring(0, i).trim();
			if(uri!="")
				return "http://statspace.linkedwidgets.org/codelist/cl_age/Y"+uri;
			else 
				return null;
		}
		
		//compare code
		if(uri!=null && !uri.isEmpty()){
			String code;
			code = extractCode(uri);		
			if(code.contains("-")){			
				String from, to;
				i = code.indexOf('-');
				from = code.substring(0, i);
				to = code.substring(i+1);
				
				/* Y10-Y14
				 * from: Y10 => remove Y
				 */
				i=0;
				while(i<from.length() && 
						((from.charAt(i)>='A' && from.charAt(i)<='Z')||
						(from.charAt(i)>='a' && from.charAt(i)<='z')))
					i++;
				if(i<from.length())
					from = from.substring(i);
				else 
					return null;
				
				//check value of from
				for(i=0; i<from.length()-1; i++)
					if(!(from.charAt(i)>='0' && from.charAt(i)<='9'))
							return null;
				
				/*  to: Y14 => remove Y	 */
				i=0;
				while(i<to.length() && 
						((to.charAt(i)>='A' && to.charAt(i)<='Z')||
						(to.charAt(i)>='a' && to.charAt(i)<='z')))
					i++;
				if(i<to.length())
					to = to.substring(i);
				else 
					return null;
				
				//check value of to
				for(i=0; i<to.length()-1; i++)
					if(!(to.charAt(i)>='0' && to.charAt(i)<='9'))
							return null;
				
				return "http://statspace.linkedwidgets.org/codelist/cl_age/Y"+from+"-"+to;
				
			}else{
				if(code.toLowerCase().contains("all")||code.toLowerCase().contains("total")||code.toLowerCase().equals("t"))
					return "http://statspace.linkedwidgets.org/codelist/cl_age/TOTAL";					
					
				if(code.toLowerCase().contains("_le15"))
					return "http://statspace.linkedwidgets.org/codelist/cl_age/Y_LE15";	
				
				if(code.toLowerCase().contains("_le25"))
					return "http://statspace.linkedwidgets.org/codelist/cl_age/Y_LE25";
				
				if(code.toLowerCase().contains("unknown")||code.toLowerCase().contains("unk")||code.toLowerCase().equals("u"))
					return "http://statspace.linkedwidgets.org/codelist/cl_age/UNK";
				
				if(code.toLowerCase().contains("y85%2f85%2b"))
					return "http://statspace.linkedwidgets.org/codelist/cl_age/Y85";		
				
				code = code.replace("%2B",  "+").replace("GE",  "+");
				if(code.contains("+"))
					if(code.contains("65"))
						return "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE65";
					else if(code.contains("70"))
						return "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE70";
					else if(code.contains("75"))
						return "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE75";
					else if(code.contains("80"))
						return "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE80";
					else if(code.contains("85"))
						return "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE85";
					else if(code.contains("90"))
						return "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE90";
					else if(code.contains("95"))
						return "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE95";
					else if(code.contains("100"))
						return "http://statspace.linkedwidgets.org/codelist/cl_age/Y_GE100";
					else
						return null;			
				
				i=0;
				while(i<code.length() && 
						((code.charAt(i)>='A' && code.charAt(i)<='Z')||
						(code.charAt(i)>='a' && code.charAt(i)<='z')))
					i++;
				if(i<code.length())
					code = code.substring(i);
				else 
					return null;
				
				//check value of from
				for(i=0; i<code.length()-1; i++)
					if(!(code.charAt(i)>='0' && code.charAt(i)<='9'))
							return null;
				
				return "http://statspace.linkedwidgets.org/codelist/cl_age/Y"+code;
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
		
		mOutput.setNsPrefix("qb", qb);	
		mOutput.setNsPrefix("skos", skos);	
		mOutput.setNsPrefix("rdfs", rdfs);		
		
		int i;
		Literal literal;	
		String s;

		Resource dimension  = mOutput.createResource("http://purl.org/linked-data/sdmx/2009/dimension#age");
		Resource conceptScheme  = mOutput.createResource("http://statspace.linkedwidgets.org/codelist/cl_age");
		Resource cls  = mOutput.createResource("http://statspace.linkedwidgets.org/codelist/cl_Age");
    	
		dimension.addProperty(RDF.type, QB.DimensionProperty);
		dimension.addProperty(RDF.type, RDF.Property);
		literal = mOutput.createLiteral("Age", "en");
		dimension.addProperty(label, literal);
		literal = mOutput.createLiteral("The length of time that a person has lived or a thing has existed", "en");
		dimension.addProperty(comment, literal);
		dimension.addProperty(codelist, conceptScheme);
		dimension.addProperty(RDFS.isDefinedBy, resource);
		
		//Class
		cls.addProperty(RDF.type, rdfsClass);
		cls.addProperty(RDF.type, owlClass);
		cls.addProperty(subClassOf, skosConcept);
		literal = mOutput.createLiteral("Code list for Age (AGE) - codelist class", "en");
		cls.addProperty(label, literal);
		literal = mOutput.createLiteral("This code list provides a set of building blocks to be used for creating simple or complex code identifiers relating to the concept of age", "en");
		cls.addProperty(comment, literal);
		cls.addProperty(seeAlso, conceptScheme);
		
		//ConceptScheme
		conceptScheme.addProperty(RDF.type, skosConceptScheme);
		literal = mOutput.createLiteral("Code list for Age (AGE) - codelist class", "en");
		conceptScheme.addProperty(prefLabel, literal);		
		conceptScheme.addProperty(label, literal);
		conceptScheme.addProperty(notation, "CL_AGE");
		literal = mOutput.createLiteral("This code list provides a set of building blocks to be used for creating simple or complex code identifiers relating to the concept of age", "en");
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
			s = extractCode(arrCL.get(i).getFirstString());
			if(!s.isEmpty()){
				literal = mOutput.createLiteral(s);
				uri.addProperty(notation, literal);
			}
		}
		
		FileOutputStream out;
		try {
			out = new FileOutputStream("data/code/cl_age.ttl");
			mOutput.write(out, "Turtle", null);		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}	
}
