package at.tuwien.ldlab.statspace.codelist;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import at.tuwien.ldlab.statspace.metadata.StringTriple;


public class CL_Subject {
	private ArrayList<StringTriple> arrCL = new ArrayList<StringTriple>();
	private String file="data/indicators.csv";
	
	public CL_Subject(){
		BufferedReader br = null;
		String topic="", indicator, label, line = "";			
		try {	 
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));		
			while ((line = br.readLine()) != null) {
				if(line.indexOf("\t")==-1){
					topic = line.trim();				
				}
				else{
					indicator = line.substring(0, line.indexOf("\t")).trim();
					line = line.substring(line.indexOf("\t")+1).trim();					
					label = line.replaceAll("\t", "").trim();										
					arrCL.add(new StringTriple(topic, indicator, label));
				}				
			}	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}	
	}
	
	
	public CL_Subject(String s){
		BufferedReader br = null;
		String topic="", indicator, label, line = "";			
		try {	 
			br = new BufferedReader(new InputStreamReader(new FileInputStream(s), "UTF-8"));		
			while ((line = br.readLine()) != null) {
				if(line.indexOf("\t")==-1){
					topic = line.trim();				
				}
				else{
					indicator = line.substring(0, line.indexOf("\t")).trim();
					line = line.substring(line.indexOf("\t")+1).trim();					
					label = line.replaceAll("\t", "").trim();										
					arrCL.add(new StringTriple(topic, indicator, label));
				}				
			}	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}	
	}

	public int getSize(){
		return arrCL.size();
	}
	
	public String getTopic(int i){
		return arrCL.get(i).getFirstString();
	}
	
	public String getUri(int i){
		return arrCL.get(i).getSecondString();
	}
	
	public String getCodeOfSubject(int i){
		String s = arrCL.get(i).getSecondString();
		int k = s.length()-1;
		while(k>0 && s.charAt(k)!='/') k--;
		if(k>0)
			return s.substring(k+1);
		return s;
	}
	
	public String getLabel(int i){
		return arrCL.get(i).getThirdString();
	}
	
	public void display(){
		int i;
		for(i=0; i<arrCL.size(); i++)
			arrCL.get(i).display();
	}
	
	public void checkIncorrection(){
		int i, j;
		for(i=0; i<arrCL.size(); i++)
			for(j=i+1; j<arrCL.size(); j++)
				if(arrCL.get(i).getSecondString().equalsIgnoreCase(arrCL.get(j).getSecondString()) &&
					!(arrCL.get(i).getThirdString().equalsIgnoreCase(arrCL.get(j).getThirdString())))
					arrCL.get(i).display();
	}

	public String identifyReference(String label) {
		int i, j, k, n, m, count, index=-1;
		double max=0;
		label = label.replace(",", "").replaceAll("\\(","").replaceAll("\\)","").replace("-", "").trim();
		String[] inputs = label.split(" ");
		n = inputs.length;
		for(i=0; i<arrCL.size(); i++){
			String[] words = arrCL.get(i).getThirdString().replace(",", "").replaceAll("\\(","").replaceAll("\\)","").replace("-", "").trim().split(" ");
			m = words.length;
			count=0;
			for(j=0; j<n; j++){
				for(k=0; k<m; k++)
					if(inputs[j].equalsIgnoreCase(words[k])){
						count++;
						break;
					}			
			}
			if(max<(count*1.0/m)){
				max=count*1.0/m;
				index=i;
			}
		}
		if(index!=-1 && max>0.5)
			return arrCL.get(index).getSecondString() + "\t" + arrCL.get(index).getThirdString();
		else
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
		
		
		//Properties
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

		Resource conceptScheme  = mOutput.createResource("http://statspace.linkedwidgets.org/codelist/cl_subject");
		Resource cls  = mOutput.createResource("http://statspace.linkedwidgets.org/codelist/cl_Subject");
    	
		//Class
		cls.addProperty(RDF.type, rdfsClass);
		cls.addProperty(RDF.type, owlClass);
		cls.addProperty(subClassOf, skosConcept);
		literal = mOutput.createLiteral("Code list for Subject (SUBJECT) - codelist class", "en");
		cls.addProperty(label, literal);
		literal = mOutput.createLiteral("This code list provides a set of subjects to be used for indicating purpose of a statistical data set", "en");
		cls.addProperty(comment, literal);
		cls.addProperty(seeAlso, conceptScheme);
		
		//ConceptScheme
		conceptScheme.addProperty(RDF.type, skosConceptScheme);
		literal = mOutput.createLiteral("Code list for Subject (SUBJECT) - codelist class", "en");
		conceptScheme.addProperty(prefLabel, literal);		
		conceptScheme.addProperty(label, literal);
		conceptScheme.addProperty(notation, "CL_SUBJECT");
		literal = mOutput.createLiteral("This code list provides a set of subjects to be used for indicating purpose of a statistical data set", "en");
		conceptScheme.addProperty(note, literal);
		conceptScheme.addProperty(definition, "http://data.worldbank.org/indicator/all");
		conceptScheme.addProperty(seeAlso, cls);
		for(i=0; i<arrCL.size(); i++){
			String s = getUri(i);
			s = s.replace("http://data.worldbank.org/indicator/", "http://statspace.linkedwidgets.org/codelist/subject/");
			Resource uri = mOutput.createResource(s);			
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
			out = new FileOutputStream("data/code/cl_subject.ttl");
			mOutput.write(out, "Turtle", null);		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
