package tuwien.ldlab.statspace.model.sparql;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;


public class SparqlQuery4OneEndpoint {
	private String sEndpoint;
	private String sQuery;
	private String sResult;
	private Boolean bHTTP;
	private Boolean bError;
	
	public SparqlQuery4OneEndpoint(String sE, String sQ, Boolean bH){
		sEndpoint = sE;
		sQuery = sQ;
		bHTTP = bH;
		sResult="";
		bError=false;
	}
	
	public void setEndpoint(String sE){sEndpoint = sE;}
	public void setQuery(String sQ){sQuery = sQ;}
	public String getResult(){return sResult;}
	public boolean getErrorStatus(){return bError;}
	
	public String query(){
		sResult = "";
		bError = false;
		if(bHTTP==false)
			queryByJena();		
		if(sResult=="")
			queryByHTTPRequest();			
		return sResult;
	}
	
	public void queryByJena() throws QueryParseException {	
		QueryExecution queryExecution = null;	
		try{		
			Query query = QueryFactory.create(sQuery);
		    queryExecution = QueryExecutionFactory.sparqlService(sEndpoint, query);			   
		    ResultSetRewindable results = ResultSetFactory.makeRewindable(queryExecution.execSelect());
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    PrintStream ps = new PrintStream(baos);
		    ResultSetFormatter.outputAsJSON(ps, results);
            sResult = new String(baos.toByteArray(), "UTF-8");
		}
		catch(Exception e){	
			bError=true;
		}		
		finally {
			if(queryExecution!=null)
				queryExecution.close() ;
		}
	}
	
	public void queryByHTTPRequest(){			 
		try{	
			String request = sEndpoint + "?query=" + URLEncoder.encode(sQuery, "UTF-8") + "&format=application/json";
			
			URL obj = new URL(request);		
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	 
			// optional default is GET
			con.setRequestMethod("GET");
	 
			//add request header
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setRequestProperty("Content-Type", "application/json");
			
			int responseCode = con.getResponseCode();	
			if(responseCode==200){			
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	            String inputLine; 
	            while ((inputLine = in.readLine()) != null) {
	                sResult = sResult + inputLine;	            	
	            }
	            in.close();
			}else
				bError=true;
				
		}catch(Exception e){
			bError=true;
		}
	}	
}
