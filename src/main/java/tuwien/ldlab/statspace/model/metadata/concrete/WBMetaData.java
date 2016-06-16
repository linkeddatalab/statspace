package tuwien.ldlab.statspace.model.metadata.concrete;

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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.model.Parameters;
import be.ugent.mmlab.rml.model.RMLMapping;
import tuwien.ldlab.statspace.codelist.CL_Subject;
import tuwien.ldlab.statspace.codelist.CL_Area;
import tuwien.ldlab.statspace.codelist.CL_Unit_Measure;
import tuwien.ldlab.statspace.model.mediator.StringTriple;
import tuwien.ldlab.statspace.model.util.QB;
import tuwien.ldlab.statspace.model.widgetgeneration.DataSet;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class WBMetaData {
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
	private static String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static String rdfs = "http://www.w3.org/2000/01/rdf-schema#";
	private static String sRML ="http://statspace.linkedwidgets.org/rml?rmlsource=http://statspace.linkedwidgets.org/mapping/wb.ttl";
		
	static CL_Subject indicators = new CL_Subject();
	static CL_Area countries = new CL_Area();
	static CL_Area wbCountries = new CL_Area(true);	
	static DataSet ds;
	
	public static void main(String[] args) {	
//		System.out.println("Extracting list of indicators and countries from WorldBank");		
//		extractIndicatorFromHTML();
//		removeDuplicateIndicators();
//		extractCountryFromXML();
//		mergeWBandISO();
//		
		System.out.println("Querying Worldbank to download data, then saving to JSON files");		
//		downloadWBData();
//		checkMissingJSONFile();
		
		System.out.println("Using RMLProcessor to generate RDF file for each indicator");		
		createRDFFile();
//		checkMissingRDFFile();
		
		System.out.println("Anayzing RDF files to generate metadata");		
		createMetadata();
		mergeMetadata();
		System.out.println("Finished");		
	}	
	
	public static void checkIndicators(){
		CL_Subject indicators2 = new CL_Subject("data/indicators2.csv");
		int i, j;
		try{
			for(i=0; i<indicators.getSize(); i++){
				for(j=0; j<indicators2.getSize(); j++){
					if(indicators.getUri(i).equals(indicators2.getUri(j))){
						break;
					}
				}
				if(j==indicators2.getSize()){
					System.out.println(i + "\t" + indicators.getUri(i));
					File fList =new File("data/error.csv");
	  	      		if(!fList.exists()){
	  	      			fList.createNewFile();
	  	      		}     	      		
	  	      		FileWriter fWriter = new FileWriter("data/error.csv",true);
	      	        BufferedWriter buf = new BufferedWriter(fWriter);
	      	        buf.write(i+"\t"+indicators.getUri(i));
	      	        buf.newLine();
	      	        buf.close(); 	         
				}
			}
		}catch(Exception e){  
			
		}		
	}

	
	public static void mergeWBandISO(){		
		CL_Area wbCountries = new CL_Area(true);
		CL_Area isoCountries = new CL_Area(false);
		int i, j;
		try{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/countries.csv", false), "UTF-8"));
			for(j=0; j<wbCountries.getSize(); j++)
				out.write(wbCountries.getCountryName(j)+"\t"+wbCountries.getIso2Code(j)+"\t"+wbCountries.getIso3Code(j)+"\n");			
			
			for(i=0; i<isoCountries.getSize(); i++){
				for(j=0; j<wbCountries.getSize(); j++){
					if(isoCountries.getIso2Code(i).equalsIgnoreCase(wbCountries.getIso2Code(j)))
						break;
				}
				if(j==wbCountries.getSize()){					
					out.write(isoCountries.getCountryName(i)+"\t"+isoCountries.getIso2Code(i)+"\t"+isoCountries.getIso3Code(i)+"\n");
				}
			}
			out.close();
		}catch(Exception e){				
		}
	}
	
	public static void extractCountryFromXML(){
		try{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/wbcountries.csv", false), "UTF-8"));	
			String url = "http://api.worldbank.org/countries?per_page=1000";		
			URL obj = new URL(url);		
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();		
			con.setRequestMethod("GET"); 
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
	 
			int i, j;			
			String sTag;
			int responseCode = con.getResponseCode();			
			if(responseCode==200){			
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(con.getInputStream());
				doc.getDocumentElement().normalize();
				NodeList nList = doc.getElementsByTagName("wb:country");				
				for (i = 0; i < nList.getLength(); i++) {		
					StringTriple wbC = new StringTriple();
					Node nNode = nList.item(i);
					
					//iso3Code
					Element e = (Element) nNode;
					wbC.setSecondString(e.getAttribute("id"));
					
					//list of child nodes
					NodeList nNodeList = nNode.getChildNodes();					
					for(j=0; j<nNodeList.getLength(); j++){
						if(nNodeList.item(j).getNodeType() == Node.ELEMENT_NODE){							
							Element eElement = (Element) nNodeList.item(j);
							sTag=eElement.getTagName();												
							if(sTag.equalsIgnoreCase("wb:iso2Code")) wbC.setFirstString(eElement.getTextContent());
							else if(sTag.equalsIgnoreCase("wb:name")) wbC.setThirdString(eElement.getTextContent());																					
						}					
					}
					out.write(wbC.getThirdString()+ "\t" + wbC.getFirstString()+"\t"+wbC.getSecondString()+"\n");										
				}	
				out.close();
			}
		}catch(Exception e){				
		}				
	}
	
	public static void removeDuplicateIndicators(){
		try {
			BufferedReader br_in = new BufferedReader(new InputStreamReader(
    			    new FileInputStream("data/indicators_all_all.csv"), "UTF-8"));
			
			BufferedWriter br_out = new BufferedWriter(new OutputStreamWriter(
    			    new FileOutputStream("data/indicators_all.csv"), "UTF-8"));  		
						
			System.out.println("Starting...");
			
			String line, indicator, label;
			ArrayList<String> arrIndicators = new ArrayList<String>();
			int i;
			while ((line = br_in.readLine()) != null) {
				if(line.contains("http:")){
					i = line.indexOf("\t");
					indicator = line.substring(0, i);
					label = line.substring(i+1);
					if(!arrIndicators.contains(indicator)){
						arrIndicators.add(indicator);
						br_out.write(indicator + "\t" + label);
						br_out.newLine();
					}				
				}
				else{
					br_out.write(line);
					br_out.newLine();
				}
					
			}
			br_in.close();
			br_out.close();
			System.out.println("Finished");		
		} catch (IOException e) {
			System.out.println("Exception caught when writing file: " + e.toString());
		}	
	}
	
	
	public static void extractIndicatorFromHTML(){
		try {					
			String url = "http://data.worldbank.org/indicator/all";
			URL obj = new URL(url);						
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();		
			// optional default is GET
			con.setRequestMethod("GET");			 
			//add request header
			con.setRequestProperty("User-Agent", "Mozilla/5.0");	 
			int responseCode = con.getResponseCode();		
			System.out.println("\t" + responseCode);
			if(responseCode==200){	
				BufferedReader br_in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
				BufferedWriter br_out = new BufferedWriter(new OutputStreamWriter(
	    			    new FileOutputStream("data/indicators_all_feature.csv"), "UTF-8"));  		
							
				System.out.println("Starting...");
				
				String line, topic, indicator, label;
				int i, j, k;
				while ((line = br_in.readLine()) != null) {
					if(line.contains("h3 id='topic-")){
						System.out.println(line);		
						i = line.indexOf("class='view'");
						if(i!=-1){
							j = line.indexOf(">", i);
							k = line.indexOf("</h3><");
							if(j!=-1){
								topic = line.substring(j+1, k);
								topic = topic.replace("&amp;", "&").trim();
								br_out.write(topic);
								br_out.newLine();
							}
						}
					}else if(line.contains("<span class=\"field-content\">")){
//						System.out.println(line);					
						j = line.indexOf("<a href=\"");
						k = line.indexOf(">", j);
						if(k!=-1){
							indicator = line.substring(j+9, k-1);
							i = line.indexOf("</a>", k);
							if(i!=-1)
								label = line.substring(k+1, i);
							else
								label = line.substring(k+1);
							label = label.replace("&amp;", "&").trim();
							br_out.write(indicator + "\t" + label);
							br_out.newLine();
						}
						
					}
				}
				br_in.close();
				br_out.close();
				System.out.println("Finished");	
			}
		} catch (IOException e) {
			System.out.println("Exception caught when writing file: " + e.toString());
		}	
	}
	
	public static void downloadWBData(){
		int i, k;
		String sIndicator, url;
		try{
			BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(
    			    new FileOutputStream("data/error.csv"), "UTF-8"));	
			
			for(i=900; i<indicators.getSize(); i++){				
				sIndicator = indicators.getUri(i);
				k = sIndicator.indexOf("indicator");
				sIndicator = sIndicator.substring(k+10);				
				new File("data/wb/"+sIndicator).mkdir();
				
				System.out.println("----------------");
				System.out.println(i+"/"+indicators.getSize()+". " + sIndicator);				
				delay(1);
				
				//WB Request
				url = "http://api.worldbank.org/countries/all/indicators/"+ sIndicator + "?per_page=20000&format=json";
				URL obj = new URL(url);						
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();		
				// optional default is GET
				con.setRequestMethod("GET");			 
				//add request header
				con.setRequestProperty("User-Agent", "Mozilla/5.0");	 
				int responseCode = con.getResponseCode();		
				System.out.println("\t" + responseCode);
				if(responseCode==200){		
					//overwrite existing file
		    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
		    			    new FileOutputStream("data/wb/"+sIndicator+"/"+sIndicator+".json", false), "UTF-8"));			    				
		    		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
				    String inputLine;
				    while ((inputLine = in.readLine()) != null){	    	
				        out.write(inputLine);
				        out.write("\n");
				    }
				    in.close();
				    out.close();
				}else
	    			out2.write(i+"\t"+url+"\n");
			}			
			out2.close();
		}catch(Exception e){
			System.out.println(e.toString());			
		}
	}
	
	public static void checkMissingJSONFile(){		
		int i, k;
		String sIndicator;
		try{
			for(i=0; i<indicators.getSize(); i++){	
//			for(i=0; i<=100; i++){			
				sIndicator = indicators.getUri(i);
				k = sIndicator.indexOf("indicator");
				sIndicator = sIndicator.substring(k+10);
				File file = new File("data/wb/"+sIndicator+"/"+sIndicator+".json");
				if(!file.exists() || file.length()<1000){
					System.out.println("Missing: "+ i + "\t"+ sIndicator);					
					try{
						//reRun this request
						String url;						
						url = "http://api.worldbank.org/countries/all/indicators/"+ sIndicator + "?per_page=20000&format=json";
						URL obj = new URL(url);						
						HttpURLConnection con = (HttpURLConnection) obj.openConnection();		
						// optional default is GET
						con.setRequestMethod("GET");			 
						//add request header
						con.setRequestProperty("User-Agent", "Mozilla/5.0");	 
						int responseCode = con.getResponseCode();		
						System.out.println("\t" + responseCode);
						if(responseCode==200){		
							//overwrite existing file
				    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				    			    new FileOutputStream("data/wb/"+sIndicator+"/"+sIndicator+".json", false), "UTF-8"));			    				
				    		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
						    String inputLine;
						    while ((inputLine = in.readLine()) != null){	    	
						        out.write(inputLine);
						        out.write("\n");
						    }
						    in.close();
						    out.close();
						}			
					}catch(Exception e){
						System.out.println(e.toString());			
					}
				}
			}
			
		}catch(Exception e){
			System.out.println(e.toString());			
		}
	}
	
	public static void createRDFFile(){
		int i, k;
		String sIndicator, sInput, sOutput;
		
		for(i=0; i<indicators.getSize(); i++){
			sIndicator = indicators.getUri(i);			
			k = sIndicator.indexOf("indicator");
			sIndicator = sIndicator.substring(k+10);	
			System.out.println(i+"/"+indicators.getSize()+". " + sIndicator);
			sInput  = "data/mapping/wb_file.ttl";
			File dir = new File("data/wb/"+sIndicator+"/"+sIndicator+".json");
			if(dir.exists() && dir.length()>500){
				Parameters parameters = new Parameters();       
		        parameters.addParameterValue("indicator", sIndicator);
		        parameters.addParameterValue("refArea", "all");
		        sOutput = "data/wb/"+sIndicator+"/"+sIndicator+".rdf";  			       
		        
	            RMLMapping mapping = RMLMappingFactory.extractRMLMapping(sInput, parameters);
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
	}	
	
	public static void checkMissingRDFFile(){		
		int i, k;
		String sIndicator;
		try{
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
    			    new FileOutputStream("data/wb/error.csv", true), "UTF-8"));	
			
			for(i=0; i<indicators.getSize(); i++){			
				sIndicator = indicators.getUri(i);
				k = sIndicator.indexOf("indicator");
				sIndicator = sIndicator.substring(k+10);
				File file = new File("data/wb/"+sIndicator+"/"+sIndicator+".rdf");				
				if(!file.exists()){
					System.out.println("Missing: "+ i + "\t"+ sIndicator);		   				    				
		    		out.write(indicators.getUri(i)+"\n");		    					
				}
			}
			out.close();			
		}catch(Exception e){
			System.out.println(e.toString());			
		}
	}
	
	
	public static void createMetadata(){
		int i, j, k;
		String sIndicator;			
		
		try{
			for(i=0; i<indicators.getSize(); i++){		
//			for(i=0; i<=100; i++){			
				ds = new DataSet();
				
				System.out.println(i+"/"+indicators.getSize());
				sIndicator = indicators.getUri(i);
				k = sIndicator.indexOf("indicator");
				sIndicator = sIndicator.substring(k+10);
				
				//read RDF file
			    InputStream is = FileManager.get().open("data/wb/"+sIndicator+"/"+sIndicator+".rdf");			         
				Model mInput = ModelFactory.createDefaultModel().read(is, null, "N-TRIPLE");	
				
				//analyze the input model
				querySubject(mInput);
				queryUriandLabel(mInput);
				queryComponent(mInput);
				if(ds.getAttributeSize()==0) identifyAttribute(mInput);
				queryValue(mInput);
				
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
				
				Property pLabel = mOutput.createProperty(rdfs+"label");	
				
				//Metadata
				Resource rMetaData  = mOutput.createResource("http://statspace.linkedwidgets.org/metadata/WorldBank-"+sIndicator);
				
				//Provenance information				
		    	Property pPublisher = mOutput.createProperty(dcterms+"publisher");		    	
		    	rMetaData.addProperty(pPublisher, "World Bank");
		    	Property pSource = mOutput.createProperty(dcterms+"source");
		    	Resource rSource = mOutput.createResource(indicators.getUri(i));
		    	rMetaData.addProperty(pSource, rSource);		    	
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
		    	rDataSet.addProperty(pMethod, "API");
		    	Property pRML 	 = mOutput.createProperty(dcat+"accessURL");
		    	rDataSet.addProperty(pRML, mOutput.createResource(sRML+"&indicator="+sIndicator));
		    	rDataSet.addProperty(pLabel, ds.getLabel());    	
		      	Property pValue = mOutput.createProperty(rdf+"value");		    	
		      	
		    	//Component		    
		    	Property pComponent = mOutput.createProperty(qb+"component");
		    				
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
		    		System.out.println("Error at " + sIndicator);
				
				//Dimension
				for(j=0; j<ds.getDimensionSize(); j++){
					Resource rDimension = mOutput.createResource(ds.getDimensionUri(j));
					rDimension.addProperty(RDF.type, QB.DimensionProperty);
					rMetaData.addProperty(pComponent, rDimension);
					for(k=0; k<ds.getDimension(j).getValueSize(); k++){
						Resource rValue = mOutput.createResource(ds.getDimension(j).getValueUri(k));
				    	rDimension.addProperty(pValue, rValue);
				    	rDataSet.addProperty(pValue, rValue);
					}
				}
		
		      	FileOutputStream out = new FileOutputStream("data/wb/"+sIndicator+"/"+sIndicator+"_metadata.ttl");
				mOutput.write(out, "Turtle", null);	
				out.close();
			}
		}catch(Exception e){
			System.out.println(e.toString());			
		}
		
	}
	
	public static void mergeMetadata(){
		int i, k;
		String sIndicator;	
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
	        
	        
			for(i=0; i<indicators.getSize(); i++){
//			for(i=0; i<=100; i++){			
				sIndicator = indicators.getUri(i);
				k = sIndicator.indexOf("indicator");
				sIndicator = sIndicator.substring(k+10);
				InputStream is = FileManager.get().open("data/wb/"+sIndicator+"/"+sIndicator+"_metadata.ttl");		         
				Model model_tmp = ModelFactory.createDefaultModel().read(is, null, "TTL");				
				final_model.add(model_tmp);			
				is.close();
			}
			try (final OutputStream out = new FileOutputStream( new File( "data/metadata/wb.ttl" )) ) {
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
	
	public static void delay(int n){
		try {
		    Thread.sleep(n*20);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}
}
