package tuwien.ldlab.statspace.model.widgetgeneration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

import tuwien.ldlab.statspace.model.util.FileOperation;
import tuwien.ldlab.statspace.model.util.Support;


public class DataSet{
	private String uri;
	private String label;	
	private DimensionList d;
	private MeasureList m;
	private AttributeList a;
	private String subject;
	private int count;
	
	private boolean bUseDistinct;

	private boolean bUseQBMeasureType;

	public DataSet(){
		uri = "";
		label="";		
		d  = new DimensionList();
		m  = new MeasureList();
		a  = new AttributeList();
		bUseDistinct = true;	
		bUseQBMeasureType=false;
		subject = "";
		count=0;
	}	
	
	public DataSet(String uri, String label){
		this.uri = uri;
		this.label= label;
		d  = new DimensionList();
		m  = new MeasureList();
		a  = new AttributeList();
		bUseDistinct = true;		
		bUseQBMeasureType=false;
		subject = "";
		count=0;
	}	
	
	public void setUri(String uri){this.uri = uri;}	
	public void setLabel(String label){this.label = label;}	
	public void setSubject(String sub){this.subject = sub;}	
	public void setBUseDistinct(boolean bDistinct){this.bUseDistinct = bDistinct;}	
	
	public void addDimension(String uri, String label){this.d.addDimension(uri, label);}
	public void removeDimension(String uri){this.d.removeDimension(uri);}	
	public void addMeasure(String uri, String label){this.m.addMeasure(uri, label);}	
	public void removeMeasure(String uri){this.m.removeMeasure(uri);}
	public void addAttribute(String uri, String label){this.a.addAttribute(uri, label);}
		
	public String getUri(){return uri;}	
	public String getLabel(){return label;}	
	public String getSubject(){return subject;}	
	public String getDimensionUri(int i){return d.getDimensionUri(i);}		
	public String getDimensionLabel(int i){return d.getDimensionLabel(i);}	
	public String getMeasureUri(int i){return m.getMeasureUri(i);}	
	public String getMeasureLabel(int i){return m.getMeasureLabel(i);}
	public String getAttributeUri(int i){return a.getAttributeUri(i);}	
	public String getAttributeLabel(int i){return a.getAttributeLabel(i);}	
	public boolean getBUseDistinct(){return bUseDistinct;}	
	public DimensionList getDimension(){return d;}		
	public Dimension getDimension(int i){return d.getDimension(i);}		
	public MeasureList getMeasure(){return m;}
	public AttributeList getAttribute(){return a;}		
	public Attribute getAttribute(int i){return a.getAttribute(i);}		
	public int getDimensionSize(){return d.getSize();}	
	public int getMeasureSize(){return m.getSize();}
	public int getAttributeSize(){return a.getSize();}
	
	public String createSpecialGeoArea(String sCountry){return uri+"/cl_area/tmp/"+sCountry;}	
	public String createSpecialTemporalValue(String sTime){return uri+"/cl_period/tmp/"+sTime;}
	public String createSpecialAttribute(){return uri+"/unitMeasure";}
	public String createSpecialSpatialDimension(){return uri+"/refArea";}	
	public String createSpecialTemporalDimension(){return uri+"/refPeriod";}
		
	

	
	public String identifyTimeValue(){
		String sYear   = "[1-9][0-9]{3}";
		String sQuater = "[1-9][0-9]{3}-Q[1-4]";		
		
		Pattern pYear   = Pattern.compile(sYear);
		Pattern pQuater = Pattern.compile(sQuater);
		
		Matcher m;
		
		//find timespan in label of data set
		m = pQuater.matcher(label);
		if(m.find())
			return label.substring(m.start(),  m.end());			
		else{
			m = pYear.matcher(label);
			if(m.find())
				return label.substring(m.start(),  m.end());
		}
		
		//find timespan in uri of data set
		m = pQuater.matcher(uri);
		if(m.find())
			return uri.substring(m.start(),  m.end());			
		else{
			m = pYear.matcher(uri);
			if(m.find())
				return uri.substring(m.start(),  m.end());
		}
		return "";
	}
	
	public void remove(){
		uri="";		
		d.clearAll();
		m.clearAll();
		a.clearAll();
	}
	
	public void queryComponent(String endpointForQuery, boolean bHTTP, String sUseDistinct){
		String queryString;	
		int i;
		
		if(sUseDistinct.equals("")){
			queryCounter(endpointForQuery, bHTTP);			
		}else 
			if(sUseDistinct.equalsIgnoreCase("No"))
				bUseDistinct = false;
		
//		System.out.println("Query components...");
		/* Step 1. DataSet - DataStructureDefinition - ComponentProperty - Dimension/Measure
		 * 1.1. Query dimensions
		 */		
//		System.out.print("\tWay 1 - DSD: ");
		queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+						
						"SELECT ?c ?l \n"+ 
						"WHERE{ \n"+       
							"<"+ uri + "> qb:structure ?dsd. \n"+
							"?dsd qb:component ?cp. \n"+
							"?cp qb:dimension ?c. \n"+
							"optional{" +
								"?c ?r ?l. \n"+
								"FILTER (REGEX(str(?r), \"label\", \"i\")) \n"+							
							"} \n"+			
						"}";		
		if(!bHTTP)
			getComponent(endpointForQuery, queryString, 0);
		else
			getComponent2(endpointForQuery, queryString, 0);
		
		delay(2);			
		
		if(d.getSize()!=0){	
			
			//1.2. Query Measures if we have result from the first step  			
			queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+							
							"SELECT ?c ?l \n"+ 
							"WHERE{ \n"+       
								"<"+ uri + "> qb:structure ?dsd. \n"+
								"?dsd qb:component ?cp. \n"+
								"?cp qb:measure ?c. \n"+
								"optional{" +
									"?c ?r ?l. \n"+
									"FILTER (REGEX(str(?r), \"label\", \"i\")) \n"+							
								"} \n"+			
							"}";		
			if(!bHTTP)
				getComponent(endpointForQuery, queryString, 1);
			else
				getComponent2(endpointForQuery, queryString, 1);		
			
			//check the number of measures in each observation => Multiple Measure or Dimension Measure approaches
			//can use other approach: check qb:measureType in DSD
			
			if(m.getSize()>1){
				delay(2);
				String queryMeasure="";
				boolean bCheck;				
				for(i=0; i<m.getSize(); i++)
					queryMeasure = queryMeasure + "\n " +
				                   "?o <" + m.getMeasureUri(i) + "> ?m"+i + ". \n";
				if(uri.contains(" "))			
					queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+					
									"SELECT ?o \n"+
									"WHERE{ \n"+						
										"?o qb:dataSet ?ds. "+
										queryMeasure + 
										"FILTER (REGEX(str(?ds), \"" + uri + "\", \"i\")) \n"+						
									"}LIMIT 1";	
				else
					queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+					
									"SELECT ?o \n"+
									"WHERE{ \n"+						
										"?o qb:dataSet <"+ uri + ">. \n"+			
										queryMeasure +								
									"}LIMIT 1";	
				if(!bHTTP)
					bCheck = checkMultiMeasure(endpointForQuery, queryString);
				else
					bCheck = checkMultiMeasure2(endpointForQuery, queryString);
				m.setBMultipleMeasure(bCheck);
			}else
				m.setBMultipleMeasure(false);
			
			//1.3. Query Attribute in case of there is only one measure
			if(m.getSize()==1){
				queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+							
								"SELECT ?c ?l \n"+ 
								"WHERE{ \n"+       
									"<"+ uri + "> qb:structure ?dsd. \n"+
									"?dsd qb:component ?cp. \n"+
									"?cp qb:attribute ?c. \n"+
									"optional{" +
										"?c ?r ?l. \n"+
										"FILTER (REGEX(str(?r), \"label\", \"i\")) \n"+									
									"} \n"+			
								"}";		
				if(!bHTTP)
					getComponent(endpointForQuery, queryString, 2);
				else
					getComponent2(endpointForQuery, queryString, 2);		
			}
									
//			System.out.println("Dimension: " + d.getSize() + "; Measure: " + m.getSize() + "; Attribute: " + a.getSize());
					
		}else{
			
			/*
			 * Step 2.1 Choose one observation
			 */
//			System.out.print("\n\tWay 2.1 - ");	
			ArrayList<String> results=new ArrayList<String>();
			queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+					
							"SELECT ?o \n"+
							"WHERE{ \n"+
								"?o qb:dataSet <"+ uri + ">. \n"+													
								"FILTER(!REGEX(str(?o),\" \")) \n"+
							"}LIMIT 1";
			if(!bHTTP)
				results = getObservation(endpointForQuery, queryString);
			else
				results = getObservation2(endpointForQuery, queryString);
			
			if(results.size()==1){			
				delay(2);
				queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
								"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
								"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
								"SELECT ?c ?t ?v ?l \n"+
								"WHERE{ \n"+								
									"<" + results.get(0) + "> ?c ?v. \n"+
									"optional{?c rdf:type ?t.} \n"+
									"optional{" +
										"?c ?r ?l. \n"+
										"FILTER (REGEX(str(?r), \"label\", \"i\")) \n"+							
									"} \n"+
								"}";
				
				if(!bHTTP)
					getComponent(endpointForQuery, queryString, 3);
				else
					getComponent2(endpointForQuery, queryString, 3);
				
				if(m.getSize()==1) m.setBMultipleMeasure(false);
				else m.setBMultipleMeasure(true);	
			}
			
			/* Step 2.2 - Get sets: ?c ?v ?r ?l
			 * Maybe the above Obs has only one measure
			 */	
//			System.out.print("\n\tWay 2.2 - ");
			delay(2);			
			
			if(bUseQBMeasureType){				
				queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
								"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
								"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
								"SELECT ?c ?l \n"+
								"WHERE{ \n"+
									"?o qb:dataSet <"+ uri + ">. \n"+
									"?o ?c ?v. \n" +
									"?c rdf:type qb:MeasureProperty. \n"+									
									"optional{" +									    
										"?c ?r ?l. \n"+													
										"FILTER (REGEX(str(?r), \"label\", \"i\")) \n"+							
									"} \n"+												
								"}LIMIT 100";
				if(!bHTTP)
					getComponent(endpointForQuery, queryString, 1);
				else
					getComponent2(endpointForQuery, queryString, 1);				
			}
			else{				
				queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
								"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
								"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
								"SELECT ?c ?v ?l \n"+
								"WHERE{ \n"+
									"?o qb:dataSet <"+ uri + ">. \n"+
									"?o ?c ?v. \n"+											
									"optional{ \n" +
										"?c ?r ?l. \n"+
										"FILTER (REGEX(str(?r), \"label\", \"i\")) \n"+							
									"} \n"+
									"FILTER(?v != <http://purl.org/linked-data/cube#Observation>) \n"+
								"}LIMIT 100";
				if(!bHTTP)
					getComponent(endpointForQuery, queryString, 3);
				else
					getComponent2(endpointForQuery, queryString, 3);
			}
		}	
			
//		System.out.println("Dimension: " + d.getSize() + "; Measure: " + m.getSize() + "; Attribute: " + a.getSize() + " TypeM: " + bUseQBMeasureType);
		
		//Remain only one unit of measure
		i=0;
		while(a.getSize()!=1 && i<a.getSize()){
			if(!a.getAttributeUri(i).toLowerCase().contains("unit")){
				System.out.println("Remove attribute: " + a.getAttributeUri(i));
				a.removeAttribute(i);
			}else
				i++;			
		}
		while(a.getSize()>1){
			System.out.println("To remain only one attribute, remove attribute: " + a.getAttributeUri(1));
			a.removeAttribute(1);
		}
		
		/* Notifications
		*/			
		if(d.getSize()==0){	
			System.out.println("*****************************");
			System.out.println("Can not find any Dimension ");
			System.out.println("*****************************");
		}			
		
		if(m.getSize()==0){	
			System.out.println("*****************************");
			System.out.println("Can not find any Measure ");
			System.out.println("*****************************");
			
		}		
	}
	
	public void queryCounter(String endpointForQuery, boolean bHTTP){
		String queryString, scounter;
		int j, n;		
		queryString =	
				"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+					
				"SELECT (count(?o) as ?c) \n"+
				"WHERE{ \n"+
					"?o qb:dataSet <" + uri +">\n"+				
				"}";
		
		if(!bHTTP)
			scounter = getCounter(endpointForQuery, queryString);
		else
			scounter = getCounter2(endpointForQuery, queryString);		
		try{
			j = scounter.indexOf("^^");
			if(j!=-1)
				scounter = scounter.substring(0, j);
			n = Integer.parseInt(scounter);
			count=n;
		}catch (Exception e){
			n=100000;
		}
		if(n>=100000){
			bUseDistinct = false;			
		}
		else
			bUseDistinct = true;
		
//		System.out.println("Counter: "+ n + "; useDistinct: " +bUseDistinct);		
	}
	
	public void getComponent(String endpointForQuery, String queryString, int type) {
		QueryExecution queryExecution=null;
		String scomponent, stype, svalue, slabel;
		
		int i, k;		
		try{			
			ComponentInfoList temp_all = new ComponentInfoList();
			ComponentInfoList temp_d = new ComponentInfoList();
			ComponentInfoList temp_m = new ComponentInfoList();
			
			Query query = QueryFactory.create(queryString);		
			queryExecution = QueryExecutionFactory.sparqlService(endpointForQuery, query);	
			ResultSet rs = queryExecution.execSelect();		
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();				
				scomponent = sol.get("c").toString();				
				stype="";
				if(sol.contains("t"))	stype = sol.get("t").toString().replace("\n", "").replace("\r", "").trim();	
				slabel="";
				if(sol.contains("l"))	slabel = sol.get("l").toString().replace("\n", "").replace("\r", "").trim();	
				svalue="";
				if(sol.contains("v")) 	svalue = sol.get("v").toString().replace("\n", "").replace("\r", "").trim();		
				
				if(type==0){				
					addDimension(scomponent, slabel);
				}else if(type==1){
					bUseQBMeasureType = true;
					addMeasure(scomponent, slabel);
				}else if(type==2){
					addAttribute(scomponent, slabel);
				}else if(type==3){					
					//If this component already appeared in dimension, measure list => ignore
					if(d.indexOf(scomponent)!=-1 || m.indexOf(scomponent)!=-1){
						if(slabel.contains("@en"))
							if(d.indexOf(scomponent)!=-1)
								addDimension(scomponent, slabel);
							else
								addMeasure(scomponent, slabel);								
						continue;
					}
					
					k = Support.detectProperty(scomponent, stype, svalue);					
					if(k==0){					
						addDimension(scomponent, slabel);
						temp_all.removeComponentInfo(scomponent);
						temp_d.removeComponentInfo(scomponent);
						temp_m.removeComponentInfo(scomponent);
					}else if(k==1){
						addMeasure(scomponent, slabel);	
						if(stype.contains("measure"))
							bUseQBMeasureType = true;
						temp_all.removeComponentInfo(scomponent);
						temp_d.removeComponentInfo(scomponent);
						temp_m.removeComponentInfo(scomponent);
					}else if(k==2){
						addAttribute(scomponent, slabel);
						temp_all.removeComponentInfo(scomponent);
						temp_d.removeComponentInfo(scomponent);
						temp_m.removeComponentInfo(scomponent);
					}else if(k==3) {						
						temp_d.addComponentInfo(scomponent, slabel);								
					}else if(k==4) {
						temp_m.addComponentInfo(scomponent, slabel);								
					}else if(k==5) {
						temp_all.addComponentInfo(scomponent, slabel);
					}else if(k==6) {
						temp_all.removeComponentInfo(scomponent);
						temp_d.removeComponentInfo(scomponent);
						temp_m.removeComponentInfo(scomponent);
					}
				}
			}
			if(type==3){	
				for(i=0; i<temp_m.getSize(); i++){
					addMeasure(temp_m.getIndex(i).getComponent(), temp_m.getIndex(i).getLabel());
				}				
				for(i=0; i<temp_d.getSize(); i++){
					addDimension(temp_d.getIndex(i).getComponent(), temp_d.getIndex(i).getLabel());
				}				
				for(i=0; i<temp_all.getSize(); i++){			
					scomponent = temp_all.getIndex(i).getComponent().toLowerCase();
					slabel = temp_all.getIndex(i).getLabel();				
					if(m.getSize()>0)
						addDimension(temp_all.getIndex(i).getComponent(),slabel);
					else if(scomponent.contains("value") || scomponent.contains("obs") || scomponent.contains("amount"))
						addMeasure(temp_all.getIndex(i).getComponent(), slabel);
					else 
						System.out.println("Cannot detect type of element: " + scomponent+";" + slabel);
				}
			}
		}catch (QueryExceptionHTTP e){	
		}		
		catch(QueryException e){
		}
		catch(Exception e){
		}		
		finally {
			if(queryExecution!=null)
				queryExecution.close() ;
		}	
	}
	
	public void getComponent2(String endpointForQuery, String queryString, int type) {
		String scomponent, stype, svalue, slabel, sname, stext;
		int i, j, k;			
		ComponentInfoList temp_all = new ComponentInfoList();
		ComponentInfoList temp_d = new ComponentInfoList();
		ComponentInfoList temp_m = new ComponentInfoList();
		
		try{
			String url = endpointForQuery + "?query=" + URLEncoder.encode(queryString, "UTF-8");
			if(url.indexOf("xml")==-1)
				url = url + "&format=application%2Fsparql-results%2Bxml";
			
			URL obj = new URL(url);		
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();		
			con.setRequestMethod("GET"); 
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
	 
			int responseCode = con.getResponseCode();			
			if(responseCode==200){
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(con.getInputStream());
				doc.getDocumentElement().normalize();
				NodeList nList = doc.getElementsByTagName("result");			
				for (i = 0; i < nList.getLength(); i++) {					
					Node nNode = nList.item(i);						
					NodeList nNodeList = nNode.getChildNodes();
					scomponent=""; svalue=""; stype=""; slabel=""; 				
					for(j=0; j<nNodeList.getLength(); j++){
						if(nNodeList.item(j).getNodeType() == Node.ELEMENT_NODE){
							Element eElement = (Element) nNodeList.item(j);
							sname = eElement.getAttribute("name");
							stext = eElement.getTextContent();
							stext = stext.replace("\n", "").replace("\r", "").trim();							
							if(sname.equals("c")) 		scomponent = stext;
							else if(sname.equals("v")) 	svalue = stext;
							else if(sname.equals("t"))	stype = stext;
							else if(sname.equals("l"))	slabel = stext;							
						}					
					}					
					if(type==0){
						addDimension(scomponent, slabel);					
					}else if(type==1){
						addMeasure(scomponent, slabel);
						bUseQBMeasureType=true;
					}else if(type==2){
						addAttribute(scomponent, slabel);
					}else if(type==3){						
						//If this component already appeared in dimension, measure list => ignore
						if(d.indexOf(scomponent)!=-1 || m.indexOf(scomponent)!=-1){
							if(d.indexOf(scomponent)!=-1 || m.indexOf(scomponent)!=-1){
								if(slabel.contains("@en"))
									if(d.indexOf(scomponent)!=-1)
										addDimension(scomponent, slabel);
									else
										addMeasure(scomponent, slabel);								
								continue;
							}
						}
						
						k = Support.detectProperty(scomponent, stype, svalue);					
						if(k==0){					
							addDimension(scomponent, slabel);						
							temp_all.removeComponentInfo(scomponent);
							temp_d.removeComponentInfo(scomponent);
							temp_m.removeComponentInfo(scomponent);
						}else if(k==1){							
							addMeasure(scomponent, slabel);
							if(stype.contains("measure"))
								bUseQBMeasureType=true;
							temp_all.removeComponentInfo(scomponent);
							temp_d.removeComponentInfo(scomponent);
							temp_m.removeComponentInfo(scomponent);
						}else if(k==2){	
							addAttribute(scomponent, slabel);
							temp_all.removeComponentInfo(scomponent);
							temp_d.removeComponentInfo(scomponent);
							temp_m.removeComponentInfo(scomponent);
						}else if(k==3) {						
							temp_d.addComponentInfo(scomponent, slabel);								
						}else if(k==4) {
							temp_m.addComponentInfo(scomponent, slabel);								
						}else if(k==5) {
							temp_all.addComponentInfo(scomponent, slabel);
						}else if(k==6) {
							temp_all.removeComponentInfo(scomponent);
							temp_d.removeComponentInfo(scomponent);
							temp_m.removeComponentInfo(scomponent);
						}										
					}			
				}
				if(type==3){	
					for(i=0; i<temp_m.getSize(); i++){
						addMeasure(temp_m.getIndex(i).getComponent(), temp_m.getIndex(i).getLabel());
					}				
					for(i=0; i<temp_d.getSize(); i++){
						addDimension(temp_d.getIndex(i).getComponent(), temp_d.getIndex(i).getLabel());
					}				
					for(i=0; i<temp_all.getSize(); i++){			
						scomponent = temp_all.getIndex(i).getComponent().toLowerCase();
						slabel = temp_all.getIndex(i).getLabel();				
						if(m.getSize()>0)
							addDimension(temp_all.getIndex(i).getComponent(),slabel);
						else if(scomponent.contains("value") || scomponent.contains("obs") || scomponent.contains("amount"))
							addMeasure(temp_all.getIndex(i).getComponent(), slabel);
						else 
							System.out.println("Cannot detect type of element: " + scomponent+";" + slabel);
					}
				}
			}	
		}catch(Exception e){			
		}		
	}
	
	public boolean checkMultiMeasure(String endpointForQuery, String queryString) {		
		QueryExecution queryExecution = null;		
		boolean bCheck=false;
		try{	
			Query query = QueryFactory.create(queryString);		
			queryExecution = QueryExecutionFactory.sparqlService(endpointForQuery, query);	
			// execute query
			ResultSet rs = queryExecution.execSelect();				
			while (rs!=null && rs.hasNext()) {		
				bCheck=true;
				break;
			}			
		}catch (QueryExceptionHTTP e){	
		}	
		catch(QueryException e){
		}
		catch(Exception e){
		}		
		finally {
			if(queryExecution!=null)
				queryExecution.close() ;
		}	
		return bCheck;
	}
	
	public boolean checkMultiMeasure2(String endpointForQuery, String queryString) {
		boolean bCheck = false;
		try{
			String url = endpointForQuery + "?query=" + URLEncoder.encode(queryString, "UTF-8");
			if(url.indexOf("xml")==-1)
				url = url + "&format=application%2Fsparql-results%2Bxml";
			
			URL obj = new URL(url);		
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	 
			// optional default is GET
			con.setRequestMethod("GET");
	 
			//add request header
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
	 
			int responseCode = con.getResponseCode();		
			if(responseCode==200){
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(con.getInputStream());
				doc.getDocumentElement().normalize();
				NodeList nList = doc.getElementsByTagName("binding");
				if(nList.getLength()>0) bCheck = true;				
			}	
		}catch(Exception e){			
		}		
		return bCheck;
	}
	
	public ArrayList<String> getObservation(String endpointForQuery, String queryString) {		
		String name="";
		ArrayList<String> results=new ArrayList<String>();
		QueryExecution queryExecution = null;			
		try{	
			Query query = QueryFactory.create(queryString);		
			queryExecution = QueryExecutionFactory.sparqlService(endpointForQuery, query);	
			// execute query
			ResultSet rs = queryExecution.execSelect();
			
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();
				name = sol.get("o").toString();	
				name = name.replace("\n", "").replace("\r", "").trim();
				results.add(name);					
			}			
		}catch (QueryExceptionHTTP e){	
		}	
		catch(QueryException e){
		}
		catch(Exception e){	
		}		
		finally {
			if(queryExecution!=null)
				queryExecution.close() ;
		}
		return results;
		
	}
	
	public ArrayList<String> getObservation2(String endpointForQuery, String queryString) {
		ArrayList<String> results=new ArrayList<String>();
		try{
			String url = endpointForQuery + "?query=" + URLEncoder.encode(queryString, "UTF-8");
			if(url.indexOf("xml")==-1)
				url = url + "&format=application%2Fsparql-results%2Bxml";
			
			URL obj = new URL(url);		
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	 
			// optional default is GET
			con.setRequestMethod("GET");
	 
			//add request header
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
	 
			int responseCode = con.getResponseCode();		
			if(responseCode==200){
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(con.getInputStream());
				doc.getDocumentElement().normalize();
				NodeList nList = doc.getElementsByTagName("binding");			
				for (int temp = 0; temp < nList.getLength(); temp++) {	 
					Node nNode = nList.item(temp);					 
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {	 
						Element eElement = (Element) nNode;	
						String name = eElement.getTextContent();
						name = name.replace("\n", "").replace("\r", "").trim();
						results.add(name);											
					}
				}
			}	
		}catch(Exception e){			
		}
		
		return results;		
	}
	
	public String getCounter(String endpointForQuery, String queryString){		
		String counter="";		
		QueryExecution queryExecution= null;	
		Query query = QueryFactory.create(queryString);
		try{	
			queryExecution = QueryExecutionFactory.sparqlService(endpointForQuery, query);	
			// execute query
			ResultSet rs = queryExecution.execSelect();			
			while (rs!=null && rs.hasNext()) {							
				QuerySolution sol = rs.nextSolution();
				counter = sol.get("c").toString();	
				counter = counter.replace("\n", "").replace("\r", "").trim();
			}			
		}catch (QueryExceptionHTTP e){
		}catch(QueryException e){
		}catch(Exception e){
		}
		finally {
			if(queryExecution!=null)
				queryExecution.close() ;
		}		
		return counter;
	}
	
	public String getCounter2(String endpointForQuery, String queryString){			
		try{			
			String url = endpointForQuery + "?query=" + URLEncoder.encode(queryString, "UTF-8");
			if(url.indexOf("xml")==-1)
				url = url + "&format=application%2Fsparql-results%2Bxml";
			
			URL obj = new URL(url);		
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	 
			// optional default is GET
			con.setRequestMethod("GET");
	 
			//add request header
			con.setRequestProperty("User-Agent", "Mozilla/5.0");	 
			int responseCode = con.getResponseCode();		
			if(responseCode==200){
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(con.getInputStream());
				doc.getDocumentElement().normalize();
				NodeList nList = doc.getElementsByTagName("binding");			
				for (int temp = 0; temp < nList.getLength(); temp++) {	 
					Node nNode = nList.item(temp);					 
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {	 
						Element eElement = (Element) nNode;		
						String counter = eElement.getTextContent();	
						counter = counter.replace("\n", "").replace("\r", "").trim();
						return counter;											
					}
				}
			}
		}catch(Exception e){			
		}		
		return "";	
		
	}	

	public void queryValue(String endpointForQuery, boolean bHTTP, boolean bFindOther, boolean bRemove) {
		// TODO Auto-generated method stub
		int i;
		if(count>5000000){
			/*
			 * Step 1. Query 1000 observations
			 * Step 2. Query values of dimensions, attribute of these observation
			 */
			ArrayList<String> arrObs=new ArrayList<String>();	
			String queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+					
									"SELECT ?o \n"+
									"WHERE{ \n"+
										"?o qb:dataSet <"+ uri + ">. \n"+										
									"}LIMIT 1000";
			if(!bHTTP)
				arrObs = getObservation(endpointForQuery, queryString);
			else
				arrObs = getObservation2(endpointForQuery, queryString);
			
			for(i=0; i<arrObs.size(); i++){			
				queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
								"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
								"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
								"SELECT ?c ?v ?r ?l \n"+
								"WHERE{ \n"+								
									"<" + arrObs.get(i) + "> ?c ?v. \n"+
									"optional{ \n"+
										"?v ?r ?l. \n"+
										"FILTER (REGEX(str(?r), \"label\", \"i\")) \n"+
										"} \n" +
								"}";				
				if(!bHTTP)
					getValue(endpointForQuery, queryString);
				else
					getValue2(endpointForQuery, queryString);
			}
			
		}else{		
			for(i=0; i<d.getSize(); i++){
				delay(2);
				d.getDimension(i).queryValue(endpointForQuery, bHTTP, bUseDistinct, bFindOther, uri);				
			}
			if(bRemove)
				d.removeDuplicate();
			
			for(i=0; i<a.getSize(); i++){
				delay(2);
				a.getAttribute(i).queryValue(endpointForQuery, bHTTP, bUseDistinct, uri);
			}
		}
	}
	
	public void getValue(String endpointForQuery, String queryString) {	
		String code, label, uri, labelType;
		int i;	
		QueryExecution queryExecution = null;			
		try{	
			Query query = QueryFactory.create(queryString);		
			queryExecution = QueryExecutionFactory.sparqlService(endpointForQuery, query);	
			// execute query
			ResultSet rs = queryExecution.execSelect();
			
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();
				uri = sol.get("c").toString();	
				uri = uri.replace("\n", "").replace("\r", "").trim();				  
				code = sol.get("v").toString();				
				if(sol.contains("l"))
					label = sol.get("l").toString().replace("\n", "").replace("\r", "").trim();
				else			
					label= "";				
				if(sol.contains("r"))
					labelType = sol.get("r").toString().replace("\n", "").replace("\r", "").trim();			
				else
					labelType="";
				
				for(i=0; i<d.getSize(); i++)
					if(d.getDimensionUri(i).equals(uri)){
						d.getDimension(i).addValue(code, label);
						if(labelType!="")
							d.getDimension(i).setLabelType(labelType);
						break;
					}
				for(i=0; i<a.getSize(); i++)
					if(a.getAttributeUri(i).equals(uri)){
						a.getAttribute(i).addValue(code, label);
						break;
					}
			 
					
			}			
		}catch (QueryExceptionHTTP e){	
		}	
		catch(QueryException e){
		}
		catch(Exception e){	
		}		
		finally {
			if(queryExecution!=null)
				queryExecution.close() ;
		}
	}
	
	public void getValue2(String endpointForQuery, String queryString) {
		String code, label, sname, stext, sdatatype, labelType="", uri="";		
		int i,j;
		try{
			String url = endpointForQuery + "?query=" + URLEncoder.encode(queryString, "UTF-8");
			if(url.indexOf("xml")==-1)
				url = url + "&format=application%2Fsparql-results%2Bxml";
			
			URL obj = new URL(url);		
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();	 
			con.setRequestMethod("GET");	
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
	 
			int responseCode = con.getResponseCode();		
			if(responseCode==200){
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();				
				try{
					Document doc = dBuilder.parse(con.getInputStream());				
					doc.getDocumentElement().normalize();				
					NodeList nList = doc.getElementsByTagName("result");			
					for (i = 0; i < nList.getLength(); i++) {		
						Node nNode = nList.item(i);						
						NodeList nNodeList = nNode.getChildNodes();
						code=""; label="";sdatatype="";uri="";labelType="";
						for(j=0; j<nNodeList.getLength(); j++){
							if(nNodeList.item(j).getNodeType() == Node.ELEMENT_NODE){
								Element eElement = (Element) nNodeList.item(j);
								sname = eElement.getAttribute("name");
								stext = eElement.getTextContent();
								stext = stext.replace("\n", "").replace("\r", "").trim();
								stext = stext.trim();							
								NodeList nListType = eElement.getElementsByTagName("literal");
								if(nListType.getLength()==1){							
									Node nNodeType = nListType.item(0);
									if(nNodeType.getNodeType() == Node.ELEMENT_NODE)
										sdatatype = ((Element) nNodeType).getAttribute("datatype");								
								}								
								if(sname.equals("c")) uri=stext;								
								else if(sname.equals("v")){
									if(!sdatatype.equals(""))
										code = stext + "^^" + sdatatype;
									else
										code = stext;
								}
								else if(sname.equals("l"))	label = stext;	
								else if(sname.equals("r")) 
									labelType=stext;
							}					
						}
						for(j=0; j<d.getSize(); j++)
							if(d.getDimensionUri(j).equals(uri)){
								d.getDimension(j).addValue(code, label);
								if(labelType!="")
									d.getDimension(j).setLabelType(labelType);
								break;
							}
						for(j=0; j<a.getSize(); j++)
							if(a.getAttributeUri(j).equals(uri)){
								a.getAttribute(j).addValue(code, label);
								break;
							}					 				
					}
				}catch (SAXException e) {
				    e.printStackTrace();
				} catch (IOException e) {
				    e.printStackTrace();
				}
			}	
		}catch(Exception e){			
		}
	}
	
	public void queryValueandCache(String sEndpointForQuery, boolean bHTTP, boolean bFindOther, boolean bRemove, boolean bUseCache, boolean bCreateCache) {
		// TODO Auto-generated method stub
		
		//query
		int i, j;
		String sEP, sDataSet, sDimension="", sAttribute="", s;
		boolean bFound=false;
		
		sEP = sEndpointForQuery;
		sEP = Support.extractFolderName(sEndpointForQuery);
		sDataSet = uri;		
		sDataSet = Support.extractFileName(sDataSet);
			
//		System.out.println("Query values...");
		for(i=0; i<d.getSize(); i++){			
			delay(2);			
			//query dimension
			bFound=false;
			if(bUseCache){
				sDimension = d.getDimensionUri(i);
				sDimension = Support.extractFileName(sDimension);	    		
				bFound = FileOperation.findFile("data" + File.separator + "datasources" + File.separator + sEP + File.separator + "cache" + File.separator +  sDataSet , sDimension +".csv");
			}
			if(bFound){
				BufferedReader br;
				int count=0;
				String sU="", sL="";
				try {
					br = new BufferedReader(new InputStreamReader(new FileInputStream("data" + File.separator + "datasources" + File.separator + sEP + File.separator + "cache" + File.separator + sDataSet + File.separator + sDimension + ".csv"), "UTF-8"));
					while ((s = br.readLine()) != null) {
						//set labelType
						if(count==0){
							d.getDimension(i).setLabelType(s);
						}else{
							if(count %2 ==1){
								sU = s;
							}else{
								sL = s;
								d.getDimension(i).addValue(sU, sL);					
							}	
						}
						count++;
						//set value
					}
					if(count%2==0)
						d.getDimension(i).addValue(sU, "");	
					br.close();
				}catch(IOException e){
		    		e.printStackTrace();
		    	}  
				
			}
			else{				
				if(count<5000000)
					d.getDimension(i).queryValue(sEndpointForQuery, bHTTP, bUseDistinct, bFindOther, uri);
			}
		}
		if(bRemove)
			d.removeDuplicate();
		
		//query attribute
		if(a.getSize()==1){
			bFound=false;			
			if(bUseCache){
				sAttribute = a.getAttributeUri(0);
				sAttribute = Support.extractFileName(sAttribute); 		
				bFound = FileOperation.findFile("data" + File.separator + "datasources" + File.separator + sEP + File.separator + "cache" + File.separator + sDataSet , sAttribute +".csv");
			}
			if(bFound){
				BufferedReader br;
				int count=0;
				String sU="", sL="";
				try {
					br = new BufferedReader(new InputStreamReader(new FileInputStream("data" + File.separator + "datasources" + File.separator + sEP + File.separator + "cache" + File.separator + sDataSet + File.separator + sAttribute + ".csv"), "UTF-8"));
					while ((s = br.readLine()) != null) {
						//set labelType
						if(count==0){
							a.getAttribute(0).setLabelType(s);
						}else{
							if(count %2 ==1){
								sU = s;
							}else{
								sL = s;
								a.getAttribute(0).addValue(sU, sL);					
							}	
						}
						count++;
						//set value
					}
					if(count%2==0)
						a.getAttribute(0).addValue(sU, "");	
					br.close();
				}catch(IOException e){
		    		e.printStackTrace();
		    	}  
				
			}
			else
				if(count<5000000)
					a.getAttribute(0).queryValue(sEndpointForQuery, bHTTP, bUseDistinct, uri);			
		}
		
		if(bFound==false && count>=5000000){
			queryValue(sEndpointForQuery, bHTTP, bFindOther, bRemove);
		}
		
		//create cache		
		if(bCreateCache){				
			File fEndpoint, fDataset;
			//create folder endpoint	    		
    		fEndpoint = new File("data" + File.separator + "datasources" + File.separator + sEP + File.separator + "cache");
    		if(!fEndpoint.exists())  
    			fEndpoint.mkdir();
    			    	
			//create folder dataset    	
    		fDataset = new File("data" + File.separator + "datasources" + File.separator + sEP + File.separator + "cache" + File.separator + sDataSet);
    		if(!fDataset.exists())  
    			fDataset.mkdir(); 	
    		
    		//create cache for dimensions
    		for(i=0; i<d.getSize(); i++){    			
    			if(d.getDimension(i).getValueSize()>0){    				
    				try {
    					sDimension = d.getDimensionUri(i);
    					sDimension = Support.extractFileName(sDimension);
						BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
							    new FileOutputStream("data" + File.separator + "datasources" + File.separator + sEP + File.separator + "cache" + File.separator + sDataSet + File.separator + sDimension +".csv"), "UTF-8"));
						//write labelType
						out.write(d.getDimension(i).getLabelType());				
						
						//write value
						for(j=0; j<d.getDimension(i).getValueSize(); j++){
							out.write("\n");
							out.write(d.getDimension(i).getValueUri(j));
							out.write("\n");
							out.write(d.getDimension(i).getValueLabel(j));							
						}				
						out.close();						
					}catch(IOException e){
			    		e.printStackTrace();
			    	}  	
    			}
    		}
    		
    		//create cache for attribute
    		if(a.getSize()==1){    			
    			if(a.getAttribute(0).getValueSize()>0){    				
    				try {
    					sAttribute = a.getAttributeUri(0);
    					sAttribute = Support.extractFileName(sAttribute);
						BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
							    new FileOutputStream("data" + File.separator + "datasources" + File.separator + sEP + File.separator + "cache" + File.separator + sDataSet + File.separator + sAttribute +".csv"), "UTF-8"));
						//write labelType
						out.write(a.getAttribute(0).getLabelType());				
						
						//write value
						for(j=0; j<a.getAttribute(0).getValueSize(); j++){
							out.write("\n");
							out.write(a.getAttribute(0).getValueUri(j));
							out.write("\n");
							out.write(a.getAttribute(0).getValueLabel(j));							
						}				
						out.close();						
					}catch(IOException e){
			    		e.printStackTrace();
			    	}  	
    			}
    		}    		
		}		
	}	
	
	public void delay(int n){
		try {
		    Thread.sleep(n*100);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}
	
	private class ComponentInfoList{
		private ArrayList<ComponentInfo> arrComp;
		
		public ComponentInfoList(){
			arrComp = new ArrayList<ComponentInfo>();
		}
		
		public ComponentInfo getIndex(int i){
				return arrComp.get(i);
		}
			
		public int getSize(){
				return arrComp.size();
		}
			
			
		public void addComponentInfo(String c, String l){
				int i;
				for(i=0; i<arrComp.size(); i++)
					if(arrComp.get(i).getComponent().equalsIgnoreCase(c))
						break;
				if(i==arrComp.size())
					arrComp.add(new ComponentInfo(c, l));
				else if(l.contains("@en"))
					arrComp.get(i).setLabel(l);	
			}
			
		public void removeComponentInfo(String c){
				int i;
				for(i=0; i<arrComp.size(); i++)
					if(arrComp.get(i).getComponent().equalsIgnoreCase(c))
						arrComp.remove(i);
			}
		}
	private class ComponentInfo{
		private String c;
		private String l;
		
		
		public ComponentInfo(String component, String label){
			c = component;
			l = label;			
		}
			
		public String getComponent(){return c;}
		public String getLabel(){return l;}		
		public void setLabel(String s){l=s;}		
	}
	
	public void queryComponentFromMetaData(String metadata) {
		String queryString;
	
		queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+				
						"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
						"SELECT DISTINCT ?cp ?t ?l \n"+ 
						"WHERE{ \n"+	
						" graph <http://statspace.linkedwidgets.org> { \n" +		
							"<" + metadata + "> qb:component ?cp. \n"+
							"?cp rdf:type ?t. \n"+
							"optional{?cp rdfs:label ?l. }\n"+	
						" }\n"+
						"}";		
			
		getComponentFromMetaData(queryString);		
	}	
	
	public void getComponentFromMetaData(String queryString) {
		QueryExecution queryExecution=null;
		String scomponent, stype, slabel;		
		try{
			Query query = QueryFactory.create(queryString);		
			queryExecution = QueryExecutionFactory.sparqlService("http://ogd.ifs.tuwien.ac.at/sparql", query);
//			queryExecution = QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", query);
			// execute query
			ResultSet rs = queryExecution.execSelect();			
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();				
				scomponent = sol.get("cp").toString();		
				stype = sol.get("t").toString();
				slabel="";
				if(sol.contains("l"))	slabel = sol.get("l").toString();								
				if(stype.contains("Dimension")){
					d.addDimension(scomponent, slabel);
				}else if(stype.contains("Measure")){
					m.addMeasure(scomponent, slabel);
				}else
					a.addAttribute(scomponent, slabel);
			}			
		}catch (QueryExceptionHTTP e){	
//			System.out.println(e);
		}		
		catch(QueryException e){	
//			System.out.println(e);
		}
		catch(Exception e){		
//			System.out.println(e);
		}		
		finally {
			if(queryExecution!=null)
				queryExecution.close();
		}	
	}

	public void queryValueFromMetaData() {
		int k;
		for(k=0; k<d.getSize(); k++)
    		d.getDimension(k).queryValueFromMetaData(uri);
		for(k=0; k<a.getSize(); k++)
			a.getAttribute(k).queryValueFromMetaData(uri);
		
	}
	
	
	public void identifyReference(){		
		d.identifyReferenceDimension();
		if(m.getSize()==1){
			System.out.println(m.getMeasureUri(0) +"\t\t" + "sdmx-m:obsValue");
		}else if(m.getSize()==0){
			System.out.println("No measure is detected");			
		}else{
			System.out.println("Multiple measures");
			for(int i=0; i<m.getSize(); i++)
				System.out.println(m.getMeasureUri(i)+"\t"+m.getMeasureLabel(i));
		}
		if(a.getSize()==1){
			System.out.println(a.getAttributeUri(0) +"\t\t" + "sdmx-t:unitMeasure");
		}else if(a.getSize()==0){
			System.out.println("No attribute is detected");
		}else{
			System.out.println("Many attributes");
			
		}
	}
}