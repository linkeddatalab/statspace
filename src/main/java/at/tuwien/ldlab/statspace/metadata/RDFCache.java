package at.tuwien.ldlab.statspace.metadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import at.tuwien.ldlab.statspace.util.Support;

public class RDFCache implements Runnable{

	private String folderRDFCache="";	
	private String sEndpoint = Support.sparql;
	private Log log = LogFactory.getLog(RDFCache.class);
	
	public RDFCache (String sFolder){
		folderRDFCache = sFolder;		
	}
	
	public void run() {
		int i;
		String sDSFeature, sRMLQuery, sOutput;	
		ArrayList<MetaData> arrMetaData = new ArrayList<MetaData>();
		arrMetaData = queryMetaDataInfor();
		for(i=0; i<arrMetaData.size(); i++){
			sDSFeature = arrMetaData.get(i).getDataSet().getFeature();
			if(sDSFeature.toLowerCase().equals("api")||sDSFeature.toLowerCase().equals("rml")){
				delay(2);
				
				sRMLQuery = arrMetaData.get(i).getDataSet().getAccessURL();
//				sRMLQuery = sRMLQuery.replace("http://statspace.linkedwidgets.org/rml", "http://localhost:8080/statspace/rml");//			
				log.info("Creating rdf for " + sRMLQuery);
				try{
					sOutput = sRMLQuery;
					sOutput = sOutput.replace("http://statspace.linkedwidgets.org/rml?rmlsource=", "");
//					sOutput = sOutput.replace("http://localhost:8080/statspace/rml?rmlsource=", "");
					sOutput = folderRDFCache + File.separator + Support.extractFolderName(sOutput) + ".rdf";
					URL obj = new URL(sRMLQuery);		
					HttpURLConnection con = (HttpURLConnection) obj.openConnection();		
					con.setRequestMethod("GET"); 
					con.setRequestProperty("User-Agent", "Mozilla/5.0");
			 
					int responseCode = con.getResponseCode();			
					if(responseCode==200){						
						OutputStream outputStream = null;
					    outputStream = new FileOutputStream(new File(sOutput));
						int read = 0;
						byte[] bytes = new byte[1024];
						while ((read = con.getInputStream().read(bytes)) != -1) {
							outputStream.write(bytes, 0, read);
						}
						outputStream.close();
					}
				}catch(Exception e){			
				}			
			}
		}
	}
	
	public ArrayList<MetaData> queryMetaDataInfor() {	
		String sQuery = "PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+							
						"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+								
						"PREFIX dcterms: <http://purl.org/dc/terms/> \n"+
						"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
						"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"+
						"PREFIX dcat: <http://www.w3.org/ns/dcat#> \n"+		
						"PREFIX void: <http://rdfs.org/ns/void#> \n"+								
						"Select Distinct ?md ?ds ?dsa ?dsf\n" +
						"Where{ \n" +
						"	graph <http://statspace.linkedwidgets.org> { \n" +
						"		?md qb:dataSet ?ds. \n"+													
						"		?ds dcat:accessURL ?dsa. \n"+
						"		?ds void:feature ?dsf. \n"+
						"	} \n"+
						"}";
		
        return getMetaDataInfor(sQuery);        	
	}
	
	public ArrayList<MetaData> getMetaDataInfor(String sQuery) throws QueryParseException {		
		String sDSUri, sDSFeature, sDSAccessURL, sMDUri;	
		QueryExecution queryExecution = null;
		ArrayList<MetaData> arrMetaData = new ArrayList<MetaData>();
		try{
			Query query = QueryFactory.create(sQuery);
		    queryExecution = QueryExecutionFactory.sparqlService(sEndpoint, query);	
		   
			// execute query
			ResultSet rs = queryExecution.execSelect();					
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();
				sMDUri 		= sol.get("md").toString().replace("\n", "").replace("\r", "").trim();
				sDSUri 		= sol.get("ds").toString().replace("\n", "").replace("\r", "").trim();					
				sDSFeature 	= sol.get("dsf").toString().replace("\n", "").replace("\r", "").trim();
				sDSAccessURL= sol.get("dsa").toString().replace("\n", "").replace("\r", "").trim();	
				MetaData md = new MetaData();
				md.setUri(sMDUri);
				md.setDataSet(new DataSet(sDSUri,"", "", sDSFeature, sDSAccessURL, "", "" ));	
				arrMetaData.add(md);
			}		
		}catch(Exception e){			
		}
		return arrMetaData;
	}
	
	public void delay(int n){
		try {
		    Thread.sleep(n*100);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}
}

