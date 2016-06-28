package at.tuwien.ldlab.statspace.metadata.concrete;

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
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
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

import at.tuwien.ldlab.statspace.codelist.CL_Area;
import at.tuwien.ldlab.statspace.codelist.CL_Unit_Measure;
import at.tuwien.ldlab.statspace.codelist.StandardDimensions;
import at.tuwien.ldlab.statspace.metadata.GeoAreaList;
import at.tuwien.ldlab.statspace.metadata.GoogleArea;
import at.tuwien.ldlab.statspace.metadata.GoogleAreaList;
import at.tuwien.ldlab.statspace.metadata.StringCouple;
import at.tuwien.ldlab.statspace.metadata.StringTriple;
import at.tuwien.ldlab.statspace.util.QB;
import at.tuwien.ldlab.statspace.util.SpecialEndpointList;
import at.tuwien.ldlab.statspace.util.Support;
import at.tuwien.ldlab.statspace.widgetgeneration.Endpoint;

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
	private static ArrayList<GoogleAreaList> googleAreas = new ArrayList<GoogleAreaList>();
	private static ArrayList<String> times = new ArrayList<String>();
	private static GeoAreaList geoAreas = new GeoAreaList();		
	private static String folderName="";
	private static Model mOutput;
	
	public static void main(String[] args) {		
		System.out.println("Anayzing endpoint...");		
//		analyzeEndpoint();
		
		System.out.println("Using Google APIs to identify geographical areas...");		
//		queryArea();
		
		System.out.println("Creating metadata...");	
//		queryArea();
			
//		identifyMapping();
		
		createMetaData();
		
		System.out.println("Finished");		
	}
	
	public static void analyzeEndpoint() {	
		int i,j,k,n,m;
		SpecialEndpointList specialList = new SpecialEndpointList("data/list.xml");	
		boolean bHTTP, bRemove, bFindOther, bCheck;
		String sUseDistinct, s, uri, time_value;		
		String sEndpoint;		
		
		for(i=0; i<endpoints.getSize(); i++){
			geoAreas.clear();
			times.clear();
			dsInfor.clear();
			
			sEndpoint = endpoints.getEndpoint(i);		
			k=specialList.getEndpointIndex(sEndpoint);
			if(k!=-1){
				if(!specialList.getEndpointForQuery(k).equals(""))
					sEndpoint = specialList.getEndpointForQuery(k);					
				
				bHTTP = specialList.getHTTPRequest(k);
				bRemove = specialList.getRemoveDuplicate(k);
				sUseDistinct = specialList.getUseDistinct(k);
				bFindOther = specialList.getFindOtherValue(k);			
			}else{						
				bHTTP   = false;
				bRemove = false;
				sUseDistinct = "";
				bFindOther = true;
			}        		
			
			folderName = Support.extractFolderName(sEndpoint);
			//create folder
			File fDatasource = new File("data" + File.separator + "datasources" + File.separator + folderName);
			if(!fDatasource.exists())
				fDatasource.mkdirs();
			File fCache = new File("data" + File.separator + "datasources" + File.separator + folderName + File.separator + "cache");
			if(!fCache.exists())
				fCache.mkdirs();
			File fAreas = new File("data" + File.separator + "datasources" + File.separator + folderName + File.separator + "areas");
			if(!fAreas.exists())
				fAreas.mkdirs();		
			
			//query sparql endpoint					
			System.out.println("************************************");
			System.out.println("Endpoint: " + sEndpoint);
			Endpoint endpoint = new Endpoint(sEndpoint, "", bHTTP, bRemove, sUseDistinct, bFindOther);
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
//				delay(1);
				endpoint.getDataSet(j).queryComponent(sEndpoint, bHTTP, sUseDistinct);
				endpoint.getDataSet(j).identifyReference();
				endpoint.getDataSet(j).queryValueandCache(sEndpoint, bHTTP, bFindOther, bRemove, false, true);
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
		int from, end, i, j, index;
		from=0; end=5000;
		String geo = "https://maps.googleapis.com/maps/api/geocode/xml?address=";
		String sEndpoint, sLabel, sUri, sUri_BroaderArea, sLabel_BroaderArea, sQuery="", url, folderAreas, fileXML="";	 
		Boolean bSpecial, bUseBroaderArea;		
		File fXML;		
		SpecialEndpointList specialList = new SpecialEndpointList("data/list.xml");	
			
		for(j=0; j<endpoints.getSize(); j++){
			sEndpoint = endpoints.getEndpoint(j);	
			int k=specialList.getEndpointIndex(sEndpoint);
			if(k!=-1){
				if(!specialList.getEndpointForQuery(k).equals(""))
					sEndpoint = specialList.getEndpointForQuery(k);							
			}
			folderName = Support.extractFolderName(sEndpoint);
			System.out.println("--------------------");
			System.out.println("Query data for geographical data for " + folderName);
			
			geoAreas.clear();
			readAreaList();
			folderAreas ="data" + File.separator + "datasources" + File.separator + folderName + File.separator + "areas";		
			File fAreas = new File(folderAreas);		
			if (!fAreas.exists()) 
			    fAreas.mkdir();
			
			for(i=0; i<geoAreas.getSize(); i++){			
				if(i<from) continue;				
				if(i>end) break;				
				
			    sLabel = geoAreas.getLabel(i);		  
			    sUri = geoAreas.getUri(i);
			    fileXML = folderAreas + File.separator + Support.extractFileName(sUri) + ".xml";
			    fXML = new File(fileXML);
			    if(fXML.exists())			    
					continue;
			 
			    delay(2);			
			    if(sLabel.isEmpty()) continue;	    
				if(folderName.contains("opendatacommunities.org")){
					if(sLabel.contains(" E"))
						sLabel = sLabel.substring(0, sLabel.indexOf(" E"));	
					sLabel = sLabel + " England";
				}				
					    
			    //find broader area
				bSpecial=false;
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
			    			sLabel_BroaderArea.toLowerCase().startsWith("mittel")||
			    			sLabel.toLowerCase().startsWith("west")||sLabel.toLowerCase().endsWith("west")||
			    			sLabel.toLowerCase().startsWith("north")||sLabel.toLowerCase().endsWith("north")||
			    			sLabel.toLowerCase().startsWith("nord")||sLabel.toLowerCase().endsWith("nord")||
			    			sLabel.toLowerCase().startsWith("south ")||sLabel.toLowerCase().endsWith("south")||
			    			sLabel.toLowerCase().startsWith("süd")||sLabel.toLowerCase().endsWith("süd")||
			    			sLabel.toLowerCase().startsWith("east")||sLabel.toLowerCase().endsWith("east")||
			    			sLabel.toLowerCase().startsWith("ost")||sLabel.toLowerCase().endsWith("ost")||					
			    			sLabel.toLowerCase().startsWith("mittel"))){
			    		bSpecial=true;
						
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
			    	}
			    }else{
			    	//special cases
					if(sUri.contains("bwd.eea.europa.eu/resource/provinces") && !bSpecial){
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
			    			    new FileOutputStream(fileXML), "UTF-8"));			    				
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
					    			    new FileOutputStream(fileXML), "UTF-8"));			    				
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
		}		
	}
	
	public static void identifyMapping() {
		int j, i, k, index, t, level, googleLevel;			
		String uri, label, sEndpoint, googleTypeBroaderArea="",  googleUriBroaderArea="", googleUri, googleLabel, folderAreas;
		SpecialEndpointList specialList = new SpecialEndpointList("data/list.xml");	
		for(i=0; i<endpoints.getSize(); i++){
			sEndpoint = endpoints.getEndpoint(i);	
			k=specialList.getEndpointIndex(sEndpoint);
			if(k!=-1){
				if(!specialList.getEndpointForQuery(k).equals(""))
					sEndpoint = specialList.getEndpointForQuery(k);							
			}
			folderName = Support.extractFolderName(sEndpoint);
			geoAreas.clear();
			googleAreas.clear();
			readAreaList();	
			System.out.println("Identifying mappings for geographical areas of  " + folderName);	
			folderAreas ="data" + File.separator + "datasources" + File.separator + folderName + File.separator + "areas";		
			
			//step 1. read xml files	
			for(j=0; j< geoAreas.getSize(); j++){				
				GoogleAreaList gList = new GoogleAreaList();	
				googleAreas.add(gList);								
				setArea(j, folderAreas);					
			}	
			
			//step 2. identify mappings for areas which do not have broader areas
			for(j=0; j< geoAreas.getSize(); j++){				
				//used to debug
				if(j==1176){
					index = 0;
				}			
				
				uri = geoAreas.getUri(j);
				label = geoAreas.getLabel(j);
				googleLabel = getCountryName(uri, label);
				if(googleLabel!=null){
					GoogleAreaList gList = new GoogleAreaList();				
					GoogleArea gArea = new GoogleArea();
					gArea.setFullname(googleLabel);
					googleLabel = googleLabel.replaceAll("\\s+", "");
					gArea.setUri(googleLabel);					
					gList.addGoogleArea(gArea);
					gList.setType("administrative-area");
					googleAreas.remove(j);
					googleAreas.add(j, gList);
					continue;
				}
				
				if(label.isEmpty()){
					GoogleAreaList gList = new GoogleAreaList();			
					GoogleArea gArea = new GoogleArea();
					k=uri.length()-1;
					while(k>0 && uri.charAt(k)!='/') k--;				
					gArea.setFullname("Undefined area - code " + uri.substring(k+1));
					uri = "undefined/"+uri.substring(k+1);					
					gArea.setUri(uri);
					gList.addGoogleArea(gArea);
					gList.setType("non-administrative-area");
					googleAreas.remove(j);
					googleAreas.add(j, gList);
					continue;
				}
				
				index = geoAreas.indexOfBroaderArea(uri);			
				if(index==-1){					
					if(googleAreas.get(j).getSize()==1){
						googleAreas.get(j).setType("administrative-area");
					}else if(googleAreas.get(j).getSize()>1){					
						level = getLevelOfArea(uri);				
						googleLevel = -1;
						for(k=j-1; k>=0; k--){
							if(getLevelOfArea(geoAreas.getUri(k))==level &&
									googleAreas.get(k).getSize()==1){
								googleLevel = getLevelOfArea(googleAreas.get(k).getGoogleArea(0).getUri());
								break;
							}							
						}
						if(googleLevel==-1){
							for(k=j+1; k<geoAreas.getSize(); k++){
								if(getLevelOfArea(geoAreas.getUri(k))==level &&
										googleAreas.get(k).getSize()==1){
									googleLevel = getLevelOfArea(googleAreas.get(k).getGoogleArea(0).getUri());
									break;
								}							
							}
						}
						
						if(googleLevel!=-1){
							for(k=0; k<googleAreas.get(j).getSize(); k++){								
								if(googleLevel != getLevelOfArea(googleAreas.get(j).getGoogleArea(k).getUri())){
									googleAreas.get(j).removeGoogleArea(k);	
									k--;
								}									
							}
						}
						if(googleAreas.get(j).getSize()==0){
							googleAreas.get(j).setType("non-administrative-area");							
						} else if(googleAreas.get(j).getSize()==1){
							googleAreas.get(j).setType("administrative-area");
						}else{
							//array of indexes
							ArrayList<Integer> arrIndexes = new ArrayList<Integer>();
							for(k=j+1; k<geoAreas.getSize(); k++){
								uri = geoAreas.getUri(k);
								if(geoAreas.indexOfBroaderArea(uri)==j)
									arrIndexes.add(k);	
								else
									break;
							}
							if(arrIndexes.size()>0){
								int max=0, count, pos=-1;
								for(k=0; k<googleAreas.get(j).getSize(); k++){
									uri = googleAreas.get(j).getGoogleArea(k).getUri();
									count=0;
									for(t=0; t<arrIndexes.size(); t++){
										if(googleAreas.get(arrIndexes.get(t)).getSize()>0 && 
												googleAreas.get(arrIndexes.get(t)).containGoogleArea(uri))
											count++;								
									}
									if(count>max) {
										max=count;
										pos = k;
									}
								}
								if(pos!=-1){
									for(k=pos+1; k<googleAreas.get(j).getSize(); k++)
										googleAreas.get(j).removeGoogleArea(k);
									for(k=0; k<pos; k++)
										googleAreas.get(j).removeGoogleArea(k);		
									googleAreas.get(j).setType("administrative-area");
								}
							}
						}						
					}
				}		
			}	
						
			//step3. identify mappings for areas which have broader area
			for(j=0; j<geoAreas.getSize(); j++){				
				//used to debug
				if(j==1411){
					index = 0;
				}
				
				if(googleAreas.get(j).getSize()==1 && !googleAreas.get(j).getType().isEmpty()) 
					continue;
				
				uri = geoAreas.getUri(j);
				label = geoAreas.getLabel(j);				
				
				googleUriBroaderArea="";
				googleTypeBroaderArea="";
				index = geoAreas.indexOfBroaderArea(uri);
				if(index!=-1 && !googleAreas.get(index).getType().isEmpty()){
					googleTypeBroaderArea = googleAreas.get(index).getType();
					googleUriBroaderArea = googleAreas.get(index).getGoogleArea(0).getUri();
					if(googleTypeBroaderArea.equalsIgnoreCase("non-administrative-area")){		
						googleUriBroaderArea = getGoogleBroaderArea(googleUriBroaderArea);
					}						
										
					if(googleAreas.get(j).getSize()>0){				
						// filter by broader area
						for(k=0; k<googleAreas.get(j).getSize(); k++){
							googleUri = googleAreas.get(j).getGoogleArea(k).getUri();
							if(!isGoogleBroaderArea(googleUriBroaderArea, googleUri)&&
									!googleUri.equals(googleUriBroaderArea)){						
								googleAreas.get(j).removeGoogleArea(k);
								k--;
							}						
						}
						
						//filter by distance
						if(googleAreas.get(j).getSize()>1){						
							//distance to j-2, j-1, j+1, j+2
							double dtemp, dmin = Double.MAX_VALUE;
							int pos=-1;
							for(t=0; t<googleAreas.get(j).getSize(); t++){
								dtemp= 0;
								if(j>=1 && googleAreas.get(j-1).getType().equals("administrative-area"))
									dtemp = dtemp + Support.distance(googleAreas.get(j).getGoogleArea(t), googleAreas.get(j-1).getGoogleArea(0));
								if(j>=2 && googleAreas.get(j-2).getType().equals("administrative-area"))
									dtemp = dtemp + Support.distance(googleAreas.get(j).getGoogleArea(t), googleAreas.get(j-2).getGoogleArea(0));
								if(j+1<googleAreas.size() && googleAreas.get(j+1).getType().equals("administrative-area"))
									dtemp = dtemp + Support.distance(googleAreas.get(j).getGoogleArea(t), googleAreas.get(j+1).getGoogleArea(0));
								if(j+2<googleAreas.size() && googleAreas.get(j+2).getType().equals("administrative-area"))
									dtemp = dtemp + Support.distance(googleAreas.get(j).getGoogleArea(t), googleAreas.get(j+2).getGoogleArea(0));
								if(dtemp<dmin){
									dmin = dtemp;
									pos = t;
								}
							}
							//choose the minimum distance											
							GoogleArea gTemp = new GoogleArea(googleAreas.get(j).getGoogleArea(pos));					
							googleAreas.get(j).removeAllGoogleArea();
							googleAreas.get(j).addGoogleArea(gTemp);
						}		
					}
					
					if(googleAreas.get(j).getSize()==1){
						googleAreas.get(j).setType("administrative-area");				
					}else{						
						GoogleArea gArea = new GoogleArea();						
						gArea.setUri(googleUriBroaderArea + "/" + label.replaceAll("\\s+",""));	
						gArea.setFullname(label);		
						googleAreas.get(j).addGoogleArea(gArea);
						googleAreas.get(j).setType("non-administrative-area");				
					}			
				}
			}
			
		
			/*
			 * Step 4. remaining areas
			 */		
			for(j=0; j<geoAreas.getSize(); j++){			
				//used to debug
				if(j==1699){
					index = 0;
				}			
				
				if(googleAreas.get(j).getSize()==1 && !googleAreas.get(j).getType().isEmpty()) 
					continue;			
				
				uri = geoAreas.getUri(j);
				label = geoAreas.getLabel(j);
				
				// find similar names
				for(k=0; k<geoAreas.getSize(); k++){
					if(k!=j && geoAreas.getLabel(k).equalsIgnoreCase(label)){
						if(googleAreas.get(k).getSize()==1 && !googleAreas.get(k).getType().isEmpty()){	
							googleAreas.get(j).removeAllGoogleArea();
							googleAreas.get(j).addGoogleArea(googleAreas.get(k).getGoogleArea(0));
							googleAreas.get(j).setType(googleAreas.get(k).getType());							
							break;
						}
					}
				}
				if(k!=geoAreas.getSize()) continue;		
				
				if(googleAreas.get(j).getSize()>0){
					
					//filter by country name
					if(googleAreas.get(j).getSize()>1){
						String sCountryBefore="", sCountryAfter="";
						for(t=j-1; t>=0; t--){
							if(googleAreas.get(t).getSize()==1){
								sCountryBefore = googleAreas.get(t).getGoogleArea(0).getUri();
								k = sCountryBefore.indexOf("/");
								if(k!=-1) sCountryBefore = sCountryBefore.substring(0,  k);
								break;
							}
						}
						for(t=j+1; t<googleAreas.size(); t++){
							if(googleAreas.get(t).getSize()==1){
								sCountryAfter = googleAreas.get(t).getGoogleArea(0).getUri();
								k = sCountryAfter.indexOf("/");
								if(k!=-1) sCountryAfter = sCountryAfter.substring(0,  k);
								break;
							}
						}	
						if(j==0) sCountryBefore = sCountryAfter;
						else if(j==googleAreas.size()-1) sCountryAfter = sCountryBefore;
						if(!sCountryAfter.isEmpty() && sCountryBefore.equalsIgnoreCase(sCountryAfter)){
							for(t=0; t<googleAreas.get(j).getSize(); t++){
								googleUri = googleAreas.get(j).getGoogleArea(t).getUri();
								if(!googleUri.startsWith(sCountryAfter)){
									googleAreas.get(j).removeGoogleArea(t);
									t--;
								}
							}
						}
					}
				
					//filter by distance
					if(googleAreas.get(j).getSize()>1){						
						//distance to j-2, j-1, j+1, j+2
						double dtemp, dmin = Double.MAX_VALUE;
						int pos=-1;
						for(t=0; t<googleAreas.get(j).getSize(); t++){
							dtemp= 0;
							if(j>=1 && googleAreas.get(j-1).getType().equals("administrative-area"))
								dtemp = dtemp + Support.distance(googleAreas.get(j).getGoogleArea(t), googleAreas.get(j-1).getGoogleArea(0));
							if(j>=2 && googleAreas.get(j-2).getType().equals("administrative-area"))
								dtemp = dtemp + Support.distance(googleAreas.get(j).getGoogleArea(t), googleAreas.get(j-2).getGoogleArea(0));
							if(j+1<googleAreas.size() && googleAreas.get(j+1).getType().equals("administrative-area"))
								dtemp = dtemp + Support.distance(googleAreas.get(j).getGoogleArea(t), googleAreas.get(j+1).getGoogleArea(0));
							if(j+2<googleAreas.size() && googleAreas.get(j+2).getType().equals("administrative-area"))
								dtemp = dtemp + Support.distance(googleAreas.get(j).getGoogleArea(t), googleAreas.get(j+2).getGoogleArea(0));
							if(dtemp<dmin){
								dmin = dtemp;
								pos = t;
							}
						}
						//choose the minimum distance											
						GoogleArea gTemp = new GoogleArea(googleAreas.get(j).getGoogleArea(pos));					
						googleAreas.get(j).removeAllGoogleArea();
						googleAreas.get(j).addGoogleArea(gTemp);
					}								
				}
				if(googleAreas.get(j).getSize()==1){
					googleAreas.get(j).setType("administrative-area");				
				}else{						
					GoogleArea gArea = new GoogleArea();	
					index = geoAreas.indexOfBroaderArea(uri);			
					if(index!=-1){
						googleUriBroaderArea = googleAreas.get(index).getGoogleArea(0).getUri();
						googleTypeBroaderArea = googleAreas.get(index).getType();
						if(googleTypeBroaderArea.equalsIgnoreCase("non-administrative-area"))	
							googleUriBroaderArea = getGoogleBroaderArea(googleUriBroaderArea);										
						gArea.setUri(googleUriBroaderArea + "/" + label.replaceAll("\\s+",""));
					}
					else
						gArea.setUri("undefined/" + label.replaceAll("\\s+",""));
					gArea.setFullname(label);		
					googleAreas.get(j).addGoogleArea(gArea);
					googleAreas.get(j).setType("non-administrative-area");				
				}		
			}		
			
			// write metadata			
			writeResult2File();
		}
	}	
	
	public static void createMetaData() {	
		int i,j,k,n,v,m,t,index,size;
		SpecialEndpointList specialList = new SpecialEndpointList("data/list.xml");	
		boolean bHTTP, bRemove, bFindOther, bArea, bTime;
		String sUseDistinct, uri, timeValue, s, aUri, aLabel, dUri, dRefUri, mUri, mLabel, vUri, vLabel, vRefUri;		
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
			
			folderName = Support.extractFolderName(sEndpoint);
			//create Metadata for Area
			geoAreas.clear();
			googleAreas.clear();
			dsInfor.clear();
			
			readSubject();			
			createMetaDataForArea();			
			
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
			    	Resource rSource = mOutput.createResource(endpoint.getDataSet(j).getUri());
			    	//Resource rSource = mOutput.createResource(endpoint.getEndpointForQuery());
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
			    	if(size==1){
			    		if(!endpoint.getDataSet(j).getLabel().isEmpty())			    		
			    			rDataSet.addProperty(pLabel, endpoint.getDataSet(j).getLabel());
			    	}else{			    		
			    		s = endpoint.getDataSet(j).getLabel();
			    		if(!s.isEmpty()){
			    			mLabel = endpoint.getDataSet(j).getMeasureLabel(t);
			    			if(!mLabel.isEmpty())
			    				s = s + " - " + "Measure: " + mLabel;
			    			else
			    				s = s + " - " + "Measure: " + t ;
			    		}else{
			    			mLabel = endpoint.getDataSet(j).getMeasureLabel(t);
			    			if(!mLabel.isEmpty())
			    				s = "Measure: " + mLabel;
			    			else
			    				s = "Measure: " + t ;
			    		}
			    		rDataSet.addProperty(pLabel, s);			    			
			    	}
			    	
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
			    		rAttribute.addProperty(RDFS.label, "Unit of measure");
			    		rAttribute.addProperty(RDF.type, rTempProperty);
			    		Resource rRefAttribute = mOutput.createResource("http://purl.org/linked-data/sdmx/2009/attribute#unitMeasure");
			    		rRefAttribute.addProperty(pSameAs, rAttribute);			    									
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
								timeValue   = endpoint.getDataSet(j).getDimension(k).getValueUri(v);
								vLabel = endpoint.getDataSet(j).getDimension(k).getValueLabel(v).trim();
								times.add(timeValue);								
								if(timeValue.startsWith("http:")){
									Resource rValue = mOutput.createResource(timeValue);									
									if(!vLabel.isEmpty())
										rValue.addProperty(RDFS.label, vLabel);
									rDimension.addProperty(pValue, rValue);
									rDataSet.addProperty(pValue, rValue);								
								}
								else{
									rDimension.addProperty(pValue, timeValue);
									rDataSet.addProperty(pValue, timeValue);
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
							Resource rRefDimension = mOutput.createResource("http://purl.org/linked-data/sdmx/2009/dimension#refArea");
				    		rRefDimension.addProperty(pSameAs, rDimension);	
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
						timeValue =  endpoint.getDataSet(j).identifyTimeValue();						
						if(timeValue!=""){									
							uri = endpoint.getDataSet(j).createSpecialTemporalValue(timeValue);
							times.add(uri);
							Resource rDimension = mOutput.createResource(endpoint.getDataSet(j).createSpecialTemporalDimension());
							rDimension.addProperty(RDF.type, QB.DimensionProperty);
							rDimension.addProperty(RDFS.label, "Ref Period");							
							rDimension.addProperty(RDF.type, rTempProperty);
							Resource rRefDimension = mOutput.createResource("http://purl.org/linked-data/sdmx/2009/dimension#refPeriod");
				    		rRefDimension.addProperty(pSameAs, rDimension);	
							Resource rValue = mOutput.createResource(uri);
							rValue.addProperty(RDFS.label, timeValue);						
		        			rDimension.addProperty(pValue, rValue); 
		        			rDataSet.addProperty(pValue, rValue);
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
				fout.close();
			} catch (IOException e) {
				System.out.println("Exception caught when writing file: " + e.toString());
			}
			
		}			
		System.out.println("Done");
	}
	
	public static void createMetaDataForArea() {	
		int i, j;	
		BufferedReader br = null;
		String label, uri, gUri, gLabel, gType, gBroaderArea, line = "";
		geoAreas.clear();
		googleAreas.clear();
		try {	 
			br = new BufferedReader(new InputStreamReader(new FileInputStream("data/datasources/"+folderName+"/areas2.csv"), "UTF-8"));		
			while ((line = br.readLine()) != null) {				
				label = line.substring(0, line.indexOf("\t")).trim();
				line = line.substring(line.indexOf("\t")+1);
				uri = line.substring(0, line.indexOf("\t")).trim();
				line = line.substring(line.indexOf("\t")+1);
				gUri = line.substring(0, line.indexOf("\t")).trim();
				line = line.substring(line.indexOf("\t")+1);
				gLabel = line.substring(0, line.indexOf("\t")).trim();
				line = line.substring(line.indexOf("\t")+1);
				gType = line.trim();
				geoAreas.addGeoArea(label,  uri);
				
				GoogleArea gArea = new GoogleArea();
				gArea.setUri(gUri);
				gArea.setFullname(gLabel);				
				GoogleAreaList gList = new GoogleAreaList();
				gList.addGoogleArea(gArea);
				gList.setType(gType);
				googleAreas.add(gList);				
			}		
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
		
		//set broader, narrower relationships
		for(j=0; j< geoAreas.getSize(); j++){	
			uri = geoAreas.getUri(j);
			i = geoAreas.indexOfBroaderArea(uri);
			if(i!=-1){
				gUri = googleAreas.get(i).getGoogleArea(0).getUri();
				googleAreas.get(j).setUriGoogleBroaderArea(gUri);
			}
		}
		
		//create metadata
		for(i=0; i< geoAreas.getSize(); i++){			
			Resource rGoogleArea    = mOutput.createResource(geo+googleAreas.get(i).getGoogleArea(0).getUri().trim());			
			Property pType          = mOutput.createProperty(sdterms + googleAreas.get(i).getType());			
			Property pNarrower 		= mOutput.createProperty(skos+"narrower");
			Property pBroader 		= mOutput.createProperty(skos+"broader");
			if(!googleAreas.get(i).getGoogleArea(0).getFullname().isEmpty())
				rGoogleArea.addProperty(RDFS.label, googleAreas.get(i).getGoogleArea(0).getFullname());			
			rGoogleArea.addProperty(RDF.type, pType);
			if(!googleAreas.get(i).getUriGoogleBroaderArea().isEmpty() && 
					!googleAreas.get(i).getGoogleArea(0).getUri().equals(googleAreas.get(i).getUriGoogleBroaderArea())){				
				Resource rGoogleBroaderArea = mOutput.getResource(geo+googleAreas.get(i).getUriGoogleBroaderArea());
				rGoogleArea.addProperty(pBroader, rGoogleBroaderArea);
				rGoogleBroaderArea.addProperty(pNarrower, rGoogleArea);
			}
			gBroaderArea = getGoogleBroaderArea(googleAreas.get(i).getGoogleArea(0).getUri());
			if(gBroaderArea!=null && !gBroaderArea.isEmpty() &&
					gBroaderArea.equals(googleAreas.get(i).getUriGoogleBroaderArea())){
				Resource rGoogleBroaderArea = mOutput.getResource(geo+gBroaderArea);
				rGoogleArea.addProperty(pBroader, rGoogleBroaderArea);
				rGoogleBroaderArea.addProperty(pNarrower, rGoogleArea);
			}			
		}		
	}
	
	public static void createMetaDataForTime(){
		int i, j, n = times.size();			
		String sInterval ="[1-9][0-9]{3}-[1-9][0-9]{3}";
		String sYear     = "[1-9][0-9]{3}";
		String sMonth    = "[1-9][0-9]{3}-[0-1][0-9]";
		String sQuarter  = "[1-9][0-9]{3}-Q[1-4]";
		String sDate     = "[1-9][0-9]{3}-[0-1][0-9]-[0-3][0-9]";
		String value, timeValue="";
		
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
					timeValue = value.substring(m.start(), m.end());
					String sFrom = timeValue.substring(0, 4);
					String sEnd  = timeValue.substring(5);
					int yFrom = Integer.parseInt(sFrom);
					int yEnd  = Integer.parseInt(sEnd);
					timeValue = Integer.toString(yFrom)+"-01-01T00:00:00/P"+Integer.toString(yEnd-yFrom)+"Y";							
					Resource rUKTime   = mOutput.createResource(id+"gregorian-interval/"+timeValue);
					rUKTime.addProperty(RDF.type, rIntervalType);
					if(value.startsWith("http")){
						Resource rValue = mOutput.createResource(value);
						if(rValue.getProperty(RDFS.label)==null)
							rValue.addProperty(RDFS.label,timeValue);
						rUKTime.addProperty(pSameAs, rValue);
					}else{
						rUKTime.addProperty(pSameAs, value);
					}			
				}else{		
					//year
					m = pYear.matcher(value);
					if(m.find()){	
						bOther=true;
						timeValue = value.substring(m.start(), m.end());	
						Resource rUKTime   = mOutput.createResource(id+"gregorian-year/"+timeValue);
						rUKTime.addProperty(RDF.type, rYearType);
						if(value.startsWith("http")){
							Resource rValue = mOutput.createResource(value);
							if(rValue.getProperty(RDFS.label)==null)
								rValue.addProperty(RDFS.label,timeValue);							
							rUKTime.addProperty(pSameAs, rValue);
						}else{
							rUKTime.addProperty(pSameAs, value);
						}
						createMetaDataForYear(timeValue);
					}else
						System.out.println("Not found + " + value + "\t index: " + i);
				}
			}
			else if(value.toLowerCase().contains("quarter")){
				m = pQuarter.matcher(value);
				if(m.find()){	
					bOther=true;
					timeValue = value.substring(m.start(), m.end());
					Resource rUKTime   = mOutput.createResource(id+"gregorian-quarter/"+timeValue);
					rUKTime.addProperty(RDF.type, rQuarterType);
					if(value.startsWith("http")){
						Resource rValue = mOutput.createResource(value);
						if(rValue.getProperty(RDFS.label)==null)
							rValue.addProperty(RDFS.label,timeValue);		
						rUKTime.addProperty(pSameAs, rValue);
					}else{
						rUKTime.addProperty(pSameAs, value);
					}
					createMetaDataForQuarter(timeValue);
					
				}else
					System.out.println("Not found + " + value + "\t index: " + i);
			}
			else if(value.toLowerCase().contains("month")){
				m = pMonth.matcher(value);
				if(m.find()){
					bOther=true;
					timeValue = value.substring(m.start(), m.end());	
					Resource rUKTime = mOutput.createResource(id+"gregorian-month/"+timeValue);
					rUKTime.addProperty(RDF.type, rMonthType);
					if(value.startsWith("http")){
						Resource rValue = mOutput.createResource(value);
						if(rValue.getProperty(RDFS.label)==null)
							rValue.addProperty(RDFS.label,timeValue);		
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
						timeValue = value.substring(m.start(), m.end());
						Resource rUKTime   = mOutput.createResource(id+"gregorian-quarter/"+timeValue);
						rUKTime.addProperty(RDF.type, rQuarterType);
						if(value.startsWith("http")){
							Resource rValue = mOutput.createResource(value);
							if(rValue.getProperty(RDFS.label)==null)
								rValue.addProperty(RDFS.label,timeValue);		
							rUKTime.addProperty(pSameAs, rValue);
						}else{
							rUKTime.addProperty(pSameAs, value);
						}
						createMetaDataForQuarter(timeValue);						
					}else{
						//month						
						m = pMonth.matcher(value);
						if(m.find()){
							bOther=true;
							timeValue = value.substring(m.start(), m.end());
							Resource rUKTime = mOutput.createResource(id+"gregorian-month/"+timeValue);
							rUKTime.addProperty(RDF.type, rMonthType);							
							if(value.startsWith("http")){
								Resource rValue = mOutput.createResource(value);
								if(rValue.getProperty(RDFS.label)==null)
									rValue.addProperty(RDFS.label,timeValue);		
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
								timeValue = value.substring(m.start(), m.end());
								String sFrom = timeValue.substring(0, 4);
								String sEnd  = timeValue.substring(5);
								int yFrom = Integer.parseInt(sFrom);
								int yEnd  = Integer.parseInt(sEnd);
								timeValue = Integer.toString(yFrom)+"-01-01T00:00:00/P"+Integer.toString(yEnd-yFrom)+"Y";							
								Resource rUKTime   = mOutput.createResource(id+"gregorian-interval/"+timeValue);
								rUKTime.addProperty(RDF.type, rIntervalType);
								if(value.startsWith("http")){
									Resource rValue = mOutput.createResource(value);
									if(rValue.getProperty(RDFS.label)==null)
										rValue.addProperty(RDFS.label,timeValue);		
									rUKTime.addProperty(pSameAs, rValue);
								}else{
									rUKTime.addProperty(pSameAs, value);
								}			
							}else{	
								//year							
								m = pYear.matcher(value);
								if(m.find()){
									bOther=true;
									timeValue = value.substring(m.start(), m.end());
									Resource rUKTime   = mOutput.createResource(id+"gregorian-year/"+timeValue);
									rUKTime.addProperty(RDF.type, rYearType);
									if(value.startsWith("http")){
										Resource rValue = mOutput.createResource(value);
										if(rValue.getProperty(RDFS.label)==null)
											rValue.addProperty(RDFS.label,timeValue);		
										rUKTime.addProperty(pSameAs, rValue);
									}else{
										rUKTime.addProperty(pSameAs, value);
									}
									createMetaDataForYear(timeValue);
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
						timeValue = value.substring(m.start(), m.end());	
						Resource rUKTime   = mOutput.createResource(id+"gregorian-date/"+timeValue);
						rUKTime.addProperty(RDF.type, rDateType);
						if(value.startsWith("http")){
							Resource rValue = mOutput.createResource(value);
							rValue.addProperty(RDFS.label,timeValue);
							rUKTime.addProperty(pSameAs, rValue);
						}else{
							rUKTime.addProperty(pSameAs, value);
						}	
					}
				}									
			}else{
				for(i=0; i<arrDate.size(); i++){
					value = arrDate.get(i);
					m = pYear.matcher(value);
					if(m.find()){
						timeValue = value.substring(m.start(), m.end());	
						Resource rUKTime   = mOutput.createResource(id+"gregorian-year/"+timeValue);
						rUKTime.addProperty(RDF.type, rYearType);
						if(value.startsWith("http")){
							Resource rValue = mOutput.createResource(value);
							rValue.addProperty(RDFS.label,timeValue);
							rUKTime.addProperty(pSameAs, rValue);
						}else{
							rUKTime.addProperty(pSameAs, value);
						}
						createMetaDataForYear(timeValue);
					}
				}
			}				
		}		
	}
	
	public static void createMetaDataForYear(String timeValue){
		int j;
		
		Resource rQuarterType = mOutput.createResource(intervals + "CalendarQuarter");
		Resource rMonthType = mOutput.createResource(intervals + "CalendarMonth");	
		Property pNarrower 	= mOutput.createProperty(skos+"narrower");
		Property pBroader 	= mOutput.createProperty(skos+"broader");	
		Resource rUKTime    = mOutput.getResource(id+"gregorian-year/"+timeValue);	
		
		for(j=1; j<=12; j++){
			Resource rUKMonth;
			if(j<=9)
				rUKMonth = mOutput.createResource(id+"gregorian-month/"+timeValue+"-0"+j);
			else
				rUKMonth = mOutput.createResource(id+"gregorian-month/"+timeValue+"-"+j);
			rUKMonth.addProperty(RDF.type, rMonthType);
			rUKTime.addProperty(pNarrower, rUKMonth);
			rUKMonth.addProperty(pBroader, rUKTime);
		}
		for(j=1; j<=4; j++){
			Resource rUKQuarter = mOutput.createResource(id+"gregorian-quarter/"+timeValue+"-Q"+j);
			rUKQuarter.addProperty(RDF.type, rQuarterType);
			rUKTime.addProperty(pNarrower, rUKQuarter);
			rUKQuarter.addProperty(pNarrower, rUKTime);
		}
	}
	
	public static void createMetaDataForQuarter(String timeValue){
		int j, from, to;
		
		Resource rMonthType = mOutput.createResource(intervals + "CalendarMonth");		
		Property pNarrower 	= mOutput.createProperty(skos+"narrower");
		Property pBroader 	= mOutput.createProperty(skos+"broader");	
		Resource rUKTime    = mOutput.getResource(id+"gregorian-quarter/"+timeValue);	
		
		if(timeValue.endsWith("1")){from=1; to=3;}
		else if(timeValue.endsWith("2")){from=4; to=6;}
		else if(timeValue.endsWith("3")){from=7; to=9;}
		else {from=10; to=12;}
		
		for(j=from; j<=to; j++){
			Resource rUKMonth;
			if(j<=9)
				rUKMonth = mOutput.createResource(id+"gregorian-month/"+timeValue+"-0"+j);
			else
				rUKMonth = mOutput.createResource(id+"gregorian-month/"+timeValue+"-"+j);
			rUKMonth.addProperty(RDF.type, rMonthType);
			rUKTime.addProperty(pNarrower, rUKMonth);
			rUKMonth.addProperty(pBroader, rUKTime);
		}		
	}
		
	public static void setArea(int index, String folderAreas) {		
		int j, i;		
		String sStatus, sUri, sGoogleName, sFullName, s, sGeoName = geoAreas.getLabel(index), fileXML;
		double lat, lng;	
		GoogleArea gArea;		
		try {			
			sUri = geoAreas.getUri(index);
			fileXML = folderAreas + File.separator + Support.extractFileName(sUri) + ".xml";			 
			File fXML = new File(fileXML);
			if(!fXML.exists()){
				googleAreas.set(index, new GoogleAreaList());
			}else{
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fXML);	
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
							sGoogleName = sGoogleName.replace("&quot;", "").replaceAll("\"", "").trim();
							
							for(j=nAddress.getLength()-1; j>=0;j--){
								//ignore "Post code" type
								if(((Element)nAddress.item(j)).getElementsByTagName("type").getLength()>0){
									if(((Element)nAddress.item(j)).getElementsByTagName("type").item(0).getTextContent().contains("postal_code"))
										continue;
								}			
								//ignore "route" type
								if(((Element)nAddress.item(j)).getElementsByTagName("type").getLength()>0){
									if(((Element)nAddress.item(j)).getElementsByTagName("type").item(0).getTextContent().equals("route"))
										break;
								}	
								sFullName = ((Element)nAddress.item(j)).getElementsByTagName("long_name").item(0).getTextContent();								
								sFullName = sFullName.replace("District", "").replace("&quot;", "").replaceAll("\"", "").trim();
								sFullName = sFullName.replace("Gemeinde", "").trim();
								sFullName = sFullName.replaceAll("\\s+","");					
						
								if(sUri=="")
									sUri = sFullName; //full name
								else
									sUri = sUri + "/" + sFullName; //full name
								
								sFullName = ((Element)nAddress.item(j)).getElementsByTagName("long_name").item(0).getTextContent();
								sFullName = sFullName.replace("&quot;", "").replaceAll("\"", "").trim();
								if(sFullName.equals(sGoogleName)||sFullName.startsWith(sGoogleName+" ")||sFullName.endsWith(" "+sGoogleName)||sFullName.contains(" "+sGoogleName+" ")||
										sFullName.equals(sGeoName)||sFullName.startsWith(sGeoName+" ")||sFullName.endsWith(" "+sGeoName)||sFullName.contains(" "+sGeoName+" ")){
									//create a Google area and add to value element
									gArea = new GoogleArea();	
									gArea.setUri(sUri);		
									s = ((Element)nAddress.item(j)).getElementsByTagName("long_name").item(0).getTextContent();
									s = s.replace("&quot;", "").replaceAll("\"", "").trim();								
									gArea.setFullname(s);
									gArea.setLat(lat);
									gArea.setLng(lng);
									googleAreas.get(index).addGoogleArea(gArea);
								}													
							}						
						}					
					}			
				}
			}					
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	System.out.println(index);
	    }		
	}

	/*
	 * Check if sURI1 is a broader area of sURI2	
	 */
	public static boolean isGoogleBroaderArea(String sUri1, String sUri2) {	
		if(sUri1==null || sUri2==null || sUri1.isEmpty()||sUri2.isEmpty())
			return false;
		if(sUri2.startsWith(sUri1)){
			if(sUri2.startsWith(sUri1+"/") && sUri2.length()> sUri1.length()+1 && sUri2.substring(sUri1.length()+1).indexOf("/")==-1)
				return true;
			
			/* sUri1. Austria/Vienna
			 * sUri2. Austria/Vienna/Vienna/Floridoft
			 */
			
			if(sUri2.contains("/") && sUri1.contains("/")){				
				List<String> arrUri2 = new LinkedList<String>(Arrays.asList(sUri2.split("/")));
				List<String> arrUri1 = new LinkedList<String>(Arrays.asList(sUri1.split("/")));
				int i;
				for(i=0; i<arrUri2.size()-1; i++)
					if(arrUri2.get(i).equals(arrUri2.get(i+1))){
						arrUri2.remove(i+1);
						i--;
					}
				for(i=0; i<arrUri1.size()-1; i++)
					if(arrUri1.get(i).equals(arrUri1.get(i+1))){
						arrUri1.remove(i+1);
						i--;
					}
				if(arrUri2.size()==arrUri1.size()+1)
					return true;
			}
		}		
		return false;		
	}
	
	public static boolean isGoogleSameArea(String sUri1, String sUri2) {	
		if(sUri1==null || sUri2==null || sUri1.isEmpty()||sUri2.isEmpty())
			return false;
		if(sUri2.startsWith(sUri1)||sUri1.startsWith(sUri2)){			
			/* sUri1. Austria/Vienna
			 * sUri2. Austria/Vienna/Vienna
			 */			
			if(sUri2.contains("/") && sUri1.contains("/")){				
				List<String> arrUri2 = new LinkedList<String>(Arrays.asList(sUri2.split("/")));
				List<String> arrUri1 = new LinkedList<String>(Arrays.asList(sUri1.split("/")));
				int i;
				for(i=0; i<arrUri2.size()-1; i++)
					if(arrUri2.get(i).equals(arrUri2.get(i+1))){
						arrUri2.remove(i+1);
						i--;
					}
				for(i=0; i<arrUri1.size()-1; i++)
					if(arrUri1.get(i).equals(arrUri1.get(i+1))){
						arrUri1.remove(i+1);
						i--;
					}
				if(arrUri2.size()==arrUri1.size())
					return true;
			}
		}		
		return false;		
	}
	
	public static String getGoogleBroaderArea(String sUri){	
		int i=sUri.length()-1;
		while(i>0&& sUri.charAt(i)!='/')	i--;
		if(i>0)
			return sUri.substring(0, i);
		else
			return null;
		
	}
	
	public static int getLevelOfArea(String sUri){	
		if(sUri==null || sUri.isEmpty())
			return -1;
		if(sUri.contains("/"))
			return sUri.split("/").length;
		return -1;		
	}
	
	public static String getCountryName(String sUri, String sLabel){	
		int i;	
		if(sUri.equals("http://dd.eionet.europa.eu/vocabulary/eurostat/geo/EL"))
			return "Greece";		
		
		if(sLabel.contains("Korea") && (sLabel.contains("Democratic")))
			return "North Korea";	
		
		
		i=sUri.length()-1;
		while(i>0 && sUri.charAt(i)!='/') i--;
		String s =  sUri.substring(i+1);
		s = s.replace("country#", "").replace("geo#", "");
		if(s.equalsIgnoreCase("UK"))
			s= "GB";
		
		for(i=0; i<countries.getSize(); i++)
			if(countries.getCountryName(i).equalsIgnoreCase(s))
				return countries.getLabel(i);
		
		if(s.length()==2 || s.length()==3)
			for(i=0; i<countries.getSize(); i++)
				if(countries.getIso2Code(i).equals(s) || countries.getIso3Code(i).equals(s))
					return countries.getLabel(i);
		
		return null;
	}
	
	public static void delay(int n){
		try {
		    Thread.sleep(n*100);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}
	
	public static void readAreaList() {			
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
	
	public static void readSubject() {		
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
	        	out.write(googleAreas.get(i).getGoogleArea(0).getFullname());
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
			File fEndpoint = new File("data/datasources/"+folderName);		
			if (!fEndpoint.exists()) 
			    fEndpoint.mkdir();
			
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
