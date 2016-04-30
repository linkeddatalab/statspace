package tuwien.ldlab.statspace.model.mediator;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import tuwien.ldlab.statspace.controller.mediator.ReceiveMediatorQuery;

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
	private ArrayList<Component> arrComp;
	private static Log log = LogFactory.getLog(MetaData.class);
	
	public MetaData(){
		sUri="";
		ds = new DataSet();
		arrComp = new ArrayList<Component>();
	}
	
	public MetaData(MetaData md){
		sUri="";
		ds = new DataSet();
		arrComp = new ArrayList<Component>();
		int i;
		for(i=0; i<md.getNumberofComponent(); i++){
			arrComp.add(new Component(md.getComponent(i)));			
		}				
	}
	
	
	public void setUri(String s){sUri=s;}
	public void addComponent(Component p){
		int i;
		for(i=0; i<arrComp.size(); i++)
			if(arrComp.get(i).getUri()!="" && arrComp.get(i).getUri().equalsIgnoreCase(p.getUri()))
				break;
		if(i==arrComp.size())
			arrComp.add(p);
	}
	public void setDataSet(DataSet d){
		ds.setEndpoint(d.getEndpoint());
		ds.setLabel(d.getLabel());
		ds.setMethod(d.getMethod());
		ds.setRML(d.getRML());
		ds.setSubject(d.getSubject());
		ds.setUri(d.getUri());	
		ds.setVariable(d.getVariable());
		ds.setVariableLabel(d.getVariableLabel());
	}
	
	public String getUri(){return sUri;}
	public DataSet getDataSet(){return ds;}
	public int getNumberofComponent(){return arrComp.size();}
	public Component getComponent(int i){return arrComp.get(i);}
	
//	public void display(){
//		ds.display();
//		for(int i=0; i<arrComp.size(); i++){
//			System.out.println();
//			arrComp.get(i).display();
//		}
//	}
	
	public ArrayList<MetaData> searchMetaData() {		
		int i, n;
		
		String sQuery=			"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+							
								"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
								"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+								
								"PREFIX dcterms: <http://purl.org/dc/terms/> \n"+
								"PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n"+
								"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"+
								"PREFIX dcat: <http://www.w3.org/ns/dcat#> \n"+		
								"PREFIX void: <http://rdfs.org/ns/void#> \n"+		
								"PREFIX sdterms: <http://statspace.linkedwidgets.org/terms/> \n"+				
								"Select Distinct * \n" +
								"Where{ \n" +
								"	?md qb:dataSet ?ds. \n"+
								"	?ds dcterms:subject ?dss. \n"+
								"	optional{?ds rdfs:label ?dsl.} \n"+
								"	?ds void:feature ?dsm. \n"+
								"	?ds dcat:accessURL ?dsr. \n";			
		//dataset
		if(ds.getSubject()!="")
			sQuery = sQuery +  "	FILTER(?dss=<"+ds.getSubject() + ">). \n";
		
		if(ds.getUri()!="")
			sQuery =           "   FILTER(?ds=<"+ds.getUri() + ">). \n";
		
		n=0;
		for(i=0; i<arrComp.size(); i++)
			if(arrComp.get(i).getFilterValue()!=""){
				sQuery = sQuery + "		{ \n" +
								  "  		?ds rdf:value ?v_"+ n + ". \n"+
								  "  		?cp_"+ n + " rdf:value ?v_"+ n +". \n" +
								  "  		<"+arrComp.get(i).getFilterValue()+"> owl:sameAs ?v_"+ n +". \n"+
								  "		}\n "    +
								  "		UNION \n"+
								  "		{ \n" +
								  "  		?ds rdf:value ?v_"+ n + ". \n"+
								  "  		?cp_"+ n + " rdf:value ?v_"+ n +". \n" +
								  "  		FILTER(?v_"+n+"= <"+arrComp.get(i).getFilterValue()+">) \n"+
								  "		}";
				n++;
			}
		 
		//component
		sQuery = sQuery + 	  	 "	?md qb:component ?cp.\n" +							   
								 "	?cp rdf:type ?t. \n" +		
								 "	optional{?cpr owl:sameAs ?cp.} \n" +
								 "	optional{?cp rdfs:label ?cpl.} \n" +						   
						   	   	"}";
		
        log.info(sQuery);	
		return queryMetaData(sQuery, n);		
	}
	
	public ArrayList<MetaData> queryMetaData(String sQuery, int n) throws QueryParseException {		
		String sMDUri, sDSUri, sDSLabel, sDSSubject, sDSMethod, sDSEndpoint, sDSRml, sC, sCValue, sCUri, sCRef, sCType, sCLabel, sCFixedValue;
		ArrayList<MetaData> arrMetaData = new ArrayList<MetaData>();
		ArrayList<StringTriple> arrCV = new ArrayList<StringTriple>();
		//String sEndpoint="http://ogd.ifs.tuwien.ac.at/sparql";
		String sEndpoint="http://localhost:8090/sparql";		
		int i, j, k;
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
				sDSSubject 	= sol.get("dss").toString().replace("\n", "").replace("\r", "").trim();
				sDSMethod 	= sol.get("dsm").toString().replace("\n", "").replace("\r", "").trim();
				
				if(sol.contains("dsl"))	sDSLabel 	= sol.get("dsl").toString().replace("\n", "").replace("\r", "").trim();
				else					sDSLabel	= "";
				if(sol.contains("dse"))	sDSEndpoint = sol.get("dse").toString().replace("\n", "").replace("\r", "").trim();
				else					sDSEndpoint = "";
				if(sol.contains("dsr"))	sDSRml = sol.get("dsr").toString().replace("\n", "").replace("\r", "").trim();
				else					sDSRml = "";
								
				sCUri  = sol.get("cp").toString().replace("\n", "").replace("\r", "").trim();
				sCType = sol.get("t").toString().replace("\n", "").replace("\r", "").trim();
				if(sol.contains("cpr"))	sCRef = sol.get("cpr").toString().replace("\n", "").replace("\r", "").trim();
				else					sCRef = "";				
				if(sol.contains("cpl"))	sCLabel = sol.get("cpl").toString().replace("\n", "").replace("\r", "").trim();
				else					sCLabel = "";				
				if(sol.contains("cpv"))	sCFixedValue = sol.get("cpv").toString().replace("\n", "").replace("\r", "").trim();
				else					sCFixedValue = "";	
				
				//add triples Dataset - Component - Local value
				for(i=0; i<n; i++){
					sC = sol.get("cp_"+i).toString().replace("\n", "").replace("\r", "").trim();
					sCValue =  sol.get("v_"+i).toString().replace("\n", "").replace("\r", "").trim();
					for(j=0; j<arrCV.size(); j++)
						if(arrCV.get(j).getFirstString().equalsIgnoreCase(sDSUri) && arrCV.get(j).getSecondString().equalsIgnoreCase(sCFixedValue))
							break;
					if(j==arrCV.size())
						arrCV.add(new StringTriple(sDSUri, sC, sCValue));
				}
					
				
				for(i=0; i<arrMetaData.size(); i++)
					if(arrMetaData.get(i).getUri().equalsIgnoreCase(sMDUri))
						break;
				if(i==arrMetaData.size()){
					arrMetaData.add(new MetaData());
					arrMetaData.get(i).setUri(sMDUri);
					arrMetaData.get(i).setDataSet(new DataSet(sDSUri, sDSLabel, sDSSubject, sDSMethod, sDSEndpoint, sDSRml, ds.getVariable(), ds.getVariableLabel()));
				}
				arrMetaData.get(i).addComponent(new Component(sCUri, sCType, sCLabel, sCFixedValue, sCRef));				
			}		
		}catch(Exception e){			
		}
		finally {
			if(queryExecution!=null){
				queryExecution.close();
				
				//compare components of each result with components of this metadata (metadata of the query)
				for(i=0; i<arrMetaData.size(); i++){
					for(j=0; j<arrMetaData.get(i).getNumberofComponent(); j++){					
						
						// Dimensions which are not available in the input metadata must have fixed value
						if(arrMetaData.get(i).getComponent(j).getFixedValue()!="")
							continue;
						
						//Other dimensions - must appear in the input metadata
						for(k=0; k<arrComp.size(); k++){							
							if(arrMetaData.get(i).getComponent(j).getUri().equalsIgnoreCase(arrComp.get(k).getUri())||
									arrMetaData.get(i).getComponent(j).getUriReference().equalsIgnoreCase(arrComp.get(k).getUri())){
								arrMetaData.get(i).getComponent(j).setVariable(arrComp.get(k).getVariable());
								arrMetaData.get(i).getComponent(j).setVariableLabel(arrComp.get(k).getVariableLabel());
								break;
							}
							//Attribute - ignore
							if(arrMetaData.get(i).getComponent(j).getType().contains("Attribute")){
								arrMetaData.get(i).getComponent(j).setVariable("?unit");								
								break;
							}
						}
						if(k==arrComp.size()){							
							arrMetaData.remove(i);
							i--;
							break;
						}
					}
				}	
				
				//add local value filter to each component
				for(i=0; i<arrMetaData.size(); i++){
					for(j=0; j<arrMetaData.get(i).getNumberofComponent(); j++){
						for(k=0; k<arrCV.size(); k++){	
							if(arrCV.get(k).getFirstString().equalsIgnoreCase(arrMetaData.get(i).getDataSet().getUri()) &&
									(arrCV.get(k).getSecondString().equalsIgnoreCase(arrMetaData.get(i).getComponent(j).getUriReference())||
									arrCV.get(k).getSecondString().equalsIgnoreCase(arrMetaData.get(i).getComponent(j).getUri())))
								arrMetaData.get(i).getComponent(j).setFilterValue(arrCV.get(k).getThirdString());
						}
					}
				}
				return arrMetaData;
			}		
		}
		return null;
	}
	
	public void rewriteQuery(String sVarObs){
		String sRDFQuery="", sRMLQuery="";
		int i;		
		
		sRDFQuery=	"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+					
					"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+					
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+				
					"Select Distinct * \n" +
					"Where{ \n" +
						"	"+ sVarObs + " qb:dataSet " + ds.getVariable() + ". \n";						
		
		//FILTER based on fixed values of components which are not available in the query
		for(i=0; i<arrComp.size(); i++){
			if(arrComp.get(i).getVariable()=="")
				sRDFQuery = sRDFQuery + "	"+
						sVarObs + " <"+ arrComp.get(i).getUri()+ "> <"+arrComp.get(i).getFixedValue() + "> . \n";
			else 
				if(arrComp.get(i).getType().contains("Attribute"))
					sRDFQuery = sRDFQuery +
						"	optional{"+	sVarObs + " <"+ arrComp.get(i).getUri()+ "> "+arrComp.get(i).getVariable() + "}. \n";
				else
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
		
			
		if(ds.getMethod().equalsIgnoreCase("SPARQL")){	
			sRDFQuery = sRDFQuery + "	FILTER("+ds.getVariable()+"=<"+ds.getUri()+">) \n";
			sRDFQuery = sRDFQuery + "}";
			
			//find refPeriod component to order values
			for(i=0; i<arrComp.size();i++){
				if(arrComp.get(i).getUri().contains("refPeriod")||arrComp.get(i).getUriReference().contains("refPeriod"))
					if(arrComp.get(i).getVariable()!="")
						sRDFQuery = sRDFQuery + "Order by "+ arrComp.get(i).getVariable();
			}
			
			String sEndpoint = ds.getEndpoint();
			queryBySPARQL(sRDFQuery, sEndpoint);			
		}else if(ds.getMethod().equalsIgnoreCase("API")){
			sRDFQuery = sRDFQuery + "}";
			
			//find refPeriod component to order values
			for(i=0; i<arrComp.size();i++){
				if(arrComp.get(i).getUri().contains("refPeriod")||arrComp.get(i).getUriReference().contains("refPeriod"))
					if(arrComp.get(i).getVariable()!="")
						sRDFQuery = sRDFQuery + "Order by "+ arrComp.get(i).getVariable();
			}
			
			sRMLQuery = ds.getRML();
			if(ds.getSubject()!="")
				sRMLQuery = sRMLQuery + "&subject="+ds.getSubject();
			
			//FILTER based on the input query			
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
			
			sRMLQuery = ds.getRML();
			queryByRML(sRDFQuery, sRMLQuery);
		}
	}
	
	public void queryBySPARQL(String sRDFQuery, String sEndpoint)throws QueryParseException{
//		System.out.println("--------");
//		System.out.println(sRDFQuery);
//		System.out.println(sEndpoint);
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
//		System.out.println("--------");
//		System.out.println(sRDFQuery);
//		System.out.println(sRMLQuery);
		
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

	public void rewriteResult() {
		int i;		
		String sCompUri="";
		String sQuery="";
		for(i=0; i<arrComp.size(); i++){
			sCompUri = arrComp.get(i).getUri();	
			if(arrComp.get(i).getType().contains("Dimension")){
				sQuery= "PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+						
						"PREFIX map:  <http://linkedwidgets.org/statisticalwidgets/mapping/> \n"+	
						"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
						"PREFIX dc:   <http://purl.org/dc/elements/1.1/> \n"+
						"Select DISTINCT ?v ?vref \n" +
						"Where{ \n" +
						"<"+ds.getUri() + "> rdf:value ?v. \n"+
						"<"+sCompUri + "> rdf:value ?v. \n"+					
						"?vref owl:SameAs ?v.\n"+
						"}";
				queryReferenceValue(sQuery, i, 0);			
			}
			else if(arrComp.get(i).getType().contains("Attribute")){
				sQuery= "PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+						
						"PREFIX map:  <http://linkedwidgets.org/statisticalwidgets/mapping/> \n"+	
						"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
						"PREFIX dc:   <http://purl.org/dc/elements/1.1/> \n"+
						"Select DISTINCT ?v ?vref \n" +
						"Where{ \n" +
						"<"+ds.getUri() + "> rdf:value ?v. \n"+
						"<"+sCompUri + "> rdf:value ?v. \n"+					
						"optional{?vref owl:sameAs ?v.}\n"+
						"}";
				queryReferenceValue(sQuery, i, 1);			
			}
				
			
		}	
		
	}
	
	public void queryReferenceValue(String sQuery, int index, int type)throws QueryParseException{
//		String sEndpoint="http://ogd.ifs.tuwien.ac.at/sparql";		
		String sEndpoint="http://localhost:8090/sparql";		
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
						if(arrComp.get(index).getValue(i).equalsIgnoreCase(sValue))
							arrComp.get(index).setValueRefence(i, sRefValue);
				}else{
					arrComp.get(index).addValue(sValue, sRefValue);
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
	
//	public void integrateData(MetaData metaData) {	
//		int i, j, k, m, n, t;
//		String sValueRef, sValueRef2;
//		String sUriRef, sUriRef2;
//		for(i=0; i<arrComp.size(); i++){
//			if(arrComp.get(i).getType().contains("Dimension")){
//				sUriRef = arrComp.get(i).getUriReference();
//				if(sUriRef.equals(""))
//					sUriRef = arrComp.get(i).getUri();
//				
//				//find the equivalent dimension in metaData variable
//				for(k=0; k<metaData.getNumberofComponent(); k++){
//					sUriRef2 =metaData.getComponent(k).getUriReference();
//					if(sUriRef2.equals(""))
//						sUriRef2 = metaData.getComponent(k).getUri();
//					if(sUriRef.equals(sUriRef2))
//						break;
//				}
//				
//				//not found
//				if(k==metaData.getNumberofComponent()){
//					//remove this component
//					arrComp.remove(i);
//					i--;					
//				}else{
//					//check the duplicate values of two metadata									
//					for(j=0; j<arrComp.get(i).getValueSize(); j++){
//						sValueRef = arrComp.get(i).getValueReference(j);						
//						
//						//check if this value is available in other metaData
//						for(m=0; m<metaData.getComponent(k).getValueSize(); m++){
//							sValueRef2 = metaData.getComponent(k).getValueReference(m);							
//							if(sValueRef.equals(sValueRef2))
//								break;								
//						}
//						//if not fould, remove this value
//						if(m==metaData.getComponent(k).getValueSize()){
//							for(n=0; n<arrComp.size(); n++)
//								if(!arrComp.get(n).getType().contains("Attribute") && arrComp.get(n).getValueSize()>j)
//									arrComp.get(n).removeValue(j);	
//							j--;
//						}							
//					}
//				}			
//			}
//		}
//				
//	}
	
	public void removeExternalComponent(MetaData metaData) {	
		int i, k;		
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
			}
		}
	}

	//i: index of observation in arrComp
	//j: index of observation in result
	public boolean hasSameValue(int i, MetaData metaData, int j){
		int k;
		for(k=0; k<arrComp.size(); k++)
			if(arrComp.get(k).getType().contains("Dimension")){
				if(!arrComp.get(k).getValueReference(i).equalsIgnoreCase(metaData.getComponent(k).getValueReference(j)))
					return false;
			}
		return true;
	}
	
	public void rewriteObservedValue(Double multi) {
		int i, j, k;
		String sValue;
		Double value;
		DecimalFormat df = new DecimalFormat("#.0"); 
		for(i=0; i<arrComp.size(); i++)
			if(arrComp.get(i).getType().contains("Measure"))
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
}

