/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tuwien.ldlab.statspace.controller.mediator;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import tuwien.ldlab.statspace.codelist.CL_Unit_Measure;
import tuwien.ldlab.statspace.model.mediator.MetaData;
import tuwien.ldlab.statspace.model.mediator.SparqlQuery;


public class ReceiveMediatorQuery extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(ReceiveMediatorQuery.class);

	public ReceiveMediatorQuery() {		
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
		doGet(request, response);
	}
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {    	 
    	
    	String sQuery    = request.getParameter("query");
    	String sFormat   = request.getParameter("format");
		
		if(sQuery==null){			
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        	response.addHeader("Access-Control-Allow-Origin", "*");
        	response.getWriter().println("No result");			
		}else{ 			
			SparqlQuery query = new SparqlQuery(sQuery);			
			if(query.getErrorStatus()){
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
	        	response.addHeader("Access-Control-Allow-Origin", "*");
	        	response.getWriter().println("Can not analyze the input query!!!");	
	        	return;
			}
			
			MetaData inputMD = query.createMetaData();			
			String sVarObs   = query.getVarObservation();
			int i, j;
			
			//Step 1. Identify all suitable datasets with the input query
			ArrayList<MetaData> arrMetaData = inputMD.searchMetaData();				
			
			if(arrMetaData.size()==0){
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
	        	response.addHeader("Access-Control-Allow-Origin", "*");
	        	response.getWriter().println("No result");	
	        	return;
			}
			
			//Step 2. Rewrite & send query for each dataset
			for(i=0; i<arrMetaData.size(); i++){
				arrMetaData.get(i).rewriteQuery(sVarObs);	
			}				
			
			//Step 3.   Rewrite results
			//Step 3.1. Remove components do not appear in the input metadata
			for(i=0; i<arrMetaData.size(); i++)
				arrMetaData.get(i).removeExternalComponents(inputMD);
				
			
			//Step 3.2. Rewrite values of dimensions and unit		
			for(i=0; i<arrMetaData.size(); i++)
				arrMetaData.get(i).rewriteResult();		
			
			//Step 3.3. Rewrite observed values if they use different units
			Double scale;
			String unit;
			CL_Unit_Measure cl_unit = new CL_Unit_Measure();
			for(i=0; i<arrMetaData.size(); i++){
				for(j=0; j<arrMetaData.get(i).getNumberofComponent(); j++)
					if(arrMetaData.get(i).getComponent(j).getType().contains("Attribute")){
						if(arrMetaData.get(i).getComponent(j).getValueSize()>0){
							unit = arrMetaData.get(i).getComponent(j).getValue(0);
							scale = cl_unit.getScale(unit);
							arrMetaData.get(i).rewriteObservedValue(scale);
						}
						break;
					}
			}
			
			//Step 3.4. Rewrite values of refPeriod e.g., /date/1960-01-01 => /year/1960
//			for(i=0; i<arrMetaData.size(); i++)
//				arrMetaData.get(i).rewriteTemporalValue();

			//Step 4. Integrate achieved results
			ArrayList<String> arrTemporalValue = new ArrayList<String>();
			
			//Step 4.1. Find all temporal value
			int index = inputMD.getIndexOfTemporalDimension();
			for(i=0; i<arrMetaData.size(); i++)
				arrTemporalValue = arrMetaData.get(i).getTemporalValues(index, arrTemporalValue);
			
			//Step 5. Return result
			String  sResult;		
			
			if(sFormat==null||sFormat.toLowerCase().contains("json")){
				sResult = getJsonFormat(inputMD, arrMetaData, arrTemporalValue);
				response.setContentType("application/json");
			}else if(sFormat.toLowerCase().contains("xml")){
				sResult = getXmlFormat(inputMD, arrMetaData, arrTemporalValue);				
				response.setContentType("text/xml");
			}else{
				sResult = getHtmlFormat(inputMD, arrMetaData, arrTemporalValue);				
				response.setContentType("text");
			}	
			
			/******************************
			 * return result to the request
			 * 
			 *****************************/
			response.setStatus(HttpServletResponse.SC_OK);
			response.addHeader("Access-Control-Allow-Origin", "*");
			response.getWriter().println(sResult);
    	}  	
    }				
    
	/********************************************
	 * return result in JSON format
	 * 
	*******************************************/
    public static String getJsonFormat(MetaData inputMD, ArrayList<MetaData> arrMetaData, ArrayList<String> arrTemporalValue){
    	int i, j, k, index, n, t, m;
    	ArrayList<String> arrVar = new ArrayList<String>();
    	
    	String sTime, sResult="";    	
		sResult="{\n"+
				"	\"head\":{\n"+
				"		\"vars\":[";
		
		/*
		 * Note that we maybe need to return the label of dataset, component, and value
		 * Solution: 
		 *   + Do not add these variables to arrVar
		 *   + If the variable for ds, component is available, check the variable of its label
		 */
		
		//variable for dataset		
		if(inputMD.getDataSet().getVariable()!=""){
			arrVar.add(inputMD.getDataSet().getVariable());
			sResult = sResult +"\"" + inputMD.getDataSet().getVariable().substring(1) +"\",";
			if(inputMD.getDataSet().getVariableLabel()!=""){
				sResult = sResult +"\"" + inputMD.getDataSet().getVariableLabel().substring(1) +"\",";
			}
		}
		else{			
			arrVar.add("?dataset");
		}
		
		
		//variables for components
		for(i=0; i<inputMD.getNumberofComponent(); i++){
			if(inputMD.getComponent(i).getVariable()!=""){				
				arrVar.add(inputMD.getComponent(i).getVariable());
				sResult = sResult +"\"" + inputMD.getComponent(i).getVariable().substring(1) +"\",";
				
				if(inputMD.getComponent(i).getVariableLabel()!="")
					sResult = sResult +"\"" +inputMD.getComponent(i).getVariableLabel().substring(1) +"\",";		
			}
		}
	
		sResult = sResult.substring(0, sResult.length()-1) + "]\n"+
				"	},\n"+
				"	\"results\":{\n"+
				"		\"distinct\": false, \"ordered\": true,\n" +
				"		\"bindings\":[\n";
		
		//add values
		index = inputMD.getIndexOfTemporalDimension();
		n = arrTemporalValue.size();	
		
		for(i=0; i<n; i++){	
			sTime = arrTemporalValue.get(i);
			for(j=0; j<arrMetaData.size(); j++){
				m = arrMetaData.get(j).getComponent(index).indexOf(sTime);
				if(m!=-1){
					sResult = sResult +
							"			{\n";
					//value of dataset variable
					sResult = sResult + 
							"				\""+arrVar.get(0).substring(1)+"\":{"+
							"\"type\": \"uri\", \"value\": \"" + arrMetaData.get(j).getDataSet().getUri() + "\"},\n";
					
					//value of dataset'label varialbe
					if(arrMetaData.get(j).getDataSet().getVariableLabel()!="")
						sResult = sResult + 
							"				\""+arrMetaData.get(j).getDataSet().getVariableLabel().substring(1)+"\":{"+
							"\"type\": \"literal\", \"value\": \"" + arrMetaData.get(j).getDataSet().getLabel() + "\"},\n";
					

					//value of other variables
					for(k=1; k<arrVar.size(); k++){
						for(t=0; t<arrMetaData.get(j).getNumberofComponent(); t++)
							if(arrMetaData.get(j).getComponent(t).getVariable().equals(arrVar.get(k))){
								if(arrMetaData.get(j).getComponent(t).getValueSize()>=m && !arrMetaData.get(j).getComponent(t).getType().contains("Attribute")){
									if(arrMetaData.get(j).getComponent(t).getType().contains("Measure"))
										sResult = sResult + 
										"				\""+arrVar.get(k).substring(1)+"\":{"+
										"\"type\": \"literal\", \"value\":\"" + arrMetaData.get(j).getComponent(t).getValue(m) + "\"},\n";
									else
										sResult = sResult + 
										"				\""+arrVar.get(k).substring(1)+"\":{"+
										"\"type\": \"uri\", \"value\": \"" + arrMetaData.get(j).getComponent(t).getValueReference(m) + "\"},\n";								
									
								}
								else if(arrMetaData.get(j).getComponent(t).getValueSize()==1){
									sResult = sResult + 
											"				\""+arrVar.get(k).substring(1)+"\":{"+
											"\"type\": \"uri\", \"value\":\"" + arrMetaData.get(j).getComponent(t).getValueReference(0) + "\"},\n";								
								}
								
								//value of label variable
								if(arrMetaData.get(j).getComponent(t).getVariableLabel()!="")
									sResult = sResult + 
									"				\""+ arrMetaData.get(j).getComponent(t).getVariableLabel().substring(1)+"\":{"+
									"\"type\": \"string\", \"value\": \"" + arrMetaData.get(j).getComponent(t).getLabel() + "\"},\n";					
							}
					}
					//remove the last comma of each value in a binding
					sResult = sResult.substring(0, sResult.length()-2);
					
					//add comma at the end of each binding
					sResult = sResult +
							"\n			},\n";
					break;
				}		
				
			}		
		}				
		
		//remove the last comma of the last binding
		sResult = sResult.substring(0, sResult.length()-2);
		
		sResult = sResult +
				"\n		]\n"+
				"	}\n"+
				"}";				
	   	return sResult;    	
    }
    
    /********************************************
	 * return result in XML format
	 * 
	 *******************************************/		
	
    public static String getXmlFormat(MetaData inputMD, ArrayList<MetaData> arrMetaData, ArrayList<String> arrTemporalValue){
    	int i, j, k, index, n, t, m;
    	ArrayList<String> arrVar = new ArrayList<String>();
    	String sTime, sResult="";    	
    	
    	sResult="<?xml version='1.0' encoding='UTF-8'?>\n"+
   			 "<sparql xmlns='http://www.w3.org/2005/sparql-results#'>\n"+
   			 "	<head>\n";
    
		//variable for dataset
    	if(inputMD.getDataSet().getVariable()!=""){	
			arrVar.add(inputMD.getDataSet().getVariable());
			sResult = sResult +"		<variable name='" + inputMD.getDataSet().getVariable().substring(1) +"'/>\n";		
			if(inputMD.getDataSet().getVariableLabel()!=""){
				sResult = sResult +"		<variable name='" + inputMD.getDataSet().getVariableLabel().substring(1) +"'/>\n";
			}
    	}
		else			
			arrVar.add("?dataset");				
		
		//variables for components
		for(i=0; i<inputMD.getNumberofComponent(); i++){
			if(inputMD.getComponent(i).getVariable()!=""){
				arrVar.add(inputMD.getComponent(i).getVariable());			
				sResult = sResult +"		<variable name='" + inputMD.getComponent(i).getVariable().substring(1) +"'/>\n";
				if(inputMD.getComponent(i).getVariableLabel()!="")
					sResult = sResult +"		<variable name='" + inputMD.getComponent(i).getVariableLabel().substring(1) +"'/>\n";	
			}
		}		
		
		sResult = sResult +
			 "	</head>\n"+
			 "	<results distinct=\"false\" ordered=\"true\">\n";				
		
		
		//add values
		index = inputMD.getIndexOfTemporalDimension();
		n = arrTemporalValue.size();	
		
		for(i=0; i<n; i++){	
			sTime = arrTemporalValue.get(i);
			for(j=0; j<arrMetaData.size(); j++){
				m = arrMetaData.get(j).getComponent(index).indexOf(sTime);
				if(m!=-1){	
					sResult = sResult +
							"		<result>\n";
					
					//value of dataset variable
					sResult = sResult + 
							"			<binding name='"+arrVar.get(0).substring(1)+"'>\n"+
							"				<uri>"+arrMetaData.get(j).getDataSet().getUri() + "</uri>\n"+
							"			</binding>\n";
					
					//value of dataset'label varialbe
					if(arrMetaData.get(j).getDataSet().getVariableLabel()!="")
						sResult = sResult + 
						"			<binding name='"+arrMetaData.get(j).getDataSet().getVariableLabel().substring(1)+"'>\n"+
						"				<uri>"+arrMetaData.get(j).getDataSet().getLabel() + "</uri>\n"+
						"			</binding>\n";				
						
	
					//value of other variables
					for(k=1; k<arrVar.size(); k++){
						for(t=0; t<arrMetaData.get(j).getNumberofComponent(); t++)
							if(arrMetaData.get(j).getComponent(t).getVariable().equals(arrVar.get(k))){
								if(arrMetaData.get(j).getComponent(t).getValueSize()>=m && !arrMetaData.get(j).getComponent(t).getType().contains("Attribute")){
									if(arrMetaData.get(j).getComponent(t).getType().contains("Measure"))
										sResult = sResult + 
										"			<binding name='"+arrVar.get(k).substring(1)+"'>\n"+
										"				<literal datatype='http://www.w3.org/2001/XMLSchema#decimal'>"+ arrMetaData.get(j).getComponent(t).getValue(m) + "</literal>\n"+
										"			</binding>\n";
									else
										sResult = sResult + 
										"			<binding name='"+arrVar.get(k).substring(1)+"'>\n"+
										"				<uri>"+ arrMetaData.get(j).getComponent(t).getValueReference(m) + "</uri>\n"+
										"			</binding>\n";
								}
								else if( arrMetaData.get(j).getComponent(t).getValueSize()==1){
									sResult = sResult + 
											"			<binding name='"+arrVar.get(k).substring(1)+"'>\n"+
											"				<uri>"+ arrMetaData.get(j).getComponent(t).getValueReference(0) + "</uri>\n"+
											"			</binding>\n";
								}
								
								//value of label variable
								if(arrMetaData.get(j).getComponent(t).getVariableLabel()!="")
									sResult = sResult + 
									"			<binding name='"+arrMetaData.get(j).getComponent(t).getVariableLabel().substring(1)+"'>\n"+
									"				<literal>"+ arrMetaData.get(j).getComponent(t).getLabel() + "</literal>\n"+
									"			</binding>\n";								
							}					
						
					}				
					sResult = sResult +
							"		</result>\n";
				}
			}		
		}		
		
		sResult = sResult +
				"	</results>\n"+
				"</sparql>";	
		return sResult;
    }
    
    /********************************************
	 * return result in HTML format
	 * 
	 *******************************************/		
	
    public static String getHtmlFormat(MetaData inputMD, ArrayList<MetaData> arrMetaData, ArrayList<String> arrTemporalValue){
    	int i, j, k, index, n, t, m;
    	ArrayList<String> arrVar = new ArrayList<String>();
    	
    	String sTime, sResult="";    	
		sResult="<table>\n"+
				"	<thead>\n"+
				"		<tr>\n";		
				
		//variable for dataset
		if(inputMD.getDataSet().getVariable()!=""){
			arrVar.add(inputMD.getDataSet().getVariable());
			sResult = sResult +
					  "			<th>" + inputMD.getDataSet().getVariable().substring(1) +"</th>\n";			
			if(inputMD.getDataSet().getVariableLabel()!="")
				sResult = sResult +
				  "			<th>" + inputMD.getDataSet().getVariableLabel().substring(1) +"</th>\n";			
			
		}else			
			arrVar.add("?dataset");		
		
		
		//variables for components
		for(i=0; i<inputMD.getNumberofComponent(); i++){
			if(inputMD.getComponent(i).getVariable()!=""){				
				arrVar.add(inputMD.getComponent(i).getVariable());
				sResult = sResult +
						  "			<th>" + inputMD.getComponent(i).getVariable().substring(1) +"</th>\n";
				if(inputMD.getComponent(i).getVariableLabel()!="")
					sResult = sResult +
					  "			<th>" + inputMD.getComponent(i).getVariableLabel().substring(1) +"</th>\n";
			}
		}	
		
		sResult = sResult +
				  "		</tr>\n"+
				  "	</thead>\n"+
				  "	<tbody>\n";				
		
		//add values
		index = inputMD.getIndexOfTemporalDimension();
		n = arrTemporalValue.size();	
		
		for(i=0; i<n; i++){	
			sTime = arrTemporalValue.get(i);
			for(j=0; j<arrMetaData.size(); j++){				
				m = arrMetaData.get(j).getComponent(index).indexOf(sTime);
				if(m!=-1){		
					sResult = sResult +
							"	<tr class='ds"+ j%arrMetaData.size() +"'>\n";
					
					//value of dataset variable
					sResult = sResult + 
							"		<td>"+arrMetaData.get(j).getDataSet().getUri() + "</td>\n";
					
					//value of dataset'label varialbe
					if(arrMetaData.get(j).getDataSet().getVariableLabel()!="")
						sResult = sResult + 
						"		<td>"+arrMetaData.get(j).getDataSet().getLabel() + "</td>\n";
						
					//value of other variables
					for(k=1; k<arrVar.size(); k++){
						for(t=0; t<arrMetaData.get(j).getNumberofComponent(); t++)
							if(arrMetaData.get(j).getComponent(t).getVariable().equals(arrVar.get(k))){
								if(arrMetaData.get(j).getComponent(t).getValueSize()>=m && !arrMetaData.get(j).getComponent(t).getType().contains("Attribute")){
									if(arrMetaData.get(j).getComponent(t).getType().contains("Measure"))
										sResult = sResult + 
												  "		<td>" + arrMetaData.get(j).getComponent(t).getValue(m) + "</td>\n";
									else
										sResult = sResult + 
												 "		<td>" + arrMetaData.get(j).getComponent(t).getValueReference(m) + "</td>\n";
								}
								else if(arrMetaData.get(j).getComponent(t).getValueSize()==1){
									sResult = sResult + 
											  "		<td>" + arrMetaData.get(j).getComponent(t).getValueReference(0) + "</td>\n";								
								}
								if(arrMetaData.get(j).getComponent(t).getVariableLabel()!=""){
									sResult = sResult + 
											 "		<td>" + arrMetaData.get(j).getComponent(t).getLabel() + "</td>\n";
								}
							}
					}			
				
					sResult = sResult +
							"	</td>\n";
				}	
			}
		}		
		
		sResult = sResult +
				"	</tbody>\n"+
				"</table>";
								
	   	return sResult; 
    }
}
