package at.tuwien.ldlab.statspace.widgetgeneration;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

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

public class Dimension{
	private ArrayList<DimensionValue> value;
	private String uri;
	private String label;
	private String labelType;   //rdfs:label or skos:prefLabel...
	private String refDimension; //co-reference dimension in the vocabulary
		
	public Dimension(){		
		value = new ArrayList<DimensionValue>();		
		this.uri = "";
		this.label = "";
		this.labelType="";
		this.refDimension = "";
	}
	
	public Dimension(String uri, String label){		
		value = new ArrayList<DimensionValue>();		
		if(label=="")
			label=Support.getName(uri);
		if(label.indexOf("@")!=-1 && label.indexOf('@')==label.length()-3)
			label = label.substring(0, label.length()-3);		
		label = label.substring(0, 1).toUpperCase() + label.substring(1);
		this.uri = uri;
		this.label = label;	
		this.labelType="";
		this.refDimension="";	
	}
	
	public void setUri(String uri){		
		this.uri = uri;
	}
	public void setLabel(String label){
		if(label.indexOf("@")!=-1 && label.indexOf('@')==label.length()-3)
			label = label.substring(0, label.length()-3);
		label = label.substring(0, 1).toUpperCase() + label.substring(1);
		this.label = label;
	}
	public void setRefDimension(String sRef){this.refDimension=sRef;}	
	public void setLabelType(String s){if(labelType==""||labelType.contains("altLabel")) labelType=s;}
	
	public String getLabelType(){return labelType;}
	public String getRefDimension(){return refDimension;}
	public String getUri(){return uri;}
	public String getLabel(){return label;}	
		
	public void addValue(String suri, String slabel){
		int i;
		for(i=0; i<value.size(); i++)
			if(value.get(i).getUri().equals(suri))
				break;
		if(i==value.size()){
			DimensionValue v = new DimensionValue(suri, slabel);
			value.add(v);
		}else{
			if(slabel.contains("@en"))
				value.get(i).setLabel(slabel);
		}
	}
	
	public String getValueList(String sname){
		int i;
		String sFilter="";
		for(i=0; i<value.size(); i++){	
			if(value.get(i).getUri().indexOf(" ")==-1)
				sFilter = sFilter + "  " + Support.getFilter(sname, value.get(i).getUri());
		}		
		return sFilter;
	}
	
	public boolean haveValue(String code){
		int i, n = value.size();
		for(i=0; i<n; i++)
			if(value.get(i).getUri().equals(code))
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
	
	public DimensionValue getValueDimension(int i){
		if(!value.isEmpty() && 0<=i && i<value.size())
			return value.get(i);
		return new DimensionValue();		
	}	
		
	public int getValueSize(){
		return value.size();
	}		
		
	public void clearAll(){
		value.clear();
	}
	
	public void queryValue(String addQuery, boolean bHTTP, boolean bUseDistinct,
			boolean bFindOther, String dsUri){
		
		String queryString;
		boolean bEnd;
			
		/* Way 1 - Observation - value
		 * 
		 */		
		
		System.out.print("\t"+ uri +": ");

		if(bUseDistinct && !uri.endsWith("#freq")){		
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
					
		}
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
			
			if(bFindOther){					
				bEnd=false;
				long lStartTime = new Date().getTime();
				long lEndTime, difference = 0;				
				while(bEnd==false && difference < 10000){				
					lEndTime = new Date().getTime();						 
					difference = lEndTime - lStartTime;	
					delay(2);					
					queryString =	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+									
									"SELECT ?v ?r ?l \n"+
									"WHERE{ \n"+
										"?o qb:dataSet <"+ dsUri +">.  \n"+							
										"?o <"+ uri + "> ?v. \n"+
										"optional{ \n"+
											"?v ?r ?l. \n"+
											"FILTER (REGEX(str(?r), \"label\", \"i\")) \n"+
										"} \n" +			
										getValueList("?v") +							
									"}LIMIT 1 ";
																
					if(queryString.length()<2000){
						if(!bHTTP)
							bEnd = getValue(addQuery, queryString);
						else
							bEnd = getValue2(addQuery, queryString);
					}else
						bEnd = true;
				}
			}	
		}
		orderValue();
		System.out.println(value.size());
		
	}
	
	public boolean getValue(String address, String queryString) throws QueryParseException {		
		String code, label;	
		QueryExecution queryExecution = null;		
		boolean bEnd = true;
		try{		
			
			Query query = QueryFactory.create(queryString);			
			queryExecution =  QueryExecutionFactory.sparqlService(address, query);			
			// execute query
			ResultSet rs = queryExecution.execSelect();
			
			while (rs!=null && rs.hasNext()) {	
				bEnd = false;
				
				QuerySolution sol = rs.nextSolution();
				code = sol.get("v").toString();				
				if(sol.contains("l")){
					label = sol.get("l").toString().replace("\n", "").replace("\r", "").trim();
					addValue(code, label);
				}
				else{				
					addValue(code, "");
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
	
	public boolean getValue2(String address, String queryString) throws QueryParseException {		
		boolean bEnd = true;
		String code, label, sname, stext, sdatatype;	
		int i,j;
		try{
			String url = address + "?query=" + URLEncoder.encode(queryString, "UTF-8");
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
						code=""; label="";sdatatype="";
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
								if(sname.equals("v")){
									if(!sdatatype.equals(""))
										code = stext + "^^" + sdatatype;
									else
										code = stext;
								}
								else if(sname.equals("l"))	label = stext;	
								else if(sname.equals("r") && (labelType.equals("") || labelType.contains("altLabel"))) 
									labelType=stext;
							}					
						}
						addValue(code, label);								
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
	
	public String getAreaLabel(int index, String sCountryName){
		String label;	        		
		label = value.get(index).getLabel();
		int i;
		if(label!="" && !Support.isNumber(label) && !Support.ignoreAreaLabel(label)){
			if(label.contains(",")){
				i = label.indexOf(",");
				if(i!=0)
					label = label.substring(0, label.indexOf(","));
				else
					label = label.substring(1);
			}
			if(label.contains("(")){
				i = label.indexOf("(");
				if(i!=0)
					label = label.substring(0, label.indexOf("("));
				else
					label = label.substring(1);
			}
			if(label.contains("^^")){
				i = label.indexOf("^^");
				if(i!=0)
					label = label.substring(0, label.indexOf("^^"));
				else
					label = label.substring(1);
			}
			label = label.trim();
			if(sCountryName!="" &&
				(!value.get(index).getLabel().toLowerCase().contains("country") &&
						!value.get(index).getUri().toLowerCase().contains("country")))		        					
				label = label + " " + sCountryName;
			return label;     		
    	}
		return "";
	}
	
	public void delay(int n){
		try {
		    Thread.sleep(n*100);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}
	
	public boolean isYearDimension(){
		if(value.size()==0)		
			return false;		
			
		if(value.get(0).isValueYear())
			return true;
		return false;
	}
	
	public boolean isDateDimension(){
		if(value.size()==0)		
			return false;		
			
		if(value.get(0).isValueDate())
			return true;
		return false;
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
						" }\n"+
						"}";	
		getValueFromMetaData(queryString);	
		orderValue();
	}

	public void getValueFromMetaData(String queryString) {
		// TODO Auto-generated method stub
		String uri, label;		
		QueryExecution queryExecution = null;		
		try{		
			
			Query query = QueryFactory.create(queryString);			
			queryExecution =  QueryExecutionFactory.sparqlService("http://ogd.ifs.tuwien.ac.at/sparql", query);			
//			queryExecution =  QueryExecutionFactory.sparqlService("http://localhost:8890/sparql", query);
			// execute query
			ResultSet rs = queryExecution.execSelect();
			
			while (rs!=null && rs.hasNext()) {
				QuerySolution sol = rs.nextSolution();
				uri = sol.get("v").toString();				
				if(sol.contains("l")){
					label = sol.get("l").toString();
					addValue(uri, label);
				}
				else{				
					addValue(uri, "");
				}		
				labelType = "http://www.w3.org/2000/01/rdf-schema#label";												
					
			}			
			
		}catch (QueryExceptionHTTP e){	
		}catch(QueryException e){		
		}catch(Exception e){		
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
   		DimensionValue temp = new DimensionValue(value.get(i).getUri(), value.get(i).getLabel());
		value.set(i, value.get(j));
		value.set(j, temp);
   }	
}
	