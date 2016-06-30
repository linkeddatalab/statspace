package at.tuwien.ldlab.statspace.metadata.concrete;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import at.tuwien.ldlab.statspace.codelist.CL_Age;
import at.tuwien.ldlab.statspace.codelist.CL_Area;
import at.tuwien.ldlab.statspace.codelist.CL_Period;
import at.tuwien.ldlab.statspace.codelist.CL_Unit_Measure;
import at.tuwien.ldlab.statspace.util.QB;
import at.tuwien.ldlab.statspace.widgetgeneration.DataSet;
import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.model.Parameters;
import be.ugent.mmlab.rml.model.RMLMapping;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class UKMetaData {
	private static String qb = "http://purl.org/linked-data/cube#";		
	private static String sdmx_dimension = "http://purl.org/linked-data/sdmx/2009/dimension#";
	private static String sdmx_measure = "http://purl.org/linked-data/sdmx/2009/measure#";
	private static String sdmx_attribute = "http://purl.org/linked-data/sdmx/2009/attribute#";
	private static String sdmx_code = "http://purl.org/linked-data/sdmx/2009/code#";
	private static String vd = "http://rdfs.org/ns/void#";
	private static String dcterms = "http://purl.org/dc/terms/";
	private static String sdterms = "http://statspace.linkedwidgets.org/terms/";
	private static String dcat = "http://www.w3.org/ns/dcat#";
	private static String skos = "http://www.w3.org/2004/02/skos/core#";
	private static String owl = "http://www.w3.org/2002/07/owl#";
	private static String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
	private static String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	

	static CL_Area countries = new CL_Area();	
	static DataSet ds;
	
	public static void main(String[] args) {	
		System.out.println("Using RMLProcessor to generate RDF file for each indicator");		
		createRDFFile();	
		
		System.out.println("Anayzing RDF files to generate metadata");		
		createMetadata();
		
		System.out.println("Merging metadata files to an unique metadata");		
		mergeMetadata();
		
		System.out.println("Finished");
	}	

		
	public static void createRDFFile(){
		int i;
		String sInput, sOutput;
		
		for(i=0; i<=7; i++){
			System.out.println(i);
			sInput  = "data/mapping/uk"+i+".ttl";
			sOutput = "data/uk/uk"+i+".rdf";			 
			RMLMapping mapping = RMLMappingFactory.extractRMLMapping(sInput, new Parameters());
            if(mapping == null){
            	System.out.println("Can not read mapping");
            }else{            
	            RMLEngine engine = new RMLEngine();	     
	            engine.runRMLMapping(mapping, "", sOutput, true);
	            if(engine.getStatus()==false){
	            	System.out.println("Can not read data set");
	            }		           
            }
		}		
	}	
		
	public static void createMetadata(){
		int i, j, k;
		String dsName="";		
		try{
			for(i=0; i<=7; i++){		
				ds = new DataSet();				
				System.out.println(i);
				
				//read RDF file
			    InputStream is = FileManager.get().open("data/uk/uk"+i+".rdf");			         
				Model mInput = ModelFactory.createDefaultModel().read(is,null,"N-TRIPLE");	
				
				//analyze the input model
				querySubject(mInput);
				queryUriandLabel(mInput);
				queryComponent(mInput);
				if(ds.getAttributeSize()==0) identifyAttribute(mInput);
				queryValue(mInput);
				
				dsName = ds.getUri();
				k = dsName.length()-1;
				while(k>0 && dsName.charAt(k)!='/') k--;
				if(k>0) dsName = dsName.substring(k+1);
				else dsName = ds.getLabel();
				
				//create an empty Jena Model and set required prefix				
				Model mOutput = ModelFactory.createDefaultModel();	
				mOutput.setNsPrefix("qb", qb);								
				mOutput.setNsPrefix("sdmx-dimension", sdmx_dimension);
				mOutput.setNsPrefix("sdmx-measure", sdmx_measure);
				mOutput.setNsPrefix("sdmx-attribute", sdmx_attribute);
				mOutput.setNsPrefix("sdmx-code", sdmx_code);				
				mOutput.setNsPrefix("dcterms", dcterms);
				mOutput.setNsPrefix("sdterms", sdterms);			
				mOutput.setNsPrefix("dcat", dcat);	
				mOutput.setNsPrefix("skos", skos);	
				mOutput.setNsPrefix("owl", owl);
				mOutput.setNsPrefix("void", vd);
				mOutput.setNsPrefix("rdfs", rdfs);
				mOutput.setNsPrefix("rdf", rdf);
				
				//Metadata
				Resource rMetaData  = mOutput.createResource("http://statspace.linkedwidgets.org/metadata/"+dsName);
				
				//Provenance information				
		    	Property pPublisher = mOutput.createProperty(dcterms+"publisher");		    	
		    	rMetaData.addProperty(pPublisher, "United Kingdom - Office for National Statistics");
		    	Property pSource = mOutput.createProperty(dcterms+"source");		
		    	if(i<7){
		    		Resource rSource = mOutput.createResource("http://www.ons.gov.uk/ons/about-ons/business-transparency/freedom-of-information/what-can-i-request/published-ad-hoc-data/pop/july-2015/uk-population-estimates-1851-2014.xls");
			    	rMetaData.addProperty(pSource, rSource);		    		
		    	}else{
		    		Resource rSource = mOutput.createResource("http://www.ons.gov.uk/ons/rel/pop-estimate/population-estimates-for-uk--england-and-wales--scotland-and-northern-ireland/2013/chd-1-for-story.xls");
			    	rMetaData.addProperty(pSource, rSource);		    		
		    	}		    	
		    	Property pLicense = mOutput.createProperty(dcterms+"license");				    	
		    	Resource rLicense = mOutput.createResource("http://creativecommons.org/licenses/by-sa/4.0/");
		    	rMetaData.addProperty(pLicense, rLicense);
		    	Property pCreator = mOutput.createProperty(dcterms+"creator");
		    	Resource rCreator = mOutput.createResource( "http://www.ifs.tuwien.ac.at/user/383");
		    	rMetaData.addProperty(pCreator, rCreator);
		    	Property pCreated = mOutput.createProperty(dcterms+"created");		    	
		    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    	Date date = new Date();
		    	rMetaData.addProperty(pCreated,dateFormat.format(date));   
		    	
				//Dataset
		    	Resource rDataSet   = mOutput.createResource(ds.getUri());
		    	Property pDataSet 	= mOutput.createProperty(qb+"dataSet");
		    	rMetaData.addProperty(pDataSet, rDataSet);		
		    	Property pSubject = mOutput.createProperty(dcterms+"subject");
		    	rDataSet.addProperty(pSubject, mOutput.createResource(ds.getSubject()));
		    	Property pMethod = mOutput.createProperty(vd+"feature");
		    	rDataSet.addProperty(pMethod, "RML");
		    	Property pRML 	 = mOutput.createProperty(dcat+"accessURL");
		    	rDataSet.addProperty(pRML, mOutput.createResource("http://statspace.linkedwidgets.org/rml?rmlsource=http://statspace.linkedwidgets.org/mapping/uk"+i+".ttl"));
		    	rDataSet.addProperty(RDFS.label, ds.getLabel());    	
		      	Property pValue = mOutput.createProperty(rdf+"value");		    	
		      	
		    	//Component		    
		    	Property pComponent = mOutput.createProperty(qb+"component");		    	
		    	Property pSameAs  = mOutput.createProperty(owl+"sameAs");
		    				
		    	//Measure
		    	Resource rObsValue   = mOutput.createResource("http://purl.org/linked-data/sdmx/2009/measure#obsValue");
		    	rObsValue.addProperty(RDF.type, QB.MeasureProperty);
		    	rMetaData.addProperty(pComponent, rObsValue);  	
		    	
		    	//Attribute
		    	Resource rAttribute = mOutput.createResource("http://purl.org/linked-data/sdmx/2009/attribute#unitMeasure");
		    	rAttribute.addProperty(RDF.type, QB.AttributeProperty);
		    	rMetaData.addProperty(pComponent, rAttribute);	    			    	
		    	if(ds.getAttributeSize()==1){
			    	Resource rAttValue   = mOutput.createResource(ds.getAttribute(0).getValueUri(0));
					rAttribute.addProperty(pValue, rAttValue);
					rDataSet.addProperty(pValue, rAttValue);	
		    	}else
		    		System.out.println("Error at " + i);
				
				//Dimension
		    	CL_Period cl_period = new CL_Period();
		    	CL_Age cl_age = new CL_Age();
		    	
				for(j=0; j<ds.getDimensionSize(); j++){
					Resource rDimension = mOutput.createResource(ds.getDimensionUri(j));
					rDimension.addProperty(RDF.type, QB.DimensionProperty);
					rMetaData.addProperty(pComponent, rDimension);
					
					if(ds.getDimension(j).getUri().contains("refPeriod")){						
						for(k=0; k<ds.getDimension(j).getValueSize(); k++){
							String uri = ds.getDimension(j).getValueUri(k);	
							String ref = cl_period.identifyReference(uri);			
							if(ref==null)
								System.out.println("error at " + ds.getDimension(j).getValueUri(k));
							Resource rValue = mOutput.createResource(uri);
					    	rDimension.addProperty(pValue, rValue);
					    	rDataSet.addProperty(pValue, rValue);
					    	if(!uri.equalsIgnoreCase(ref)){
					    		Resource rRef = mOutput.createResource(ref);
					    		rRef.addProperty(pSameAs, rValue);
					    	}
						}
								
					}else if(ds.getDimension(j).getUri().contains("age")){
						for(k=0; k<ds.getDimension(j).getValueSize(); k++){
							String uri = ds.getDimension(j).getValueUri(k);	
							String ref = cl_age.identifyReference(uri,"");	
							Resource rValue = mOutput.createResource(uri);
					    	rDimension.addProperty(pValue, rValue);
					    	rDataSet.addProperty(pValue, rValue);
					    	
							if(ref==null)
								System.out.println("error at " + ds.getDimension(j).getValueUri(k));
							if(ref.equals("http://statspace.linkedwidgets.org/cl_age/TOTAL")){
								Property pTopValue = mOutput.createProperty(skos+"hasTopConcept");
								rDimension.addProperty(pTopValue, rValue);
								rDataSet.addProperty(pTopValue, rValue);
							}							
					    	if(!uri.equalsIgnoreCase(ref)){
					    		Resource rRef = mOutput.createResource(ref);
					    		rRef.addProperty(pSameAs, rValue);
					    	}
						}
					}
					else
						for(k=0; k<ds.getDimension(j).getValueSize(); k++){										
							Resource rValue = mOutput.createResource(ds.getDimension(j).getValueUri(k));
					    	rDimension.addProperty(pValue, rValue);
					    	rDataSet.addProperty(pValue, rValue);
					    	if((ds.getDimension(j).getValueSize()==1)||(ds.getDimension(j).getUri().contains("sex") && 
					    			ds.getDimension(j).getValueUri(k).equals("http://purl.org/linked-data/sdmx/2009/code#sex-T"))){
								Property pTopValue = mOutput.createProperty(skos+"hasTopConcept");
								rDimension.addProperty(pTopValue, rValue);
								rDataSet.addProperty(pTopValue, rValue);
							}	
						}
				}
		
		      	FileOutputStream out = new FileOutputStream("data/uk/uk_metadata"+i+".ttl");
				mOutput.write(out, "Turtle", null);		
				out.close();
			}
		}catch(Exception e){
			System.out.println(e.toString());			
		}
		
	}
	
	public static void mergeMetadata(){
		int i;		
		try{		
	        Model final_model = ModelFactory.createDefaultModel();	       
	        final_model.setNsPrefix("qb", qb);								
	        final_model.setNsPrefix("sdmx-dimension", sdmx_dimension);
	        final_model.setNsPrefix("sdmx-measure", sdmx_measure);
	        final_model.setNsPrefix("sdmx-attribute", sdmx_attribute);
	        final_model.setNsPrefix("sdmx-code", sdmx_code);				
	        final_model.setNsPrefix("dcterms", dcterms);
	        final_model.setNsPrefix("sdterms", sdterms);	       
	        final_model.setNsPrefix("dcat", dcat);	
	        final_model.setNsPrefix("skos", skos);	
	        final_model.setNsPrefix("owl", owl);
	        final_model.setNsPrefix("void", vd);
	        final_model.setNsPrefix("rdfs", rdfs);
	        
			for(i=0; i<=7; i++){		
				InputStream is = FileManager.get().open("data/uk/uk_metadata"+i+".ttl");			         
				Model model_tmp = ModelFactory.createDefaultModel().read(is, null, "TTL");				
				final_model.add(model_tmp);		
				is.close();
			}
			try (final OutputStream out = new FileOutputStream( new File( "data/metadata/uk.ttl" )) ) {
		        final_model.write( out, "Turtle", null );
		        out.close();
		    }	
		}catch(Exception e){
			System.out.println(e.toString());
		}
	}
	
	public static void queryComponent(Model mInput){
		String sQuery, l, c;
		Query query;
		QueryExecution qe = null;
		ResultSet rs;
		try{
			//dimensions
			sQuery=	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+		
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
					"SELECT ?c ?l \n"+ 
					"WHERE{ \n"+						
						"?cp qb:dimension ?c. \n"+
						"optional{" +
							"?c rdfs:label ?l. \n"+														
						"} \n"+			
					"}";			
			query = QueryFactory.create(sQuery);
			qe = QueryExecutionFactory.create(query, mInput);
			rs =  qe.execSelect();			
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();
				c = sol.get("c").toString().replace("\n", "").replace("\r", "").trim();
				if(sol.contains("?l"))
					l = sol.get("l").toString().replace("\n", "").replace("\r", "").trim();
				else
					l = "";
				ds.addDimension(c, l);
			}
			
			//measure
			sQuery=	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+	
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
					"SELECT ?c ?l \n"+ 
					"WHERE{ \n"+						
						"?cp qb:measure ?c. \n"+
						"optional{" +
							"?c rdfs:label ?l. \n"+														
						"} \n"+			
					"}";			
			query = QueryFactory.create(sQuery);
			qe = QueryExecutionFactory.create(query, mInput);
			rs =  qe.execSelect();			
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();
				c = sol.get("c").toString().replace("\n", "").replace("\r", "").trim();
				if(sol.contains("?l"))
					l = sol.get("l").toString().replace("\n", "").replace("\r", "").trim();
				else
					l = "";
				ds.addMeasure(c, l);
			}
			
			//attribute
			sQuery=	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+	
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
					"SELECT ?c ?l \n"+ 
					"WHERE{ \n"+						
						"?cp qb:attribute ?c. \n"+
						"optional{" +
							"?c rdfs:label ?l. \n"+														
						"} \n"+			
					"}";			
			query = QueryFactory.create(sQuery);
			qe = QueryExecutionFactory.create(query, mInput);
			rs =  qe.execSelect();			
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();
				c = sol.get("c").toString().replace("\n", "").replace("\r", "").trim();
				if(sol.contains("?l"))
					l = sol.get("l").toString().replace("\n", "").replace("\r", "").trim();
				else
					l = "";
				ds.addAttribute(c, l);
			}
		}catch(QueryException e){
			
		}finally {
			if(qe!=null)
				qe.close() ;
		}	
	}
	
	public static void identifyAttribute(Model mInput){
		CL_Unit_Measure units = new CL_Unit_Measure();		
		ds.addAttribute("http://purl.org/linked-data/sdmx/2009/attribute#unitMeasure", "");
		String label, uri, unit;
		label = ds.getLabel();
		uri = ds.getSubject();
		unit = units.identifyReference(label, uri);
		ds.getAttribute(0).addValue(unit, "");		
	}
	
	public static void queryValue(Model mInput){
		String sQuery, l, v, uri;
		Query query;
		QueryExecution qe = null;
		ResultSet rs;
		int i;
		try{
			//dimensions
			for(i=0; i<ds.getDimensionSize(); i++){
				uri = ds.getDimensionUri(i);
				
				sQuery=	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
						"PREFIX sdmx-measure:  <http://purl.org/linked-data/sdmx/2009/measure#> \n"+
						"SELECT DISTINCT ?v ?l \n"+
						"WHERE{ \n"+															
							"?o <"+ uri + "> ?v. \n"+
							"?o sdmx-measure:obsValue ?m. \n"+
							"optional{ \n"+
								"?v rdfs:label ?l. \n"+								
							"} \n" +			
						"}";		
				query = QueryFactory.create(sQuery);
				qe = QueryExecutionFactory.create(query, mInput);
				rs =  qe.execSelect();			
				while (rs!=null && rs.hasNext()) {		
					QuerySolution sol = rs.nextSolution();
					v = sol.get("v").toString().replace("\n", "").replace("\r", "").trim();
					if(sol.contains("?l"))
						l = sol.get("l").toString().replace("\n", "").replace("\r", "").trim();
					else
						l = "";
					ds.getDimension(i).addValue(v, l);
				}
			}
			
			//attributes
			for(i=0; i<ds.getAttributeSize(); i++){
				uri = ds.getAttributeUri(i);
				
				sQuery=	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
						"PREFIX sdmx-measure:  <http://purl.org/linked-data/sdmx/2009/measure#> \n"+
						"SELECT DISTINCT ?v ?l \n"+
						"WHERE{ \n"+															
							"?o <"+ uri + "> ?v. \n"+
							"?o sdmx-measure:obsValue ?m. \n"+
							"optional{ \n"+
								"?v rdfs:label ?l. \n"+								
							"} \n" +			
						"}";		
				query = QueryFactory.create(sQuery);
				qe = QueryExecutionFactory.create(query, mInput);
				rs =  qe.execSelect();			
				while (rs!=null && rs.hasNext()) {		
					QuerySolution sol = rs.nextSolution();
					v = sol.get("v").toString().replace("\n", "").replace("\r", "").trim();
					if(sol.contains("?l"))
						l = sol.get("l").toString().replace("\n", "").replace("\r", "").trim();
					else
						l = "";
					ds.getAttribute(i).addValue(v, l);
				}
			}
			
		}catch(QueryException e){
			
		}finally {
			if(qe!=null)
				qe.close() ;
		}	
	}
	
	
	public static void querySubject(Model mInput){
		String sQuery, subj="";
		Query query;
		QueryExecution qe = null;
		ResultSet rs;
		try{
			sQuery= "PREFIX dc:   <http://purl.org/dc/terms/> \n"+	
					"SELECT DISTINCT ?ds ?subj \n"+
					"WHERE{ \n"+
						"?ds dc:subject ?subj. \n"+					
					"}";
			
			query = QueryFactory.create(sQuery);
			qe = QueryExecutionFactory.create(query, mInput);
			rs =  qe.execSelect();
			
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();
				subj = sol.get("subj").toString().replace("\n", "").replace("\r", "").trim();
				break;
			}
		}catch(QueryException e){
			
		}finally {
			if(qe!=null)
				qe.close() ;
		}		
		ds.setSubject(subj);
	}
	
	public static void queryUriandLabel(Model mInput){
		String sQuery, uri="", label="";
		Query query;
		QueryExecution qe = null;
		ResultSet rs;
		try{
			sQuery= "PREFIX dc:   <http://purl.org/dc/terms/> \n"+	
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
					"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
					"SELECT DISTINCT ?ds ?l \n"+
					"WHERE{ \n"+
						"?ds a qb:DataSet. \n" +
						"?ds rdfs:label ?l. \n"+					
					"}";
			
			query = QueryFactory.create(sQuery);
			qe = QueryExecutionFactory.create(query, mInput);
			rs =  qe.execSelect();
			
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();
				uri = sol.get("ds").toString().replace("\n", "").replace("\r", "").trim();
				label = sol.get("l").toString().replace("\n", "").replace("\r", "").trim();
				break;
			}
		}catch(QueryException e){
			
		}finally {
			if(qe!=null)
				qe.close() ;
		}
		ds.setUri(uri);
		ds.setLabel(label);
	}	
}
