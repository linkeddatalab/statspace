package tuwien.ldlab.statspace.controller.exploration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openjena.atlas.logging.Log;

import tuwien.ldlab.statspace.codelist.CL_Unit_Measure;
import tuwien.ldlab.statspace.model.mediator.MetaData;


public class CompareDataSet  extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
		doGet(request, response);
	}
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { 
    	String sMDUri1  = request.getParameter("id1");
    	String sMDUri2  = request.getParameter("id2");
    	String sIdRequest   = request.getParameter("idRequest");
    	int i, j, k, t, n;
      	   	
        if(sMDUri1!=null && !sMDUri1.isEmpty() && sMDUri2!=null && !sMDUri2.isEmpty()){
        	ArrayList<MetaData> inputs = new ArrayList<MetaData>();           
       		MetaData md1 = new MetaData();      
        	md1.setUri(sMDUri1);
        	md1.queryMetaDataInfor();
        	MetaData md2 = new MetaData();      
        	md2.setUri(sMDUri2);
        	md2.queryMetaDataInfor();
        	inputs.add(md1);
        	inputs.add(md2);  
           	
           	
           	//Step 1. Set variables           	
           	for(i=0; i<inputs.size(); i++){
           		inputs.get(i).setVariable();
           	}
           	
           	//Reorder components to same positions e.g., obsValue, refArea, refPeriod, unit, ...
           	
           	inputs.get(0).reorderComponents();
           	inputs.get(1).reorderComponents();
           	n = inputs.get(1).reorderComponents(inputs.get(0));
           	
           	//Step 2. Query
           	String sVarObs = "?o";
        	String sWebApp =  getServletContext().getRealPath("/");		
			String sSeparator = File.separator;				
			for(i=0; i<inputs.size(); i++)
           		inputs.get(i).rewriteQuery2(sVarObs, sWebApp, sSeparator);
           	          	          	
           	
           //Step 3.   Rewrite results
           //Step 3.1. Query unit of hidden property
			for(i=0; i<inputs.size(); i++)
           		if(inputs.get(i).getComponent(0).getType().contains("Attribute")){
           			if(inputs.get(i).getComponent(0).getValueSize()==0){
           				inputs.get(i).queryUnit(0);
           			}
           			break;
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
			//n: number of duplication components in both metadata
//			MetaData mdCommon;	
//			if(inputs.get(0).getComponent(0).getValueSize()>inputs.get(1).getComponent(1).getValueSize()){
//				mdCommon = new MetaData(inputs.get(1));
//				k = 1;
//			}
//			else{
//				mdCommon = new MetaData(inputs.get(0));
//				k = 0;
//			}
//			//only keep components from 2 to n+2	
//			while(mdCommon.getNumberofComponent()>n+2){
//				mdCommon.removeComponent(n+2);
//			}
//			
//			//keep distinct values
//			mdCommon.keepDistinctValues(n);
//			
//			//compare with results			
//			for(t=0; t<inputs.size(); t++){
//				if(t==k) continue;
//				mdCommon.filterValue(inputs.get(t), n);
//			}
//			
//			for(i=0; i<inputs.size(); i++){
//				inputs.get(i).filterValue(mdCommon, n);
//			}
			int index;
			for(index=2; index<2+n; index++){
				ArrayList<String> arrValue0 = inputs.get(0).getDistinctValueReference(index);
				ArrayList<String> arrValue1 = inputs.get(1).getDistinctValueReference(index);
				for(i=0; i<arrValue0.size(); i++){
					if(arrValue1.indexOf(arrValue0.get(i))==-1){
						arrValue0.remove(i);
						i--;					
					}
				}
				inputs.get(0).filterValue(index, arrValue0);
				inputs.get(1).filterValue(index, arrValue0);
			}
			
		 	//return to users
//			String sResult0 = inputs.get(0).getJSONFormat();
//			String sResult1 = inputs.get(1).getJSONFormat();
//			String sVar0 = inputs.get(0).getListOfVariable();
//			String sVar1 = inputs.get(1).getListOfVariable();
           	request.setAttribute("idRequest", Integer.parseInt(sIdRequest));			
           	request.getServletContext().removeAttribute(sIdRequest);
    		request.getServletContext().setAttribute(sIdRequest, inputs);
    		
    		RequestDispatcher view = request.getRequestDispatcher("/exploration/compare.jsp");
    		view.forward(request, response);	
			
        }    	
    } 
      
}
