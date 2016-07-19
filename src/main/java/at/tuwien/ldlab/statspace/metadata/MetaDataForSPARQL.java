package at.tuwien.ldlab.statspace.metadata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.hp.hpl.jena.query.Query;
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

import at.tuwien.ldlab.statspace.codelist.CL_Area;
import at.tuwien.ldlab.statspace.codelist.CL_Unit_Measure;
import at.tuwien.ldlab.statspace.codelist.StandardDimensions;
import at.tuwien.ldlab.statspace.metadata.GeoAreaList;
import at.tuwien.ldlab.statspace.metadata.GoogleArea;
import at.tuwien.ldlab.statspace.metadata.GoogleAreaList;
import at.tuwien.ldlab.statspace.util.FileOperation;
import at.tuwien.ldlab.statspace.util.QB;
import at.tuwien.ldlab.statspace.util.Support;
import at.tuwien.ldlab.statspace.widgetgeneration.Endpoint;

public class MetaDataForSPARQL{
	private static Log log = LogFactory.getLog(MetaDataForSPARQL.class);		
	private String qb = "http://purl.org/linked-data/cube#";		
	private String sdmx_dimension = "http://purl.org/linked-data/sdmx/2009/dimension#";
	private String sdmx_measure = "http://purl.org/linked-data/sdmx/2009/measure#";
	private String sdmx_attribute = "http://purl.org/linked-data/sdmx/2009/attribute#";
	private String sdmx_code = "http://purl.org/linked-data/sdmx/2009/code#";
	private String vd = "http://rdfs.org/ns/void#";
	private String dcterms = "http://purl.org/dc/terms/";
	private String sdterms = "http://statspace.linkedwidgets.org/terms/";
	private String dcat = "http://www.w3.org/ns/dcat#";
	private String skos = "http://www.w3.org/2004/02/skos/core#";
	private String owl = "http://www.w3.org/2002/07/owl#";
	private String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private String rdfs = "http://www.w3.org/2000/01/rdf-schema#";	
	private String geo = "http://statspace.linkedwidgets.org/codelist/cl_area/";	
	private String id = "http://reference.data.gov.uk/id/";	
	private String intervals = "http://reference.data.gov.uk/def/intervals/";	
	private CL_Area countries;
	private CL_Unit_Measure units;
	private StandardDimensions dimensions;
	private Endpoint endpoint;
	private ArrayList<GoogleAreaList> googleAreas;
	private ArrayList<String> times;
	private List<String> listDataset;
	private GeoAreaList geoAreas;		
	private String folderEndpoint;
	private String folderId;	
	private String idRequest;
	private Model mOutput;
	
	public MetaDataForSPARQL(Endpoint e, List<String> l, String folderWebApp, String id){
		 countries = new CL_Area();
		 units = new CL_Unit_Measure();
		 dimensions = new StandardDimensions();
		 endpoint = e;
		 googleAreas = new ArrayList<GoogleAreaList>();
		 times = new ArrayList<String>();
		 geoAreas = new GeoAreaList();				
		 listDataset = l;
		 idRequest = id;
		 
		 String sEndpoint = endpoint.getEndpointForQuery();	
		 sEndpoint = Support.extractFolderName(sEndpoint); 
		 
		 //create a folder to store metadata for returning results to users
		 folderId =  folderWebApp + "download" + File.separator +  sEndpoint + "_" + idRequest;
		 File fId = new File(folderId);
		 fId.mkdirs();
		 
		 //create a folder to store metadata for storage	
		 folderEndpoint = folderWebApp.substring(0, folderWebApp.length()-1) + "_cache" + File.separator + "metadata" + File.separator +  sEndpoint;
		 boolean bEndpoint = FileOperation.findFile(folderWebApp.substring(0, folderWebApp.length()-1) + "_cache" + File.separator + "metadata", sEndpoint);
   		 if(bEndpoint == false){   	
   			 File fileEP = new File(folderEndpoint);
   			 fileEP.mkdir(); 	         	    	
   		}	
	}
		
	public void analyzeEndpoint() {	
		int i,j,k,n,m;	
		String s, uri, timeValue;		
		String dsName;	
		boolean bCheck, bDataset, bNewDataSet=false;	
		
		n = listDataset.size();					
		for(i=0; i<n; i++){				
			j = Integer.parseInt(listDataset.get(i));
    		dsName = endpoint.getDataSet(j).getUri();
    		dsName = Support.extractFileName(dsName);		
			bDataset = FileOperation.findFile(folderEndpoint, dsName + ".ttl");  		
			if(bDataset==false){
 				bNewDataSet = true;
 				endpoint.getDataSet(j).queryComponent(endpoint.getEndpointForQuery(), endpoint.getHTTP(), endpoint.getUseDistict());
     	     	endpoint.getDataSet(j).queryValue(endpoint.getEndpointForQuery(), endpoint.getHTTP(), endpoint.getFindOther(), endpoint.getRemove());
     	     	endpoint.getDataSet(j).identifyReference();
     	     	
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
					if(s!=""){	
						uri = endpoint.getDataSet(j).createSpecialGeoArea(s.replaceAll("\\s+",""));					
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
							timeValue = endpoint.getDataSet(j).getDimension(k).getValueUri(m);													
							times.add(timeValue);	        		
			        	}
				    }
			    }
				//temporal dimension - label of dataset
				if(bCheck==false){
					timeValue =  endpoint.getDataSet(j).identifyTimeValue();					
					if(timeValue!=""){											
						times.add(timeValue);
					}					
				}
 			}		
		}
		if(bNewDataSet){
			geoAreas.sortInAscending();				
		}	
		queryArea();		
	}
	
	
	public void queryArea(){
		int i, index, count;	
		String geo = "https://maps.googleapis.com/maps/api/geocode/xml?address=";
		String sLabel, sUri, sUri_BroaderArea, sLabel_BroaderArea, sQuery="", url;	 
		Boolean bSpecial, bUseBroaderArea;	
		String fileXML, folderAreas;
		File fXML;
		
		folderAreas = folderEndpoint + File.separator + "areas";
		File fArea = new File(folderAreas);		
		if (!fArea.exists()) 
			fArea.mkdir();
		
		count=0;
		for(i=0; i<geoAreas.getSize(); i++){	
			sLabel = geoAreas.getLabel(i);		  
			sUri = geoAreas.getUri(i);
			fileXML = folderAreas + File.separator + Support.extractFileName(sUri) + ".xml";
			fXML = new File(fileXML);
			if(fXML.exists())			    
				continue;
			
			count++;				
			if(count>500) break;
			delay(2);		   
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
			if(folderEndpoint.contains("opendatacommunities.org")){
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
					//create file
				   	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
		    			    new FileOutputStream(fileXML), "UTF-8"));			    				
		    		BufferedReader in = new BufferedReader(new InputStreamReader(
                            con.getInputStream(), "UTF-8"));
				    String inputLine;
				    while ((inputLine = in.readLine()) != null){
				    	if(inputLine.contains("OVER_QUERY_LIMIT")){
				    		System.out.println("OVER QUERY LIMIT");
				    		if(geo.endsWith("?address"))
				    			geo = "https://maps.googleapis.com/maps/api/geocode/xml?key=AIzaSyB9aPwMdaizXi5-K63rTtsvj1YiWAQxALU&address=";
				    		else
				    			geo = "https://maps.googleapis.com/maps/api/geocode/xml?key=AIzaSyD4QbuZzxKOyrBIw-LfVe31n38pxSSd2co&address=";
				    		bSpecial = true;
				    		break;				    		
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
	
	public void createMetaData(boolean bUseCache) {	
		int i,j,k,n,v,m,t,index,size;
		boolean bArea, bTime, bDataset;
		String uri, timeValue, s, aUri, aLabel, dUri, dRefUri, mUri, mLabel, vUri, vLabel, vRefUri;		
		String sDSName, sEndpointShortName;				
		sEndpointShortName = endpoint.getEndpointForQuery();
		sEndpointShortName = getShortName(sEndpointShortName);
		
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
		
		createMetaDataForArea();	
		createMetaDataForTime();
		
		n = listDataset.size();	
		for(i=0; i<n; i++){				
			bArea = false;
			bTime = false;		
			j = Integer.parseInt(listDataset.get(i));
			sDSName = endpoint.getDataSet(j).getUri();
			sDSName = Support.extractFileName(sDSName);			
			bDataset = FileOperation.findFile(folderEndpoint, sDSName + ".ttl");  
			
			if(bUseCache && bDataset){
				if(n>1)
					FileOperation.copyFolder(folderEndpoint+ File.separator + sDSName+".ttl", folderId + File.separator + sDSName+".ttl");
				InputStream is = FileManager.get().open(folderEndpoint + File.separator + sDSName + ".ttl");		         
				Model mDataSet = ModelFactory.createDefaultModel().read(is, null, "TTL");				
				mOutput.add(mDataSet);		
				continue;
			}
			
			//create an empty Jena Model and set required prefix	
			Model mDataSet;
			mDataSet = ModelFactory.createDefaultModel();	
			mDataSet.setNsPrefix("qb", qb);								
			mDataSet.setNsPrefix("sdmx-dimension", sdmx_dimension);
			mDataSet.setNsPrefix("sdmx-measure", sdmx_measure);
			mDataSet.setNsPrefix("sdmx-attribute", sdmx_attribute);
			mDataSet.setNsPrefix("sdmx-code", sdmx_code);				
			mDataSet.setNsPrefix("dcterms", dcterms);
			mDataSet.setNsPrefix("sdterms", sdterms);		
			mDataSet.setNsPrefix("dcat", dcat);	
			mDataSet.setNsPrefix("skos", skos);	
			mDataSet.setNsPrefix("owl", owl);
			mDataSet.setNsPrefix("void", vd);
			mDataSet.setNsPrefix("rdf", rdf);
			mDataSet.setNsPrefix("rdfs", rdfs);
			
			//create Metadata for describe components & values
	    	times.clear();	
	    	size = endpoint.getDataSet(j).getMeasureSize();
	    	
	    	Property pLabel = mDataSet.createProperty(rdfs+"label");	   
	    	for(t=0; t<size; t++){	    		
	    		//Metadata
	    		Resource rMetaData;
	    		if(size==1)
	    			rMetaData = mDataSet.createResource("http://statspace.linkedwidgets.org/metadata/"+ sEndpointShortName + "_"+ j);
	    		else
	    			rMetaData = mDataSet.createResource("http://statspace.linkedwidgets.org/metadata/"+ sEndpointShortName + "_"+ j+"_measure_"+t);				
    		
		    	Property pSource = mDataSet.createProperty(dcterms+"source");
		    	Resource rSource = mDataSet.createResource(endpoint.getEndpointForQuery());
		    	rMetaData.addProperty(pSource, rSource);
		    	Property pLicense = mDataSet.createProperty(dcterms+"license");				    	
		    	Resource rLicense = mDataSet.createResource("http://creativecommons.org/licenses/by-sa/4.0/");
		    	rMetaData.addProperty(pLicense, rLicense);
		    	Property pCreator = mDataSet.createProperty(dcterms+"creator");
		    	Resource rCreator = mDataSet.createResource( "http://www.ifs.tuwien.ac.at/user/383");
		    	rMetaData.addProperty(pCreator, rCreator);
		    	Property pCreated = mDataSet.createProperty(dcterms+"created");		    	
		    	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    	Date date = new Date();
		    	rMetaData.addProperty(pCreated,dateFormat.format(date)); 
		    	
				//Dataset
		    	Resource rDataSet   = mDataSet.createResource(endpoint.getDataSet(j).getUri());
		    	Property pDataSet 	= mDataSet.createProperty(qb+"dataSet");
		    	rMetaData.addProperty(pDataSet, rDataSet);
		    	Property pMethod = mDataSet.createProperty(vd+"feature");
		    	rDataSet.addProperty(pMethod, "SPARQL endpoint");
		    	Property pRML 	 = mDataSet.createProperty(dcat+"accessURL");
		    	rDataSet.addProperty(pRML, mDataSet.createResource(endpoint.getEndpointForQuery()));
		    	if(!endpoint.getDataSet(j).getLabel().isEmpty())			    		
		    		rDataSet.addProperty(pLabel, endpoint.getDataSet(j).getLabel());
		    	
		      	Property pValue = mDataSet.createProperty(rdf+"value");		
		      	
		        //Component		    
		    	Property pComponent = mDataSet.createProperty(qb+"component");			    
		    	Property pSameAs  = mDataSet.createProperty(owl+"sameAs");
		    	Resource rTempProperty = mDataSet.createProperty(sdterms+"HiddenProperty");		
		    	rTempProperty.addProperty(RDFS.label, "This component is not defined in data structure. However, it is added in metadata to support data integration and exploration");
		    	  	
		    	//Measure			    	
	    		mUri = endpoint.getDataSet(j).getMeasureUri(t);
	    		mLabel = endpoint.getDataSet(j).getMeasureLabel(t);
	    		Resource rMeasure   = mDataSet.createResource(mUri);
		    	rMeasure.addProperty(RDF.type, QB.MeasureProperty);
		    	if(!mLabel.isEmpty())
		    		rMeasure.addProperty(RDFS.label, mLabel);			    	
		    	rMetaData.addProperty(pComponent, rMeasure);  	
	    		if(!mUri.equalsIgnoreCase("http://purl.org/linked-data/sdmx/2009/measure#obsValue")){
	    			Resource rRefMeasure   = mDataSet.createResource("http://purl.org/linked-data/sdmx/2009/measure#obsValue");
	    			rRefMeasure.addProperty(pSameAs, rMeasure);
	    		}  	 	
		      			    	
		    	//Attribute
		    	if(endpoint.getDataSet(j).getAttributeSize()>0){
		    		aUri   = endpoint.getDataSet(j).getAttributeUri(0);
		    		aLabel = endpoint.getDataSet(j).getAttributeLabel(0);
		    		Resource rAttribute = mDataSet.createResource(aUri);
		    		rAttribute.addProperty(RDF.type, QB.AttributeProperty);
		    		if(!aLabel.isEmpty())
		    			rAttribute.addProperty(RDFS.label, aLabel);			    		
			    	rMetaData.addProperty(pComponent, rAttribute);	    
			    	if(!aUri.equalsIgnoreCase("http://purl.org/linked-data/sdmx/2009/attribute#unitMeasure")){
			    		Resource rRefAttribute = mDataSet.createResource("http://purl.org/linked-data/sdmx/2009/attribute#unitMeasure");
		    			rRefAttribute.addProperty(pSameAs, rAttribute);
			    	}    	
		    		for(m=0; m<endpoint.getDataSet(j).getAttribute(0).getValueSize(); m++){
		    			vUri   = endpoint.getDataSet(j).getAttribute(0).getValueUri(m);
		    			vLabel = endpoint.getDataSet(j).getAttribute(0).getValueLabel(m);	    			
		    			Resource rValue = mDataSet.createResource(vUri);
		    			if(!vLabel.isEmpty())
		    				rValue.addProperty(RDFS.label, vLabel);
		    			rAttribute.addProperty(pValue, rValue);
						rDataSet.addProperty(pValue, rValue);
						vRefUri = units.identifyReference(vUri, vLabel);
						if(!vRefUri.equalsIgnoreCase(vUri)){
							Resource rRefValue   = mDataSet.createResource(vRefUri);						
							rRefValue.addProperty(pSameAs, rValue);
						}
		    		}		    	
		    	}else{
		    		Resource rAttribute = mDataSet.createResource(endpoint.getDataSet(j).createSpecialAttribute());
		    		rAttribute.addProperty(RDF.type, QB.AttributeProperty);
		    		rAttribute.addProperty(RDFS.label, "Unit of measure");
		    		rAttribute.addProperty(RDF.type, rTempProperty);
		    		Resource rRefAttribute = mDataSet.createResource("http://purl.org/linked-data/sdmx/2009/attribute#unitMeasure");
		    		rRefAttribute.addProperty(pSameAs, rAttribute);			    									
					Resource rValue = mDataSet.createResource(units.getDefaultUnit());
					rAttribute.addProperty(pValue, rValue);	
        			rDataSet.addProperty(pValue, rValue);
        			rMetaData.addProperty(pComponent, rAttribute);
		    	}
		    			
		    	//Dimension			    	
				for(k=0; k<endpoint.getDataSet(j).getDimensionSize();k++){	
					Resource rDimension = mDataSet.createResource(endpoint.getDataSet(j).getDimensionUri(k));
					rDimension.addProperty(RDF.type, QB.DimensionProperty);
					rMetaData.addProperty(pComponent, rDimension);					
					if(!endpoint.getDataSet(j).getDimensionLabel(k).isEmpty())
						rDimension.addProperty(RDFS.label, endpoint.getDataSet(j).getDimensionLabel(k));
					
					dUri  = endpoint.getDataSet(j).getDimensionUri(k);					
					dRefUri = endpoint.getDataSet(j).getDimension(k).getRefDimension();							
					if(dRefUri!=null && !dRefUri.equalsIgnoreCase(dUri)){
						Resource rRefDimension = mDataSet.createResource(dRefUri);
						rRefDimension.addProperty(pSameAs, rDimension);
					}						
					
					//spatial dimension
					if(dRefUri!=null && dRefUri.equals("http://purl.org/linked-data/sdmx/2009/dimension#refArea")){									
						bArea=true;							
						for(v=0; v<endpoint.getDataSet(j).getDimension(k).getValueSize(); v++){							
							vUri   = endpoint.getDataSet(j).getDimension(k).getValueUri(v);
							index = geoAreas.getIndex(vUri);
			        		if(index!=-1){
			        			Resource rValue = mDataSet.createResource(vUri);	
			        			if(!geoAreas.getLabel(index).isEmpty())
			        				rValue.addProperty(RDFS.label, geoAreas.getLabel(index));				        			
			        			rDimension.addProperty(pValue, rValue);
			        			rDataSet.addProperty(pValue, rValue);
			        			Resource rGoogleArea = mDataSet.createResource(geo+googleAreas.get(index).getGoogleArea(0).getUri());
			        			rGoogleArea.addProperty(pSameAs, rValue);	
			        		}
			        	}
					}
					
					//temporal dimension
					else if(dRefUri!=null && dRefUri.equals("http://purl.org/linked-data/sdmx/2009/dimension#refPeriod")){									
						bTime=true;								
						for(v=0; v<endpoint.getDataSet(j).getDimension(k).getValueSize(); v++){							
							timeValue   = endpoint.getDataSet(j).getDimension(k).getValueUri(v);
							vLabel = endpoint.getDataSet(j).getDimension(k).getValueLabel(v);
							times.add(timeValue);	
							Resource rValue=null;
							String sRefTime = getReferenceResource(timeValue);
							if(timeValue.startsWith("http:")){
								rValue = mDataSet.createResource(timeValue);									
								if(vLabel!="")
									rValue.addProperty(RDFS.label, vLabel);
								rDimension.addProperty(pValue, rValue);
								rDataSet.addProperty(pValue, rValue);								
							}
							else{
								rDimension.addProperty(pValue, timeValue);
								rDataSet.addProperty(pValue, timeValue);
							}							
							if(sRefTime!=null && !sRefTime.isEmpty()){
								Resource rRefTime = mDataSet.createResource(sRefTime);
								if(timeValue.startsWith("http"))
									rRefTime.addProperty(pSameAs, rValue);
								else
									rRefTime.addProperty(pSameAs, timeValue);
							}
			        	}
					}	
					//other dimensions
					else{
						for(v=0; v<endpoint.getDataSet(j).getDimension(k).getValueSize(); v++){							
							vUri   = endpoint.getDataSet(j).getDimension(k).getValueUri(v);
							vLabel   = endpoint.getDataSet(j).getDimension(k).getValueLabel(v);							
							if(vUri.startsWith("http")){
								Resource rValue = mDataSet.createResource(vUri);
								if(!vLabel.isEmpty())
									rValue.addProperty(RDFS.label, vLabel);
								if(dRefUri!=null){
									vRefUri = dimensions.getValueReference(dRefUri, vUri, vLabel);
									if(vRefUri!=null && !vRefUri.equalsIgnoreCase(vUri)){
										Resource rRefValue = mDataSet.createResource(vRefUri);
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
										Resource rRefValue = mDataSet.createResource(vRefUri);
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
					
					uri = endpoint.getDataSet(j).createSpecialGeoArea(s.replaceAll("\\s+",""));
					index=geoAreas.getIndex(uri);
					if(index!=-1){
						Resource rDimension = mDataSet.createResource(endpoint.getDataSet(j).createSpecialSpatialDimension());
						rDimension.addProperty(RDF.type, QB.DimensionProperty);
						rDimension.addProperty(RDFS.label, "Ref Area");
						rDimension.addProperty(RDF.type, rTempProperty);
						Resource rRefDimension = mDataSet.createResource("http://purl.org/linked-data/sdmx/2009/dimension#refArea");
			    		rRefDimension.addProperty(pSameAs, rDimension);	
						Resource rValue = mDataSet.createResource(uri);						
						rValue.addProperty(RDFS.label, s);
						Resource rGoogleArea  = mDataSet.createResource(geo+googleAreas.get(index).getGoogleArea(0).getUri());		        				        					        			
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
						Resource rDimension = mDataSet.createResource(endpoint.getDataSet(j).createSpecialTemporalDimension());
						rDimension.addProperty(RDF.type, QB.DimensionProperty);
						rDimension.addProperty(RDFS.label, "Ref Period");							
						rDimension.addProperty(RDF.type, rTempProperty);
						Resource rRefDimension = mDataSet.createResource("http://purl.org/linked-data/sdmx/2009/dimension#refPeriod");
			    		rRefDimension.addProperty(pSameAs, rDimension);	
						Resource rValue = mDataSet.createResource(uri);
						rValue.addProperty(RDFS.label, timeValue);	
						String sRefTime = getReferenceResource(timeValue);
						if(sRefTime!=null && !sRefTime.isEmpty()){
							Resource rRefTime = mDataSet.createResource(sRefTime);							
							rRefTime.addProperty(pSameAs, rValue);
						}
	        			rDimension.addProperty(pValue, rValue); 
	        			rDataSet.addProperty(pValue, rValue);
	        			rMetaData.addProperty(pComponent, rDimension);
					}										
				}					
	    	}	    
	    	try {
	    		mOutput.add(mDataSet);
	    		FileOutputStream fout = new FileOutputStream(folderEndpoint + File.separator + sDSName + ".ttl");
				log.info("Writing metadata for dataset " + 	sDSName);
				mDataSet.write(fout, "Turtle", null);	
				if(n>1)
					FileOperation.copyFolder(folderEndpoint + File.separator + sDSName+".ttl", 
								folderId + File.separator + sDSName+".ttl");
				fout.close();
			} catch (IOException e) {
				log.info("Exception caught when writing file: " + e.toString());
			} 
		}
		try {    		
			FileOutputStream fout = new FileOutputStream(folderId + File.separator + "0_all.ttl");
			log.info("Writing metadata for all selected datasets ");
			mOutput.write(fout, "Turtle", null);	
			fout.close();
		} catch (IOException e) {
			log.info("Exception caught when writing file: " + e.toString());
		}	
		System.out.println("Done");
	}
	
	public void createMetaDataForTime(){
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
	
	public  void createMetaDataForYear(String time_value){
		int j;
		
		Resource rQuarterType = mOutput.createResource(intervals + "CalendarQuarter");
		Resource rMonthType = mOutput.createResource(intervals + "CalendarMonth");	
		Property pNarrower 	= mOutput.createProperty(skos+"narrower");
		Property pBroader 	= mOutput.createProperty(skos+"broader");	
		Resource rUKTime    = mOutput.getResource(id+"gregorian-year/"+time_value);	
		
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
	
	public void createMetaDataForQuarter(String time_value){
		int j, from, to;
		
		Resource rMonthType = mOutput.createResource(intervals + "CalendarMonth");		
		Property pNarrower 	= mOutput.createProperty(skos+"narrower");
		Property pBroader 	= mOutput.createProperty(skos+"broader");	
		Resource rUKTime    = mOutput.getResource(id+"gregorian-quarter/"+time_value);	
		
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
	
	public void createMetaDataForArea() {
		int i, j, index, t;			
		String uri, label, typeGoogleBroaderArea="",  uriGoogleBroaderArea="", uriGoogleBoBArea="", sGoogleUri, sCountryLabel, folderAreas;
		boolean bSpecial;		
		
		/* Part 1. Read data from XML files
		 * Filter the results based on hierarchical level
		 */
		folderAreas = folderEndpoint + File.separator + "areas";
		
		for(i=0; i< geoAreas.getSize(); i++){		
			
			GoogleAreaList gList = new GoogleAreaList();	
			uriGoogleBroaderArea="";
			uriGoogleBoBArea="";
			typeGoogleBroaderArea="";
			
			uri = geoAreas.getUri(i);
			label = geoAreas.getLabel(i);
			sCountryLabel = isCountry(uri, label);
			if(sCountryLabel!=null){
				googleAreas.add(gList);
				GoogleArea gArea = new GoogleArea();
				sCountryLabel = sCountryLabel.replaceAll("\\s+", "");
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
				uriGoogleBroaderArea = googleAreas.get(index).getGoogleArea(0).getUri();
				typeGoogleBroaderArea = googleAreas.get(index).getType();
				if(typeGoogleBroaderArea.equalsIgnoreCase("non-administrative-area")){
					uriGoogleBoBArea = getGoogleBroaderArea(uriGoogleBroaderArea);
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
			setArea(i, folderAreas);
					
			if(index!=-1 && googleAreas.get(i).getSize()>0){				
				//Step 1. filter by broader area
				for(j=0; j<googleAreas.get(i).getSize(); j++){
					sGoogleUri = googleAreas.get(i).getGoogleArea(j).getUri();
					if((bSpecial==false && typeGoogleBroaderArea.equals("administrative-area") && 
							!isGoogleBroaderArea(uriGoogleBroaderArea, sGoogleUri))||
					   (typeGoogleBroaderArea.equals("non-administrative-area") && 
						    !isGoogleBroaderArea(uriGoogleBoBArea, sGoogleUri))  ||
					   (bSpecial==true && 
					   		!uriGoogleBroaderArea.equalsIgnoreCase(sGoogleUri))){						
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
			if(!uriGoogleBroaderArea.isEmpty())
				googleAreas.get(i).setUriGoogleBroaderArea(uriGoogleBroaderArea);	
			if(!uriGoogleBoBArea.isEmpty())
				googleAreas.get(i).setUriGoogleBroaderArea(uriGoogleBoBArea);
			
		}	
		
		/*
		 * Part 2. Areas have broader area
		 */
		for(i=0; i<geoAreas.getSize(); i++){		
			
			uriGoogleBroaderArea="";
			uriGoogleBoBArea="";
			typeGoogleBroaderArea="";
			
			uri = geoAreas.getUri(i);
			label = geoAreas.getLabel(i);
			if(googleAreas.get(i).getSize()==1 && !googleAreas.get(i).getType().isEmpty()) 
				continue;
			
			index = geoAreas.indexOfBroaderArea(uri);			
			if(index!=-1 && !googleAreas.get(index).getType().isEmpty()){				
				uriGoogleBroaderArea = googleAreas.get(index).getGoogleArea(0).getUri();
				typeGoogleBroaderArea = googleAreas.get(index).getType();
				if(typeGoogleBroaderArea.equalsIgnoreCase("non-administrative-area")){
					uriGoogleBoBArea = getGoogleBroaderArea(uriGoogleBroaderArea);
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
						if((bSpecial==false && typeGoogleBroaderArea.equals("administrative-area") && 
								!isGoogleBroaderArea(uriGoogleBroaderArea, sGoogleUri))||
						   (typeGoogleBroaderArea.equals("non-administrative-area") && 
							    !isGoogleBroaderArea(uriGoogleBoBArea, sGoogleUri))  ||
						   (bSpecial==true && 
						   		!uriGoogleBroaderArea.equalsIgnoreCase(sGoogleUri))){						
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
					gTemp.setUri(uriGoogleBroaderArea + "/" + label.replaceAll("\\s+",""));					
					googleAreas.get(i).addGoogleArea(gTemp);
					googleAreas.get(i).setType("non-administrative-area");				
				}
				if(!uriGoogleBroaderArea.isEmpty())
					googleAreas.get(i).setUriGoogleBroaderArea(uriGoogleBroaderArea);	
				if(!uriGoogleBoBArea.isEmpty())
					googleAreas.get(i).setUriGoogleBroaderArea(uriGoogleBoBArea);
			}
		}
		
		
		/*
		 * Part 3. Remaining areas
		 */
		for(i=0; i<geoAreas.getSize(); i++){		
			
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
				gTemp.setUri(uriGoogleBroaderArea + "World/tmp/" + label.replaceAll("\\s+",""));					
				googleAreas.get(i).addGoogleArea(gTemp);
				googleAreas.get(i).setType("non-administrative-area");				
			}
			if(!uriGoogleBroaderArea.isEmpty())
				googleAreas.get(i).setUriGoogleBroaderArea(uriGoogleBroaderArea);	
			if(!uriGoogleBoBArea.isEmpty())
				googleAreas.get(i).setUriGoogleBroaderArea(uriGoogleBoBArea);
		}
		
		// write metadata		
		System.out.println("Creating metadata for geographical areas");
		generateMetaData();	
	}
	
	public void generateMetaData() {	
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
	
	public void setArea(int index, String folderAreas) {		
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
	
	public String isCountry(String sUri, String sLabel){	
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

	public static String getShortName(String endpointURI){	
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
	
	
	public String getReferenceResource(String sTimeValue){
		String rTimeRef=null;
		String sQuery = "";
		if(sTimeValue.startsWith("http"))
			sQuery ="PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+								
					"SELECT ?r \n"+
					"WHERE{ \n"+
						"?r owl:sameAs <" + sTimeValue + ">. \n"+				    
					"}";
		else
			sQuery ="PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+								
					"SELECT ?r \n"+
					"WHERE{ \n"+
						"?r owl:sameAs \"" + sTimeValue + "\". \n"+				    
					"}";
		QueryExecution queryExecution = null;
		try{			
			Query query = QueryFactory.create(sQuery);		
			queryExecution = QueryExecutionFactory.create(query, mOutput);	
			ResultSet rs = queryExecution.execSelect();		
			while (rs!=null && rs.hasNext()) {
				QuerySolution sol = rs.nextSolution();
				if(sol.contains("r"))
					rTimeRef = sol.get("r").toString();	
			}
		}finally {
			if(queryExecution!=null)
				queryExecution.close() ;
		}
		return rTimeRef;
	}	
}
