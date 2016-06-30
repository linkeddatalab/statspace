package at.tuwien.ldlab.statspace.widgetgeneration;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

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
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP;

import at.tuwien.ldlab.statspace.util.Support;

public class Attribute{
	private ArrayList<AttributeValue> value;
	private String uri;
	private String label;
	private String labelType;

	
	public Attribute(){		
		value = new ArrayList<AttributeValue>();		
		this.uri = "";
		this.label = "";
		this.labelType="";	
	}
	
	public Attribute(String sUri, String sLabel){		
		value = new ArrayList<AttributeValue>();		
		if(sLabel=="")
			sLabel=Support.getName(sUri);
		if(sLabel.indexOf("@")!=-1 && sLabel.indexOf('@')==sLabel.length()-3)
			sLabel = sLabel.substring(0, sLabel.length()-3);		
		sLabel = sLabel.substring(0, 1).toUpperCase() + sLabel.substring(1);
		this.uri = sUri;
		this.label = sLabel;	
		this.labelType="";
	}
	
	public void setUri(String sUri){this.uri = sUri;}
	public void setLabel(String sLabel){
		if(sLabel.indexOf("@")!=-1 && sLabel.indexOf('@')==sLabel.length()-3)
			sLabel = sLabel.substring(0, sLabel.length()-3);
		sLabel = sLabel.substring(0, 1).toUpperCase() + sLabel.substring(1);
		this.label = sLabel;
	}
	public void setLabelType(String s){labelType=s;}
	
	public String getUri(){return uri;}
	public String getLabel(){return label;}		
	public String getLabelType(){return labelType;}
	
	public void addValue(String sUri, String sLabel){
		int i;
		for(i=0; i<value.size(); i++)
			if(value.get(i).getUri().equals(sUri))
				break;
		if(i==value.size()){
			AttributeValue v = new AttributeValue(sUri, sLabel);
			value.add(v);
		}else{
			if(sLabel.contains("@en"))
				value.get(i).setLabel(sLabel);
		}
	}
	
	public boolean haveValue(String sUri){
		int i, n = value.size();
		for(i=0; i<n; i++)
			if(value.get(i).getUri().equals(sUri))
				return true;
		return false;
	}
	
	public String getValueUri(int i){
		if(!value.isEmpty() && 0<=i && i<value.size())
			return value.get(i).getUri();
		return "";		
	}	
	
	public String getValueLabel(int i){
		if(!value.isEmpty() && 0<=i && i<value.size())
			return value.get(i).getLabel();
		return "";		
	}	
	
	public AttributeValue getValueDimension(int i){
		if(!value.isEmpty() && 0<=i && i<value.size())
			return value.get(i);
		return new AttributeValue();		
	}	
		
	public int getValueSize(){
		return value.size();
	}		
		
	public void clearAll(){
		value.clear();
	}
	
	
	public void queryValue(String addQuery, boolean bHTTP, boolean bUseDistinct, String dsUri){	
		
		String queryString;		
		System.out.print("\t"+ uri +": ");

		queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+							
						"SELECT DISTINCT ?v ?r ?l \n"+
						"WHERE{ \n"+
							"?o qb:dataSet <"+ dsUri +">. \n"+								
							"?o <"+ uri + "> ?v. \n"+
							"optional{ \n"+
								"?v ?r ?l. \n"+
								"FILTER (REGEX(str(?r), \"label\", \"i\")) \n"+
							"} \n" +			
						"}";
		
		if(!bHTTP)
			getValue(addQuery, queryString);	
		else
			getValue2(addQuery, queryString);	
		
		if(value.size()==0){			
			queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
							"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
							"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
							"SELECT ?v ?r ?l \n"+
							"WHERE{ \n"+
								"?o qb:dataSet <"+ dsUri +">. \n"+	
								"?o <"+ uri + "> ?v. \n"+
								"optional{ \n"+
									"?v ?r ?l. \n"+
									"FILTER (REGEX(str(?r), \"label\", \"i\")) \n"+
								"} \n" +			
							"}LIMIT 1000";	
			
			if(!bHTTP)
				getValue(addQuery, queryString);	
			else
				getValue2(addQuery, queryString);		
		}
		orderValue();
		System.out.println(value.size());
		
	}
	
	public boolean getValue(String addQuery, String queryString) throws QueryParseException {		
		String sUri, sLabel;	
		QueryExecution queryExecution = null;		
		boolean bEnd = true;
		try{		
			
			Query query = QueryFactory.create(queryString);			
			queryExecution =  QueryExecutionFactory.sparqlService(addQuery, query);			
			// execute query
			ResultSet rs = queryExecution.execSelect();
			
			while (rs!=null && rs.hasNext()) {	
				bEnd = false;
				
				QuerySolution sol = rs.nextSolution();
				sUri = sol.get("v").toString();				
				if(sol.contains("l")){
					sLabel = sol.get("l").toString().replace("\n", "").replace("\r", "").trim();
					addValue(sUri, sLabel);
				}
				else{				
					addValue(sUri, "");
				}
				if(sol.contains("r") && (labelType.equals("") || labelType.contains("altLabel"))){
					labelType = sol.get("r").toString().replace("\n", "").replace("\r", "").trim();												
				}	
			}			
		}catch (QueryExceptionHTTP e){	
		}catch(QueryException e){
		}catch(Exception e){
		}
		finally {
			if(queryExecution!=null)
				queryExecution.close() ;
		}		
		return bEnd;
	}
	
	public boolean getValue2(String addQuery, String queryString) throws QueryParseException {		
		boolean bEnd = true;
		String sUri, sLabel, sName, sText, sDataType;	
		int i,j;
		try{
			String url = addQuery + "?query=" + URLEncoder.encode(queryString, "UTF-8");
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
						sUri=""; sLabel="";sDataType="";
						for(j=0; j<nNodeList.getLength(); j++){
							if(nNodeList.item(j).getNodeType() == Node.ELEMENT_NODE){
								Element eElement = (Element) nNodeList.item(j);
								sName = eElement.getAttribute("name");
								sText = eElement.getTextContent();
								sText = sText.replace("\n", "").replace("\r", "").trim();
								sText = sText.trim();
								NodeList nListType = eElement.getElementsByTagName("literal");
								if(nListType.getLength()==1){							
									Node nNodeType = nListType.item(0);
									if(nNodeType.getNodeType() == Node.ELEMENT_NODE)
										sDataType = ((Element) nNodeType).getAttribute("datatype");								
								}							
								if(!sDataType.equals(""))
									sText = sText + "^^" + sDataType;
								
								if(sName.equals("v")) 		sUri = sText;
								else if(sName.equals("l"))	sLabel = sText;	
								else if(sName.equals("r") && (labelType.equals("") || labelType.contains("altLabel"))) 
									labelType=sText;
							}					
						}
						addValue(sUri, sLabel);								
					}						
				}catch (SAXException e) {
				    e.printStackTrace();
				} catch (IOException e) {
				    e.printStackTrace();
				}
			}	
		}catch(Exception e){			
		}		
		return bEnd;
	
	}
	
	public void delay(int n){
		try {
		    Thread.sleep(n*100);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}
	
	public boolean haveValueLabel(){		
		int i, n = value.size(), count=0;		
		for(i=0; i<n; i++)
			if(value.get(i).getLabel().equals(""))
				count++;
		if(count*100/5 <= n){
			for(i=0; i<n; i++)
				if(value.get(i).getLabel().equals("")){
					value.get(i).setLabel(value.get(i).getUri());
				}
			return true;
		}			
		return false;
	}

	public void queryValueFromMetaData(String uriDs) {
		// TODO Auto-generated method stub
		String queryString;		
		queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+						
						"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
						"SELECT DISTINCT ?v ?l\n"+						
						"WHERE{ \n"+
						" graph <http://statspace.linkedwidgets.org> { \n" +		
							"<"+uri+"> rdf:value ?v. \n" +
						    "<"+uriDs+"> rdf:value ?v. \n"+
							"optional{" +
								"?v rdfs:label ?l. \n"+									
							"}\n"+	
						  "}\n"+		
						"}";	
		getValueFromMetaData(queryString);	
		orderValue();
	}

	public void getValueFromMetaData(String queryString) {
		// TODO Auto-generated method stub
		String sUri, sLabel;		
		QueryExecution queryExecution = null;		
		try{		
			
			Query query = QueryFactory.create(queryString);			
			queryExecution =  QueryExecutionFactory.sparqlService(Support.sparql, query);
			// execute query
			ResultSet rs = queryExecution.execSelect();
			
			while (rs!=null && rs.hasNext()) {
				QuerySolution sol = rs.nextSolution();
				sUri = sol.get("v").toString();				
				if(sol.contains("l")){
					sLabel = sol.get("l").toString();
					addValue(sUri, sLabel);
				}
				else{				
					addValue(sUri, "");
				}
				labelType = "http://www.w3.org/2000/01/rdf-schema#label";				
			}			
			
		}catch (QueryExceptionHTTP e){	
//			System.out.println(queryString + e);
		}catch(QueryException e){		
//			System.out.println(queryString + e);
		}catch(Exception e){		
//			System.out.println(queryString + e);
		}
		finally {
			if(queryExecution!=null)
				queryExecution.close() ;
		}	
		
	}
	
	public void orderValue(){
		 quicksort(0, value.size()-1);
	}
	
	public void quicksort(int left, int right) {		 	
	        if (right <= left) return;
	        int i = partition(left, right);
	        quicksort(left, i-1);
	        quicksort(i+1, right);
	    }

   // partition a[left] to a[right], assumes left < right
   private  int partition(int left, int right) {
       int i = left - 1;
       int j = right;
       while (true) {
           while (Support.compare(value.get(++i).getUri(), value.get(right).getUri()) <0);      // find item on left to swap a[right] acts as sentinel
           while (Support.compare(value.get(right).getUri(), value.get(--j).getUri()) <0)
           	if (j == left) break;           // don't go out-of-bounds
           if (i >= j) break;               // check if pointers cross
           exch(i, j);                      // swap two elements into place
       }
       exch(i, right);                      // swap with partition element
       return i;
   }
   
   private void exch(int i, int j) {
   		AttributeValue temp = new AttributeValue(value.get(i).getUri(), value.get(i).getLabel());
		value.set(i, value.get(j));
		value.set(j, temp);
   }
	
}
	