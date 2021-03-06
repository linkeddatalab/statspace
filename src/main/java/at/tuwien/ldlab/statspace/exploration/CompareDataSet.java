package at.tuwien.ldlab.statspace.exploration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import at.tuwien.ldlab.statspace.codelist.CL_Unit_Measure;
import at.tuwien.ldlab.statspace.metadata.MetaData;



public class CompareDataSet  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(CompareDataSet.class);

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
		doGet(request, response);
	}
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { 
    	String sMDUri1  = request.getParameter("id1");
    	String sMDUri2  = request.getParameter("id2");    	
    	int i, j, n;
      	   	
        if(sMDUri1!=null && !sMDUri1.isEmpty() && sMDUri2!=null && !sMDUri2.isEmpty()){
        	log.info("Compare " + sMDUri1 + "&" + sMDUri2);		
        	
        	ArrayList<MetaData> inputs = new ArrayList<MetaData>();           
       		MetaData md = new MetaData();      
        	md.setUri(sMDUri1);
        	md.queryMetaDataInfor();          	
        	inputs.add(md);
        	
        	md = new MetaData();        	      
        	md.setUri(sMDUri2);
        	md.queryMetaDataInfor();        	
        	inputs.add(md);       	
        	
           	//Check validity of input metadata
        	boolean bValid = true;
        	for(i=0; i<inputs.size(); i++){
           		if(inputs.get(i).getNumberofComponent()==0){
           			bValid = false;
           			break;
           		}
           	}
        	if(bValid==false){
        		 response.addHeader("Access-Control-Allow-Origin", "*");
		         response.setContentType("text/html");		         
		         response.getWriter().println("Sorry, metadata in your request is not found in our repository. Please update to the lastest URIs in our SPARQL endpoint"); 
        	}else{        	
	           	//Step 1. Set variables           	
	           	for(i=0; i<inputs.size(); i++){
	           		inputs.get(i).setVariable();
	           	}
	           	
	           	//Reorder components to same positions e.g., obsValue, refArea, refPeriod, unit, ...
	           	
	           	inputs.get(0).reorderComponents();
	           	inputs.get(1).reorderComponents();
	           	if(inputs.get(0).getNumberofComponent()<=inputs.get(1).getNumberofComponent())
	           		inputs.get(1).reorderComponents(inputs.get(0));
	           	else
	           		inputs.get(0).reorderComponents(inputs.get(1));
	           	
	           	n = inputs.get(0).getNumberofComponent();
	           	//Step 2. Query
	           	String sVarObs = "?o";
	        	String sWebApp =  getServletContext().getRealPath("/");		
				String sSeparator = File.separator;				
				for(i=0; i<inputs.size(); i++){		
					delay(1);
	           		inputs.get(i).rewriteQuery(sVarObs, sWebApp, sSeparator,false);           		
				}
	           	
	           //Step 3.   Rewrite results
	           //Step 3.1. Query hidden properties
				for(i=0; i<inputs.size(); i++){
	           		if(inputs.get(i).getComponent(0).getType().contains("Attribute")){
	           			if(inputs.get(i).getComponent(0).getValueSize()==0){
	           				inputs.get(i).queryHiddenProperty(0);
	           			}           			
	           		}
	           		for(j=2; j<inputs.get(i).getNumberofComponent(); j++){
		           		if(inputs.get(i).getComponent(j).getHiddenStatus()){	           			
		           				inputs.get(i).queryHiddenProperty(j);	           			          			
		           		}
	           		}
				}
				
				//Step 3.2. Rewrite values of dimensions and unit	
				for(i=0; i<inputs.size(); i++)
					inputs.get(i).rewriteResult();		
				
				//Step 3.3. Rewrite observed values if they use different units
				Double scale;
				String unit;
				CL_Unit_Measure cl_unit = new CL_Unit_Measure();
				for(i=0; i<inputs.size(); i++){
					for(j=0; j<inputs.get(i).getNumberofComponent(); j++)
						if(inputs.get(i).getComponent(j).getType().contains("Attribute")){
							if(inputs.get(i).getComponent(j).getValueSize()>0){
								unit = inputs.get(i).getComponent(j).getValue(0);
								scale = cl_unit.getScale(unit);
								if(scale != 1.0)
									inputs.get(i).rewriteObservedValue(scale);
							}
							break;
						}
				}
				
				//Step 4. Integrate achieved results
				int index;
				ArrayList<String> arrValue0 = new ArrayList<String>();
				ArrayList<String> arrValue1 = new ArrayList<String>();
				for(index=2; index<n; index++){
					arrValue0 = inputs.get(0).getDistinctRefValue(index);
					arrValue1 = inputs.get(1).getDistinctRefValue(index);
					for(i=0; i<arrValue0.size(); i++){
						if(arrValue1.indexOf(arrValue0.get(i))==-1){
							arrValue0.remove(i);
							i--;					
						}
					}
	//				if(arrValue0.size()==0 && index>3)
	//					break;
					inputs.get(0).filterValue(index, arrValue0);
					inputs.get(1).filterValue(index, arrValue0);
				}
				
				if(inputs.get(0).getComponent(0).getValueSize()==0 || inputs.get(1).getComponent(0).getValueSize()==0){
					 response.addHeader("Access-Control-Allow-Origin", "*");
			         response.setContentType("text/html");
			         response.getWriter().println("Sorry, two data sets do not have any common observation");   
				}else{
				 	//return to users	 	
				 	request.setAttribute("result", inputs);			
		    		RequestDispatcher view = request.getRequestDispatcher("/exploration/compare.jsp");
		    		view.forward(request, response);	
				}		
        	}
        }else{
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        	response.addHeader("Access-Control-Allow-Origin", "*");
        	response.getWriter().println("No result");
        }   	
    } 
    
    public void delay(int n){
		try {
		    Thread.sleep(n*200);
		} catch(InterruptedException ex) {
		    Thread.currentThread().interrupt();
		}
	}
   
}

