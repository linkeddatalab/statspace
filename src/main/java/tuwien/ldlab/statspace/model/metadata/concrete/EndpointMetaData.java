package tuwien.ldlab.statspace.model.metadata.concrete;

import tuwien.ldlab.statspace.codelist.CL_Area;
import tuwien.ldlab.statspace.codelist.CL_Unit_Measure;
import tuwien.ldlab.statspace.codelist.StandardDimensions;
import tuwien.ldlab.statspace.model.mediator.StringCouple;
import tuwien.ldlab.statspace.model.mediator.StringTriple;
import tuwien.ldlab.statspace.model.metadata.Endpoints;
import tuwien.ldlab.statspace.model.metadata.GeoAreas;
import tuwien.ldlab.statspace.model.metadata.GoogleArea;
import tuwien.ldlab.statspace.model.metadata.GoogleAreas;
import tuwien.ldlab.statspace.model.util.QB;
import tuwien.ldlab.statspace.model.util.SpecialEndpointList;
import tuwien.ldlab.statspace.model.util.Support;
import tuwien.ldlab.statspace.model.widgetgeneration.Endpoint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class EndpointMetaData{
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
	private static String geo = "http://statspace.linkedwidgets.org/codelist/cl_area/";	
	private static String id = "http://reference.data.gov.uk/id/";	
	private static String intervals = "http://reference.data.gov.uk/def/intervals/";	
	
	private static CL_Area countries = new CL_Area();
	private static CL_Unit_Measure units = new CL_Unit_Measure();
	private static StandardDimensions dimensions = new StandardDimensions();
	private static Endpoints endpoints = new Endpoints();
	private static ArrayList<StringTriple> dsInfor = new ArrayList<StringTriple>();	
	private static ArrayList<StringCouple> attInfor = new ArrayList<StringCouple>();
	private static ArrayList<GoogleAreas> googleAreas = new ArrayList<GoogleAreas>();
	private static ArrayList<String> times = new ArrayList<String>();
	private static GeoAreas geoAreas = new GeoAreas();		
	private static String folderName="opendatacommunities.org";
	private static Model mOutput;
	
	public static void main(String[] args) {		
		System.out.println("Anayzing endpoint...");		
//		analyzeEndpoint();
		
		System.out.println("Using Google APIs to identify geographical areas...");		
//		queryArea();
		
		System.out.println("Creating metadata...");		
		createMetaData();
//		createMetaDataForArea();
		
		System.out.println("Finished");		
	}
	
	public static void analyzeEndpoint() {	
		int i,j,k,n,m;
		SpecialEndpointList specialList = new SpecialEndpointList("data/list.xml");	
		boolean bHTTP, bRemove, bFindOther, bCheck;
		String sUseDistinct, s, uri, time_value;		
		String sEndpoint;	
		String sEndpointForWidget;		
		
		for(i=0; i<endpoints.getSize(); i++){
			geoAreas.clear();
			times.clear();
			dsInfor.clear();
			
			sEndpoint = endpoints.getEndpoint(i);
			folderName = extractFolderName(sEndpoint);
			
			k=specialList.getEndpointIndex(sEndpoint);
			if(k!=-1){
				if(!specialList.getEndpointForQuery(k).equals(""))
					sEndpoint = specialList.getEndpointForQuery(k);				
				if(!specialList.getEndpointForWidget(k).equals(""))
        			sEndpointForWidget = specialList.getEndpointForWidget(k);
        		else
        			sEndpointForWidget = sEndpoint;
				
				bHTTP = specialList.getHTTPRequest(k);
				bRemove = specialList.getRemoveDuplicate(k);
				sUseDistinct = specialList.getUseDistinct(k);
				bFindOther = specialList.getFindOtherValue(k);			
			}else{			
				sEndpointForWidget = sEndpoint;
				bHTTP   = false;
				bRemove = false;
				sUseDistinct = "";
				bFindOther = true;
			}        		
			
			//query sparql endpoint					
			System.out.println("************************************");
			System.out.println("Endpoint: " + sEndpoint);
			Endpoint endpoint = new Endpoint(sEndpoint, sEndpointForWidget, bHTTP, bRemove, sUseDistinct, bFindOther);
			endpoint.queryDataSet();
			
			n = endpoint.getDataSet().size();			
			
			for(j=0; j<n; j++){
				if(endpoints.getEndpointMetaData(i).containDataSet(endpoint.getDataSet(j).getUri())==false)
					continue;		
				
				if(endpoint.getEndpointForQuery().contains("data.cso") && (endpoint.getDataSet(j).getUri().contains("persons-socio-economic/cty") || !(endpoint.getDataSet(j).getUri().endsWith("/cty"))))
					continue;	
				if((endpoint.getDataSet(j).getUri().startsWith("http://linkedwidgets.org/ontology")))
					continue;
				System.out.println("--------------------");	
				System.out.println((j+1) +". " + endpoint.getDataSet(j).getUri());				
				
				//analyze the endpoint
				endpoint.getDataSet(j).queryComponent(sEndpoint, bHTTP, sUseDistinct);
				endpoint.getDataSet(j).identifyReference();
				endpoint.getDataSet(j).queryValueandCache(sEndpoint, bHTTP, bFindOther, bRemove, true, true);
				uri = endpoint.getDataSet(j).getUri();
				s =  endpoint.getDataSet(j).getLabel();
				dsInfor.add(new StringTriple(uri, s, ""));
				
				if(endpoint.getDataSet(j).getAttributeSize()==1){					
					for(k=0; k<endpoint.getDataSet(j).getAttribute(0).getValueSize(); k++){
						uri = endpoint.getDataSet(j).getAttribute(0).getValueUri(k);
						s = endpoint.getDataSet(j).getAttribute(0).getValueLabel(k);
						attInfor.add(new StringCouple(uri, s));
					}						
				}
				//spatial dimension - value of dimension
				bCheck = false;
				for(k=0; k<endpoint.getDataSet(j).getDimensionSize();k++){		
				    if(endpoint.getDataSet(j).getDimension(k).getRefDimension()!=null &&
				    		endpoint.getDataSet(j).getDimension(k).getRefDimension().equals("http://purl.org/linked-data/sdmx/2009/dimension#refArea")){
						bCheck=true;
						for(m=0; m<endpoint.getDataSet(j).getDimension(k).getValueSize(); m++){
							s = endpoint.getDataSet(j).getDimension(k).getAreaLabel(m,"");
							uri = endpoint.getDataSet(j).getDimension(k).getValueUri(m);		
							geoAreas.addGeoArea(s, uri);		        		
			        	}
				    }
			    }
				
				//spatial dimension - label of dataset
				if(bCheck==false){
					s = countries.getCountryName(endpoint.getDataSet(j).getLabel());
					if(s=="")
						s= endpoints.getCountryName(i);				
					if(s!=""){	
						uri = endpoint.getDataSet(j).createSpecialGeoArea(s.replaceAll("\\s+",""));
						System.out.println("Detected geographical area from the label of dataset "+s);
						geoAreas.addGeoArea(s, uri);	
					}						
				}
				
				
				//temporal dimension - value of dimension
				bCheck = false;
				for(k=0; k<endpoint.getDataSet(j).getDimensionSize();k++){		
					 if(endpoint.getDataSet(j).getDimension(k).getRefDimension()!=null &&
							 endpoint.getDataSet(j).getDimension(k).getRefDimension().equals("http://purl.org/linked-data/sdmx/2009/dimension#refPeriod")){
						bCheck=true;
						for(m=0; m<endpoint.getDataSet(j).getDimension(k).getValueSize(); m++){							
							time_value = endpoint.getDataSet(j).getDimension(k).getValueUri(m);													
							times.add(time_value);	        		
			        	}
				    }
			    }
				//temporal dimension - label of dataset
				if(bCheck==false){
					time_value =  endpoint.getDataSet(j).identifyTimeValue();					
					if(time_value!=""){	
						System.out.println("Detected time value from the label of dataset " + time_value);						
						times.add(time_value);
					}					
				}
			}	
			geoAreas.sortInAscending();
			writeArea2File();	
			writeTime2File();
			writeInfor2File();
		}		
		System.out.println("Done");
	}
	
	
	public static void queryArea(){
		int from, end, i, index;
		from=20; end=4000;
		String geo = "https://maps.googleapis.com/maps/api/geocode/xml?address=";
		String sLabel, sUri, sUri_BroaderArea, sLabel_BroaderArea, sQuery="", url;	 
		Boolean bSpecial, bUseBroaderArea;		
		
		if(geoAreas.getSize()==0)
			readAreaList();
		File folder = new File("data/area/"+folderName);		
		if (!folder.exists()) 
		    folder.mkdir();
		
		for(i=0; i<geoAreas.getSize(); i++){			
			if(i<from) continue;				
			if(i>end) break;			
			delay(2);			
			
		    sLabel = geoAreas.getLabel(i);		  
		    sUri = geoAreas.getUri(i);
		    if(sLabel.isEmpty()) continue;
		    
		   //special cases
			if(!(sLabel.toLowerCase().equals("north korea")||
					sLabel.toLowerCase().equals("south korea")||
					sLabel.toLowerCase().equals("western sahara")||
					sLabel.toLowerCase().equals("northern mariana islands")||
					sLabel.toLowerCase().equals("western sahara")||
					sLabel.toLowerCase().equals("south africa")||
					sLabel.toLowerCase().equals("south sudan"))){
				
				 if(sLabel.toLowerCase().startsWith("west ")||sLabel.toLowerCase().endsWith(" west")) 
					 sLabel = sLabel.replace("west", "").replace("WEST", "").trim();
				
				 if(sLabel.toLowerCase().startsWith("north ")||sLabel.toLowerCase().endsWith(" north"))
					 sLabel = sLabel.replace("north", "").replace("NORTH", "").trim();
					
				 if(sLabel.toLowerCase().startsWith("nord")||sLabel.toLowerCase().endsWith("nord"))
					 sLabel = sLabel.replace("nord", "").replace("NORD", "");	
				 
				 if(sLabel.toLowerCase().startsWith("south")||sLabel.toLowerCase().endsWith("south"))
					sLabel = sLabel.replace("south", "").replace("SOUTH", "");
					
				 if(sLabel.toLowerCase().startsWith("süd")||sLabel.toLowerCase().endsWith("süd"))
					sLabel = sLabel.replace("sud", "").replace("SÜD", "");
				
				 if(sLabel.toLowerCase().startsWith("east")||sLabel.toLowerCase().endsWith("east"))
					sLabel = sLabel.replace("east", "").replace("EAST", "");
				
				 if(sLabel.toLowerCase().startsWith("ost")||sLabel.toLowerCase().endsWith("ost"))
					sLabel = sLabel.replace("ost", "").replace("OST", "");
					
				 if(sLabel.toLowerCase().startsWith("mittel"))
					sLabel = sLabel.replace("mittel", "").replace("MITTEL", "");		
				 
				 sLabel = sLabel.trim();
				 if(sLabel.length()<2)
					 sLabel = geoAreas.getLabel(i);	
			}
			
			//special case
//			sLabel = sLabel.replace("Community Health Partnership", "").trim();
			if(folderName.equals("opendatacommunities.org")){
				if(sLabel.contains(" E"))
					sLabel = sLabel.substring(0, sLabel.indexOf(" E"));	
				sLabel = sLabel + " England";
			}
			
				    
		    //find broader area
		    index = geoAreas.indexOfBroaderArea(sUri);
		    if(index!=-1){ 		    	
		    	sLabel_BroaderArea = geoAreas.getLabel(index);	
		    	if(!(sLabel_BroaderArea.toLowerCase().startsWith("west")||sLabel_BroaderArea.toLowerCase().endsWith("west")||
		    			sLabel_BroaderArea.toLowerCase().startsWith("north")||sLabel_BroaderArea.toLowerCase().endsWith("north")||
		    			sLabel_BroaderArea.toLowerCase().startsWith("nord")||sLabel_BroaderArea.toLowerCase().endsWith("nord")||
		    			sLabel_BroaderArea.toLowerCase().startsWith("south ")||sLabel_BroaderArea.toLowerCase().endsWith("south")||
		    			sLabel_BroaderArea.toLowerCase().startsWith("süd")||sLabel_BroaderArea.toLowerCase().endsWith("süd")||
		    			sLabel_BroaderArea.toLowerCase().startsWith("east")||sLabel_BroaderArea.toLowerCase().endsWith("east")||
		    			sLabel_BroaderArea.toLowerCase().startsWith("ost")||sLabel_BroaderArea.toLowerCase().endsWith("ost")||					
		    			sLabel_BroaderArea.toLowerCase().startsWith("mittel"))){
					
				   	if(!sLabel.equalsIgnoreCase(sLabel_BroaderArea)){
			    		sQuery = sLabel + " " + sLabel_BroaderArea;
			    		bUseBroaderArea = true;
			    	}
			    	else{
			    		sUri_BroaderArea = geoAreas.getUri(index);
			    		index = geoAreas.indexOfBroaderArea(sUri_BroaderArea);
			    		if(index!=-1){
			    			sLabel_BroaderArea = geoAreas.getLabel(index);
			    			if(!sLabel.equalsIgnoreCase(sLabel_BroaderArea)){
					    		sQuery = sLabel + " " + sLabel_BroaderArea;
					    		bUseBroaderArea = true;
			    			}
					    	else{
					    		sQuery = sLabel;
					    		bUseBroaderArea = false;
					    	}
			    		}else{
			    			sQuery = sLabel;
			    			bUseBroaderArea = false;
			    		}
			    	}
		    	}
		    	else{
		    		bUseBroaderArea = false;
		    		sQuery = sLabel;
		    		bSpecial = true;
		    	}
		    }else{
		    	//special cases
				if(sUri.contains("bwd.eea.europa.eu/resource/provinces")){
					index = sUri.length()-1;
					while(index>0 && sUri.charAt(index)!='/') index--;
					sUri = sUri.substring(index+1);
					sUri = sUri.substring(0,  2);
					sQuery = sLabel + " " + countries.getCountryNameByISO(sUri);
					bUseBroaderArea = true;
				}
				else{
					bUseBroaderArea = false;
					sQuery = sLabel;
				}
		    }		    
		 
		    //Use Google Geocoding service to detect area	
		  	try{
				url = geo + URLEncoder.encode(sQuery, "UTF-8");
				URL obj = new URL(url);		
				
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();		
				con.setRequestMethod("GET");			 
				con.setRequestProperty("User-Agent", "Mozilla/5.0");	 
				int responseCode = con.getResponseCode();
				bSpecial = true;
				
				if(responseCode==200){
					bSpecial = false;
					System.out.println(i + "\t" + sQuery);
					//create file
				   	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
		    			    new FileOutputStream("data/area/"+folderName+"/"+i+".xml"), "UTF-8"));			    				
		    		BufferedReader in = new BufferedReader(new InputStreamReader(
                            con.getInputStream(), "UTF-8"));
				    String inputLine;
				    while ((inputLine = in.readLine()) != null){
				    	if(inputLine.contains("OVER_QUERY_LIMIT")){
				    		System.out.println("OVER QUERY LIMIT");					    		
				    		return;
				    	}else if(inputLine.contains("ZERO_RESULTS")||
				    			inputLine.contains("REQUEST_DENIED")||
				    			inputLine.contains("INVALID_REQUEST")||
				    			inputLine.contains("UNKNOWN_ERROR"))
				    		bSpecial = true;				    	
				        out.write(inputLine);
				        out.write("\n");
				    }
				    in.close();
				    out.close();
				}					
				//write to file data/replace.csv
				if(bSpecial){
					//try second time if we already used label of broader area
					if(bUseBroaderArea){
						url = geo + URLEncoder.encode(sLabel, "UTF-8");
						obj = new URL(url);		
						
						con = (HttpURLConnection) obj.openConnection();								
						con.setRequestMethod("GET");							
						con.setRequestProperty("User-Agent", "Mozilla/5.0");	 
						responseCode = con.getResponseCode();					
						
						if(responseCode==200){							
							System.out.println(i + "\t" + sLabel +"\t *** Requery");
							//create file
							BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				    			    new FileOutputStream("data/area/"+folderName+"/"+i+".xml"), "UTF-8"));			    				
				    		BufferedReader in = new BufferedReader(new InputStreamReader(
		                            con.getInputStream(), "UTF-8"));
						    String inputLine;
						    while ((inputLine = in.readLine()) != null){
						    	if(inputLine.contains("OVER_QUERY_LIMIT")){
						    		System.out.println("OVER QUERY LIMIT");					    		
						    		return;
						    	}					    	
						        out.write(inputLine);
						        out.write("\n");
						    }
						    in.close();
						    out.close();						
						}
					}					
		    	}				
				
			}catch(Exception e){			
			}			    
		}
		System.out.println("Done!");
		
	}
	
	//query special areas
	public static void requeryArea(){		
		//Read file CSV
		String csvFile = "data/missing.csv";
		String geo = "https://maps.googleapis.com/maps/api/geocode/xml?address=";
		BufferedReader br = null;
		String s, url, sIndex, sNew;
		int index;
		
		Boolean bSpecial;
		try {	 
			br = new BufferedReader(new InputStreamReader(new FileInputStream(csvFile), "UTF-8"));
			while ((s = br.readLine()) != null) {				
				delay(2);
				/*Index \t sOld \t sNew
				 * 100  ABC   DEF
				 */
				
			    s = s.trim();	
			    sIndex = s.substring(0, s.indexOf("\t"));
			    index = Integer.parseInt(sIndex);
			    s = s.substring(s.indexOf("\t")+1);
			    sNew = s.substring(s.indexOf("\t")).replaceAll("\t", "").trim();
			    
			    //Use Google Geocoding service to detect area			  
			    url = geo + URLEncoder.encode(sNew, "UTF-8");

				URL obj = new URL(url);				
				try{
					HttpURLConnection con = (HttpURLConnection) obj.openConnection();		
					// optional default is GET
					con.setRequestMethod("GET");			 
					//add request header
					con.setRequestProperty("User-Agent", "Mozilla/5.0");	 
					int responseCode = con.getResponseCode();
					bSpecial = true;
					
					if(responseCode==200){
						bSpecial = false;
						System.out.println(index + "\t" + s);
						
						//overwrite existing file
			    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
			    			    new FileOutputStream("data/area/"+folderName+index+".xml", false), "UTF-8"));			    				
			    		BufferedReader in = new BufferedReader(new InputStreamReader(
                                con.getInputStream(), "UTF-8"));
					    String inputLine;
					    while ((inputLine = in.readLine()) != null){
					    	if(inputLine.contains("OVER_QUERY_LIMIT")){
					    		System.out.println("OVER QUERY LIMIT");					    		
					    		return;
					    	}else if(inputLine.contains("ZERO_RESULTS")||
					    			inputLine.contains("REQUEST_DENIED")||
					    			inputLine.contains("INVALID_REQUEST")||
					    			inputLine.contains("UNKNOWN_ERROR"))
					    		bSpecial = true;
					    	
					        out.write(inputLine);
					        out.write("\n");
					    }
					    in.close();
					    out.close();
					}				
					//write to file data/replace.csv
					if(bSpecial){						
						File file =new File("data/missing2.csv");    		
			    		if(!file.exists()){
			    			file.createNewFile();
			    		}
			    		//append
			    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
			    			    new FileOutputStream(file, true), "UTF-8"));			    			
			    		out.write(String.valueOf(index));
			    		out.write("\t");
			    	    out.write(sNew);
			    	    out.write("\n");
			    	    out.close();				    	    
			    	}					
				}catch(Exception e){			
				}			    
			}
			System.out.println("Done!");
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
	
	public static void createMetaData() {	
		int i,j,k,n,v,m,t,index,size;
		SpecialEndpointList specialList = new SpecialEndpointList("data/list.xml");	
		boolean bHTTP, bRemove, bFindOther, bArea, bTime;
		String sUseDistinct, uri, time_value, s, aUri, aLabel, dUri, dLabel, dRefUri, mUri, mLabel, vUri, vLabel, vRefUri;		
		String sEndpoint;	
		String sEndpointForWidget;	
		
		for(i=0; i<endpoints.getSize(); i++){	
			
			//create an empty Jena Model and set required prefix		
			mOutput = ModelFactory.createDefaultModel();	
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
			mOutput.setNsPrefix("rdf", rdf);
			mOutput.setNsPrefix("rdfs", rdfs);
			
			sEndpoint = endpoints.getEndpoint(i);
			folderName = extractFolderName(sEndpoint);
					
			//create Metadata for Area
			geoAreas.clear();
			googleAreas.clear();
			dsInfor.clear();
			readSubject();			
			createMetaDataForArea();	
			
			k=specialList.getEndpointIndex(sEndpoint);
			if(k!=-1){
				if(!specialList.getEndpointForQuery(k).equals(""))
					sEndpoint = specialList.getEndpointForQuery(k);				
				if(!specialList.getEndpointForWidget(k).equals(""))
        			sEndpointForWidget = specialList.getEndpointForWidget(k);
        		else
        			sEndpointForWidget = sEndpoint;
				
				bHTTP = specialList.getHTTPRequest(k);
				bRemove = specialList.getRemoveDuplicate(k);
				sUseDistinct = specialList.getUseDistinct(k);
				bFindOther = specialList.getFindOtherValue(k);			
			}else{			
				sEndpointForWidget = sEndpoint;
				bHTTP   = false;
				bRemove = false;
				sUseDistinct = "";
				bFindOther = true;
			}        		
			
			//query sparql endpoint					
			System.out.println("************************************");
			System.out.println("Endpoint: " + sEndpoint);
			Endpoint endpoint = new Endpoint(sEndpoint, sEndpointForWidget, bHTTP, bRemove, sUseDistinct, bFindOther);
			endpoint.queryDataSet(); 
			n = endpoint.getDataSet().size();		
			if(n==0){
				System.out.println("\n!!!!!!!!!!!!!!!!!!!!!!!");				
			}	
			
			for(j=0; j<n; j++){		
				//if(j>160|| j<150) continue;				
				if(endpoints.getEndpointMetaData(i).containDataSet(endpoint.getDataSet(j).getUri())==false)
					continue;
				if(endpoint.getEndpointForQuery().contains("data.cso") && (endpoint.getDataSet(j).getUri().contains("persons-socio-economic/cty") || !(endpoint.getDataSet(j).getUri().endsWith("/cty"))))
					continue;		
				if((endpoint.getDataSet(j).getUri().startsWith("http://linkedwidgets.org/ontology")))
					continue;
				
				System.out.println((j+1) +". " + endpoint.getDataSet(j).getUri());
				endpoint.getDataSet(j).queryComponent(sEndpoint, bHTTP, sUseDistinct);	
				if(endpoint.getDataSet(j).getDimensionSize()==0 || endpoint.getDataSet(j).getMeasureSize()==0)
					continue;
				endpoint.getDataSet(j).queryValueandCache(sEndpoint, bHTTP, bFindOther, bRemove, true, false);
				endpoint.getDataSet(j).getDimension().identifyReferenceDimension();
				bArea = false;
				bTime = false;
				
				//create Metadata for describe components & values
		    	times.clear();	
		    	size = endpoint.getDataSet(j).getMeasureSize();
		    	
		    	Property pLabel = mOutput.createProperty(rdfs+"label");  	
		   
		    	for(t=0; t<size; t++){
		    		
		    		//Metadata
		    		Resource rMetaData;
		    		if(size==1)
		    			rMetaData = mOutput.createResource("http://statspace.linkedwidgets.org/metadata/"+ folderName + "_"+ j);
		    		else
		    			rMetaData = mOutput.createResource("http://statspace.linkedwidgets.org/metadata/"+ folderName + "_"+ j+"_measure_"+t);
					
					//Provenance information	
		    		if(!endpoints.getDataProvider(i).isEmpty()){		    			
		    			Property pPublisher = mOutput.createProperty(dcterms+"publisher");
		    			s = endpoints.getDataProvider(i);
		    			if(s.startsWith("http")){
		    				Resource rPublisher = mOutput.createResource(s);
		    				rMetaData.addProperty(pPublisher, rPublisher);
		    			}else
		    				rMetaData.addProperty(pPublisher, endpoints.getDataProvider(i));
		    		}		    		
			    	Property pSource = mOutput.createProperty(dcterms+"source");
			    	Resource rSource = mOutput.createResource(endpoint.getEndpointForQuery());
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
			    	Resource rDataSet   = mOutput.createResource(endpoint.getDataSet(j).getUri());
			    	Property pDataSet 	= mOutput.createProperty(qb+"dataSet");
			    	rMetaData.addProperty(pDataSet, rDataSet);		
			    	Property pSubject = mOutput.createProperty(dcterms+"subject");
			    	for(k=0; k<dsInfor.size(); k++)
			    		if(dsInfor.get(k).getFirstString().equals(endpoint.getDataSet(j).getUri()))
		    				if(!dsInfor.get(k).getThirdString().isEmpty())
		    					rDataSet.addProperty(pSubject, mOutput.createResource("http://statspace.linkedwidgets.org/codelist/cl_subject/"+dsInfor.get(k).getThirdString()));
		    				else
		    					rDataSet.addProperty(pSubject, mOutput.createResource("http://statspace.linkedwidgets.org/codelist/cl_subject/UN.UND.UNDE"));
			      	Property pMethod = mOutput.createProperty(vd+"feature");
			    	rDataSet.addProperty(pMethod, "SPARQL endpoint");
			    	Property pRML 	 = mOutput.createProperty(dcat+"accessURL");
			    	rDataSet.addProperty(pRML, mOutput.createResource(endpoint.getEndpointForQuery()));
			    	if(!endpoint.getDataSet(j).getLabel().isEmpty())			    		
			    		rDataSet.addProperty(pLabel, endpoint.getDataSet(j).getLabel());
			    	
			      	Property pValue = mOutput.createProperty(rdf+"value");		
			      	
			        //Component		    
			    	Property pComponent = mOutput.createProperty(qb+"component");			    
			    	Property pSameAs  = mOutput.createProperty(owl+"sameAs");
			    	Resource rTempProperty = mOutput.createProperty(sdterms+"HiddenProperty");		
			    	rTempProperty.addProperty(RDFS.label, "This component is not defined in data structure. However, it is added in metadata to support data integration and exploration");
			    	  	
			    	//Measure			    	
		    		mUri = endpoint.getDataSet(j).getMeasureUri(t);
		    		mLabel = endpoint.getDataSet(j).getMeasureLabel(t);
		    		Resource rMeasure   = mOutput.createResource(mUri);
			    	rMeasure.addProperty(RDF.type, QB.MeasureProperty);
			    	if(!mLabel.isEmpty())
			    		rMeasure.addProperty(RDFS.label, mLabel);			    	
			    	rMetaData.addProperty(pComponent, rMeasure);  	
		    		if(!mUri.equalsIgnoreCase("http://purl.org/linked-data/sdmx/2009/measure#obsValue")){
		    			Resource rRefMeasure   = mOutput.createResource("http://purl.org/linked-data/sdmx/2009/measure#obsValue");
		    			rRefMeasure.addProperty(pSameAs, rMeasure);
		    		}  	 	
			      			    	
			    	//Attribute
			    	if(endpoint.getDataSet(j).getAttributeSize()>0){
			    		aUri   = endpoint.getDataSet(j).getAttributeUri(0);
			    		aLabel = endpoint.getDataSet(j).getAttributeLabel(0);
			    		Resource rAttribute = mOutput.createResource(aUri);
			    		rAttribute.addProperty(RDF.type, QB.AttributeProperty);
			    		if(!aLabel.isEmpty())
			    			rAttribute.addProperty(RDFS.label, aLabel);			    		
				    	rMetaData.addProperty(pComponent, rAttribute);	    
				    	if(!aUri.equalsIgnoreCase("http://purl.org/linked-data/sdmx/2009/attribute#unitMeasure")){
				    		Resource rRefAttribute = mOutput.createResource("http://purl.org/linked-data/sdmx/2009/attribute#unitMeasure");
			    			rRefAttribute.addProperty(pSameAs, rAttribute);
				    	}    	
			    		for(m=0; m<endpoint.getDataSet(j).getAttribute(0).getValueSize(); m++){
			    			vUri   = endpoint.getDataSet(j).getAttribute(0).getValueUri(m);
			    			vLabel = endpoint.getDataSet(j).getAttribute(0).getValueLabel(m);	    			
			    			Resource rValue = mOutput.createResource(vUri);
			    			if(!vLabel.isEmpty())
			    				rValue.addProperty(RDFS.label, vLabel);
			    			rAttribute.addProperty(pValue, rValue);
							rDataSet.addProperty(pValue, rValue);
							vRefUri = units.identifyReference(vUri, vLabel);
							if(!vRefUri.equalsIgnoreCase(vUri)){
								Resource rRefValue   = mOutput.createResource(vRefUri);						
								rRefValue.addProperty(pSameAs, rValue);
							}
			    		}		    	
			    	}else{
			    		Resource rAttribute = mOutput.createResource(endpoint.getDataSet(j).createSpecialAttribute());
			    		rAttribute.addProperty(RDF.type, QB.AttributeProperty);
			    		rAttribute.addProperty(RDFS.label, "unit of measure");			    	
			    		rAttribute.addProperty(RDF.type, rTempProperty);							
						Resource rValue = mOutput.createResource(units.getDefaultUnit());
						rAttribute.addProperty(pValue, rValue);	
	        			rDataSet.addProperty(pValue, rValue);
	        			rMetaData.addProperty(pComponent, rAttribute);
			    	}
			    			
			    	//Dimension			    	
					for(k=0; k<endpoint.getDataSet(j).getDimensionSize();k++){	
						Resource rDimension = mOutput.createResource(endpoint.getDataSet(j).getDimensionUri(k));
						rDimension.addProperty(RDF.type, QB.DimensionProperty);
						rMetaData.addProperty(pComponent, rDimension);					
						if(!endpoint.getDataSet(j).getDimensionLabel(k).isEmpty())
							rDimension.addProperty(RDFS.label, endpoint.getDataSet(j).getDimensionLabel(k));
						
						dUri  = endpoint.getDataSet(j).getDimensionUri(k);					
						dRefUri = endpoint.getDataSet(j).getDimension(k).getRefDimension();							
						if(dRefUri!=null && !dRefUri.equalsIgnoreCase(dUri)){
							Resource rRefDimension = mOutput.createResource(dRefUri);
							rRefDimension.addProperty(pSameAs, rDimension);
						}						
						
						//spatial dimension
						if(dRefUri!=null && dRefUri.equals("http://purl.org/linked-data/sdmx/2009/dimension#refArea")){									
							bArea=true;							
							for(v=0; v<endpoint.getDataSet(j).getDimension(k).getValueSize(); v++){							
								vUri   = endpoint.getDataSet(j).getDimension(k).getValueUri(v);
								index = geoAreas.getIndex(vUri);
				        		if(index!=-1){
				        			Resource rValue = mOutput.createResource(vUri);	
				        			if(!geoAreas.getLabel(index).isEmpty())
				        				rValue.addProperty(RDFS.label, geoAreas.getLabel(index));				        			
				        			rDimension.addProperty(pValue, rValue);
				        			rDataSet.addProperty(pValue, rValue);
				        			Resource rGoogleArea = mOutput.createResource(geo+googleAreas.get(index).getGoogleArea(0).getUri());
				        			rGoogleArea.addProperty(pSameAs, rValue);	
				        		}
				        	}
						}
						
						//temporal dimension
						else if(dRefUri!=null && dRefUri.equals("http://purl.org/linked-data/sdmx/2009/dimension#refPeriod")){									
							bTime=true;								
							for(v=0; v<endpoint.getDataSet(j).getDimension(k).getValueSize(); v++){							
								time_value   = endpoint.getDataSet(j).getDimension(k).getValueUri(v);
								vLabel = endpoint.getDataSet(j).getDimension(k).getValueLabel(v);
								times.add(time_value);								
								if(time_value.startsWith("http:")){
									Resource rValue = mOutput.createResource(time_value);									
									if(vLabel!="")
										rValue.addProperty(RDFS.label, vLabel);
									rDimension.addProperty(pValue, rValue);
									rDataSet.addProperty(pValue, rValue);								
								}
								else{
									rDimension.addProperty(pValue, time_value);
									rDataSet.addProperty(pValue, time_value);
								}			        				        		
				        	}
						}	
						//other dimensions
						else{
							for(v=0; v<endpoint.getDataSet(j).getDimension(k).getValueSize(); v++){							
								vUri   = endpoint.getDataSet(j).getDimension(k).getValueUri(v);
								vLabel   = endpoint.getDataSet(j).getDimension(k).getValueLabel(v);							
								if(vUri.startsWith("http")){
									Resource rValue = mOutput.createResource(vUri);
									if(!vLabel.isEmpty())
										rValue.addProperty(RDFS.label, vLabel);
									if(dRefUri!=null){
										vRefUri = dimensions.getValueReference(dRefUri, vUri, vLabel);
										if(vRefUri!=null && !vRefUri.equalsIgnoreCase(vUri)){
											Resource rRefValue = mOutput.createResource(vRefUri);
											rRefValue.addProperty(pSameAs, rValue);
										}
									}
									rDimension.addProperty(pValue, rValue);	
									rDataSet.addProperty(pValue, rValue);
								}
								else{
									rDimension.addProperty(pValue, vUri);	
									rDataSet.addProperty(pValue, vUri);
									if(dRefUri!=null){
										vRefUri = dimensions.getValueReference(dRefUri, vUri, vLabel);
										if(vRefUri!=null && !vRefUri.equalsIgnoreCase(vUri)){
											Resource rRefValue = mOutput.createResource(vRefUri);
											rRefValue.addProperty(pSameAs, vUri);
										}
									}
								}
											        			
				        	}
						}				
				    }
					
					//check area in label of dataset
					if(bArea==false){
						s = countries.getCountryName(endpoint.getDataSet(j).getLabel());	
						if(s=="")
							s= endpoints.getCountryName(i);
						uri = endpoint.getDataSet(j).createSpecialGeoArea(s.replaceAll("\\s+",""));
						index=geoAreas.getIndex(uri);
						if(index!=-1){
							Resource rDimension = mOutput.createResource(endpoint.getDataSet(j).createSpecialSpatialDimension());
							rDimension.addProperty(RDF.type, QB.DimensionProperty);
							rDimension.addProperty(RDFS.label, "Ref Area");
							rDimension.addProperty(RDF.type, rTempProperty);							
							Resource rValue = mOutput.createResource(uri);						
							rValue.addProperty(RDFS.label, s);
							Resource rGoogleArea  = mOutput.createResource(geo+googleAreas.get(index).getGoogleArea(0).getUri());		        				        					        			
							rGoogleArea.addProperty(pSameAs, rValue);		        			
		        			rDimension.addProperty(pValue, rValue);	
		        			rDataSet.addProperty(pValue, rValue);
		        			rMetaData.addProperty(pComponent, rDimension);
						}										
					}	
					
					//check time in label of dataset
					if(bTime==false){
						time_value =  endpoint.getDataSet(j).identifyTimeValue();						
						if(time_value!=""){									
							uri = endpoint.getDataSet(j).createSpecialTemporalValue(time_value);
							times.add(uri);
							Resource rDimension = mOutput.createResource(endpoint.getDataSet(j).createSpecialTemporalDimension());
							rDimension.addProperty(RDF.type, QB.DimensionProperty);
							rDimension.addProperty(RDFS.label, "Ref Period");							
							rDimension.addProperty(RDF.type, rTempProperty);								
							Resource rValue = mOutput.createResource(uri);
							rValue.addProperty(RDFS.label, time_value);						
		        			rDimension.addProperty(pValue, rValue);   
		        			rMetaData.addProperty(pComponent, rDimension);
						}										
					}					
					//create MetaData for time
					createMetaDataForTime();
				}
			}	
			try {
				FileOutputStream fout = new FileOutputStream("data/metadata/"+folderName+".ttl");
				System.out.println("--------------------------------------");
				System.out.println("Writing metadata...");
				mOutput.write(fout, "Turtle", null);		
			} catch (IOException e) {
				System.out.println("Exception caught when writing file: " + e.toString());
			}
			
		}			
		System.out.println("Done");
	}
	
	public static void createMetaDataForTime(){
		int i, j, n = times.size();	
		
		String sInterval ="[1-9][0-9]{3}-[1-9][0-9]{3}";
		String sYear     = "[1-9][0-9]{3}";
		String sMonth    = "[1-9][0-9]{3}-[0-1][0-9]";
		String sQuarter  = "[1-9][0-9]{3}-Q[1-4]";
		String sDate     = "[1-9][0-9]{3}-[0-1][0-9]-[0-3][0-9]";
		String value, time_value="";
		
		Pattern pInterval = Pattern.compile(sInterval);
		Pattern pYear = Pattern.compile(sYear);
		Pattern pQuarter = Pattern.compile(sQuarter);
		Pattern pMonth = Pattern.compile(sMonth);
		Pattern pDate = Pattern.compile(sDate);
		
		Resource rIntervalType = mOutput.createResource("http://rdfs.org/ns/void#Dataset");
		Resource rMonthType = mOutput.createResource(intervals + "CalendarMonth");
		Resource rDateType = mOutput.createResource(intervals + "CalendarDay");		
		Resource rYearType = mOutput.createResource(intervals + "CalendarYear");
		Resource rQuarterType = mOutput.createResource(intervals + "CalendarQuarter");
		Property pSameAs = mOutput.createProperty(owl+"sameAs");
		
		ArrayList<String> arrDate=new ArrayList<String>(); //array to store value of date
		Boolean bDate=false, bOther=false;
		
		Matcher m;
		
		for(i=0; i<n; i++){			
			value = times.get(i);
			if(value.startsWith(id+"gregorian"))
				continue;
			
			if(value.toLowerCase().contains("year")){
				//interval
				m = pInterval.matcher(value);
				if(m.find()){	
					bOther=true;
					time_value = value.substring(m.start(), m.end());
					String sFrom = time_value.substring(0, 4);
					String sEnd  = time_value.substring(5);
					int yFrom = Integer.parseInt(sFrom);
					int yEnd  = Integer.parseInt(sEnd);
					time_value = Integer.toString(yFrom)+"-01-01T00:00:00/P"+Integer.toString(yEnd-yFrom)+"Y";							
					Resource rUKTime   = mOutput.createResource(id+"gregorian-interval/"+time_value);
					rUKTime.addProperty(RDF.type, rIntervalType);
					if(value.startsWith("http")){
						Resource rValue = mOutput.createResource(value);
						if(rValue.getProperty(RDFS.label)==null)
							rValue.addProperty(RDFS.label,time_value);
						rUKTime.addProperty(pSameAs, rValue);
					}else{
						rUKTime.addProperty(pSameAs, value);
					}			
				}else{		
					//year
					m = pYear.matcher(value);
					if(m.find()){	
						bOther=true;
						time_value = value.substring(m.start(), m.end());	
						Resource rUKTime   = mOutput.createResource(id+"gregorian-year/"+time_value);
						rUKTime.addProperty(RDF.type, rYearType);
						if(value.startsWith("http")){
							Resource rValue = mOutput.createResource(value);
							if(rValue.getProperty(RDFS.label)==null)
								rValue.addProperty(RDFS.label,time_value);							
							rUKTime.addProperty(pSameAs, rValue);
						}else{
							rUKTime.addProperty(pSameAs, value);
						}
						createMetaDataForYear(time_value);
					}else
						System.out.println("Not found + " + value + "\t index: " + i);
				}
			}
			else if(value.toLowerCase().contains("quarter")){
				m = pQuarter.matcher(value);
				if(m.find()){	
					bOther=true;
					time_value = value.substring(m.start(), m.end());
					Resource rUKTime   = mOutput.createResource(id+"gregorian-quarter/"+time_value);
					rUKTime.addProperty(RDF.type, rQuarterType);
					if(value.startsWith("http")){
						Resource rValue = mOutput.createResource(value);
						if(rValue.getProperty(RDFS.label)==null)
							rValue.addProperty(RDFS.label,time_value);		
						rUKTime.addProperty(pSameAs, rValue);
					}else{
						rUKTime.addProperty(pSameAs, value);
					}
					createMetaDataForQuarter(time_value);
					
				}else
					System.out.println("Not found + " + value + "\t index: " + i);
			}
			else if(value.toLowerCase().contains("month")){
				m = pMonth.matcher(value);
				if(m.find()){
					bOther=true;
					time_value = value.substring(m.start(), m.end());	
					Resource rUKTime = mOutput.createResource(id+"gregorian-month/"+time_value);
					rUKTime.addProperty(RDF.type, rMonthType);
					if(value.startsWith("http")){
						Resource rValue = mOutput.createResource(value);
						if(rValue.getProperty(RDFS.label)==null)
							rValue.addProperty(RDFS.label,time_value);		
						rUKTime.addProperty(pSameAs, rValue);
					}else{
						rUKTime.addProperty(pSameAs, value);
					}					
				}else
					System.out.println("Not found + " + value + "\t index: " + i);
			}
			else if(value.toLowerCase().contains("date")){					
				m = pDate.matcher(value);
				if(m.find()){		
					bDate=true;						
					arrDate.add(value);
				}else
					System.out.println("Not found + " + value + "\t index: " + i);
			}
			else{
				m = pDate.matcher(value);
				if(m.find()){
					//date
					bDate=true;					
					arrDate.add(value);				
				}else{	
					//quarter								
					m = pQuarter.matcher(value);
					if(m.find()){	
						bOther=true;
						time_value = value.substring(m.start(), m.end());
						Resource rUKTime   = mOutput.createResource(id+"gregorian-quarter/"+time_value);
						rUKTime.addProperty(RDF.type, rQuarterType);
						if(value.startsWith("http")){
							Resource rValue = mOutput.createResource(value);
							if(rValue.getProperty(RDFS.label)==null)
								rValue.addProperty(RDFS.label,time_value);		
							rUKTime.addProperty(pSameAs, rValue);
						}else{
							rUKTime.addProperty(pSameAs, value);
						}
						createMetaDataForQuarter(time_value);						
					}else{
						//month						
						m = pMonth.matcher(value);
						if(m.find()){
							bOther=true;
							time_value = value.substring(m.start(), m.end());
							Resource rUKTime = mOutput.createResource(id+"gregorian-month/"+time_value);
							rUKTime.addProperty(RDF.type, rMonthType);							
							if(value.startsWith("http")){
								Resource rValue = mOutput.createResource(value);
								if(rValue.getProperty(RDFS.label)==null)
									rValue.addProperty(RDFS.label,time_value);		
								rUKTime.addProperty(pSameAs, rValue);
							}else{
								rUKTime.addProperty(pSameAs, value);
							}				
						}
						else{
							//interval
							m = pInterval.matcher(value);
							if(m.find()){	
								bOther=true;
								time_value = value.substring(m.start(), m.end());
								String sFrom = time_value.substring(0, 4);
								String sEnd  = time_value.substring(5);
								int yFrom = Integer.parseInt(sFrom);
								int yEnd  = Integer.parseInt(sEnd);
								time_value = Integer.toString(yFrom)+"-01-01T00:00:00/P"+Integer.toString(yEnd-yFrom)+"Y";							
								Resource rUKTime   = mOutput.createResource(id+"gregorian-interval/"+time_value);
								rUKTime.addProperty(RDF.type, rIntervalType);
								if(value.startsWith("http")){
									Resource rValue = mOutput.createResource(value);
									if(rValue.getProperty(RDFS.label)==null)
										rValue.addProperty(RDFS.label,time_value);		
									rUKTime.addProperty(pSameAs, rValue);
								}else{
									rUKTime.addProperty(pSameAs, value);
								}			
							}else{	
								//year							
								m = pYear.matcher(value);
								if(m.find()){
									bOther=true;
									time_value = value.substring(m.start(), m.end());
									Resource rUKTime   = mOutput.createResource(id+"gregorian-year/"+time_value);
									rUKTime.addProperty(RDF.type, rYearType);
									if(value.startsWith("http")){
										Resource rValue = mOutput.createResource(value);
										if(rValue.getProperty(RDFS.label)==null)
											rValue.addProperty(RDFS.label,time_value);		
										rUKTime.addProperty(pSameAs, rValue);
									}else{
										rUKTime.addProperty(pSameAs, value);
									}
									createMetaDataForYear(time_value);
								}
								else
									System.out.println("Not found + " + value + "\t index: " + i);
							}
						}
							
					}
				}				
			}		
		}
		
		if(bDate==true && bOther==false && arrDate.size()>0){	
			//if every value in this dataset has format YYYY-01-01 or YYYY-12-31 => only get Year			
			for(i=0; i<arrDate.size(); i++)
				if(!(arrDate.get(i).contains("-01-01")||arrDate.get(i).contains("-12-31")))
					break;
			
			if(i<arrDate.size()){				
				for(j=0; j<arrDate.size(); j++){
					value = arrDate.get(j);
					m = pDate.matcher(value);
					if(m.find()){
						time_value = value.substring(m.start(), m.end());	
						Resource rUKTime   = mOutput.createResource(id+"gregorian-date/"+time_value);
						rUKTime.addProperty(RDF.type, rDateType);
						if(value.startsWith("http")){
							Resource rValue = mOutput.createResource(value);
							rValue.addProperty(RDFS.label,time_value);
							rUKTime.addProperty(pSameAs, rValue);
						}else{
							rUKTime.addProperty(pSameAs, value);
						}	
					}
				}									
			}else{
				System.out.println("Year");
				for(i=0; i<arrDate.size(); i++){
					value = arrDate.get(i);
					m = pYear.matcher(value);
					if(m.find()){
						time_value = value.substring(m.start(), m.end());	
						Resource rUKTime   = mOutput.createResource(id+"gregorian-year/"+time_value);
						rUKTime.addProperty(RDF.type, rYearType);
						if(value.startsWith("http")){
							Resource rValue = mOutput.createResource(value);
							rValue.addProperty(RDFS.label,time_value);
							rUKTime.addProperty(pSameAs, rValue);
						}else{
							rUKTime.addProperty(pSameAs, value);
						}
						createMetaDataForYear(time_value);
					}
				}
			}				
		}		
	}
	
	public static void createMetaDataForYear(String time_value){
		int j;
		
		Resource rQuarterType = mOutput.createResource(intervals + "CalendarQuarter");
		Resource rMonthType = mOutput.createResource(intervals + "CalendarMonth");	
		Property pNarrower 		= mOutput.createProperty(skos+"narrower");
		Property pBroader 		= mOutput.createProperty(skos+"broader");	
		Resource rUKTime   = mOutput.getResource(id+"gregorian-year/"+time_value);	
		
		for(j=1; j<=12; j++){
			Resource rUKMonth;
			if(j<=9)
				rUKMonth = mOutput.createResource(id+"gregorian-month/"+time_value+"-0"+j);
			else
				rUKMonth = mOutput.createResource(id+"gregorian-month/"+time_value+"-"+j);
			rUKMonth.addProperty(RDF.type, rMonthType);
			rUKTime.addProperty(pNarrower, rUKMonth);
			rUKMonth.addProperty(pBroader, rUKTime);
		}
		for(j=1; j<=4; j++){
			Resource rUKQuarter = mOutput.createResource(id+"gregorian-quarter/"+time_value+"-Q"+j);
			rUKQuarter.addProperty(RDF.type, rQuarterType);
			rUKTime.addProperty(pNarrower, rUKQuarter);
			rUKQuarter.addProperty(pNarrower, rUKTime);
		}
	}
	
	public static void createMetaDataForQuarter(String time_value){
		int j, from, to;
		
		Resource rMonthType = mOutput.createResource(intervals + "CalendarMonth");		
		Property pNarrower 		= mOutput.createProperty(skos+"narrower");
		Property pBroader 		= mOutput.createProperty(skos+"broader");	
		Resource rUKTime   = mOutput.getResource(id+"gregorian-quarter/"+time_value);	
		
		if(time_value.endsWith("1")){from=1; to=3;}
		else if(time_value.endsWith("2")){from=4; to=6;}
		else if(time_value.endsWith("3")){from=7; to=9;}
		else {from=10; to=12;}
		
		for(j=from; j<=to; j++){
			Resource rUKMonth;
			if(j<=9)
				rUKMonth = mOutput.createResource(id+"gregorian-month/"+time_value+"-0"+j);
			else
				rUKMonth = mOutput.createResource(id+"gregorian-month/"+time_value+"-"+j);
			rUKMonth.addProperty(RDF.type, rMonthType);
			rUKTime.addProperty(pNarrower, rUKMonth);
			rUKMonth.addProperty(pBroader, rUKTime);
		}		
	}
	
	public static void createMetaDataForArea() {
		int i, j, index, t;			
		String uri, label, type_GoogleBroaderArea="",  uri_GoogleBroaderArea="", uri_GoogleBoBArea="", sGoogleUri, sCountryLabel;
		boolean bSpecial;
		
		if(geoAreas.getSize()==0)
			readAreaList();		
		
		/* Part 1. Read data from XML files
		 * Filter the results based on hierarchical level
		 */
		
		for(i=0; i< geoAreas.getSize(); i++){			
			if(i==43){
				index = 0;
			}		
			
			GoogleAreas gList = new GoogleAreas();	
			uri_GoogleBroaderArea="";
			uri_GoogleBoBArea="";
			type_GoogleBroaderArea="";
			
			uri = geoAreas.getUri(i);
			label = geoAreas.getLabel(i);
			sCountryLabel = isCountry(uri, label);
			if(sCountryLabel!=null){
				googleAreas.add(gList);
				GoogleArea gArea = new GoogleArea();
				sCountryLabel = sCountryLabel.replaceAll("\\s", "");
				gArea.setUri(sCountryLabel);
				googleAreas.get(i).addGoogleArea(gArea);
				googleAreas.get(i).setType("administrative-area");
				continue;
			}
			
			if(label.isEmpty()){
				j=uri.length()-1;
				while(j>0 && uri.charAt(j)!='/') j--;
				uri = "World/tmp/"+uri.substring(j+1);	
				googleAreas.add(gList);
				GoogleArea gArea = new GoogleArea();
				gArea.setUri(uri);
				gArea.setFullname("");
				googleAreas.get(i).addGoogleArea(gArea);
				googleAreas.get(i).setType("non-administrative-area");
				continue;
			}
			
			index = geoAreas.indexOfBroaderArea(uri);			
			if(index!=-1 && googleAreas.get(index).getSize()>0){				
				uri_GoogleBroaderArea = googleAreas.get(index).getGoogleArea(0).getUri();
				type_GoogleBroaderArea = googleAreas.get(index).getType();
				if(type_GoogleBroaderArea.equalsIgnoreCase("non-administrative-area")){
					uri_GoogleBoBArea = getGoogleBroaderArea(uri_GoogleBroaderArea);
				}					
			}				
						
			googleAreas.add(gList);
			bSpecial=false;
			//filter regions are classified by cardinal direction
			if((label.toLowerCase().startsWith("west")||label.toLowerCase().endsWith("west")||
					label.toLowerCase().startsWith("north")||label.toLowerCase().endsWith("north")||
					label.toLowerCase().startsWith("nord")||label.toLowerCase().endsWith("nord")||
					label.toLowerCase().startsWith("south ")||label.toLowerCase().endsWith("south")||
					label.toLowerCase().startsWith("süd")||label.toLowerCase().endsWith("süd")||
					label.toLowerCase().startsWith("east")||label.toLowerCase().endsWith("east")||
					label.toLowerCase().startsWith("ost")||label.toLowerCase().endsWith("ost")||					
					label.toLowerCase().startsWith("mittel"))){
				bSpecial=true;
			}					
			setArea(i, i);
					
			if(index!=-1 && googleAreas.get(i).getSize()>0){				
				//Step 1. filter by broader area
				for(j=0; j<googleAreas.get(i).getSize(); j++){
					sGoogleUri = googleAreas.get(i).getGoogleArea(j).getUri();
					if((bSpecial==false && type_GoogleBroaderArea.equals("administrative-area") && 
							!isGoogleBroaderArea(uri_GoogleBroaderArea, sGoogleUri))||
					   (type_GoogleBroaderArea.equals("non-administrative-area") && 
						    !isGoogleBroaderArea(uri_GoogleBoBArea, sGoogleUri))  ||
					   (bSpecial==true && 
					   		!uri_GoogleBroaderArea.equalsIgnoreCase(sGoogleUri))){						
						googleAreas.get(i).removeGoogleArea(j);
						j--;
					}							
				}							
			}
			
			if(index!=-1 && googleAreas.get(i).getSize()==1){
				if(bSpecial){
					uri = googleAreas.get(i).getGoogleArea(0).getUri()+"/"+ label.replaceAll("\\s+","");
					googleAreas.get(i).getGoogleArea(0).setUri(uri);
					googleAreas.get(i).getGoogleArea(0).setFullname(label);
					googleAreas.get(i).setType("non-administrative-area");
				}else
					googleAreas.get(i).setType("administrative-area");				
			}
			if(!uri_GoogleBroaderArea.isEmpty())
				googleAreas.get(i).setUriGoogleBroaderArea(uri_GoogleBroaderArea);	
			if(!uri_GoogleBoBArea.isEmpty())
				googleAreas.get(i).setUriGoogleBroaderArea(uri_GoogleBoBArea);
			
		}	
		
		/*
		 * Part 2. Areas have broader area
		 */
		for(i=0; i<geoAreas.getSize(); i++){			
			if(i==43){
				index = 0;
			}
		
			uri_GoogleBroaderArea="";
			uri_GoogleBoBArea="";
			type_GoogleBroaderArea="";
			
			uri = geoAreas.getUri(i);
			label = geoAreas.getLabel(i);
			if(googleAreas.get(i).getSize()==1 && !googleAreas.get(i).getType().isEmpty()) 
				continue;
			
			index = geoAreas.indexOfBroaderArea(uri);			
			if(index!=-1 && !googleAreas.get(index).getType().isEmpty()){				
				uri_GoogleBroaderArea = googleAreas.get(index).getGoogleArea(0).getUri();
				type_GoogleBroaderArea = googleAreas.get(index).getType();
				if(type_GoogleBroaderArea.equalsIgnoreCase("non-administrative-area")){
					uri_GoogleBoBArea = getGoogleBroaderArea(uri_GoogleBroaderArea);
				}					
										
		
				bSpecial=false;
				//filter regions are classified by cardinal direction
				if((label.toLowerCase().startsWith("west")||label.toLowerCase().endsWith("west")||
						label.toLowerCase().startsWith("north")||label.toLowerCase().endsWith("north")||
						label.toLowerCase().startsWith("nord")||label.toLowerCase().endsWith("nord")||
						label.toLowerCase().startsWith("south ")||label.toLowerCase().endsWith("south")||
						label.toLowerCase().startsWith("süd")||label.toLowerCase().endsWith("süd")||
						label.toLowerCase().startsWith("east")||label.toLowerCase().endsWith("east")||
						label.toLowerCase().startsWith("ost")||label.toLowerCase().endsWith("ost")||					
						label.toLowerCase().startsWith("mittel"))){
					bSpecial=true;
				}		
		
					
				// find similar names
				for(j=0; j<geoAreas.getSize(); j++){
					if(j!=i && geoAreas.getLabel(j).equalsIgnoreCase(label)){
						if(googleAreas.get(j).getSize()==1 && !googleAreas.get(j).getType().isEmpty()){					
							googleAreas.get(i).removeAllGoogleArea();
							googleAreas.get(i).addGoogleArea(googleAreas.get(j).getGoogleArea(0));
							googleAreas.get(i).setType(googleAreas.get(j).getType());						
							googleAreas.get(i).setUriGoogleBroaderArea(googleAreas.get(j).getUriGoogleBroaderArea());
							break;
						}
					}
				}
				if(j!=geoAreas.getSize()) continue;
						
				//filter by broader area, distance
				if(googleAreas.get(i).getSize()>0){				
					//Step 1. filter by broader area
					for(j=0; j<googleAreas.get(i).getSize(); j++){
						sGoogleUri = googleAreas.get(i).getGoogleArea(j).getUri();
						if((bSpecial==false && type_GoogleBroaderArea.equals("administrative-area") && 
								!isGoogleBroaderArea(uri_GoogleBroaderArea, sGoogleUri))||
						   (type_GoogleBroaderArea.equals("non-administrative-area") && 
							    !isGoogleBroaderArea(uri_GoogleBoBArea, sGoogleUri))  ||
						   (bSpecial==true && 
						   		!uri_GoogleBroaderArea.equalsIgnoreCase(sGoogleUri))){						
							googleAreas.get(i).removeGoogleArea(j);
							j--;
						}						
					}
					
					//Step 2. filter by distance
					if(googleAreas.get(i).getSize()>1){						
						//distance to i-1 and i-2
						double dtemp, dmin = Double.MAX_VALUE;
						for(t=0; t<googleAreas.get(i).getSize(); t++){
							dtemp= 0;
							if(i>=1 && googleAreas.get(i-1).getType().equals("administrative-area"))
								dtemp = dtemp + Support.distance(googleAreas.get(i).getGoogleArea(t), googleAreas.get(i-1).getGoogleArea(0));
							if(i>=2 && googleAreas.get(i-1).getType().equals("administrative-area"))
								dtemp = dtemp + Support.distance(googleAreas.get(i).getGoogleArea(t), googleAreas.get(i-2).getGoogleArea(0));
							if(dtemp<dmin)
								dmin = dtemp;					
						}
						//choose the minimum distance
						for(t=googleAreas.get(i).getSize()-1; t>=0; t--){
							dtemp=0;
							if(i>=1 && googleAreas.get(i-1).getType().equals("administrative-area"))
								dtemp = dtemp + Support.distance(googleAreas.get(i).getGoogleArea(t), googleAreas.get(i-1).getGoogleArea(0));
							if(i>=2 && googleAreas.get(i-1).getType().equals("administrative-area"))
								dtemp = dtemp + Support.distance(googleAreas.get(i).getGoogleArea(t), googleAreas.get(i-2).getGoogleArea(0));
							if(dtemp==dmin){
								index=t;				
							}
						}
						//Remove other location except index					
						GoogleArea gTemp = new GoogleArea(googleAreas.get(i).getGoogleArea(index));					
						googleAreas.get(i).removeAllGoogleArea();
						googleAreas.get(i).addGoogleArea(gTemp);	
					}		
				}
				
				if(googleAreas.get(i).getSize()==1){
					if(bSpecial){
						uri = googleAreas.get(i).getGoogleArea(0).getUri()+"/"+ label.replaceAll("\\s+","");
						googleAreas.get(i).getGoogleArea(0).setUri(uri);
						googleAreas.get(i).getGoogleArea(0).setFullname(label);
						googleAreas.get(i).setType("non-administrative-area");
					}else
						googleAreas.get(i).setType("administrative-area");				
				}else{							
					GoogleArea gTemp = new GoogleArea();
					gTemp.setUri(uri_GoogleBroaderArea + "/" + label.replaceAll("\\s+",""));					
					googleAreas.get(i).addGoogleArea(gTemp);
					googleAreas.get(i).setType("non-administrative-area");				
				}
				if(!uri_GoogleBroaderArea.isEmpty())
					googleAreas.get(i).setUriGoogleBroaderArea(uri_GoogleBroaderArea);	
				if(!uri_GoogleBoBArea.isEmpty())
					googleAreas.get(i).setUriGoogleBroaderArea(uri_GoogleBoBArea);
			}
		}
		
		
		/*
		 * Part 3. Remaining areas
		 */
		for(i=0; i<geoAreas.getSize(); i++){			
			if(i==0){
				index = 0;
			}		
			
			uri = geoAreas.getUri(i);
			label = geoAreas.getLabel(i);
			if(googleAreas.get(i).getSize()==1 && !googleAreas.get(i).getType().isEmpty()) 
				continue;
			
			bSpecial=false;
			//filter regions are classified by cardinal direction
			if((label.toLowerCase().startsWith("west")||label.toLowerCase().endsWith("west")||
					label.toLowerCase().startsWith("north")||label.toLowerCase().endsWith("north")||
					label.toLowerCase().startsWith("nord")||label.toLowerCase().endsWith("nord")||
					label.toLowerCase().startsWith("south ")||label.toLowerCase().endsWith("south")||
					label.toLowerCase().startsWith("süd")||label.toLowerCase().endsWith("süd")||
					label.toLowerCase().startsWith("east")||label.toLowerCase().endsWith("east")||
					label.toLowerCase().startsWith("ost")||label.toLowerCase().endsWith("ost")||					
					label.toLowerCase().startsWith("mittel"))){
				bSpecial=true;
			}			
			
			// find similar names
		
			for(j=0; j<geoAreas.getSize(); j++){
				if(j!=i && geoAreas.getLabel(j).equalsIgnoreCase(label)){
					if(googleAreas.get(j).getSize()==1 && !googleAreas.get(j).getType().isEmpty()){					
						googleAreas.get(i).removeAllGoogleArea();
						googleAreas.get(i).addGoogleArea(googleAreas.get(j).getGoogleArea(0));
						googleAreas.get(i).setType(googleAreas.get(j).getType());						
						googleAreas.get(i).setUriGoogleBroaderArea(googleAreas.get(j).getUriGoogleBroaderArea());
						break;
					}
				}
			}
			if(j!=geoAreas.getSize()) continue;
						
			
			if(googleAreas.get(i).getSize()>0){				
				//Step 1. filter by country name
				if(googleAreas.get(i).getSize()>1){
					String sCountryBefore="", sCountryAfter="";
					for(t=i-1; t>=0; t--){
						if(googleAreas.get(t).getSize()==1){
							sCountryBefore = googleAreas.get(t).getGoogleArea(0).getUri();
							j = sCountryBefore.indexOf("/");
							if(j!=-1) sCountryBefore = sCountryBefore.substring(0,  j);
							break;
						}
					}
					for(t=i+1; t<googleAreas.size(); t++){
						if(googleAreas.get(t).getSize()==1){
							sCountryAfter = googleAreas.get(t).getGoogleArea(0).getUri();
							j = sCountryAfter.indexOf("/");
							if(j!=-1) sCountryAfter = sCountryAfter.substring(0,  j);
							break;
						}
					}	
					if(i==0) sCountryBefore = sCountryAfter;
					else if(i==googleAreas.size()-1) sCountryAfter = sCountryBefore;
					if(!sCountryAfter.isEmpty() && sCountryBefore.equalsIgnoreCase(sCountryAfter)){
						for(t=0; t<googleAreas.get(i).getSize(); t++){
							String s = googleAreas.get(i).getGoogleArea(t).getUri();
							if(!s.startsWith(sCountryAfter)){
								googleAreas.get(i).removeGoogleArea(t);
								t--;
							}
						}
					}
				}
				
				//Step 2. filter by distance
				if(googleAreas.get(i).getSize()>1){					
					//distance to i-1 and i-2
					double dtemp, dmin = Double.MAX_VALUE;
					for(t=0; t<googleAreas.get(i).getSize(); t++){
						dtemp= 0;
						if(i>=1 && googleAreas.get(i-1).getType().equals("administrative-area"))
							dtemp = dtemp + Support.distance(googleAreas.get(i).getGoogleArea(t), googleAreas.get(i-1).getGoogleArea(0));
						if(i>=2 && googleAreas.get(i-1).getType().equals("administrative-area"))
							dtemp = dtemp + Support.distance(googleAreas.get(i).getGoogleArea(t), googleAreas.get(i-2).getGoogleArea(0));
						if(dtemp<dmin)
							dmin = dtemp;					
					}
					//choose the minimum distance
					index=0;
					for(t=googleAreas.get(i).getSize()-1; t>=0; t--){
						dtemp=0;
						if(i>=1 && googleAreas.get(i-1).getType().equals("administrative-area"))
							dtemp = dtemp + Support.distance(googleAreas.get(i).getGoogleArea(t), googleAreas.get(i-1).getGoogleArea(0));
						if(i>=2 && googleAreas.get(i-1).getType().equals("administrative-area"))
							dtemp = dtemp + Support.distance(googleAreas.get(i).getGoogleArea(t), googleAreas.get(i-2).getGoogleArea(0));
						if(dtemp==dmin){
							index=t;				
						}
					}
					//Remove other location except index					
					GoogleArea gTemp = new GoogleArea(googleAreas.get(i).getGoogleArea(index));					
					googleAreas.get(i).removeAllGoogleArea();
					googleAreas.get(i).addGoogleArea(gTemp);	
				}		
			}
			
			if(googleAreas.get(i).getSize()==1){
				if(bSpecial){
					uri = googleAreas.get(i).getGoogleArea(0).getUri()+"/"+ label.replaceAll("\\s+","");
					googleAreas.get(i).getGoogleArea(0).setUri(uri);
					googleAreas.get(i).getGoogleArea(0).setFullname(label);
					googleAreas.get(i).setType("non-administrative-area");
				}else
					googleAreas.get(i).setType("administrative-area");				
			}else{				
				GoogleArea gTemp = new GoogleArea();
				gTemp.setUri(uri_GoogleBroaderArea + "World/tmp/" + label.replaceAll("\\s+",""));					
				googleAreas.get(i).addGoogleArea(gTemp);
				googleAreas.get(i).setType("non-administrative-area");				
			}
			if(!uri_GoogleBroaderArea.isEmpty())
				googleAreas.get(i).setUriGoogleBroaderArea(uri_GoogleBroaderArea);	
			if(!uri_GoogleBoBArea.isEmpty())
				googleAreas.get(i).setUriGoogleBroaderArea(uri_GoogleBoBArea);
		}
		
		// write metadata		
		System.out.println("Creating metadata for geographical areas");
//		generateMetaData();		
		writeResult2File();
		System.out.println("Done");
	}
	
	public static void generateMetaData() {	
		int i;			
		for(i=0; i< geoAreas.getSize(); i++){			
			Resource rGoogleArea    = mOutput.createResource(geo+googleAreas.get(i).getGoogleArea(0).getUri().trim());			
			Property pType          = mOutput.createProperty(sdterms + googleAreas.get(i).getType());			
			Property pNarrower 		= mOutput.createProperty(skos+"narrower");
			Property pBroader 		= mOutput.createProperty(skos+"broader");
			if(!googleAreas.get(i).getGoogleArea(0).getFullname().isEmpty())
				rGoogleArea.addProperty(RDFS.label, googleAreas.get(i).getGoogleArea(0).getFullname());			
			rGoogleArea.addProperty(RDF.type, pType);
			if(!googleAreas.get(i).getUriGoogleBroaderArea().equals("")){				
				Resource rGoogleBroaderArea = mOutput.getResource(geo+googleAreas.get(i).getUriGoogleBroaderArea());
				rGoogleArea.addProperty(pBroader, rGoogleBroaderArea);
				rGoogleBroaderArea.addProperty(pNarrower, rGoogleArea);
			}	
		}
		
	}
	
	public static void setArea(int index_xml, int index_value) {		
		int j, i;		
		String sStatus, sUri, sGoogleName, sFullName, s, sGeoName = geoAreas.getLabel(index_value);
		double lat, lng;	
		GoogleArea gArea;		
		try {	 
			File fXmlFile = new File("data/area/"+folderName+"/"+index_xml+".xml");
			if(!fXmlFile.exists())
				  return;			
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);	
			doc.getDocumentElement().normalize();
		 
			NodeList nList = doc.getElementsByTagName("status");
			if(nList.getLength()==1 && nList.item(0).getNodeType() == Node.ELEMENT_NODE){
				Element eStatus = (Element) nList.item(0);
				sStatus = eStatus.getTextContent();
				if(sStatus.equals("OK")){
					//read result									
					NodeList nResult = doc.getElementsByTagName("result");
					for(i=0; i<nResult.getLength(); i++){
						//get location
						NodeList nGeoMetry = ((Element)nResult.item(i)).getElementsByTagName("geometry");
						NodeList nLocation = ((Element)nGeoMetry.item(0)).getElementsByTagName("location");
						NodeList nLat = ((Element)nLocation.item(0)).getElementsByTagName("lat");
						NodeList nLng = ((Element)nLocation.item(0)).getElementsByTagName("lng");
						lat = Double.parseDouble(((Element) nLat.item(0)).getTextContent());
						lng = Double.parseDouble(((Element) nLng.item(0)).getTextContent());
						
						//get name and uri
						sGoogleName=""; sUri="";
						
						//list of address_component
						NodeList nAddress = ((Element)nResult.item(i)).getElementsByTagName("address_component");
						sGoogleName  = ((Element)nAddress.item(0)).getElementsByTagName("long_name").item(0).getTextContent();
						sGoogleName = sGoogleName.replaceAll("&quot;", "").replaceAll("\"", "").trim();
						
						for(j=nAddress.getLength()-1; j>=0;j--){
							//ignore "Post code" type
							if(((Element)nAddress.item(j)).getElementsByTagName("type").getLength()>0){
								if(((Element)nAddress.item(j)).getElementsByTagName("type").item(0).getTextContent().contains("postal_code"))
									continue;
							}									
							sFullName = ((Element)nAddress.item(j)).getElementsByTagName("long_name").item(0).getTextContent();								
							sFullName = sFullName.replaceAll("District", "").replaceAll("&quot;", "").replaceAll("\"", "").trim();
							sFullName = sFullName.replaceAll("Gemeinde", "").trim();
							sFullName = sFullName.replaceAll("\\s+","");					
					
							if(sUri=="")
								sUri = sFullName; //full name
							else
								sUri = sUri + "/" + sFullName; //full name
							
							sFullName = ((Element)nAddress.item(j)).getElementsByTagName("long_name").item(0).getTextContent();
							sFullName = sFullName.replaceAll("&quot;", "").replaceAll("\"", "").trim();
							if(sFullName.equals(sGoogleName)||sFullName.startsWith(sGoogleName+" ")||sFullName.endsWith(" "+sGoogleName)||sFullName.contains(" "+sGoogleName+" ")||
									sFullName.equals(sGeoName)||sFullName.startsWith(sGeoName+" ")||sFullName.endsWith(" "+sGeoName)||sFullName.contains(" "+sGeoName+" ")){
								//create a Google area and add to value element
								gArea = new GoogleArea();	
								gArea.setUri(sUri);		
								s = ((Element)nAddress.item(j)).getElementsByTagName("long_name").item(0).getTextContent();
								s = s.replaceAll("&quot;", "").replaceAll("\"", "").trim();								
								gArea.setFullname(s);
								gArea.setLat(lat);
								gArea.setLng(lng);
								googleAreas.get(index_value).addGoogleArea(gArea);
							}													
						}						
					}					
				}			
			}
				
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	System.out.println(index_xml +";" + index_value);
	    }		
	}

	/*
	 * Check if sURI1 is a broader area of sURI2	
	 */
	private static boolean isGoogleBroaderArea(String sUri1, String sUri2) {
		if(sUri1==null || sUri2==null || sUri1.isEmpty()||sUri2.isEmpty())
			return false;
		
		if(sUri2.startsWith(sUri1+"/") && sUri2.length()> sUri1.length()+2 && sUri2.substring(sUri1.length()+1).indexOf("/")==-1)
			return true;
		
		if(sUri2.contains("/") && sUri1.contains("/")){
			String[] arrUri2 = sUri2.split("/");
			String[] arrUri1 = sUri1.split("/");
			if(sUri2.startsWith(sUri1) && arrUri2.length==arrUri1.length+1)
				return true;
		}
		
		return false;
	}
	
//	private static boolean isGoogleBroaderArea(String sUri1, String sUri2) {	
//		if(!sUri1.endsWith("Vienna")){
//			if(sUri2.startsWith(sUri1) && sUri2.length()>sUri1.length()+2 && sUri2.substring(sUri1.length()+1).indexOf("/")==-1)
//				return true;	
//			
//			String[] arrUri2 = sUri2.split("/");
//			String[] arrUri1 = sUri1.split("/");
//			if(arrUri2.length==arrUri1.length+1 && arrUri2.length>2 && arrUri2[arrUri2.length-2].equals(arrUri1[arrUri1.length-1]))
//				return true;
//		}else{			
//			String[] arrUri2 = sUri2.split("/");
//			String[] arrUri1 = sUri1.split("/");
//			if(arrUri2.length==arrUri1.length+2 && arrUri2[arrUri2.length-2].equals("Vienna"))
//				return true;	
//			if(arrUri2.length==arrUri1.length+1 && arrUri2[arrUri2.length-1].equals("Vienna"))
//				return true;
//		}
//		return false;
//
//	}
	
	public static String getGoogleBroaderArea(String sUri){	
		int i=sUri.length()-1;
		while(i>0&& sUri.charAt(i)!='/')	i--;
		if(i>0)
			return sUri.substring(0, i);
		else
			return null;
		
	}
	
	public static String isCountry(String sUri, String sLabel){	
		int i;	
		if(sUri.equals("http://dd.eionet.europa.eu/vocabulary/eurostat/geo/EL"))
			return "Greece";		
		
		if(sLabel.contains("Korea") && (sLabel.contains("Democratic")))
			return "North Korea";	
		
		
		i=sUri.length()-1;
		while(i>0 && sUri.charAt(i)!='/') i--;
		String s =  sUri.substring(i+1);
		s = s.replace("country#", "").replace("geo#", "");
		
		for(i=0; i<countries.getSize(); i++)
			if(countries.getCountryName(i).equalsIgnoreCase(s))
				return countries.getLabel(i);
		
		if(s.length()==2 || s.length()==3)
			for(i=0; i<countries.getSize(); i++)
				if(countries.getIso2Code(i).equals(s) || countries.getIso3Code(i).equals(s))
					return countries.getLabel(i);
		
		return null;
	}
	
	/*
	 * Input: URI of an endpoint e.g., http://semantic.eea.europa.eu/sparql
	 * Output: An extract of this name, e.g. semantic
	 */	
	public static String extractFolderName(String endpointURI){	
		int i;
		endpointURI = endpointURI.substring(7);
		i=0;
		while(i<endpointURI.length() && endpointURI.charAt(i)!='.' && endpointURI.charAt(i)!='-') 
			i++;
		i++;
		while(i<endpointURI.length() && endpointURI.charAt(i)!='.' && endpointURI.charAt(i)!='-'&& endpointURI.charAt(i)!='/') 
			i++;
		
		return endpointURI.substring(0, i);		
	}

	public static void delay(int n){
		try {
		    Thread.sleep(n*100);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}
	
	private static void readAreaList() {			
		BufferedReader br = null;
		String label, uri, line = "";		
		try {	 
			br = new BufferedReader(new InputStreamReader(new FileInputStream("data/datasources/"+folderName+"/areas.csv"), "UTF-8"));		
			while ((line = br.readLine()) != null) {				
				label = line.substring(0, line.indexOf("\t")).trim();
				uri = line.substring(line.indexOf("\t"));
				uri = uri.replaceAll("\t", "").trim();
				geoAreas.addGeoArea(label,  uri);	 
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
	
	private static void readSubject() {		
		BufferedReader br = null;
		String subject, uri, label, line = "";
		int i, k=-1;
		try {	 
			br = new BufferedReader(new InputStreamReader(new FileInputStream("data/datasources/"+folderName+"/subject.csv"), "UTF-8"));		
			while ((line = br.readLine()) != null) {				
				k++;				
				i = line.indexOf("\t");				
				uri = line.substring(0, i).trim();
				line = line.substring(i+1).trim();
				i = line.indexOf("\t");				
				if(i!=-1){
					label = line.substring(0, i);
					subject = line.substring(i+1).trim();
				}
				else{
					label = line.trim();
					subject = "";
				}
				if(dsInfor.size()>k)
					dsInfor.get(k).setThirdString(subject);
				else
					dsInfor.add(new StringTriple(uri, label, subject));
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
//		for(int j=0; j<dsInfor.size(); j++)
//			dsInfor.get(j).display();
	}
	
	public static void writeInfor2File() {	
		int i;
		try{ 
			File folder = new File("data/datasources/"+folderName);		
			if (!folder.exists()) 
			    folder.mkdir();
			
    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
    			    new FileOutputStream("data/datasources/"+folderName+"/subject.csv"), "UTF-8"));    		
    		
	        for(i=0; i<dsInfor.size();i++){	        	
	        	out.write(dsInfor.get(i).getFirstString()+"\t"+dsInfor.get(i).getSecondString()+"\n");      
	       }
	       out.close(); 
	       
	       out = new BufferedWriter(new OutputStreamWriter(
   			    new FileOutputStream("data/datasources/"+folderName+"/attribute.csv"), "UTF-8"));    		
   		
	        for(i=0; i<attInfor.size();i++){	        	
	        	out.write(attInfor.get(i).getFirstString()+"\t"+attInfor.get(i).getSecondString()+"\n");      
	       }
	       out.close(); 
	       
    	}catch(IOException e){
    		e.printStackTrace();
    	}		
	}
	
	public static void writeResult2File() {	
		int i;
		try{ 	
    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
    			    new FileOutputStream("data/datasources/"+folderName+"/areas2.csv"), "UTF-8"));    		
    		
	        for(i=0; i<geoAreas.getSize();i++){	
	        	if(googleAreas.get(i).getSize()!=1 || googleAreas.get(i).getType().isEmpty()) continue;
	        	out.write(geoAreas.getLabel(i));
	        	out.write("\t");
	        	out.write(geoAreas.getUri(i));
	        	out.write("\t");
	        	out.write(googleAreas.get(i).getGoogleArea(0).getUri());
	        	out.write("\t");
	        	out.write(googleAreas.get(i).getType());
	        	out.write("\n");
	        }
	       out.close();
 
    	}catch(IOException e){
    		e.printStackTrace();
    	}		
	}
	
	public static void writeArea2File() {	
		int i;
		try{ 
			File folder = new File("data/datasources/"+folderName);		
			if (!folder.exists()) 
			    folder.mkdir();
			
    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
    			    new FileOutputStream("data/datasources/"+folderName+"/areas.csv"), "UTF-8"));    		
    		
	        for(i=0; i<geoAreas.getSize();i++){	        	
	        	out.write(geoAreas.getLabel(i));
	        	out.write("\t");
	        	out.write(geoAreas.getUri(i));
	        	out.write("\n");
	        }
	       out.close();
 
    	}catch(IOException e){
    		e.printStackTrace();
    	}		
	}
	
	public static void writeTime2File() {	
		int i;
		try{ 
			File folder = new File("data/datasources/"+folderName);		
			if (!folder.exists()) 
			    folder.mkdir();
			
    		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
    			    new FileOutputStream("data/datasources/"+folderName+"/times.csv"), "UTF-8"));    		
    		
	        for(i=0; i<times.size();i++){	        	
	        	out.write(times.get(i));	        	
	        	out.write("\n");
	        }
	       out.close();
 
    	}catch(IOException e){
    		e.printStackTrace();
    	}		
	}

	
}
