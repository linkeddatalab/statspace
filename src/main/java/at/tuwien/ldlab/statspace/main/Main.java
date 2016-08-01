package at.tuwien.ldlab.statspace.main;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import at.tuwien.ldlab.statspace.codelist.*;
import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.model.Parameters;
import be.ugent.mmlab.rml.model.RMLMapping;

public class Main {
	public static void main(String[] args) {
		mergeMetaData();
//		generateRandomQueries();
//		evaluatePerformance();
//		rml();
//		createPrefix();			
	}
	
	public static void generateRandomQueries(){
		int i, j, k, n, m;
		String sCountry, sIndicator, sQuery;
		try{ 
			CL_Area countries = new CL_Area();
    		CL_Subject indicators = new CL_Subject();
			
    		Random random = new Random();
    		n = countries.getSize();
    		m = indicators.getSize();
    		
			BufferedWriter out1 = new BufferedWriter(new OutputStreamWriter(
    			    new FileOutputStream("data/queries1.csv"), "UTF-8")); 		
			BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(
					    new FileOutputStream("data/queries2.csv"), "UTF-8")); 
    		
			//generate 50 queries to transform data of one country
	        for(i=0; i<50;i++){
	        	j = random.nextInt(n-1);
	        	k = random.nextInt(m-1);
	        	sCountry = countries.getIso2Code(j);
	        	sIndicator = indicators.getCodeOfSubject(k);
	        	sQuery = "http://statspace.linkedwidgets.org/rml?rmlsource=http://statspace.linkedwidgets.org/mapping/wb.ttl&refArea="+sCountry+"&indicator="+sIndicator+"&cache=no";	        	
	        	out1.write(sQuery);	        	
	        	out1.write("\n");
	        	sQuery = "http://statspace.linkedwidgets.org/rml?rmlsource=http://statspace.linkedwidgets.org/mapping/wb.ttl&refArea=all&indicator="+sIndicator+"&cache=no";
	        	out2.write(sQuery);	        	
	        	out2.write("\n");
	        }
	       for(i=0; i<=7; i++){
	    	   sQuery = "http://statspace.linkedwidgets.org/rml?rmlsource=http://statspace.linkedwidgets.org/mapping/uk"+i+".ttl";	        	
	    	   out1.write(sQuery);	        	
	    	   out1.write("\n");
	       }      
	       out1.close();	      
	       out2.close(); 
    	}catch(IOException e){
    		e.printStackTrace();
    	}		
	}
		
	public static void evaluatePerformance() {		
		BufferedReader br = null;
		String s, sCountry, sIndicator, line, sOutput, sRMLSource;
		Parameters parameters;
		ArrayList<String> arrQueries = new ArrayList<String>();		
		ArrayList<String> arrTimes = new ArrayList<String>();
		ArrayList<Integer> arrIndex = new ArrayList<Integer>();	
		long lStartTime, lEndTime, difference;
		int i, j, k;
		try {	 
			//read random queries
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream("data/queries1.csv"), "UTF-8"));		
			while ((line = br.readLine()) != null) {				
				line = line.trim();
				arrQueries.add(line);				
			}
			br.close();
			
			for(i=0; i<arrQueries.size(); i++){	
				parameters = new Parameters();
				s = arrQueries.get(i);			
				if(i<50){						
					j = s.indexOf("refArea");	
					j = s.indexOf("=",j);
					sCountry = s.substring(j+1, j+3);
					j = s.indexOf("=", j+1);
					sIndicator = s.substring(j+1);
					j = sIndicator.indexOf("&");
					sIndicator = sIndicator.substring(0, j);					
					parameters.addParameterValue("refArea", sCountry);
					parameters.addParameterValue("indicator", sIndicator);	
					sRMLSource  = s.substring(s.indexOf("=")+1, s.indexOf("&"));	
				}else{
					j = s.indexOf("=");	
					k = s.indexOf("&", j);	
					sRMLSource = s.substring(j+1, k);
					
				}
				System.out.print(i+"\t");				
				        	
	        	String sTime="";	        
	        	for(k=1; k<=3; k++){
	        		sOutput = "data/test/1_"+i + "_" + k + ".rdf";
		        	lStartTime = new Date().getTime();    	
		            RMLMapping mapping = RMLMappingFactory.extractRMLMapping(sRMLSource, parameters);
		            if(mapping == null){
		            	System.out.println("Can not read mapping");
		            	arrIndex.add(i);
		            	sTime="NULL";
		            	break;
		            }else{            
			            RMLEngine engine = new RMLEngine();
			            engine.runRMLMapping(mapping, "", sOutput, true);
			            if(engine.getStatus()==false){
			            	System.out.println("Can not read data set");
			            	arrIndex.add(i);
			            	sTime="NULL";
			            	break;
			            }		           
		            }
					lEndTime = new Date().getTime();
					difference = lEndTime - lStartTime;
					System.out.print("\t" + difference);
					if(sTime.isEmpty())
						sTime = Long.toString(difference);
					else
						sTime = sTime + "\t" + Long.toString(difference);					
					delay(60);
	        	}
	        	System.out.println();
	        	arrTimes.add(sTime);
	        	
	        	File file =new File("data/result1.csv");	    		
	    		//if file doesnt exists, then create it
	    		if(!file.exists()){
	    			file.createNewFile();
	    		}
	    		
	    		//true = append file
	    		FileWriter fileWritter = new FileWriter("data/result1.csv",true);
    	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
    	        bufferWritter.write(sTime);
    	        bufferWritter.write("\n");
    	        bufferWritter.close();
			}	
			
			//write results
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream("data/exceptions1.csv"), "UTF-8"));
			BufferedWriter out1 = new BufferedWriter(new OutputStreamWriter(
    			    new FileOutputStream("data/result1_2.csv"), "UTF-8")); 	
			for(i=0; i<arrIndex.size();i++){		                	
			   	out.write(arrIndex.get(i).toString());	        	
			   	out.write("\n");		        	
			}
			out.close();
			for(i=0; i<arrTimes.size();i++){		                	
		       	out1.write(arrTimes.get(i));	        	
		       	out1.write("\n");		        	
			}
		    out1.close();  
		    
		    
		   //quries for all countries;
		    arrQueries.clear();
		    arrTimes.clear();
		    arrIndex.clear();
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream("data/queries2.csv"), "UTF-8"));		
			while ((line = br.readLine()) != null) {				
				line = line.trim();
				arrQueries.add(line);				
			}
			br.close();			
			
			for(i=0; i<arrQueries.size(); i++){				
				s = arrQueries.get(i);				
				j = s.indexOf("refArea");	
				j = s.indexOf("=",j);						
				sCountry = "all";
				j = s.indexOf("=", j+1);
				sIndicator = s.substring(j+1);
				j = sIndicator.indexOf("&");
				sIndicator = sIndicator.substring(0, j);
				parameters = new Parameters();
				parameters.addParameterValue("refArea", sCountry);
				parameters.addParameterValue("indicator", sIndicator);				
				System.out.print(i+"\t");				
				sRMLSource  = s.substring(s.indexOf("=")+1, s.indexOf("&"));	             	
	        	String sTime="";	       
	        	for(k=1; k<=3; k++){
	        		sOutput = "data/test/2_"+i + "_" + k + ".rdf";
		        	lStartTime = new Date().getTime();    	
		            RMLMapping mapping = RMLMappingFactory.extractRMLMapping(sRMLSource, parameters);
		            if(mapping == null){		            	
		            	arrIndex.add(i);
		            	sTime="NULL";
		            	break;
		            }else{            
			            RMLEngine engine = new RMLEngine();
			            engine.runRMLMapping(mapping, "", sOutput, true);
			            if(engine.getStatus()==false){			            
			            	arrIndex.add(i);
			            	sTime="NULL";
			            	break;
			            }		           
		            }		        	
					lEndTime = new Date().getTime();
					difference = lEndTime - lStartTime;
					System.out.print("\t" + difference);
					if(sTime.isEmpty())
						sTime = Long.toString(difference);
					else
						sTime = sTime + "\t" + Long.toString(difference);					
					delay(70);
	        	}
	        	System.out.println();
	        	arrTimes.add(sTime);
	        	
	        	File file =new File("data/result2.csv");	    		
	    		//if file doesnt exists, then create it
	    		if(!file.exists()){
	    			file.createNewFile();
	    		}
	    		
	    		//true = append file
	    		FileWriter fileWritter = new FileWriter("data/result2.csv",true);
    	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
    	        bufferWritter.write(sTime);
    	        bufferWritter.write("\n");
    	        bufferWritter.close();
			}	
			
			//write results
			BufferedWriter out3 = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream("data/exceptions2.csv"), "UTF-8"));		
			BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(
					    new FileOutputStream("data/resul2_2.csv"), "UTF-8"));
			for(i=0; i<arrIndex.size();i++){		                	
			   	out3.write(arrIndex.get(i).toString());	        	
			   	out3.write("\n");		        	
			}
			out3.close();
			for(i=0; i<arrTimes.size();i++){		                	
		       	out2.write(arrTimes.get(i).toString());	        	
		       	out2.write("\n");		        	
			}
		    out2.close(); 		  
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void delay(int n){
		try {
		    Thread.sleep(n*1000);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}
	
	public static void createPrefix(){
		String qb = "http://purl.org/linked-data/cube#";		
		String sdmx_dimension = "http://purl.org/linked-data/sdmx/2009/dimension#";
		String sdmx_measure = "http://purl.org/linked-data/sdmx/2009/measure#";
		String sdmx_attribute = "http://purl.org/linked-data/sdmx/2009/attribute#";
		String sdmx_code = "http://purl.org/linked-data/sdmx/2009/code#";
		String vd = "http://rdfs.org/ns/void#";
		String dcterms = "http://purl.org/dc/terms/";
		String sdterms = "http://statspace.linkedwidgets.org/terms/";
		String dcat = "http://www.w3.org/ns/dcat#";
		String skos = "http://www.w3.org/2004/02/skos/core#";
		String owl = "http://www.w3.org/2002/07/owl#";
		String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
		String rdfs = "http://www.w3.org/2000/01/rdf-schema#";	
		
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
		try{
			FileOutputStream out = new FileOutputStream("data/StatSpace.owl");
			mOutput.write(out, "RDF/XML-ABBREV", null);				
		}catch(Exception e){
			System.out.println(e.toString());			
		}
		
	}
	public static void rml() {       
		String sInput, sOutput;
        Parameters parameters = new Parameters();       
        parameters.addParameterValue("indicator", "DC.DAC.DEUL.CD");
        parameters.addParameterValue("refArea", "EU");
        
        for(int i=0; i<1; i++){
        	sInput  = "data/mapping/wb.ttl";
        	sOutput = "data/test.rdf";   	

            RMLMapping mapping = RMLMappingFactory.extractRMLMapping(sInput, parameters);
            if(mapping == null){
            	System.out.println("Can not read mapping");
            }else{            
	            RMLEngine engine = new RMLEngine();
	            engine.runRMLMapping(mapping, "", sOutput, true);
	            if(engine.getStatus()==false){
	            	System.out.println("Can not read data set");
	            }
	            System.out.println("Finished");
            }
        }  
    }
	
	public static void mergeMetaData2(){
		try{
			//generate code lists
			StandardDimensions sd = new StandardDimensions();
			sd.generateCodeList();
			
			//merge with metadata
			Model model = ModelFactory.createDefaultModel();			
			model.read(new FileInputStream("data/code/cl_economicActivity.ttl"),null,"TTL");
			model.read(new FileInputStream("data/code/cl_age.ttl"),null,"TTL");
			model.read(new FileInputStream("data/code/cl_area.ttl"),null,"TTL");
			model.read(new FileInputStream("data/code/cl_civilStatus.ttl"),null,"TTL");		
			model.read(new FileInputStream("data/code/cl_cofog.ttl"),null,"TTL");		
			model.read(new FileInputStream("data/code/cl_coicop.ttl"),null,"TTL");		
			model.read(new FileInputStream("data/code/cl_copni.ttl"),null,"TTL");
			model.read(new FileInputStream("data/code/cl_copp.ttl"),null,"TTL");			
			model.read(new FileInputStream("data/code/cl_currency.ttl"),null,"TTL");	
			model.read(new FileInputStream("data/code/cl_educationLev.ttl"),null,"TTL");		
			model.read(new FileInputStream("data/code/cl_freq.ttl"),null,"TTL");
			model.read(new FileInputStream("data/code/cl_occupation.ttl"),null,"TTL");	
			model.read(new FileInputStream("data/code/cl_period.ttl"),null,"TTL");			
			model.read(new FileInputStream("data/code/cl_sex.ttl"),null,"TTL");			
			model.read(new FileInputStream("data/code/cl_subject.ttl"),null,"TTL");		
			model.read(new FileInputStream("data/code/cl_unitMeasure.ttl"),null,"TTL");		
			model.read(new FileInputStream("data/metadata/cr.eionet.europa.eu_sparql.ttl"),null,"TTL");			
			model.read(new FileInputStream("data/metadata/opendatacommunities.org_sparql.xml.ttl"),null,"TTL");
			model.read(new FileInputStream("data/metadata/unodc.publicdata.eu_sparql.ttl"),null,"TTL");
			model.read(new FileInputStream("data/metadata/statistics.gov.scot_sparql.ttl"),null,"TTL");				
			model.read(new FileInputStream("data/metadata/ogd.ifs.tuwien.ac.at_sparql.ttl"),null,"TTL");			
			model.read(new FileInputStream("data/metadata/data.cso.ie_sparql.ttl"),null,"TTL");			
			model.read(new FileInputStream("data/metadata/semantic.eea.europa.eu_sparql.ttl"),null,"TTL");				
			model.read(new FileInputStream("data/metadata/data.europa.eu_euodp_sparqlep.ttl"),null,"TTL");		
			model.read(new FileInputStream("data/metadata/wb.ttl"),null,"TTL");
			model.read(new FileInputStream("data/metadata/uk.ttl"),null,"TTL");			
			OutputStream out = new FileOutputStream("data/metadata/statspace2.ttl" );
		    model.write( out, "Turtle", null );		    
		    System.out.println("Done!");
		}catch(Exception e){
			System.out.println(e.toString());
		}
	}	

	public static void mergeMetaData(){
		try{
			//generate code lists
			StandardDimensions sd = new StandardDimensions();
			sd.generateCodeList();
			
			//merge with metadata
			Model model = ModelFactory.createDefaultModel();
			InputStream is;
			is = FileManager.get().open("data/code/cl_economicActivity.ttl");	
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/code/cl_age.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/code/cl_area.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/code/cl_civilStatus.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/code/cl_cofog.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/code/cl_coicop.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/code/cl_copni.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/code/cl_copp.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/code/cl_currency.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/code/cl_educationLev.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/code/cl_freq.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/code/cl_occupation.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/code/cl_period.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/code/cl_sex.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/code/cl_subject.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/code/cl_unitMeasure.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/metadata/cr.eionet.europa.eu_sparql.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/metadata/opendatacommunities.org_sparql.xml.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/metadata/unodc.publicdata.eu_sparql.ttl");		
			model.read(is,null,"TTL");	
			is.close();			
			is = FileManager.get().open("data/metadata/statistics.gov.scot_sparql.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/metadata/ogd.ifs.tuwien.ac.at_sparql.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/metadata/data.cso.ie_sparql.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/metadata/semantic.eea.europa.eu_sparql.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/metadata/data.europa.eu_euodp_sparqlep.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/metadata/wb.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/metadata/uk.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			
			OutputStream out = new FileOutputStream( new File( "data/metadata/statspace.ttl" ));
		    model.write( out, "Turtle", null );
		    
		    System.out.println("Done!");
		}catch(Exception e){
			System.out.println(e.toString());
		}
	}	
	
}
