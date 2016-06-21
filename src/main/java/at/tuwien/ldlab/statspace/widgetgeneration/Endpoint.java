package at.tuwien.ldlab.statspace.widgetgeneration;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

import at.tuwien.ldlab.statspace.util.DataSetBlackList;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;



public class Endpoint {
	
	private ArrayList<DataSet> dataset;
	private	String endpointForQuery ="";
	private String endpointForWidget="";
	private int errorCode = 0;	//no dataset is detected
	private boolean bHTTP   = false;
	private boolean bRemove = false;
	private String  sUseDistinct = "";
	private boolean bFindOther = true;
	private static Log log = LogFactory.getLog(Endpoint.class);
	
	public Endpoint(String sEQuery, String sEWidget, boolean bHTTPRequest, boolean bRemoveDuplicate, String sDistinct, boolean bFind){
		dataset   = new ArrayList<DataSet>();
		endpointForQuery  = sEQuery;
		endpointForWidget = sEWidget;	
		errorCode = 0;		
		bHTTP = bHTTPRequest;
		bRemove = bRemoveDuplicate;		
		bFindOther = bFind;
		sUseDistinct = sDistinct;	
	}	

	public Endpoint() {		
	}

	public void queryDataSet(){
		String queryString;
//		log.info("Query datasets...");
		
		//Way 1. ?ds rdf:type ?qb:DataSet	
//		log.info("\tWay 1: ");
		
		queryString =	
				"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
				"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
				"SELECT DISTINCT ?ds ?l \n"+
				"WHERE{ \n"+
					"?ds rdf:type qb:DataSet. \n"+
					"FILTER EXISTS{?o qb:dataSet ?ds} \n"+
					"optional{?ds rdfs:label ?l.} \n"+						
				"}";
		/*
		 * Special case: endpoint of data.cso provides wrong labels if we use Order by
		 */
		if(!endpointForQuery.contains("http://data.cso.ie") && !endpointForQuery.contains("http://data.europa.eu"))
			queryString = queryString + "ORDER BY ?ds";
			
		if(!bHTTP)
			getDataSet(endpointForQuery, queryString);
		else
			getDataSet2(endpointForQuery, queryString);		
		
		
		//Way 2. ?ds rdf:type ?qb:DataSet
		//Remove FILTER EXITST
		
		if(dataset.size()==0) {
			delay(2);
//			log.info("\tWay 2: ");
			errorCode = 0;		
			queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
							"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
							"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
							"SELECT ?ds ?l\n"+
							"WHERE{ \n"+
								"?ds rdf:type qb:DataSet. \n"+
								"optional{?ds rdfs:label ?l.} \n"+
							"}";
			if(!bHTTP)
				getDataSet(endpointForQuery, queryString);
			else
				getDataSet2(endpointForQuery, queryString);			
						 
//			log.info(dataset.size());
		}
			
		//Way 3. ?o qb:dataSet ?ds
		
		if(dataset.size() < 3) {
			delay(2);
//			log.info("\tWay 3: ");
			errorCode = 0;		
			queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
							"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
							"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
							"SELECT DISTINCT ?ds ?l\n"+
							"WHERE{ \n"+
								"?o qb:dataSet ?ds \n"+
								"optional{?ds rdfs:label ?l.} \n"+
							"}";			
			if(!bHTTP)
				getDataSet(endpointForQuery, queryString);
			else
				getDataSet2(endpointForQuery, queryString);	
			
			log.info(dataset.size());
		}	
		
		if(dataset.size()==0 && bHTTP==false){
			delay(2);
			log.info("Change to HTTP Request");
			bHTTP = true;
			queryDataSet();
		}
	}	
	
	
	
	/* Get values list of each Dimension in each DataSet
	 * 
	 */	
	
	public void getDataSet(String sEndpoint, String sQuery) throws QueryParseException {		
		String uri, label;
		int i;
		DataSet ds;
		QueryExecution queryExecution = null;
		DataSetBlackList blackList = new DataSetBlackList();
		try{		
			Query query = QueryFactory.create(sQuery);
		    queryExecution = QueryExecutionFactory.sparqlService(sEndpoint, query);	
		   
			// execute query
			ResultSet rs = queryExecution.execSelect();			
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();
				uri = sol.get("ds").toString().replace("\n", "").replace("\r", "").trim();
				if(sol.contains("l"))	
					label = sol.get("l").toString().replace("\n", "").replace("\r", "").trim();
				else
					label = "";
				
				//ignore some bad datasets	
				if(blackList.inBlackList(uri))
					continue;
				
				ds = new DataSet(uri, label);				
				if(dataset.isEmpty()) dataset.add(ds);
				else{					
					for(i=0; i<dataset.size(); i++)
						if(dataset.get(i).getUri().equals(uri))
							break;
					if(i==dataset.size())
						dataset.add(ds);
				}			
			}		
		}catch (QueryExceptionHTTP e){	
			 errorCode = 1;			
		}		
		catch(QueryException e){
			errorCode = 2;	
			log.info(e.toString());
		}
		catch(Exception e){	
			errorCode = 3;
		}		
		finally {
			if(queryExecution!=null)
				queryExecution.close() ;
		}
	}
	
	public void getDataSet2(String sEndpoint, String sQuery){			 
		try{
			int i, j;
			DataSetBlackList blackList = new DataSetBlackList();
			String name, uri, value, label;
			String request = sEndpoint + "?query=" + URLEncoder.encode(sQuery, "UTF-8");
			if(request.indexOf("xml")==-1)
				request = request + "&format=application%2Fsparql-results%2Bxml";
			
			URL obj = new URL(request);		
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
				NodeList nList = doc.getElementsByTagName("result");			
				for (i = 0; i < nList.getLength(); i++) {					
					Node nNode = nList.item(i);						
					NodeList nNodeList = nNode.getChildNodes();	
					uri=""; label="";
					for(j=0; j<nNodeList.getLength(); j++){
						if(nNodeList.item(j).getNodeType() == Node.ELEMENT_NODE){
							Element eElement = (Element) nNodeList.item(j);
							name = eElement.getAttribute("name");
							value = eElement.getTextContent();
							value = value.replace("\n", "").replace("\r", "").trim();							
							if(name.equals("ds")) 		uri = value;
							else if(name.equals("l"))	label = value;							
						}					
					}	
					if(blackList.inBlackList(uri))
						continue;
					DataSet ds = new DataSet(uri, label);
					if(dataset.isEmpty()) dataset.add(ds);
					else{
						for(j=0; j<dataset.size(); j++)
							if(dataset.get(j).getUri().equals(uri))
								break;
						if(j==dataset.size())
							dataset.add(ds);
					}									
				}			
			}			
		}catch(Exception e){	
			errorCode = 3;
		}
	}
	
	public ArrayList<DataSet> getDataSet(){return dataset;}	
	public DataSet getDataSet(int i){return dataset.get(i);	}	
	public String getEndpointForQuery(){return endpointForQuery;}
	public String getEndpointForWidget(){return endpointForWidget;}
	public String getUseDistict(){return sUseDistinct;}
	public boolean getHTTP(){return bHTTP;}
	public boolean getRemove(){return bRemove;}
	public boolean getFindOther(){return bFindOther;}
	public int getErrorCode(){return errorCode;}
	public void addDataSet(DataSet ds){dataset.add(ds);}
	
	public void removeAll(){
		for(int i=0; i<dataset.size(); i++)
			dataset.get(i).remove();
		endpointForQuery="";
		errorCode = 0;		
	}

	public void delay(int n){
		try {
		    Thread.sleep(n*100);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}
		
}
