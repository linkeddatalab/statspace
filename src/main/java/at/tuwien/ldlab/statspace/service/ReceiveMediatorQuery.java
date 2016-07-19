/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.tuwien.ldlab.statspace.service;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import at.tuwien.ldlab.statspace.codelist.CL_Unit_Measure;
import at.tuwien.ldlab.statspace.metadata.MetaData;
import at.tuwien.ldlab.statspace.metadata.SparqlQuery;


public class ReceiveMediatorQuery extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(ReceiveMediatorQuery.class);
	
	//for request from users
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
		
		String sQuery    	= request.getParameter("query").trim();
    	String sFormat   	= request.getParameter("format");    	
    	String sCache    	= request.getParameter("cache");
    	String sProv 		= request.getParameter("provenance");
			
		if(sQuery==null){		
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        	response.addHeader("Access-Control-Allow-Origin", "*");
        	response.getWriter().println("Sorry, you need to provide a query");			
		}else{ 	
			log.info("Calling mediator service ");		
			SparqlQuery query = new SparqlQuery(sQuery);			
			if(query.getErrorStatus()){
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
	        	response.addHeader("Access-Control-Allow-Origin", "*");
	        	response.getWriter().println("Sorry, we can not analyze your input query");	
	        	return;
			}
			boolean bUseCache = true;
			boolean bGetProvenance = false;
			if(sCache!=null && sCache.toLowerCase().equals("no")){
				bUseCache = false;				
			}
			if(sProv!=null && sProv.toLowerCase().equals("yes")){
				bGetProvenance = true;				
			}
			
			MetaData inputMD = query.createMetaData();	
			if(inputMD.getNumberofComponent()==0){
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
	        	response.addHeader("Access-Control-Allow-Origin", "*");
	        	response.getWriter().println("Sorry, we can not analyze your input query. Your query should contain conditions for spatial dimension and temporal dimension");	
			}else{
				inputMD.reorderComponentsForPrettyPrint();
				String sVarObs   = query.getVarObservation();
				int i, j;
				
				//Step 1. Identify all suitable datasets with the input query
				ArrayList<MetaData> arrMetaData = inputMD.queryMetaDataByFilter();				
				
				if(arrMetaData.size()==0){
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
		        	response.addHeader("Access-Control-Allow-Origin", "*");
		        	response.getWriter().println("No dataset is suitable with your input query");	
		        	return;
				}
				
				//Step 2.  Reorder components and remove components do not appear in the input metadata
				for(i=0; i<arrMetaData.size(); i++){
					if(arrMetaData.get(i).getNumberofComponent()<inputMD.getNumberofComponent()){
						arrMetaData.remove(i);
						i--;
					}else{
						arrMetaData.get(i).reorderComponentsForPrettyPrint();
						arrMetaData.get(i).reorderComponents(inputMD);
					}
				}
				
				//Step 3.1 Rewrite & send query for each dataset
				String folderWebApp =  getServletContext().getRealPath("/");		
				String sSeparator = File.separator;				
				for(i=0; i<arrMetaData.size(); i++){
					arrMetaData.get(i).rewriteQuery(sVarObs, folderWebApp, sSeparator, true, bUseCache);	
				}		
				
				//Step 3.2. Query unit of hidden property
				for(i=0; i<arrMetaData.size(); i++){
					for(j=0; j<arrMetaData.get(i).getNumberofComponent(); j++)
						if(arrMetaData.get(i).getComponent(j).getType().contains("Attribute")){
							if(arrMetaData.get(i).getComponent(j).getValueSize()==0){
								arrMetaData.get(i).queryHiddenProperty(j);
							}
							break;
						}
				}
				
				//Step 3.3. Rewrite values of dimensions and unit	
				for(i=0; i<arrMetaData.size(); i++)
					arrMetaData.get(i).rewriteResult();		
				
				//Step 3.4. Rewrite observed values if they use different units
				Double scale;
				String unit;
				CL_Unit_Measure cl_unit = new CL_Unit_Measure();
				for(i=0; i<arrMetaData.size(); i++){
					for(j=0; j<arrMetaData.get(i).getNumberofComponent(); j++)
						if(arrMetaData.get(i).getComponent(j).getType().contains("Attribute")){
							if(arrMetaData.get(i).getComponent(j).getValueSize()>0){
								unit = arrMetaData.get(i).getComponent(j).getValue(0);
								scale = cl_unit.getScale(unit);
								if(scale != 1.0)
									arrMetaData.get(i).rewriteObservedValue(scale);
							}
							break;
						}
				}
				
				//Step 4. Integrate achieved results
				ArrayList<String> arrTemporalValue = new ArrayList<String>();
				
				//Step 4.1. Find all temporal value
				int index = inputMD.getIndexOfTemporalDimension();
				for(i=0; i<arrMetaData.size(); i++)
					arrTemporalValue = arrMetaData.get(i).getTemporalValues(index, arrTemporalValue);
				
				//Step 5. Return result
				String  sResult="", sProvenance="";	
				long lStartTime = new Date().getTime();
				
				if(sFormat==null||sFormat.toLowerCase().contains("html")){
					sResult 	= getResultHTML(inputMD, arrMetaData, arrTemporalValue);
					if(bGetProvenance)
						sProvenance = getProvenanceHTML(arrMetaData);
				}else if(sFormat.toLowerCase().contains("xml")){					
					sResult = getResultXML(inputMD, arrMetaData, arrTemporalValue).replace("&", "&amp;");				
					if(bGetProvenance) 
						sProvenance = getProvenanceXML(arrMetaData);									
				}else{
					sResult = getResultJSON(inputMD, arrMetaData, arrTemporalValue);	
					if(bGetProvenance)
						sProvenance = getProvenanceJSON(arrMetaData);		
				}	
				
				response.setContentType("text/plain");
		        response.setHeader("Content-Disposition", "attachment;filename=query.csv");
		        InputStream in;
		        if(bGetProvenance)
		        	in = new ByteArrayInputStream(("{\n\"result\":" + sResult + ",\n"
		        											+ "\"provenance\":" + sProvenance + "\n}").getBytes("UTF-8"));
		        else
		        	in = new ByteArrayInputStream(sResult.getBytes("UTF-8"));
		        
		        int length = 0;
		        byte[] byteBuffer = new byte[4096];
		        ServletOutputStream outStream = response.getOutputStream();

		        while ((in != null) && ((length = in.read(byteBuffer)) != -1))
		        {
		            outStream.write(byteBuffer,0,length);
		        }    	        
		        in.close();
		        outStream.close();		     
				long lEndTime = new Date().getTime();
				long difference = lEndTime - lStartTime;
				log.info("Elapsed milliseconds: " + difference);				
				
			}			
    	}  	
	}
	
	//for user interface
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {    	 
    	
    	String sQuery    = request.getParameter("query").trim();
    	String sFormat   = request.getParameter("format");    	
    	String sCache    = request.getParameter("cache");
			
		if(sQuery==null){			
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        	response.addHeader("Access-Control-Allow-Origin", "*");
        	response.getWriter().println("Sorry, you need to provide a query");			
		}else{ 	
			log.info("Calling mediator service ");		
			SparqlQuery query = new SparqlQuery(sQuery);			
			if(query.getErrorStatus()){
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
	        	response.addHeader("Access-Control-Allow-Origin", "*");
	        	response.getWriter().println("Sorry, we can not analyze your input query");	
	        	return;
			}
			boolean bUseCache = true;			
			if(sCache==null || !sCache.toLowerCase().equals("on")){
				bUseCache = false;
				sCache = "false";
			}else
				sCache = "true";
			
			MetaData inputMD = query.createMetaData();	
			if(inputMD.getNumberofComponent()==0){
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
	        	response.addHeader("Access-Control-Allow-Origin", "*");
	        	response.getWriter().println("Sorry, we can not analyze your input query. Your query should contain conditions for spatial dimension and temporal dimension");	
			}else{
				inputMD.reorderComponentsForPrettyPrint();
				String sVarObs   = query.getVarObservation();
				int i, j;
				
				//Step 1. Identify all suitable datasets with the input query
				ArrayList<MetaData> arrMetaData = inputMD.queryMetaDataByFilter();				
				
				if(arrMetaData.size()==0){
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
		        	response.addHeader("Access-Control-Allow-Origin", "*");
		        	response.getWriter().println("No dataset is suitable with your input query");	
		        	return;
				}
				
				//Step 2.  Reorder components and remove components do not appear in the input metadata
				for(i=0; i<arrMetaData.size(); i++){
					if(arrMetaData.get(i).getNumberofComponent()<inputMD.getNumberofComponent()){
						arrMetaData.remove(i);
						i--;
					}else{
						arrMetaData.get(i).reorderComponentsForPrettyPrint();
						arrMetaData.get(i).reorderComponents(inputMD);
					}
				}
				
				//Step 3.1 Rewrite & send query for each dataset
				String folderWebApp =  getServletContext().getRealPath("/");		
				String sSeparator = File.separator;				
				for(i=0; i<arrMetaData.size(); i++){
					arrMetaData.get(i).rewriteQuery(sVarObs, folderWebApp, sSeparator, true, bUseCache);	
				}		
				
				//Step 3.2. Query unit of hidden property
				for(i=0; i<arrMetaData.size(); i++){
					for(j=0; j<arrMetaData.get(i).getNumberofComponent(); j++)
						if(arrMetaData.get(i).getComponent(j).getType().contains("Attribute")){
							if(arrMetaData.get(i).getComponent(j).getValueSize()==0){
								arrMetaData.get(i).queryHiddenProperty(j);
							}
							break;
						}
				}
				
				//Step 3.3. Rewrite values of dimensions and unit	
				for(i=0; i<arrMetaData.size(); i++)
					arrMetaData.get(i).rewriteResult();		
				
				//Step 3.4. Rewrite observed values if they use different units
				Double scale;
				String unit;
				CL_Unit_Measure cl_unit = new CL_Unit_Measure();
				for(i=0; i<arrMetaData.size(); i++){
					for(j=0; j<arrMetaData.get(i).getNumberofComponent(); j++)
						if(arrMetaData.get(i).getComponent(j).getType().contains("Attribute")){
							if(arrMetaData.get(i).getComponent(j).getValueSize()>0){
								unit = arrMetaData.get(i).getComponent(j).getValue(0);
								scale = cl_unit.getScale(unit);
								if(scale != 1.0)
									arrMetaData.get(i).rewriteObservedValue(scale);
							}
							break;
						}
				}
				
				//Step 4. Integrate achieved results
				ArrayList<String> arrTemporalValue = new ArrayList<String>();
				
				//Step 4.1. Find all temporal value
				int index = inputMD.getIndexOfTemporalDimension();
				for(i=0; i<arrMetaData.size(); i++)
					arrTemporalValue = arrMetaData.get(i).getTemporalValues(index, arrTemporalValue);
				
				//Step 5. Return result
				String  sResult="", sProvenance="";	
				long lStartTime = new Date().getTime();
				
				if(sFormat==null||sFormat.toLowerCase().contains("html")){
					sResult 	= getResultHTML(inputMD, arrMetaData, arrTemporalValue);
					sProvenance = getProvenanceHTML(arrMetaData);
				}else if(sFormat.toLowerCase().contains("xml")){
					try{
						sResult = getResultXML(inputMD, arrMetaData, arrTemporalValue).replace("&", "&amp;");				
						sProvenance = getProvenanceXML(arrMetaData);
					}catch(Exception e){					
					}				
				}else{
					sResult = getResultJSON(inputMD, arrMetaData, arrTemporalValue);	
					sProvenance = getProvenanceJSON(arrMetaData);		
				}	
				long lEndTime = new Date().getTime();
				long difference = lEndTime - lStartTime;
				log.info("Elapsed milliseconds: " + difference);
				
				request.setAttribute("result", sResult);
				request.setAttribute("provenance", sProvenance);
				request.setAttribute("number", arrMetaData.size());
				request.setAttribute("query", sQuery);
				request.setAttribute("format", sFormat);
				request.setAttribute("cache", sCache);
				RequestDispatcher view = request.getRequestDispatcher("/mediator/index.jsp");		
				view.forward(request, response);		
			}			
    	}  	
    }				
   
    public static String getResultJSON(MetaData inputMD, ArrayList<MetaData> arrMetaData, ArrayList<String> arrTemporalValue){
    	int i, j, k, index, n, t, m;
    	ArrayList<String> arrVar = new ArrayList<String>();    	
    	ArrayList<Integer> arrIndex = new ArrayList<Integer>();
    	StringBuffer sResult = new StringBuffer();
    	String sTime;   
    	
		sResult.append("{\n").append("	\"head\":{\n").append("		\"vars\":[");	
		
		//variable for dataset		
		if(inputMD.getDataSet().getVariable()!=""){
			arrVar.add(inputMD.getDataSet().getVariable().substring(1));
			sResult.append("\"").append(arrVar.get(0)).append("\",");
		}
		else{			
			arrVar.add("?dataset");
			sResult.append("\"").append(arrVar.get(0)).append("\",");
		}		
		
		//variables for components
		for(i=0; i<inputMD.getNumberofComponent(); i++){
			if(inputMD.getComponent(i).getVariable()!=""){				
				arrVar.add(inputMD.getComponent(i).getVariable().substring(1));
				sResult.append("\"").append(arrVar.get(i+1)).append("\",");
			}
		}
	
		sResult.deleteCharAt(sResult.length()-1);
		sResult.append("]\n").append("	},\n").append("	\"results\":{\n").append("		\"bindings\":[\n");
		
		//add values
		index = inputMD.getIndexOfTemporalDimension();
		n = arrTemporalValue.size();	
		
		for(i=0; i<n; i++){	
			sTime = arrTemporalValue.get(i);
			for(j=0; j<arrMetaData.size(); j++){				
				arrIndex.clear();
				arrIndex = arrMetaData.get(j).getComponent(index).indexOf(sTime);
				if(arrIndex.size()>0){
					for(k=0; k<arrIndex.size(); k++){
						m = arrIndex.get(k);				
						sResult.append("			{\n");
						
						//value of dataset variable
						sResult.append( 
								"				\"").append(arrVar.get(0)).append("\":\"").append(arrMetaData.get(j).getDataSet().getUri()).append("\",\n");
						
						//value of other variables
						for(t=0; t<arrMetaData.get(j).getNumberofComponent(); t++){
							if(arrMetaData.get(j).getComponent(t).getValueSize()>=m && !arrMetaData.get(j).getComponent(t).getType().contains("Attribute")){
								if(arrMetaData.get(j).getComponent(t).getType().contains("Measure"))
									sResult.append( 
									"				\"").append(arrVar.get(t+1)).append("\":\"").append(arrMetaData.get(j).getComponent(t).getValue(m)).append("\",\n");
								else
									sResult.append( 
											"				\"").append(arrVar.get(t+1)).append("\":\"").append(arrMetaData.get(j).getComponent(t).getValueReference(m)).append("\",\n");
							}
							else if(arrMetaData.get(j).getComponent(t).getValueSize()==1){
								sResult.append( 
										"				\"").append(arrVar.get(t+1)).append("\":\"").append(arrMetaData.get(j).getComponent(t).getValueReference(0)).append("\",\n");								
							}else if(arrMetaData.get(j).getComponent(t).getType().contains("Attribute")){
								sResult.append( 
										"				\"").append(arrVar.get(t+1)).append("\":\"").append("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/NO").append("\",\n");								
							}
						}
					
						//remove the last comma and \n of each value in a binding
						sResult.delete(sResult.length()-2, sResult.length());
						
						//add comma at the end of each binding
						sResult.append("\n			},\n");						
					}	
				}				
			}		
		}				
		
		//remove the last comma of the last binding
		sResult.delete(sResult.length()-2, sResult.length());
		
		sResult.append("\n		]\n").append("	}\n").append("}");				
	   	return sResult.toString();    	
    }
    
    public static String getResultJSON2(MetaData inputMD, ArrayList<MetaData> arrMetaData, ArrayList<String> arrTemporalValue){
    	int i, j, k, index, n, t, m;
    	ArrayList<String> arrVar = new ArrayList<String>();    	
    	ArrayList<Integer> arrIndex = new ArrayList<Integer>();
    	StringBuffer sResult = new StringBuffer();
    	String sTime;   
    	
		sResult.append("{\n").append("	\"head\":{\n").append("		\"vars\":[");
		
		/*
		 * Note that we maybe need to return the label of dataset, component, and value
		 * Solution: 
		 *   + Do not add these variables to arrVar
		 *   + If the variable for ds, component is available, check the variable of its label
		 */
		
		//variable for dataset		
		if(inputMD.getDataSet().getVariable()!=""){
			arrVar.add(inputMD.getDataSet().getVariable().substring(1));
			sResult.append("\"").append(arrVar.get(0)).append("\",");
		}
		else{			
			arrVar.add("?dataset");
			sResult.append("\"").append(arrVar.get(0)).append("\",");
		}
		
		
		//variables for components
		for(i=0; i<inputMD.getNumberofComponent(); i++){
			if(inputMD.getComponent(i).getVariable()!=""){				
				arrVar.add(inputMD.getComponent(i).getVariable().substring(1));
				sResult.append("\"").append(arrVar.get(i+1)).append("\",");
			}
		}
	
		sResult.deleteCharAt(sResult.length()-1);
		sResult.append("]\n").append("	},\n").append("	\"results\":{\n").append("		\"bindings\":[\n");
		
		//add values
		index = inputMD.getIndexOfTemporalDimension();
		n = arrTemporalValue.size();	
		
		for(i=0; i<n; i++){	
			sTime = arrTemporalValue.get(i);
			for(j=0; j<arrMetaData.size(); j++){				
				arrIndex.clear();
				arrIndex = arrMetaData.get(j).getComponent(index).indexOf(sTime);
				if(arrIndex.size()>0){
					for(k=0; k<arrIndex.size(); k++){
						m = arrIndex.get(k);				
						sResult.append("			{\n");
						
						//value of dataset variable
						sResult.append( 
								"				\"").append(arrVar.get(0)).append("\":{").append(
								"\"type\": \"uri\", \"value\": \"").append(arrMetaData.get(j).getDataSet().getUri()).append("\"},\n");
						
	
						//value of other variables
						for(t=0; t<arrMetaData.get(j).getNumberofComponent(); t++){
							if(arrMetaData.get(j).getComponent(t).getValueSize()>=m && !arrMetaData.get(j).getComponent(t).getType().contains("Attribute")){
								if(arrMetaData.get(j).getComponent(t).getType().contains("Measure"))
									sResult.append( 
									"				\"").append(arrVar.get(t+1)).append("\":{").append(
									"\"type\": \"literal\", \"value\":\"").append(arrMetaData.get(j).getComponent(t).getValue(m)).append("\"},\n");
								else
									sResult.append( 
											"				\"").append(arrVar.get(t+1)).append("\":{").append(
											"\"type\": \"uri\", \"value\":\"").append(arrMetaData.get(j).getComponent(t).getValueReference(m)).append("\"},\n");
							}
							else if(arrMetaData.get(j).getComponent(t).getValueSize()==1){
								sResult.append( 
										"				\"").append(arrVar.get(t+1)).append("\":{").append(
										"\"type\": \"uri\", \"value\":\"").append(arrMetaData.get(j).getComponent(t).getValueReference(0)).append("\"},\n");								
							}else if(arrMetaData.get(j).getComponent(t).getType().contains("Attribute")){
								sResult.append( 
										"				\"").append(arrVar.get(t+1)).append("\":{").append(
										"\"type\": \"uri\", \"value\":\"").append("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/NO").append("\"},\n");								
							}
						}
					
						//remove the last comma and \n of each value in a binding
						sResult.delete(sResult.length()-2, sResult.length());
						
						//add comma at the end of each binding
						sResult.append("\n			},\n");						
					}	
				}				
			}		
		}				
		
		//remove the last comma of the last binding
		sResult.delete(sResult.length()-2, sResult.length());
		
		sResult.append("\n		]\n").append("	}\n").append("}");				
	   	return sResult.toString();    	
    }
    
    public static String getResultXML(MetaData inputMD, ArrayList<MetaData> arrMetaData, ArrayList<String> arrTemporalValue){
    	int i, j, k, index, n, t, m;    	
    	ArrayList<String> arrVar = new ArrayList<String>();
    	ArrayList<Integer> arrIndex = new ArrayList<Integer>();
    	StringBuffer sResult = new StringBuffer();
    	String sTime;
    	
    	sResult.append("<?xml version='1.0' encoding='UTF-8'?>\n"+
   			 "<sparql xmlns='http://www.w3.org/2005/sparql-results#'>\n"+
   			 "	<head>\n");
    
		//variable for dataset
    	if(inputMD.getDataSet().getVariable()!=""){	
			arrVar.add(inputMD.getDataSet().getVariable().substring(1));
			sResult.append("		<variable name='").append(arrVar.get(0)).append("'/>\n");				
    	}
		else{			
			arrVar.add("dataset");
			sResult.append("		<variable name='").append(arrVar.get(0)).append("'/>\n");
		}
		
		//variables for components
		for(i=0; i<inputMD.getNumberofComponent(); i++){
			if(inputMD.getComponent(i).getVariable()!=""){
				arrVar.add(inputMD.getComponent(i).getVariable().substring(1));			
				sResult.append("		<variable name='").append(arrVar.get(i+1)).append("'/>\n");
			}
		}		
		
		sResult.append(
			"	</head>\n").append(
			 "	<results>\n");				
		
		
		//add values
		index = inputMD.getIndexOfTemporalDimension();
		n = arrTemporalValue.size();	
		
		for(i=0; i<n; i++){	
			sTime = arrTemporalValue.get(i);
			for(j=0; j<arrMetaData.size(); j++){				
				arrIndex.clear();
				arrIndex = arrMetaData.get(j).getComponent(index).indexOf(sTime);
				if(arrIndex.size()>0){
					for(k=0; k<arrIndex.size(); k++){
						m = arrIndex.get(k);
					
						sResult.append(
								"		<result>\n");
						
						//value of dataset variable
						sResult.append( 
								"			<binding name='").append(arrVar.get(0)).append("'>\n").append(
								"				<uri>").append(arrMetaData.get(j).getDataSet().getUri()).append("</uri>\n").append(
								"			</binding>\n");
		
						//value of other variables
						for(t=0; t<arrMetaData.get(j).getNumberofComponent(); t++){
							if(arrMetaData.get(j).getComponent(t).getValueSize()>=m && !arrMetaData.get(j).getComponent(t).getType().contains("Attribute")){
								if(arrMetaData.get(j).getComponent(t).getType().contains("Measure"))
									sResult.append(
									"			<binding name='").append(arrVar.get(t+1)).append("'>\n").append(
									"				<literal datatype='http://www.w3.org/2001/XMLSchema#decimal'>").append(arrMetaData.get(j).getComponent(t).getValue(m)).append("</literal>\n").append(
									"			</binding>\n");
								else
									sResult.append( 
									"			<binding name='").append(arrVar.get(t+1)).append("'>\n").append(
									"				<uri>").append(arrMetaData.get(j).getComponent(t).getValueReference(m)).append("</uri>\n").append(
									"			</binding>\n");
							}
							else if( arrMetaData.get(j).getComponent(t).getValueSize()==1){
								sResult.append( 
										"			<binding name='").append(arrVar.get(t+1)).append("'>\n").append(
										"				<uri>").append(arrMetaData.get(j).getComponent(t).getValueReference(0)).append("</uri>\n").append(
										"			</binding>\n");
							}else if(arrMetaData.get(j).getComponent(t).getType().contains("Attribute")){
								sResult.append( 
										"			<binding name='").append(arrVar.get(t+1)).append("'>\n").append(
										"				<uri>").append("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/NO").append("</uri>\n").append(
										"			</binding>\n");																	
							}				
						}					
							
						sResult.append(
								"		</result>\n");
					}
				}
			}		
		}		
		
		sResult.append(
			"	</results>\n").append(
				"</sparql>");	
		return sResult.toString();
    }
    
    public static String getResultHTML(MetaData inputMD, ArrayList<MetaData> arrMetaData, ArrayList<String> arrTemporalValue){
    	int i, j, k, index, n, t, m;
    	ArrayList<String> arrVar = new ArrayList<String>();
    	ArrayList<Integer> arrIndex = new ArrayList<Integer>();
    	StringBuffer sResult = new StringBuffer();
    	String sTime;
		sResult.append("<table>\n").append(
				"	<thead>\n").append(
				"		<tr>\n");		
		//variable for dataset
		if(inputMD.getDataSet().getVariable()!=""){
			arrVar.add(inputMD.getDataSet().getVariable().substring(1));
			sResult.append(
					  "			<th>").append(arrVar.get(0)).append("</th>\n");	
		}else{			
			arrVar.add("dataset");
			sResult.append(
					  "			<th>").append(arrVar.get(0)).append("</th>\n");		
		}	
		
		//variables for components
		for(i=0; i<inputMD.getNumberofComponent(); i++){
			if(inputMD.getComponent(i).getVariable()!=""){				
				arrVar.add(inputMD.getComponent(i).getVariable().substring(1));
				sResult.append(
						  "			<th>").append(arrVar.get(i+1)).append("</th>\n");
			}
		}	
		
		sResult.append(
				  "		</tr>\n").append(
				  "	</thead>\n").append(
				  "	<tbody>\n");	
		
		//add values
		index = inputMD.getIndexOfTemporalDimension();
		n = arrTemporalValue.size();	
		
		for(i=0; i<n; i++){	
			sTime = arrTemporalValue.get(i);
			for(j=0; j<arrMetaData.size(); j++){
				arrIndex.clear();
				arrIndex = arrMetaData.get(j).getComponent(index).indexOf(sTime);
				if(arrIndex.size()>0){
					for(k=0; k<arrIndex.size(); k++){
						m = arrIndex.get(k);
						sResult.append(
								"	<tr class='ds").append(j%16).append("'>\n");
						
						//value of dataset variable
						sResult.append( 
								"		<td>").append(arrMetaData.get(j).getDataSet().getUri()).append("</td>\n");					
		
						//value of other variables
						for(t=0; t<arrMetaData.get(j).getNumberofComponent(); t++){
								if(arrMetaData.get(j).getComponent(t).getValueSize()>=m && !arrMetaData.get(j).getComponent(t).getType().contains("Attribute")){
									if(arrMetaData.get(j).getComponent(t).getType().contains("Measure"))
										sResult.append( 
												  "		<td>").append(arrMetaData.get(j).getComponent(t).getValue(m)).append("</td>\n");
									else
										sResult.append( 
												 "		<td>").append(arrMetaData.get(j).getComponent(t).getValueReference(m)).append("</td>\n");
								}
								else if(arrMetaData.get(j).getComponent(t).getValueSize()==1){
									sResult.append( 
											  "		<td>").append(arrMetaData.get(j).getComponent(t).getValueReference(0)).append("</td>\n");								
								}else if(arrMetaData.get(j).getComponent(t).getType().contains("Attribute")){
									sResult.append( 
											  "		<td>").append("http://statspace.linkedwidgets.org/codelist/cl_unitMeasure/NO").append("</td>\n");	
								}

						}				
						sResult.append(
								"	</td>\n");
					}
				}				
			}
		}		
		
		sResult.append(
				"	</tbody>\n").append(
				"</table>");
								
	   	return sResult.toString(); 
    }    
    
    public static String getProvenanceJSON(ArrayList<MetaData> arrMetaData){
    	int i, j, k, n;    	    	
    	StringBuffer arrDistintiveUri;    	
    	StringBuffer sProvenance = new StringBuffer();
    	String sUri, sRefUri, sDSUri;    	
    	
		sProvenance.append("{\n").append(
						   "	\"head\":{\n").append(
						   "		\"var\":[\n").append(
						   " 			\"Co-reference URI used in the repository\",\n").append(						   		   
						   " 			\"URI used in the dataset\",\n").append(						   
						   " 			\"Dataset\"\n").append(
						   "		]\n").append(
						   " 	},\n").append(
						   " 	\"results\":{\n").append(
						   " 		\"bindings\":[\n");
		n = arrMetaData.get(0).getNumberofComponent();
		for(i=0; i<arrMetaData.size(); i++){
			sDSUri = arrMetaData.get(i).getDataSet().getUri();
			for(j=0; j<n; j++){				
				if(arrMetaData.get(i).getComponent(j).getType().contains("Measure")) continue; //ignore measure component
				arrDistintiveUri = new StringBuffer();
				sUri = arrMetaData.get(i).getComponent(j).getUri();
				sRefUri = arrMetaData.get(i).getComponent(j).getUriReference();				
				sProvenance.append("		{\n")
						   .append(" 			\"Co-reference URI used in the repository\":")	
						   .append("\"").append(sRefUri).append("\",\n")
					       .append(" 			\"URI used in the dataset\":")						
					       .append("\"").append(sUri).append("\",\n")
						   .append(" 			\"Dataset\":")						  
						   .append("\"").append(sDSUri).append("\"\n")
						   .append("		},\n");
				for(k=0; k<arrMetaData.get(i).getComponent(j).getValueSize(); k++){
					sUri = arrMetaData.get(i).getComponent(j).getValue(k);
					sRefUri = arrMetaData.get(i).getComponent(j).getValueReference(k);
					if(arrDistintiveUri.indexOf(sUri)==-1){
						arrDistintiveUri.append(sUri+";");
						sProvenance.append("		{\n")
						 		   .append(" 			\"Co-reference URI used in the repository\":")	
						 		   .append("\"").append(sRefUri).append("\",\n")
								   .append(" 			\"URI used in the dataset\":")									  
								   .append("\"").append(sDSUri).append("\",\n")
								   .append(" 			\"Dataset\":")			
								   .append("\"").append(sDSUri).append("\"\n")
								   .append("		},\n");
					}
				}
			}
		}
		sProvenance.delete(sProvenance.length()-2, sProvenance.length());		
		sProvenance.append("\n		]\n").append("	}\n").append("}");									
	   	return sProvenance.toString(); 
    }
    
    public static String getProvenanceXML(ArrayList<MetaData> arrMetaData){
    	int i, j, k, n;    	    	
    	StringBuffer arrDistintiveUri;    	
    	StringBuffer sProvenance = new StringBuffer();
    	String sUri, sRefUri, sDSUri;    	
    	
    	sProvenance.append("<?xml version='1.0' encoding='UTF-8'?>\n"+
      			 "<sparql xmlns='http://www.w3.org/2005/sparql-results#'>\n"+
      			 "	<head>\n");   	
		sProvenance.append("		<variable name=").append("\"Co-reference URI used in the repository\"/>\n")
				   .append("		<variable name=").append("\"URI used in the dataset\"/>\n")
				   .append("		<variable name=").append("\"Dataset\"/>\n")
				   .append(" 	</head>\n");
		
		sProvenance.append("	<results>\n");   	
		
		n = arrMetaData.get(0).getNumberofComponent();
		for(i=0; i<arrMetaData.size(); i++){
			sDSUri = arrMetaData.get(i).getDataSet().getUri();
			for(j=0; j<n; j++){				
				if(arrMetaData.get(i).getComponent(j).getType().contains("Measure")) continue; //ignore measure component
				arrDistintiveUri = new StringBuffer();
				sUri = arrMetaData.get(i).getComponent(j).getUri();
				sRefUri = arrMetaData.get(i).getComponent(j).getUriReference();				
				sProvenance.append("		<result>\n")
						   .append("			<binding name=\"Co-reference URI used in the repository\">\n")
						   .append(" 		   		<uri>")
						   .append(sRefUri)
						   .append("</uri>\n")
						   .append("			</binding>\n")
						   .append(" 			<binding name=\"URI used in the dataset\">\n")
						   .append("				<uri>")					       						
						   .append(sUri)
						   .append("</uri>\n")
						   .append("			</binding>\n")
						   .append(" 			<binding name=\"Dataset\">\n")					
						   .append(" 		   		<uri>")
						   .append(sDSUri)
						   .append("</uri>\n")
						   .append("			</binding>\n")
						   .append("		</result>\n");
				for(k=0; k<arrMetaData.get(i).getComponent(j).getValueSize(); k++){
					sUri = arrMetaData.get(i).getComponent(j).getValue(k);
					sRefUri = arrMetaData.get(i).getComponent(j).getValueReference(k);
					if(arrDistintiveUri.indexOf(sUri)==-1){
						arrDistintiveUri.append(sUri+";");
						sProvenance.append("		<result>\n")
								   .append("			<binding name=\"Co-reference URI used in the repository\">\n")
								   .append(" 		   		<uri>")
								   .append(sRefUri)
								   .append("</uri>\n")
								   .append("			</binding>\n")
								   .append(" 			<binding name=\"URI used in the dataset\">\n")
								   .append("				<uri>")					       						
								   .append(sUri)
								   .append("</uri>\n")
								   .append("			</binding>\n")
								   .append(" 			<binding name=\"Dataset\">\n")					
								   .append(" 		   		<uri>")
								   .append(sDSUri)
								   .append("</uri>\n")
								   .append("			</binding>\n")
								   .append("		</result>\n");
					}
				}
			}
		}
		sProvenance.append("	</results>\n").append("</sparql>");								
	   	return sProvenance.toString(); 
    }
    
    public static String getProvenanceHTML(ArrayList<MetaData> arrMetaData){
    	int i, j, k, n;    	    	
    	StringBuffer arrDistintiveUri;    	
    	StringBuffer sProvenance = new StringBuffer();
    	String sUri, sRefUri, sDSUri;
		sProvenance.append("<table>\n").append(
						   "	<thead>\n").append(
						   "		<tr>\n").append(
						   " 			<th>Co-reference URI used in the repository</td>").append(
						   "			</th>").append(		   
						   " 			<th>URI used in the dataset</td>").append(
						   "			</th>").append(
						   " 			<th>Dataset</td>").append(
						   "		</tr>");		
		n = arrMetaData.get(0).getNumberofComponent();
		for(i=0; i<arrMetaData.size(); i++){
			sDSUri = arrMetaData.get(i).getDataSet().getUri();
			for(j=0; j<n; j++){				
				if(arrMetaData.get(i).getComponent(j).getType().contains("Measure")) continue; //ignore measure component
				arrDistintiveUri = new StringBuffer();
				sUri = arrMetaData.get(i).getComponent(j).getUri();
				sRefUri = arrMetaData.get(i).getComponent(j).getUriReference();				
				sProvenance.append("	<tr class='ds").append(i%16).append("'>\n")
						   .append("			<td>")
						   .append(sRefUri)
						   .append("			</td>\n")
						   .append("			<td>")
						   .append(sUri)
						   .append("			</td>\n")
						   .append("			<td>")
						   .append(sDSUri)
						   .append("			</td>\n");
				for(k=0; k<arrMetaData.get(i).getComponent(j).getValueSize(); k++){
					sUri = arrMetaData.get(i).getComponent(j).getValue(k);
					sRefUri = arrMetaData.get(i).getComponent(j).getValueReference(k);
					if(arrDistintiveUri.indexOf(sUri)==-1){
						arrDistintiveUri.append(sUri+";");
						sProvenance.append("	<tr class='ds").append(i%16).append("'>\n")
								   .append("			<td>")
								   .append(sRefUri)
								   .append("			</td>\n")
								   .append("			<td>")
								   .append(sUri)
								   .append("			</td>\n")
								   .append("			<td>")
								   .append(sDSUri)
								   .append("			</td>\n");
					}
				}
			}
		}	
		
		sProvenance.append(
				"	</tbody>\n").append(
				"</table>");
								
	   	return sProvenance.toString(); 
    }
}
