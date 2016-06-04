/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tuwien.ldlab.statspace.controller.mediator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.*;
import javax.servlet.http.*;
import tuwien.ldlab.statspace.codelist.CL_Unit_Measure;
import tuwien.ldlab.statspace.model.mediator.MetaData;
import tuwien.ldlab.statspace.model.mediator.SparqlQuery;


public class ReceiveMediatorQuery extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
			ArrayList<MetaData> arrMetaData = inputMD.queryMetaDataByFilter();				
			
			if(arrMetaData.size()==0){
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
	        	response.addHeader("Access-Control-Allow-Origin", "*");
	        	response.getWriter().println("No result");	
	        	return;
			}
			
			//Step 2. Rewrite & send query for each dataset
			String sWebApp =  getServletContext().getRealPath("/");		
			String sSeparator = File.separator;				
			for(i=0; i<arrMetaData.size(); i++){
				arrMetaData.get(i).rewriteQuery(sVarObs, sWebApp, sSeparator, true);	
			}				
			
			//Step 3.   Rewrite results
			//Step 3.1. Remove components do not appear in the input metadata
			for(i=0; i<arrMetaData.size(); i++)
				arrMetaData.get(i).removeExternalComponents(inputMD);
				
			
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
							arrMetaData.get(i).rewriteObservedValue(scale);
						}
						break;
					}
			}
			
			//Step 3.5. Rewrite values of refPeriod e.g., /date/1960-01-01 => /year/1960
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
    	int i, j, index, n, t, m;
    	ArrayList<String> arrVar = new ArrayList<String>();    	
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
//			if(inputMD.getDataSet().getVariableLabel()!=""){
//				sResult.append("\"").append(inputMD.getDataSet().getVariableLabel().substring(1)).append("\",");
//			}
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
				
//				if(inputMD.getComponent(i).getVariableLabel()!="")
//					sResult = sResult +"\"" +inputMD.getComponent(i).getVariableLabel().substring(1) +"\",";		
			}
		}
	
		sResult.deleteCharAt(sResult.length()-1);
		sResult.append("]\n").append("	},\n").append("	\"results\":{\n").append("		\"distinct\": false, \"ordered\": true,\n").append("		\"bindings\":[\n");
		
		//add values
		index = inputMD.getIndexOfTemporalDimension();
		n = arrTemporalValue.size();	
		
		for(i=0; i<n; i++){	
			sTime = arrTemporalValue.get(i);
			for(j=0; j<arrMetaData.size(); j++){
				m = arrMetaData.get(j).getComponent(index).indexOf(sTime);
				if(m!=-1){
					sResult.append("			{\n");
					
					//value of dataset variable
					sResult.append( 
							"				\"").append(arrVar.get(0)).append("\":{").append(
							"\"type\": \"uri\", \"value\": \"").append(arrMetaData.get(j).getDataSet().getUri()).append("\"},\n");
					
					//value of dataset'label varialbe
//					if(arrMetaData.get(j).getDataSet().getVariableLabel()!="")
//						sResult = sResult + 
//							"				\""+arrMetaData.get(j).getDataSet().getVariableLabel().substring(1)+"\":{"+
//							"\"type\": \"literal\", \"value\": \"" + arrMetaData.get(j).getDataSet().getLabel() + "\"},\n";
					

					//value of other variables
//					for(k=1; k<arrVar.size(); k++){
						for(t=0; t<arrMetaData.get(j).getNumberofComponent(); t++){
//							if(arrMetaData.get(j).getComponent(t).getVariable().equals(arrVar.get(k))){
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
							
							//value of label variable
//								if(arrMetaData.get(j).getComponent(t).getVariableLabel()!="")
//									sResult = sResult + 
//									"				\""+ arrMetaData.get(j).getComponent(t).getVariableLabel().substring(1)+"\":{"+
//									"\"type\": \"string\", \"value\": \"" + arrMetaData.get(j).getComponent(t).getLabel() + "\"},\n";					
//							}
					}
					//remove the last comma and \n of each value in a binding
					sResult.delete(sResult.length()-2, sResult.length());
					
					//add comma at the end of each binding
					sResult.append("\n			},\n");
					break;
				}		
				
			}		
		}				
		
		//remove the last comma of the last binding
		sResult.delete(sResult.length()-2, sResult.length());
		
		sResult.append("\n		]\n").append("	}\n").append("}");				
	   	return sResult.toString();    	
    }
    
    /********************************************
	 * return result in XML format
	 * 
	 *******************************************/		
	
    public static String getXmlFormat(MetaData inputMD, ArrayList<MetaData> arrMetaData, ArrayList<String> arrTemporalValue){
    	int i, j, index, n, t, m;    	
    	ArrayList<String> arrVar = new ArrayList<String>();    	
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
//				if(inputMD.getComponent(i).getVariableLabel()!="")
//					sResult = sResult +"		<variable name='" + inputMD.getComponent(i).getVariableLabel().substring(1) +"'/>\n";	
			}
		}		
		
		sResult.append(
			"	</head>\n").append(
			 "	<results distinct=\"false\" ordered=\"true\">\n");				
		
		
		//add values
		index = inputMD.getIndexOfTemporalDimension();
		n = arrTemporalValue.size();	
		
		for(i=0; i<n; i++){	
			sTime = arrTemporalValue.get(i);
			for(j=0; j<arrMetaData.size(); j++){
				m = arrMetaData.get(j).getComponent(index).indexOf(sTime);
				if(m!=-1){	
					sResult.append(
							"		<result>\n");
					
					//value of dataset variable
					sResult.append( 
							"			<binding name='").append(arrVar.get(0)).append("'>\n").append(
							"				<uri>").append(arrMetaData.get(j).getDataSet().getUri()).append("</uri>\n").append(
							"			</binding>\n");
					
					//value of dataset'label varialbe
//					if(arrMetaData.get(j).getDataSet().getVariableLabel()!="")
//						sResult = sResult + 
//						"			<binding name='"+arrMetaData.get(j).getDataSet().getVariableLabel().substring(1)+"'>\n"+
//						"				<uri>"+arrMetaData.get(j).getDataSet().getLabel() + "</uri>\n"+
//						"			</binding>\n";				
						
	
					//value of other variables
//					for(k=1; k<arrVar.size(); k++){
					for(t=0; t<arrMetaData.get(j).getNumberofComponent(); t++){
//							if(arrMetaData.get(j).getComponent(t).getVariable().equals(arrVar.get(k))){
						if(arrMetaData.get(j).getComponent(t).getValueSize()>=m && !arrMetaData.get(j).getComponent(t).getType().contains("Attribute")){
							if(arrMetaData.get(j).getComponent(t).getType().contains("Measure"))
								sResult.append(
								"			<binding name='").append(arrVar.get(t=1)).append("'>\n").append(
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
								
						//value of label variable
//								if(arrMetaData.get(j).getComponent(t).getVariableLabel()!="")
//									sResult = sResult + 
//									"			<binding name='"+arrMetaData.get(j).getComponent(t).getVariableLabel().substring(1)+"'>\n"+
//									"				<literal>"+ arrMetaData.get(j).getComponent(t).getLabel() + "</literal>\n"+
//									"			</binding>\n";								
					}					
						
					sResult.append(
							"		</result>\n");
				}
			}		
		}		
		
		sResult.append(
			"	</results>\n").append(
				"</sparql>");	
		return sResult.toString();
    }
    
    /********************************************
	 * return result in HTML format
	 * 
	 *******************************************/		
	
    public static String getHtmlFormat(MetaData inputMD, ArrayList<MetaData> arrMetaData, ArrayList<String> arrTemporalValue){
    	int i, j, index, n, t, m;
    	ArrayList<String> arrVar = new ArrayList<String>();    	
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
//			if(inputMD.getDataSet().getVariableLabel()!="")
//				sResult = sResult +
//				  "			<th>" + inputMD.getDataSet().getVariableLabel().substring(1) +"</th>\n";			
			
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
//				if(inputMD.getComponent(i).getVariableLabel()!="")
//					sResult = sResult +
//					  "			<th>" + inputMD.getComponent(i).getVariableLabel().substring(1) +"</th>\n";
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
				m = arrMetaData.get(j).getComponent(index).indexOf(sTime);
				if(m!=-1){		
					sResult.append(
							"	<tr class='ds").append(j%arrMetaData.size()).append("'>\n");
					
					//value of dataset variable
					sResult.append( 
							"		<td>").append(arrMetaData.get(j).getDataSet().getUri()).append("</td>\n");
					
					//value of dataset'label varialbe
//					if(arrMetaData.get(j).getDataSet().getVariableLabel()!="")
//						sResult = sResult + 
//						"		<td>"+arrMetaData.get(j).getDataSet().getLabel() + "</td>\n";
						
					//value of other variables
//					for(k=1; k<arrVar.size(); k++){
					for(t=0; t<arrMetaData.get(j).getNumberofComponent(); t++){
//							if(arrMetaData.get(j).getComponent(t).getVariable().equals(arrVar.get(k))){
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
//								if(arrMetaData.get(j).getComponent(t).getVariableLabel()!=""){
//									sResult = sResult + 
//											 "		<td>" + arrMetaData.get(j).getComponent(t).getLabel() + "</td>\n";
//								}
//							}
					}
//					}			
						
				
					sResult.append(
							"	</td>\n");
				}	
			}
		}		
		
		sResult.append(
				"	</tbody>\n").append(
				"</table>");
								
	   	return sResult.toString(); 
    }
}
