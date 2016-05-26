package tuwien.ldlab.statspace.model.mediator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import tuwien.ldlab.statspace.codelist.CL_Area;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;


public class MetaData {
	private String sUri;
	private DataSet ds;
	private String sPublisher;
	private String sKeyword;
	private ArrayList<Component> arrComp;	
//	private String sEndpoint="http://localhost:8890/sparql";	
	private String sEndpoint="http://ogd.ifs.tuwien.ac.at/sparql";
	
	
	public MetaData(){
		sUri = "";
		sPublisher = "";
		sKeyword = "";
		ds = new DataSet();
		arrComp = new ArrayList<Component>();
	}
	
	public MetaData(String sMDUri, String sMDPublisher, String sDSUri, String sDSSubject, String sDSLabel){
		sUri = sMDUri;
		sPublisher = sMDPublisher;		
		ds = new DataSet();
		ds.setUri(sDSUri);
		ds.setSubject(sDSSubject);
		ds.setLabel(sDSLabel);		
		arrComp = new ArrayList<Component>();
	}
	
	public MetaData(MetaData md){
		sUri = "";
		sPublisher = "";
		sKeyword = "";
		ds = new DataSet(md.getDataSet());
		arrComp = new ArrayList<Component>();
		int i;
		for(i=0; i<md.getNumberofComponent(); i++){
			arrComp.add(new Component(md.getComponent(i)));			
		}				
	}
	
	public void setKeyword(String s){sKeyword=s;}
	public void setUri(String s){sUri=s;}
	public void setPublisher(String s){sPublisher=s;}
	public void addComponent(Component p){
		int i;
		for(i=0; i<arrComp.size(); i++)
			if(arrComp.get(i).getUri()!="" && arrComp.get(i).getUri().equalsIgnoreCase(p.getUri()))
				break;
		if(i==arrComp.size())
			arrComp.add(p);
		else {
			//update type, hidden property status
			if(p.getType().endsWith("HiddenProperty"))
				arrComp.get(i).setHiddenStatus(true);			
			else if(!p.getType().endsWith("#Property")){
				arrComp.get(i).setType(p.getType());
				arrComp.get(i).setLabel(p.getLabel());
			} 
		}
	}
	public void setDataSet(DataSet d){
		ds.setAccessURL(d.getAccessURL());
		ds.setLabel(d.getLabel());
		ds.setFeature(d.getFeature());	
		ds.setSubject(d.getSubject());
		ds.setUri(d.getUri());	
		ds.setVariable(d.getVariable());
		ds.setVariableLabel(d.getVariableLabel());
	}
	
	public String getUri(){return sUri;}
	public String getPublisher(){return sPublisher;}
	public String getPublisherForDisplay(){
//		if(sPublisher.length()<=20)
			return sPublisher;
//		else
//			return sPublisher;//.substring(0,  20)+"...";
	}
	public DataSet getDataSet(){return ds;}
	public int getNumberofComponent(){return arrComp.size();}
	public Component getComponent(int i){return arrComp.get(i);}
	
	public ArrayList<MetaData> queryMetaDataByFilter() {		
		int i, j, k, n, t;
		String sFilterValue, sVar;
		
		String sQuery=			"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+							
								"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
								"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+								
								"PREFIX dcterms: <http://purl.org/dc/terms/> \n"+
								"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
								"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"+
								"PREFIX dcat: <http://www.w3.org/ns/dcat#> \n"+		
								"PREFIX void: <http://rdfs.org/ns/void#> \n"+								
								"Select Distinct ?md \n" +
								"Where{ \n" +
								"	graph <http://statspace.linkedwidgets.org> { \n" +
								"		?md qb:dataSet ?ds. \n"+
								"		?ds dcterms:subject ?dss. \n";						
		//dataset
		if(ds.getSubject()!="")
			sQuery = sQuery +  "		FILTER(?dss=<"+ds.getSubject() + ">). \n";
		
		if(ds.getUri()!="")
			sQuery =           "   		FILTER(?ds=<"+ds.getUri() + ">). \n";
		
		n=0;
		for(i=0; i<arrComp.size(); i++)
			if(arrComp.get(i).getFilterValue()!=""){
				sQuery = sQuery + "		{ \n" +
								  "  		?ds rdf:value ?v_"+ n + ". \n"+					
								  "  		<"+arrComp.get(i).getFilterValue()+"> owl:sameAs ?v_"+ n +". \n"+
								  "		}\n "    +
								  "		UNION \n"+
								  "		{ \n" +
								  "  		?ds rdf:value ?v_"+ n + ". \n"+
								  "  		FILTER(?v_"+n+"= <"+arrComp.get(i).getFilterValue()+">) \n"+
								  "		}\n";
				n++;
			}		 
		sQuery = sQuery + 		 "	}\n"+
								 "}";
		
//      log.info(sQuery);	
        ArrayList<MetaData> arrMetaData = new ArrayList<MetaData>();
        ArrayList<String> arrUri = new ArrayList<String>();        
        arrUri = getMetaDataUri(sQuery);        
        
        if(arrUri.size()>0){       	
        	for(i=0; i<arrUri.size(); i++){
        		//Query information of data set and components        		
        		MetaData md = new MetaData(); 
        		md.setUri(arrUri.get(i));
        		md.queryMetaDataInfor();
        		
        		//set variable for data set
        		md.getDataSet().setVariable(ds.getVariable());
        		md.getDataSet().setVariableLabel(ds.getVariableLabel());
        		
        		//Set varialbe based on filter value  
        		for(j=0; j<arrComp.size(); j++){
    				if(arrComp.get(j).getFilterValue()!=""){
    					for(k=0; k<md.getNumberofComponent(); k++){				
    						if(arrComp.get(j).getUri().equalsIgnoreCase(md.getComponent(k).getUriReference())){
								sFilterValue = queryFilterValue(md.getDataSet().getUri(), md.getComponent(k).getUri(),arrComp.get(j).getFilterValue());
								if(sFilterValue!=null && !sFilterValue.isEmpty()){
									md.getComponent(k).setFilterValue(sFilterValue);
									md.getComponent(k).setVariable(arrComp.get(j).getVariable());
									md.getComponent(k).setVariableLabel(arrComp.get(j).getVariableLabel());
								}
		    				}
						}				
    				}
				}
				/* Set variable based on co-reference
				 * However, two dimensions can have same co-reference e.g., region, refArea 
				 * => check if this varible is still free
				 */
				
				for(j=0; j<arrComp.size(); j++){
					if(arrComp.get(j).getVariable()!=""){
						for(k=0; k<md.getNumberofComponent(); k++){
							if(md.getComponent(k).getVariable().equals(arrComp.get(j).getVariable())){
								break;
							}							
						}
						if(k==md.getNumberofComponent()){
							for(k=0; k<md.getNumberofComponent(); k++)
								if(arrComp.get(j).getUri().equalsIgnoreCase(md.getComponent(k).getUriReference())){
									md.getComponent(k).setVariable(arrComp.get(j).getVariable());
									md.getComponent(k).setVariableLabel(arrComp.get(j).getVariableLabel());
								}							
						}					
					}
				} 			
				
				
				for(k=0; k<md.getNumberofComponent(); k++){		
					if(md.getComponent(k).getVariable()==""){
						sVar = md.getComponent(k).getUri();
						t = sVar.length()-1;
						while(t>0 && sVar.charAt(t)!='/' && sVar.charAt(t)!='#') t--;
						sVar = sVar.substring(t+1);
						md.getComponent(k).setVariable("?"+sVar);
					}
        		}        	
				arrMetaData.add(md);
        	}
        }
        return arrMetaData;           	
	}
	
	public ArrayList<String> getMetaDataUri(String sQuery) throws QueryParseException {		
		String sMDUri;
		ArrayList<String> arrUri = new ArrayList<String>();					
		QueryExecution queryExecution = null;
		
		try{					
			Query query = QueryFactory.create(sQuery);
		    queryExecution = QueryExecutionFactory.sparqlService(sEndpoint, query);	
		   
			// execute query
			ResultSet rs = queryExecution.execSelect();			
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();
				sMDUri 		= sol.get("md").toString().replace("\n", "").replace("\r", "").trim();		
//				sDSUri 		= sol.get("ds").toString().replace("\n", "").replace("\r", "").trim();	
				
				arrUri.add(sMDUri);	
				
			}		
		}catch(Exception e){			
		}		
		return arrUri;		
	}
	
	public void queryMetaDataInfor() {				
		String sQuery=			"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+							
								"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
								"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+								
								"PREFIX dcterms: <http://purl.org/dc/terms/> \n"+
								"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
								"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"+
								"PREFIX dcat: <http://www.w3.org/ns/dcat#> \n"+		
								"PREFIX void: <http://rdfs.org/ns/void#> \n"+					
								"Select Distinct * \n" +
								"Where{ \n" +
								"	graph <http://statspace.linkedwidgets.org> { \n" +
								"		<"+ sUri +">  qb:dataSet ?ds. \n"+
								"		?ds dcterms:subject ?dss. \n"+
								"		optional{?ds rdfs:label ?dsl.} \n"+
								"		?ds void:feature ?dsf. \n"+
								"		?ds dcat:accessURL ?dsa. \n"+
								"		<"+ sUri +">  qb:component ?cp.\n" +							   
								"		?cp rdf:type ?cpt. \n" +		
								"		optional{?cp rdfs:label ?cpl.} \n" +	
								"		optional{?cpr owl:sameAs ?cp.} \n";					  
		if(ds.getSubject()!="")
			sQuery = sQuery +  "		FILTER(?dss=<"+ds.getSubject() + ">). \n";
		
		if(ds.getUri()!="")
			sQuery = sQuery +  "   		FILTER(?ds=<"+ds.getUri() + ">). \n";
		
 	   	sQuery = sQuery +      "	} \n"+
 	   							"} ";
		
		getMetaDataInfor(sQuery);		
	}
	
	public void getMetaDataInfor(String sQuery) throws QueryParseException {		
		String sDSUri, sDSLabel, sDSSubject, sDSFeature, sDSAccessURL, sCUri, sCRef, sCType, sCLabel;	
		int i;
		QueryExecution queryExecution = null;
		
		try{					
			Query query = QueryFactory.create(sQuery);
		    queryExecution = QueryExecutionFactory.sparqlService(sEndpoint, query);	
		   
			// execute query
			ResultSet rs = queryExecution.execSelect();		
			i=0;
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();
				i++;
				sDSUri 		= sol.get("ds").toString().replace("\n", "").replace("\r", "").trim();				
				sDSSubject 	= sol.get("dss").toString().replace("\n", "").replace("\r", "").trim();
				if(sol.contains("dsl"))	sDSLabel= sol.get("dsl").toString().replace("\n", "").replace("\r", "").trim();
				else					sDSLabel 	= "";	
				sDSFeature 	= sol.get("dsf").toString().replace("\n", "").replace("\r", "").trim();
				sDSAccessURL= sol.get("dsa").toString().replace("\n", "").replace("\r", "").trim();	
								
				sCUri 		= sol.get("cp").toString().replace("\n", "").replace("\r", "").trim();
				sCType 		= sol.get("cpt").toString().replace("\n", "").replace("\r", "").trim();
				if(sol.contains("cpr"))	sCRef 	= sol.get("cpr").toString().replace("\n", "").replace("\r", "").trim();
				else					sCRef 	= sCUri;				
				if(sol.contains("cpl"))	sCLabel = sol.get("cpl").toString().replace("\n", "").replace("\r", "").trim();
				else					sCLabel = "";					
				
				if(i==1)
					setDataSet(new DataSet(sDSUri, sDSLabel, sDSSubject, sDSFeature, sDSAccessURL, ds.getVariable(), ds.getVariableLabel()));
			
				addComponent(new Component(sCUri, sCType, sCLabel, sCRef));				
			}		
		}catch(Exception e){			
		}
	}
	
	public MetaData queryMetaDataInfor1(String sMDUri) {				
		String sQuery=			"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+							
								"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
								"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+								
								"PREFIX dcterms: <http://purl.org/dc/terms/> \n"+
								"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
								"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"+
								"PREFIX dcat: <http://www.w3.org/ns/dcat#> \n"+		
								"PREFIX void: <http://rdfs.org/ns/void#> \n"+					
								"Select Distinct * \n" +
								"Where{ \n" +
								"	graph <http://statspace.linkedwidgets.org> { \n" +
								"		<"+ sMDUri +">  qb:dataSet ?ds. \n"+
								"		?ds dcterms:subject ?dss. \n"+
								"		optional{?ds rdfs:label ?dsl.} \n"+
								"		?ds void:feature ?dsf. \n"+
								"		?ds dcat:accessURL ?dsa. \n"+
								"		<"+ sMDUri +">  qb:component ?cp.\n" +							   
								"		?cp rdf:type ?cpt. \n" +		
								"		optional{?cp rdfs:label ?cpl.} \n" +	
								"		optional{?cpr owl:sameAs ?cp.} \n";					  
		if(ds.getSubject()!="")
			sQuery = sQuery +  "		FILTER(?dss=<"+ds.getSubject() + ">). \n";
		
		if(ds.getUri()!="")
			sQuery = sQuery +  "   		FILTER(?ds=<"+ds.getUri() + ">). \n";
		
 	   	sQuery = sQuery +      "	} \n"+
 	   							"} ";
		
		return getMetaDataInfor1(sQuery, sMDUri);		
	}
	
	public MetaData getMetaDataInfor1(String sQuery, String sMDUri) throws QueryParseException {		
		String sDSUri, sDSLabel, sDSSubject, sDSFeature, sDSAccessURL, sCUri, sCRef, sCType, sCLabel;
		MetaData metaData = new MetaData();		
		int i;
		QueryExecution queryExecution = null;
		
		try{					
			Query query = QueryFactory.create(sQuery);
		    queryExecution = QueryExecutionFactory.sparqlService(sEndpoint, query);	
		   
			// execute query
			ResultSet rs = queryExecution.execSelect();		
			i=0;
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();
				i++;
				sDSUri 		= sol.get("ds").toString().replace("\n", "").replace("\r", "").trim();				
				sDSSubject 	= sol.get("dss").toString().replace("\n", "").replace("\r", "").trim();
				if(sol.contains("dsl"))	sDSLabel= sol.get("dsl").toString().replace("\n", "").replace("\r", "").trim();
				else					sDSLabel 	= "";	
				sDSFeature 	= sol.get("dsf").toString().replace("\n", "").replace("\r", "").trim();
				sDSAccessURL= sol.get("dsa").toString().replace("\n", "").replace("\r", "").trim();	
								
				sCUri 		= sol.get("cp").toString().replace("\n", "").replace("\r", "").trim();
				sCType 		= sol.get("cpt").toString().replace("\n", "").replace("\r", "").trim();
				if(sol.contains("cpr"))	sCRef 	= sol.get("cpr").toString().replace("\n", "").replace("\r", "").trim();
				else					sCRef 	= sCUri;				
				if(sol.contains("cpl"))	sCLabel = sol.get("cpl").toString().replace("\n", "").replace("\r", "").trim();
				else					sCLabel = "";					
				
				if(i==1){
					metaData.setUri(sMDUri);
					metaData.setDataSet(new DataSet(sDSUri, sDSLabel, sDSSubject, sDSFeature, sDSAccessURL, ds.getVariable(), ds.getVariableLabel()));
				}
				metaData.addComponent(new Component(sCUri, sCType, sCLabel, sCRef));				
			}		
		}catch(Exception e){			
		}
		return metaData;
	}
	
	public String queryFilterValue(String sDSUri, String sCUri, String sVRef){
	
		String sQuery="PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+							
				"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+								
				"PREFIX dcterms: <http://purl.org/dc/terms/> \n"+
				"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"+
				"PREFIX dcat: <http://www.w3.org/ns/dcat#> \n"+		
				"PREFIX void: <http://rdfs.org/ns/void#> \n"+				
				"Select DISTINCT ?s \n" +
				"Where{ \n" +
				"	graph <http://statspace.linkedwidgets.org> { \n" +
				" 		{\n"+	
				"			<" + sDSUri + "> rdf:value ?s. \n"+
				"			<" + sCUri  + "> rdf:value ?s. \n"+						
				"			FILTER(?s=<"+ sVRef + ">). \n" +
				" 		} UNION \n "+
				" 		{ \n" +			
				"			<" + sDSUri + "> rdf:value ?s. \n"+
				"			<" + sCUri  + "> rdf:value ?s. \n"+						
				"			<"+ sVRef + "> owl:sameAs ?s. \n" +
				" 		}\n"+
				" 	}\n" +
				"}";				
				
		return getFilterValue(sQuery);
	}
	
	public String getFilterValue(String sQuery){		
		String s;			
		QueryExecution queryExecution = null;
		
		try{					
			Query query = QueryFactory.create(sQuery);
		    queryExecution = QueryExecutionFactory.sparqlService(sEndpoint, query);	
		   
			// execute query
			ResultSet rs = queryExecution.execSelect();			
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();
				if(sol.contains("s")){
					s 		= sol.get("s").toString().replace("\n", "").replace("\r", "").trim();												
					return s;				
				}
			}		
		}catch(Exception e){			
		}			
		return null;
	}
	
	public void rewriteQuery(String sVarObs, String sWebApp, String sSeparator){
		String sRDFQuery="", sRMLQuery="";
		int i;		
		
		sRDFQuery=	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+					
					"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+					
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+				
					"Select Distinct * \n" +
					"Where{ \n" +
						"	"+ sVarObs + " qb:dataSet " + ds.getVariable() + ". \n";						
		
		for(i=0; i<arrComp.size(); i++){
			if(!arrComp.get(i).getHiddenStatus())
				sRDFQuery = sRDFQuery + "	"+
						sVarObs + " <"+ arrComp.get(i).getUri()+ "> "+arrComp.get(i).getVariable() + " . \n";				
		}			
		
		//FILTER based on the input query
		for(i=0; i<arrComp.size(); i++){
			if(arrComp.get(i).getFilterValue()!=""){
				if(arrComp.get(i).getUriReference().contains("refPeriod")||arrComp.get(i).getUri().contains("refPeriod"))
					sRDFQuery = sRDFQuery + getYearFilter(arrComp.get(i).getVariable(), arrComp.get(i).getFilterValue());
				else
					sRDFQuery = sRDFQuery + 
							"	FILTER("+arrComp.get(i).getVariable()+"=<" + arrComp.get(i).getFilterValue()+">) \n";
			}
		}		
		
			
		if(ds.getFeature().contains("SPARQL")){	
			sRDFQuery = sRDFQuery + "	FILTER("+ds.getVariable()+"=<"+ds.getUri()+">) \n";
			sRDFQuery = sRDFQuery + "}";
			
			//find refPeriod component to order values
			for(i=0; i<arrComp.size();i++){
				if(arrComp.get(i).getUri().contains("refPeriod")||arrComp.get(i).getUriReference().contains("refPeriod"))
					if(arrComp.get(i).getVariable()!="")
						sRDFQuery = sRDFQuery + "Order by "+ arrComp.get(i).getVariable();
			}
			
			String sEndpoint = ds.getAccessURL();
			queryBySPARQL(sRDFQuery, sEndpoint);			
		}else if(ds.getFeature().equalsIgnoreCase("API")){
			sRDFQuery = sRDFQuery + "}";
			
			//find refPeriod component to order values
			for(i=0; i<arrComp.size();i++){
				if(arrComp.get(i).getUri().contains("refPeriod")||arrComp.get(i).getUriReference().contains("refPeriod"))
					if(arrComp.get(i).getVariable()!="")
						sRDFQuery = sRDFQuery + "Order by "+ arrComp.get(i).getVariable();
			}
			
			sRMLQuery =  ds.getAccessURL();

			//FILTER based on the input query			
			if(ds.getAccessURL().contains("wb.ttl")){
				CL_Area countries = new CL_Area(sWebApp + "mediator" + sSeparator + "countries.csv");
				for(i=0; i<arrComp.size(); i++){
					if(arrComp.get(i).getFilterValue()!=""){
						String sFilter = arrComp.get(i).getFilterValue();
						if(sFilter.contains("cl_area")){
							String sCountry = getEndingPart(sFilter);
							String sIso2Code = countries.getIso2CodeByCountryName(sCountry);
							if(!sIso2Code.isEmpty())
								sRMLQuery = sRMLQuery + "&refArea=" + sIso2Code;
						}else if(sFilter.contains("reference.data.gov.uk")){
							String sTime = getEndingPart(sFilter);
							sRMLQuery = sRMLQuery + "&refPeriod=" + sTime;
						}						
					}
				}	
			}else
				for(i=0; i<arrComp.size(); i++){
					if(arrComp.get(i).getFilterValue()!="")
						sRMLQuery = sRMLQuery + "&" + arrComp.get(i).getVariable().substring(1)+"=" + arrComp.get(i).getFilterValue();
				}			
			queryByRML(sRDFQuery, sRMLQuery);			
		}else{
			sRDFQuery = sRDFQuery + "}";
			
			//find refPeriod component to order values
			for(i=0; i<arrComp.size();i++){
				if(arrComp.get(i).getUri().contains("refPeriod")||arrComp.get(i).getUriReference().contains("refPeriod"))
					if(arrComp.get(i).getVariable()!="")
						sRDFQuery = sRDFQuery + "Order by "+ arrComp.get(i).getVariable();
			}
			
			sRMLQuery =  ds.getAccessURL();
			queryByRML(sRDFQuery, sRMLQuery);
		}
	}
	
	public void queryBySPARQL(String sRDFQuery, String sEndpoint)throws QueryParseException{
		int i;
		String value;
		
		QueryExecution queryExecution = null;		
		try{					
			Query query = QueryFactory.create(sRDFQuery);
		    queryExecution = QueryExecutionFactory.sparqlService(sEndpoint, query);	   
			// execute query
			ResultSet rs = queryExecution.execSelect();	
			//ResultSetFormatter.out(System.out, rs);
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();					
				Iterator<String> itr = sol.varNames();
				while(itr.hasNext()) {
			         String var = itr.next();
			         for(i=0; i<arrComp.size(); i++)
			        	 if(arrComp.get(i).getVariable().equalsIgnoreCase("?"+var)){
			        		 value = sol.get(var).toString();
			        		 arrComp.get(i).addValue(value, "");
			        		 break;
			        	 }
			     }					
			}		
		
		}catch(Exception e){			
		}
		finally {
			if(queryExecution!=null){
				queryExecution.close();
			}
		}		
	}
	
	public void queryByRML(String sRDFQuery, String sRMLQuery){
		QueryExecution  queryExecution =  null;
		String value;
		int i;

		try{
			URL obj = new URL(sRMLQuery);		
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();		
			con.setRequestMethod("GET"); 
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
	 
			int responseCode = con.getResponseCode();			
			if(responseCode==200){
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));				
				Model model = ModelFactory.createDefaultModel() ; 
				model.read(in, null,"N-TRIPLES") ;
				Query query = QueryFactory.create(sRDFQuery); 
				queryExecution = QueryExecutionFactory.create(query, model);
				ResultSet rs = queryExecution.execSelect();	
				while (rs!=null && rs.hasNext()) {		
					QuerySolution sol = rs.nextSolution();					
					Iterator<String> itr = sol.varNames();
					while(itr.hasNext()) {
				         String var = itr.next();
				         for(i=0; i<arrComp.size(); i++)
				        	 if(arrComp.get(i).getVariable().equalsIgnoreCase("?"+var)){
				        		 value = sol.get(var).toString();
				        		 arrComp.get(i).addValue(value, "");
				        		 break;
				        	 }
				     }					
				}				
			}
		}catch(Exception e){			
		}	
		finally {
			if(queryExecution!=null){
				queryExecution.close();
			}
		}		
	}
	
	public void queryUnit(int index){
		int i;
		String sQuery;
		
		sQuery= "PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+					
				"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+					
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+	
				"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
				"Select distinct ?v \n" +
				"Where{ \n" +
				"	graph <http://statspace.linkedwidgets.org> { \n" +
				"		<"+ds.getUri() + "> rdf:value ?v. \n"+
				"		<"+arrComp.get(index).getUri() + "> rdf:value ?v. \n"+				
				" 	}\n"+
				"}";
		String sUnit = getUnit(sQuery);
		if(!sUnit.isEmpty()){
			for(i=0; i<arrComp.get(1).getValueSize(); i++){
				arrComp.get(index).addValue(sUnit,  sUnit);
			}			
		}
	}
	
	public String getUnit(String sQuery) throws QueryParseException {		
		String sUnit;			
		QueryExecution queryExecution = null;
		
		try{					
			Query query = QueryFactory.create(sQuery);
		    queryExecution = QueryExecutionFactory.sparqlService(sEndpoint, query);	
		   
			// execute query
			ResultSet rs = queryExecution.execSelect();			
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();
				sUnit = sol.get("v").toString().replace("\n", "").replace("\r", "").trim();
				return sUnit;
			}		
		}catch(Exception e){			
		}		
		return "";
	}

	public void rewriteResult() {
		int i, j;		
		String sCompUri="";
		String sQuery="";
		for(i=0; i<arrComp.size(); i++){
			sCompUri = arrComp.get(i).getUri();	
			if(!arrComp.get(i).getType().contains("Measure")){
				sQuery= "PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+					
						"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+					
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+	
						"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
						"Select distinct ?vref ?v \n" +
						"Where{ \n" +
						"	graph <http://statspace.linkedwidgets.org> { \n" +
						"		<"+ds.getUri() + "> rdf:value ?v. \n"+
						"		<"+sCompUri + "> rdf:value ?v. \n"+					
						"		?vref owl:sameAs ?v.\n"+
						" 	}\n"+
						"}";
				getReferenceValue(sQuery, i, 0);	
			}
//			else if(arrComp.get(i).getType().contains("Attribute")){
//				sQuery= "PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+					
//						"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+					
//						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+	
//						"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
//						"Select distinct ?vref ?v \n" +
//						"Where{ \n" +
//						"	graph <http://statspace.linkedwidgets.org> { \n" +
//						"		<"+ds.getUri() + "> rdf:value ?v. \n"+
//						"		<"+sCompUri + "> rdf:value ?v. \n"+					
//						"		optional{?vref owl:sameAs ?v.}\n"+
//						" 	}\n"+
//						"}";
//				queryReferenceValue(sQuery, i, 1);			
//			}	
		}
		
		//for values don't have co-reference, set their values for themself co-reference values
		for(i=0; i<arrComp.size(); i++){
			for(j=0; j<arrComp.get(i).getValueSize(); j++)
				if(arrComp.get(i).getValueReference(j)=="")
					arrComp.get(i).setValueRefence(j, arrComp.get(i).getValue(j));
		}		
	}
	
	public void getReferenceValue(String sQuery, int index, int type) throws QueryParseException{
		int i;
		QueryExecution queryExecution = null;
		String sValue, sRefValue;
		try{					
			Query query = QueryFactory.create(sQuery);
		    queryExecution = QueryExecutionFactory.sparqlService(sEndpoint, query);	
		   
			// execute query
			ResultSet rs = queryExecution.execSelect();			
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();
				sValue  = sol.get("v").toString();
				if(sol.contains("vref"))
					sRefValue = sol.get("vref").toString();
				else
					sRefValue = sValue;	
				
				if(type==0){
					for(i=0; i<arrComp.get(index).getValueSize(); i++)
						if(arrComp.get(index).getValue(i).equalsIgnoreCase(sValue)){
							//if time-dimension, choose year > day
							if(arrComp.get(index).getValueReference(i)=="" || 
									arrComp.get(index).getValueReference(i).contains("World/tmp") ||
									arrComp.get(index).getValueReference(i).length()>sRefValue.length())								
							arrComp.get(index).setValueRefence(i, sRefValue);
						}
				}
//				else{
//					
//					arrComp.get(index).addValue(sValue, sRefValue);
//				}							
			}
		}catch(Exception e){			
		}	
		finally {
			if(queryExecution!=null){
				queryExecution.close();
			}
		}
	}

	public void integrateData(MetaData metaData) {	
		int i, j, k, m, t, n;
		String sValueRef, sValueRef2;
		String sUriRef, sUriRef2;	

		for(i=0; i<arrComp.size(); i++){
			if(arrComp.get(i).getType().contains("Dimension")){
				sUriRef = arrComp.get(i).getUriReference();
				if(sUriRef.equals(""))
					sUriRef = arrComp.get(i).getUri();
				
				//find the equivalent dimension in metaData variable
				for(k=0; k<metaData.getNumberofComponent(); k++){
					sUriRef2 =metaData.getComponent(k).getUriReference();
					if(sUriRef2.equals(""))
						sUriRef2 = metaData.getComponent(k).getUri();
					if(sUriRef.equals(sUriRef2))
						break;
				}
				
				//not found, remove components which do not appear in metaData
				if(k==metaData.getNumberofComponent()){				
					arrComp.remove(i);
					i--;
				}
				else{
				
					for(m=0; m<metaData.getComponent(k).getValueSize(); m++){
						sValueRef2 = metaData.getComponent(k).getValueReference(m);						
						
						for(j=0; j<arrComp.get(i).getValueSize(); j++){
							sValueRef = arrComp.get(i).getValueReference(j);	
							if(sValueRef.equals(sValueRef2))
								break;	
						}						
						
						if(j==arrComp.get(i).getValueSize()){
							
							//add new observation
							for(t=0; t<arrComp.size(); t++){
								sUriRef = arrComp.get(t).getUriReference();
								if(sUriRef.equals(""))
									sUriRef = arrComp.get(t).getUri();		
							
								for(n=0; n<metaData.getNumberofComponent(); n++){
									sUriRef2 =metaData.getComponent(n).getUriReference();
									if(sUriRef2.equals(""))
										sUriRef2 = metaData.getComponent(n).getUri();
									
									if(sUriRef.equals(sUriRef2)){
										if(arrComp.get(t).getType().contains("Attribute"))
											arrComp.get(t).addValue(arrComp.get(t).getValue(0), arrComp.get(t).getValueReference(0));
										else
											arrComp.get(t).addValue(metaData.getComponent(n).getValue(m), metaData.getComponent(n).getValueReference(m));
									}
								}								
							}							
						}					
					}					
				}
			}	
		}
	}
	
	public void removeExternalComponents(MetaData metaData) {	
		int i, k;		
		String sUriRef, sUriRef2;	

		//remove external components
		for(i=0; i<arrComp.size(); i++){
//			if(arrComp.get(i).getType().contains("Dimension")){
//				sUriRef = arrComp.get(i).getUriReference();
//				if(sUriRef.equals(""))
//					sUriRef = arrComp.get(i).getUri();
				
				//find the equivalent dimension in metaData variable 
				for(k=0; k<metaData.getNumberofComponent(); k++){
					if(metaData.getComponent(k).getVariable().equalsIgnoreCase(arrComp.get(i).getVariable()))
//					sUriRef2 =metaData.getComponent(k).getUriReference();
//					if(sUriRef2.equals(""))
//						sUriRef2 = metaData.getComponent(k).getUri();
//					if(sUriRef.equals(sUriRef2))
						break;
				}
				
				//not found, remove components which do not appear in metaData
				if(k==metaData.getNumberofComponent()){				
					arrComp.remove(i);
					i--;
				}
//			}
		}
		
		//order components in the same position like metadata variable
		for(k=0; k<metaData.getNumberofComponent(); k++){
			sUriRef2 =metaData.getComponent(k).getUriReference();
			if(sUriRef2.equals(""))
				sUriRef2 = metaData.getComponent(k).getUri();
			
			sUriRef = arrComp.get(k).getUriReference();
			if(sUriRef.equals(""))
				sUriRef = arrComp.get(k).getUri();
			
			if(sUriRef2.equalsIgnoreCase(sUriRef)) continue;
			else{
				for(i=0; i<arrComp.size(); i++){
					sUriRef = arrComp.get(i).getUriReference();
					if(sUriRef.equals(""))
						sUriRef = arrComp.get(i).getUri();
					if(sUriRef2.equalsIgnoreCase(sUriRef))
						exchangePosition(k, i);						
				}
			}	
		}
		for(;k<arrComp.size();k++){
			arrComp.remove(k);
			k--;
		}
	}
	
	
	public void exchangePosition(int k, int j){
		Component tmp = new Component(arrComp.get(k));
		arrComp.set(k, arrComp.get(j));
		arrComp.set(j, tmp);
	}
	
	public ArrayList<String> getTemporalValues(int index, ArrayList<String> arrTemporalValues){
		ArrayList<String> result = new ArrayList<String>();
		result = arrTemporalValues;
		int i, j, k;
		String time;
		for(i=0; i<arrComp.get(index).getValueSize(); i++){
			time = arrComp.get(index).getValueReference(i);			
			if(result.indexOf(time)!=-1) continue;
			if(result.size()==0)
				result.add(time);
			else{
				j=0;			
				while(j<result.size() && result.get(j).compareTo(time)<0) j++;
				result.add(new String());
				for(k=result.size()-1; k>j; k--)
					result.set(k, result.get(k-1));
				result.set(j, time);
			}
		}
		return result;
	}	
	
	public void rewriteObservedValue(Double multi) {
		int i, j, k;
		String sValue;
		Double value;
		DecimalFormat df = new DecimalFormat("#.0"); 
		for(i=0; i<arrComp.size(); i++)
			if(arrComp.get(i).getType().contains("Measure")){
				for(j=0; j<arrComp.get(i).getValueSize(); j++){
					sValue = arrComp.get(i).getValue(j);				
					k=sValue.indexOf("^^");
					if(k!=-1)
						sValue = sValue.substring(0, k);
					try{
						value = Double.parseDouble(sValue);						
						value = value * multi;
						arrComp.get(i).setValue(j, df.format(value));
					}catch (Exception e){
						System.out.println(i+"\t"+j);
					}
					
				}
				break;
			}
		
	}

	/* check temporal values
	 * gregorian-date/1960-01-01  => gregorian-year/1960
	   gregorian-date/1961-01-01   	 gregorian-year/1961
	 */
	public void rewriteTemporalValue() {
		int i, j, k;	
		String sDate, sYear;
		
		for(i=0; i<arrComp.size(); i++)
			if(arrComp.get(i).getUriReference().contains("refPeriod")||arrComp.get(i).getUri().contains("refPeriod"))
				break;		
		
		//all temporal values are in date format
		for(j=0; j<arrComp.get(i).getValueSize(); j++)
			if(!(arrComp.get(i).getValueReference(j).contains("/gregorian-date/")||
					arrComp.get(i).getValue(j).contains("/gregorian-date/"))){				
				break;
			}
		
		//Rewrite temporal values		
		if(j==arrComp.get(i).getValueSize()){
			for(j=0; j<arrComp.get(i).getValueSize(); j++){
				sDate = arrComp.get(i).getValueReference(j);
				if(sDate.equals("")) sDate=arrComp.get(i).getValue(j);
				k = sDate.indexOf("gregorian-date");
				sDate = sDate.substring(k+15);
				sDate = sDate.substring(0,4);
				sYear = "http://reference.data.gov.uk/id/gregorian-year/"+sDate;
				arrComp.get(i).setValueRefence(j, sYear);
			}			
		}			

	}
	
	public int getIndexOfTemporalDimension(){
		int i;
		for(i=0; i<arrComp.size(); i++)
			if(arrComp.get(i).getUri().equalsIgnoreCase("http://purl.org/linked-data/sdmx/2009/dimension#refPeriod"))
				return i;
		return -1;
	}
	
	public int getIndexOfMeasureComponent(){
		int i;
		for(i=0; i<arrComp.size(); i++)
			if(arrComp.get(i).getType().contains("Measure"))
				return i;
		return -1;
	}
	
	public int getIndexOfAttributeComponent(){
		int i;
		for(i=0; i<arrComp.size(); i++)
			if(arrComp.get(i).getType().contains("Attribute"))
				return i;
		return -1;
	}
	
	public String getEndingPart(String uri){
		int i=uri.length()-1;
		while(i>0 && uri.charAt(i)!='/' && uri.charAt(i)!='#') i--;
		if(i>0) 
			return uri.substring(i+1);
		else
			return uri;
	}
	
	public String getYearFilter(String sVar, String sValue){						
		int k = sValue.indexOf("^^");
		if(k>0){
			//xsd:gYear => use string comparison to deal with change of time zone			
			if(sValue.length()>=4)
				return ("FILTER(REGEX(str(" + sVar + "),'" + sValue.substring(0,4) + "','i'))\n");
			else
				return ("FILTER(REGEX(str(" + sVar + "),'" + sValue + "','i'))\n");
				
		}
		else{
			return ("FILTER(" + sVar + "='" + sValue + "')\n");			
		}		
	}
	
	public ArrayList<MetaData> queryMetaDataByKeyword() {			
		String sQuery=			"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+							
								"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
								"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+								
								"PREFIX dcterms: <http://purl.org/dc/terms/> \n"+
								"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
								"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"+
								"PREFIX dcat: <http://www.w3.org/ns/dcat#> \n"+		
								"PREFIX void: <http://rdfs.org/ns/void#> \n"+								
								"Select Distinct ?md ?mdp ?ds ?dss ?dsl \n" +
								"Where{ \n" +
								"	graph <http://statspace.linkedwidgets.org> { \n" +
								"		?md qb:dataSet ?ds. \n"+
								"		optional{?md dcterms:publisher ?mdp}. \n"+
								"		optional{?ds dcterms:subject ?dss.} \n" +						
								"		?ds rdfs:label ?dsl. \n" ;
		if(!sKeyword.isEmpty())
			sQuery = sQuery +	" 		filter (regex(str(?dsl), \"" + sKeyword + "\", \"i\")) \n";
	
		sQuery = sQuery 	+	"	}\n"+
								"}order by desc(?mdp) ?ds";
		
//        log.info(sQuery);
        return getMetaDataByKeyword(sQuery);        	
	}
	
	public ArrayList<MetaData>  getMetaDataByKeyword(String sQuery) throws QueryParseException {		
		String sMDUri, sDSUri, sMDPublisher, sDSSubject, sDSLabel;
		ArrayList<MetaData> arrMetaData = new ArrayList<MetaData>();				
		QueryExecution queryExecution = null;		
		try{					
			Query query = QueryFactory.create(sQuery);
		    queryExecution = QueryExecutionFactory.sparqlService(sEndpoint, query);	
		   
			// execute query
			ResultSet rs = queryExecution.execSelect();			
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();
				sMDUri 		= sol.get("md").toString().replace("\n", "").replace("\r", "").trim();		
				sDSUri 		= sol.get("ds").toString().replace("\n", "").replace("\r", "").trim();		
				sDSLabel	= sol.get("dsl").toString().replace("\n", "").replace("\r", "").trim();		
				if(sol.contains("mdp"))	sMDPublisher = sol.get("mdp").toString().replace("\n", "").replace("\r", "").trim();
				else					sMDPublisher = "";	
				if(sol.contains("dss"))	sDSSubject = sol.get("dss").toString().replace("\n", "").replace("\r", "").trim();
				else					sDSSubject = "";
				arrMetaData.add(new MetaData(sMDUri, sMDPublisher, sDSUri, sDSSubject, sDSLabel));				
			}		
		}catch(Exception e){			
		}		
		return arrMetaData;		
	}
	
	
	public ArrayList<MetaData> searchComparableDataSet2() {			
		String sQuery1=			"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+							
								"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
								"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+								
								"PREFIX dcterms: <http://purl.org/dc/terms/> \n"+
								"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
								"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"+
								"PREFIX dcat: <http://www.w3.org/ns/dcat#> \n"+		
								"PREFIX void: <http://rdfs.org/ns/void#> \n"+								
								"Select Distinct ?md2 ?md2p ?ds2 ?ds2l ?ds2s \n" +
								"Where{ \n" +
								"	graph <http://statspace.linkedwidgets.org> { \n" +
								"		<"+sUri+"> qb:dataSet ?ds1. \n" +
								"		?md2 qb:dataSet ?ds2. \n"+
								"	    optional{?md2 dcterms:publisher ?md2p} \n"+
						        " 		optional{?ds2 rdfs:label ?ds2l} \n"+
						        "		optional{?ds2 dcterms:subject ?ds2s.} \n" +	
							    " 		?ds1 rdf:value ?v1. \n"+
								"       ?ds2 rdf:value ?v2. \n"+
							    "       { \n" +
								" 			?v  owl:sameAs ?v1.\n	"+		           
								"			?v  owl:sameAs ?v2.\n   "+
								" 		    filter (regex(str(?v), \"cl_area\", \"i\" ))\n"+
								" 		}\n"+
								"		union \n" +
								"       { \n" +
								" 			?v1  owl:sameAs ?v2.\n	"+	
								" 		    filter (regex(str(?v1), \"cl_area\", \"i\" ))\n"+
								" 		}\n"+
								"		union \n" +
								"       { \n" +
								" 			?v2  owl:sameAs ?v1.\n	"+					
								" 		    filter (regex(str(?v2), \"cl_area\", \"i\" ))\n"+
								" 		}\n"+
								"		union \n" +
								"       { \n" +
								" 			?v1  owl:sameAs ?v3.\n	"+				
								" 		    filter (?v1=?v2 && regex(str(?v1), \"cl_area\", \"i\" ))\n"+
								" 		}\n"+						
								"	}\n"+
								"}order by desc(?md2) ";
		
		String sQuery2=			"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+							
				"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+								
				"PREFIX dcterms: <http://purl.org/dc/terms/> \n"+
				"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"+
				"PREFIX dcat: <http://www.w3.org/ns/dcat#> \n"+		
				"PREFIX void: <http://rdfs.org/ns/void#> \n"+								
				"Select Distinct ?md2 ?md2p ?ds2 ?ds2l ?ds2s\n" +
				"Where{ \n" +
				"	graph <http://statspace.linkedwidgets.org> { \n" +
				"		<"+sUri+"> qb:dataSet ?ds1. \n" +
				"		?md2 qb:dataSet ?ds2. \n"+
				"	    optional{?md2 dcterms:publisher ?md2p} \n"+
		        " 		optional{?ds2 rdfs:label ?ds2l} \n"+
		        "		optional{?ds2 dcterms:subject ?ds2s.} \n" +	
			    " 		?ds1 rdf:value ?v1. \n"+
				"       ?ds2 rdf:value ?v2. \n"+
			    "       { \n" +
				" 			?v  owl:sameAs ?v1.\n	"+		           
				"			?v  owl:sameAs ?v2.\n   "+
				" 		    filter (regex(str(?v), \"http://reference.data.gov.uk/id/\", \"i\" ))\n"+
				" 		}\n"+
				"		union \n" +
				"       { \n" +
				" 			?v1  owl:sameAs ?v2.\n	"+	
				" 		    filter (regex(str(?v1), \"http://reference.data.gov.uk/id/\", \"i\" ))\n"+
				" 		}\n"+
				"		union \n" +
				"       { \n" +
				" 			?v2  owl:sameAs ?v1.\n	"+					
				" 		    filter (regex(str(?v2), \"http://reference.data.gov.uk/id/\", \"i\" ))\n"+
				" 		}\n"+
				"		union \n" +
				"       { \n" +
				" 			?v1  owl:sameAs ?v3.\n	"+				
				" 		    filter (?v1=?v2 && regex(str(?v1), \"http://reference.data.gov.uk/id/\", \"i\" ))\n"+
				" 		}\n"+						
				"	}\n"+
				"} order by desc(?md2) ";
		
        ArrayList<MetaData> arrMD1 = new ArrayList<MetaData>();
        arrMD1 = getMetaDataByComponent(sQuery1);
        ArrayList<MetaData> arrMD2 = new ArrayList<MetaData>();
        arrMD2 = getMetaDataByComponent(sQuery2);
        int i, j;
        for(i=0; i<arrMD1.size(); i++){
        	for(j=0; j<arrMD2.size(); j++)
	        	if(arrMD2.get(j).getUri().equals(arrMD1.get(i).getUri())){        		
	        		break;	        	
        	}
        	if(j==arrMD2.size()){
        		arrMD1.remove(i);
        		i--;
        	}
        		
        }     
        return arrMD1;
	}
	
	public ArrayList<MetaData> searchComparableDataSet() {	
		//Filter metadata by subject
		String sQuery=			"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+							
								"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
								"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+								
								"PREFIX dcterms: <http://purl.org/dc/terms/> \n"+
								"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
								"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"+
								"PREFIX dcat: <http://www.w3.org/ns/dcat#> \n"+		
								"PREFIX void: <http://rdfs.org/ns/void#> \n"+								
								"Select Distinct ?md ?mdp ?ds ?dsl ?dss \n" +
								"Where{ \n" +
								"	graph <http://statspace.linkedwidgets.org> { \n" +
								"		<"+sUri+"> qb:dataSet ?ds1. \n" +
								"		?md qb:dataSet ?ds. \n"+
								" 		?ds1 dcterms:subject ?dss. \n"+	
								"	  	?ds dcterms:subject ?dss. \n" +
								"	    optional{?md dcterms:publisher ?mdp} \n"+
						        " 		optional{?ds rdfs:label ?dsl} \n"+							  						
								"	}\n"+
								"}order by desc(?md) ";
		ArrayList<MetaData> arrMD1 = new ArrayList<MetaData>();
		arrMD1 = getMetaDataByKeyword(sQuery);
		
		//Filter by number of component
		int i, j, n;
		n = arrComp.size();
		sQuery=	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+							
				"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+								
				"PREFIX dcterms: <http://purl.org/dc/terms/> \n"+
				"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"+
				"PREFIX dcat: <http://www.w3.org/ns/dcat#> \n"+		
				"PREFIX void: <http://rdfs.org/ns/void#> \n"+								
				"Select ?md2 ?md2p ?ds2 ?ds2l ?ds2s  \n" +
				"Where{ \n" +
				"	graph <http://statspace.linkedwidgets.org> { \n" +
				"		?md2 qb:dataSet ?ds2. \n"+
				"	    ?md2 dcterms:publisher ?md2p \n"+
		        " 		optional{?ds2 rdfs:label ?ds2l} \n"+
		        "		?ds2 dcterms:subject ?ds2s. \n" +	
				" 		?md2 qb:component ?cp. \n"+							  						
				"	}\n"+
				"}group by ?md2 ?md2p ?ds2 ?ds2l ?ds2s \n"+
				"having(count(?cp)="+n+")\n"+
				"order by ?md2";
		
		//Filter by component
		ArrayList<MetaData> arrMD2 = new ArrayList<MetaData>();
		arrMD2 = getMetaDataByComponent(sQuery);
		
		ArrayList<String> arrUri = new ArrayList<String>();	
		for(i=0; i<arrComp.size(); i++){
			if(arrComp.get(i).getType().contains("Attribute")||arrComp.get(i).getType().contains("Measure")) continue;
			if(arrComp.get(i).getUriReference().contains("refArea")||arrComp.get(i).getUriReference().contains("refPeriod")) continue;
		   	    	
	    	sQuery=	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+							
					"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+								
					"PREFIX dcterms: <http://purl.org/dc/terms/> \n"+
					"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
					"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"+
					"PREFIX dcat: <http://www.w3.org/ns/dcat#> \n"+		
					"PREFIX void: <http://rdfs.org/ns/void#> \n"+								
					"Select Distinct ?md \n" +
					"Where{ \n" +
					"	graph <http://statspace.linkedwidgets.org> { \n" +
					"		?md qb:dataSet ?ds2. \n"+					
			        "       { \n" +
				    " 			?md qb:component <" + arrComp.get(i).getUriReference() +">. \n"+	
					" 		}\n"+
					"		union \n" +
					"       { \n" +
					" 			?md qb:component ?cp2.\n	"+	
					" 			<"+arrComp.get(i).getUriReference()+"> owl:sameAs ?cp2 \n"+
					" 		}\n"+								
					"	}\n"+
					"} order by desc(?md) ";
	    	if(arrUri.size()==0){
	    		arrUri = getMetaDataUri(sQuery);
	    	}else{	
	    		ArrayList<String> tmp = new ArrayList<String>();
	        	tmp = getMetaDataUri(sQuery);
		    	for(j=0; j<arrUri.size(); j++)
		    		if(tmp.indexOf(arrUri.get(j))==-1){
		    			arrUri.remove(j);
		    			j--;
		    		}
	    	}		    	
		}
		if(arrUri.size()>0)
			for(i=0; i<arrMD2.size(); i++)
				if(arrUri.indexOf(arrMD2.get(i).getUri())==-1){
					arrMD2.remove(i);
					i--;
				}				
		
		for(j=0; j<arrMD1.size();j++){
			for(i=0; i<arrMD2.size();i++){
				if(arrMD2.get(i).getUri().equalsIgnoreCase(arrMD1.get(j).getUri()))
					break;
			}
			if(i==arrMD2.size())
				arrMD2.add(arrMD1.get(j));
		}
		
        return arrMD2;
	}	
	
	
	public ArrayList<MetaData>  getMetaDataByComponent(String sQuery) throws QueryParseException {		
		String sMDUri, sDSUri, sMDPublisher, sDSSubject, sDSLabel;
		ArrayList<MetaData> arrMetaData = new ArrayList<MetaData>();		
		QueryExecution queryExecution = null;		
		try{					
			Query query = QueryFactory.create(sQuery);
		    queryExecution = QueryExecutionFactory.sparqlService(sEndpoint, query);	
		   
			// execute query
			ResultSet rs = queryExecution.execSelect();			
			while (rs!=null && rs.hasNext()) {		
				QuerySolution sol = rs.nextSolution();
				sMDUri 		= sol.get("md2").toString().replace("\n", "").replace("\r", "").trim();	
				if(sol.contains("md2p"))	sMDPublisher = sol.get("md2p").toString().replace("\n", "").replace("\r", "").trim();
				else						sMDPublisher = "";	
				sDSUri 		= sol.get("ds2").toString().replace("\n", "").replace("\r", "").trim();	
				if(sol.contains("ds2l"))	sDSLabel = sol.get("ds2l").toString().replace("\n", "").replace("\r", "").trim();
				else						sDSLabel = "";				
				if(sol.contains("ds2s"))	sDSSubject = sol.get("ds2s").toString().replace("\n", "").replace("\r", "").trim();
				else						sDSSubject = "";
				arrMetaData.add(new MetaData(sMDUri, sMDPublisher, sDSUri, sDSSubject, sDSLabel));	
			}		
		}catch(Exception e){			
		}				
		return arrMetaData;		
	}
	
	public void setVariable(){
	  	int i;
       	String sRefUri, sVar;       	
       	//Dataset
       	ds.setVariable("?ds");
       	
       	//Components
       	for(i=0; i<arrComp.size(); i++){
       		sRefUri = arrComp.get(i).getUriReference();
       		if(sRefUri.isEmpty())
       			sRefUri =  arrComp.get(i).getUri();
       		sVar = "?"+getEndingPart(sRefUri);
       		arrComp.get(i).setVariable(sVar);       		
       	}
	}
	
	public void rewriteQuery2(String sVarObs, String sWebApp, String sSeparator){
		String sRDFQuery="", sRMLQuery="";
		int i, j;		
		
		sRDFQuery=	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+					
					"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+					
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+				
					"Select Distinct * \n" +
					"Where{ \n" +
						"	"+ sVarObs + " qb:dataSet " + ds.getVariable() + ". \n";						
		
		for(i=0; i<arrComp.size(); i++){
			if(!arrComp.get(i).getHiddenStatus())
				sRDFQuery = sRDFQuery + "	"+
						sVarObs + " <"+ arrComp.get(i).getUri()+ "> "+arrComp.get(i).getVariable() + " . \n";				
		}	
			
		if(ds.getFeature().contains("SPARQL")){	
			sRDFQuery = sRDFQuery + "	FILTER("+ds.getVariable()+"=<"+ds.getUri()+">) \n";
			sRDFQuery = sRDFQuery + "}";
			
			//find refPeriod component to order values
			for(i=0; i<arrComp.size();i++){
				if(arrComp.get(i).getUri().contains("refPeriod")||arrComp.get(i).getUriReference().contains("refPeriod"))
					if(arrComp.get(i).getVariable()!="")
						sRDFQuery = sRDFQuery + "Order by "+ arrComp.get(i).getVariable();
			}
			
			String sEndpoint = ds.getAccessURL();
			queryBySPARQL(sRDFQuery, sEndpoint);			
		}else if(ds.getFeature().equalsIgnoreCase("API")){
			sRDFQuery = sRDFQuery + "}";
			
			//find refPeriod component to order values
			for(i=0; i<arrComp.size();i++){
				if(arrComp.get(i).getUri().contains("refPeriod")||arrComp.get(i).getUriReference().contains("refPeriod"))
					if(arrComp.get(i).getVariable()!="")
						sRDFQuery = sRDFQuery + "Order by "+ arrComp.get(i).getVariable();
			}
			
			sRMLQuery =  ds.getAccessURL();

			//FILTER based on the input query			
			if(ds.getAccessURL().contains("wb.ttl")){
				CL_Area countries = new CL_Area(sWebApp + "mediator" + sSeparator + "countries.csv");
				for(i=0; i<arrComp.size(); i++){
					if(arrComp.get(i).getFilterValue()!=""){
						String sFilter = arrComp.get(i).getFilterValue();
						if(sFilter.contains("cl_area")){
							if(sFilter.contains(";")){
								String[] areas = sFilter.split(";");
								String sQuery="";
								for(j=0; j<areas.length; j++){
									String sCountry = getEndingPart(areas[j]);
									String sIso2Code = countries.getIso2CodeByCountryName(sCountry);
									if(!sIso2Code.isEmpty())
										if(sQuery.isEmpty()) sQuery = sIso2Code;
										else sQuery = sQuery + ";" + sIso2Code;
								}
								sRMLQuery = sRMLQuery + "&refArea=" + sQuery;
							}else{
								String sCountry = getEndingPart(sFilter);
								String sIso2Code = countries.getIso2CodeByCountryName(sCountry);
								if(!sIso2Code.isEmpty())
									sRMLQuery = sRMLQuery + "&refArea=" + sIso2Code;
							}
						}else if(sFilter.contains("reference.data.gov.uk")){
							String sTime = getEndingPart(sFilter);
							sRMLQuery = sRMLQuery + "&refPeriod=" + sTime;
						}						
					}
				}	
			}else
				for(i=0; i<arrComp.size(); i++){
					if(arrComp.get(i).getFilterValue()!="")
						sRMLQuery = sRMLQuery + "&" + arrComp.get(i).getVariable().substring(1)+"=" + arrComp.get(i).getFilterValue();
				}			
			queryByRML(sRDFQuery, sRMLQuery);			
		}else{
			sRDFQuery = sRDFQuery + "}";
			
			//find refPeriod component to order values
			for(i=0; i<arrComp.size();i++){
				if(arrComp.get(i).getUri().contains("refPeriod")||arrComp.get(i).getUriReference().contains("refPeriod"))
					if(arrComp.get(i).getVariable()!="")
						sRDFQuery = sRDFQuery + "Order by "+ arrComp.get(i).getVariable();
			}
			
			sRMLQuery =  ds.getAccessURL();
			queryByRML(sRDFQuery, sRMLQuery);
		}
	}
	
	public void reorderComponents(){
		int i;
		for(i=0; i<arrComp.size(); i++){
			if(arrComp.get(i).getType().contains("Attribute")){
				//swap 0 - i
				Component tmp = new Component(arrComp.get(0));
				arrComp.set(0, arrComp.get(i));
				arrComp.set(i, tmp);
				break;
			}
		}
		for(i=0; i<arrComp.size(); i++){
			if(arrComp.get(i).getType().contains("Measure")){
				//swap 1 - i
				Component tmp = new Component(arrComp.get(1));
				arrComp.set(1, arrComp.get(i));
				arrComp.set(i, tmp);
				break;
			}
		}
		for(i=0; i<arrComp.size(); i++){			
			if(arrComp.get(i).getUri().contains("refArea")){
				Component tmp = new Component(arrComp.get(2));
				arrComp.set(2, arrComp.get(i));
				arrComp.set(i, tmp);
			}else if(arrComp.get(i).getUri().contains("refPeriod")||arrComp.get(i).getUriReference().contains("refPeriod")){
				Component tmp = new Component(arrComp.get(3));
				arrComp.set(3, arrComp.get(i));
				arrComp.set(i, tmp);
			}
		}
		
		if(!arrComp.get(2).getUri().contains("refArea")){
			for(i=4; i<arrComp.size(); i++){
				if(arrComp.get(i).getUriReference().contains("refArea")){
					Component tmp = new Component(arrComp.get(2));
					arrComp.set(2, arrComp.get(i));
					arrComp.set(i, tmp);
				}
			}
		}
	}
	
	public void reorderComponents(MetaData md){
		int i, j;//unit, obsValue, refArea, refPeriod
		for(i=4; i<md.getNumberofComponent(); i++){
			for(j=4; j<arrComp.size(); j++){
				if(md.getComponent(i).getUriReference().equalsIgnoreCase(arrComp.get(j).getUriReference())){					
					Component tmp = new Component(arrComp.get(i));
					arrComp.set(i, arrComp.get(j));
					arrComp.set(j, tmp);
				}				
			}			
		}
		while(arrComp.size()>md.getNumberofComponent())
			arrComp.remove(md.getNumberofComponent());		
	}
	
	public ArrayList<String> getDistinctRefValue(int index){
		ArrayList<String> arrValue = new ArrayList<String>();
		int i;
		String s;
		for(i=0; i<arrComp.get(index).getValueSize(); i++){
			s=arrComp.get(index).getValueReference(i);
			if(arrValue.indexOf(s)==-1)
				arrValue.add(s);
		}
		return arrValue;
	}
	
	public ArrayList<String> getDistinctRefValueLabel(int index){
		ArrayList<String> arrValue = new ArrayList<String>();
		int i;
		String s;
		for(i=0; i<arrComp.get(index).getValueSize(); i++){
			s=arrComp.get(index).getValueReference(i);
			s=getEndingPart(s);
			if(arrValue.indexOf(s)==-1)
				arrValue.add(s);
		}
		return arrValue;
	}
	
	public void filterValue(int index, ArrayList<String> arrFilterValue){
		int i, j;
		String s;
		for(i=0; i<arrComp.get(index).getValueSize(); i++){
			s=arrComp.get(index).getValueReference(i);
			if(arrFilterValue.indexOf(s)==-1){
				for(j=0; j<arrComp.size(); j++){
					arrComp.get(j).removeValue(i);
				}
				i--;
			}				
		}		
	}
	
	public String getJSONFormat(){	
		int i, j;
    	ArrayList<String> arrVar = new ArrayList<String>();
    	StringBuffer sResult = new StringBuffer();
    	sResult.append("{").append("\"head\":{").append("\"vars\":[");
		
		/*
		 * Note that we maybe need to return the label of dataset, component, and value
		 * Solution: 
		 *   + Do not add these variables to arrVar
		 *   + If the variable for ds, component is available, check the variable of its label
		 */
		
		//variables for components
		for(i=0; i<arrComp.size(); i++){							
			arrVar.add(arrComp.get(i).getVariable().substring(1));
			sResult.append("\"").append(arrVar.get(i)).append("\",");			
			
		}		

		//variable for dataset		
//		if(ds.getVariable()!=""){
//			arrVar.add(ds.getVariable().substring(1));
//			sResult.append("\"").append(arrVar.get(i)).append("\",");			
//		}
//		else{			
//			ds.setVariable("ds");
//			arrVar.add("ds");
//			sResult.append("\"").append(arrVar.get(i)).append("\",");	
//		}
		
		sResult.deleteCharAt(sResult.length()-1).append("]").append("},").append("\"results\":{").append("\"bindings\":[");
		
		//add values		
		for(i=0; i<arrComp.get(1).getValueSize(); i++){				
			sResult.append("{");	
			for(j=0; j<arrComp.size(); j++){		
				//value of components			
				if(j==0)
					sResult.append("\"").append(arrVar.get(j)).append("\":{").append("\"type\": \"literal\", \"value\":\"").append(arrComp.get(j).getValue(i)).append("\"},");
				else
					sResult.append("\"").append(arrVar.get(j)).append("\":{").append("\"type\": \"uri\", \"value\":\"").append(getEndingPart(arrComp.get(j).getValueReference(i))).append("\"},");												
			}
				
			//value of dataset variable
//			sResult.append("\"").append(arrVar.get(j)).append("\":{").append("\"type\": \"uri\", \"value\": \"").append(ds.getUri()).append("\"},");			
		
			//remove the last comma of each value in a binding
			sResult.delete(sResult.length()-1, sResult.length());
			
			//add comma at the end of each binding
			sResult.append("},");			
		}

		
		//remove the last comma  of the last binding
		sResult.delete(sResult.length()-1, sResult.length());
		
		sResult = sResult.append("]").append("}").append("}");	
		
		String s = sResult.toString();
		s = s.replace("'", "");
	   	return s;    	
	}
	
	public String getListOfVariable(){
		int i;
    	String sResult="[";
    	
    	//variables for components
		for(i=0; i<arrComp.size(); i++){
			if(arrComp.get(i).getVariable()!=""){
				sResult = sResult + "{\"var\":";
				sResult = sResult + "\""+ arrComp.get(i).getVariable().substring(1) + "\"},";
			}
		}
		
		//variable for dataset		
		if(ds.getVariable()!=""){
			sResult = sResult + "{\"var\":";
			sResult = sResult + "\""+ ds.getVariable().substring(1) + "\"},";					
		}
		else{
			sResult = sResult + "{\"var\":";
			sResult = sResult + "\""+ "dataset" + "\"},";	
			
		}
		//remove the last comma of the last binding
		sResult = sResult.substring(0, sResult.length()-2);
		
		sResult = sResult +	"]";
	   	return sResult;
	}
	
	public void removeComponent(int i){
		arrComp.remove(i);
	}
	
	public void keepDistinctValues(int n){
		int i, j, k, t;
		for(i=1; i<arrComp.get(0).getValueSize(); i++){
			for(j=0; j<i; j++){				
				for(k=2; k<n+2; k++){
					if(!arrComp.get(k).getValueReference(i).equalsIgnoreCase(arrComp.get(k).getValueReference(j)))
						break;
				}
				if(k==n+2)
					break;								
			}
			if(j<i){			
				for(t=0; t<n+2; t++)
					arrComp.get(t).removeValue(i);
				i--;		
			}
		}
		
	}
	
	public void filterValue(MetaData md, int n){
		int i, j, k;	
		for(i=0; i<arrComp.get(0).getValueSize(); i++){
			for(k = 0; k<md.getComponent(0).getValueSize(); k++){
				for(j=2; j<2+n; j++){
					if(!arrComp.get(j).getValueReference(i).equalsIgnoreCase(md.getComponent(j).getValueReference(k))){
						break;
					}					
				}
				if(j==2+n)
					break;
			}	
			if(k==md.getComponent(0).getValueSize()){
				for(j=0; j<arrComp.size(); j++)
					arrComp.get(j).removeValue(i);
				i--;
			}		
		}		
	}
	
	public void queryReferenceValues(){
		int i, j;		
		String sCompUri="";
		String sQuery="";
		for(i=0; i<arrComp.size(); i++){
			sCompUri = arrComp.get(i).getUri();	
			if(!arrComp.get(i).getType().contains("Measure")){
				sQuery= "PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+					
						"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+					
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+	
						"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
						"Select distinct ?vref ?v \n" +
						"Where{ \n" +
						"	graph <http://statspace.linkedwidgets.org> { \n" +
						"		<"+ds.getUri() + "> rdf:value ?v. \n"+
						"		<"+sCompUri + "> rdf:value ?v. \n"+					
						"		?vref owl:sameAs ?v.\n"+
						" 	}\n"+
						"}";
				getReferenceValue(sQuery, i, 0);	
			}
		}
	}
}

