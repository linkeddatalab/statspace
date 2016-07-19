package at.tuwien.ldlab.statspace.service;

import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import at.tuwien.ldlab.statspace.metadata.MetaData;
import at.tuwien.ldlab.statspace.metadata.StringCouple;
import at.tuwien.ldlab.statspace.metadata.StringTriple;


public class ExploreDataSet  extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
		doGet(request, response);
	}
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { 
    	String sMDUri  = request.getParameter("id1");     	
    	
        if(sMDUri!=null && !sMDUri.isEmpty()){
        	MetaData md = new MetaData();  
        	md.setUri(sMDUri);
        	md.queryMetaDataInfor();
           	ArrayList<MetaData> arrMetaData = new ArrayList<MetaData>();
           	arrMetaData = md.searchComparableDataSet();
           	//swap inputMetaData to the first position of results
           	int i;
           	for(i=0; i<arrMetaData.size(); i++){
           		if(arrMetaData.get(i).getUri().equals(sMDUri)){
           			MetaData tmp = arrMetaData.get(0);
           			arrMetaData.set(0, arrMetaData.get(i));
           			arrMetaData.set(i, tmp);
           			break;
           		}
           	}
           	
           //list of topic
        	int j, count;
        	ArrayList<StringTriple> arrSubject = new ArrayList<StringTriple>();
        	String subject;
        	String[] arrCode={"AG", "BG", "BM", "BN", "BX", "CM", "DC", "DT", "EA", "EG", "EN", "EP", "ER", "FB", "FD", "FI", "FM", "FP", "FR", "FS", "GB", "GC", "IC", "IE", "IP", "IQ", "IS", "IT", "LP", "MS", "NE", "NV", "NY", "PA", "PX", "SE", "SG", "SH", "SI", "SL", "SM", "SN", "SP", "ST", "TG", "TM", "TT", "TX", "VC", "WP", "pe", "SO", "UN", "CT", "NA"};
        	String[] arrLabel={"Agriculture", "Balance of payments: gross", "Balance of payments: imports, payments (credit)", "Balance of payments: net", "Balance of payments: exports, receipts (debit)", "Capital markets", "Debt: aid flows from DAC", "Debt: external", "Environment: agriculture", "Environment: energy", "Environment: general", "Environment: prices", "Environment: resources", "Financial: bank (miscellaneous)", "Financial: deposit money banks", "Financial: international liquidity", "Financial: monetary survey", "Financial: prices", "Financial: interest rates", "Financial: banking survey", "Government finance: other sources", "Government finance: IMF", "Investment climate", "Infrastructure: expenditure", "Intellectual property", "Index quality assessment", "Infrastructure: transportation", "Infrastructure: telecommunications", "Logistics performance", "Military statistics", "National accounts: expenditure", "National accounts: value added", "National accounts: income", "Prices: period average", "Prices: index", "Social: education", "Social: gender", "Social: health", "Social: income", "Social: labor", "Social: migration", "Social: nutrition", "Social: population", "Social: tourism", "Trade: gross", "Trade: imports", "Trade: terms of trade", "Trade: exports", "Violence and conflict", "Mobile account", "Percentage", "Social:Politics", "Undefined", "ICT", "National Account"};
        	
        	for(j=0; j<arrCode.length; j++){
        		count=0;
    	    	for(i=1; i<arrMetaData.size(); i++){
    	    		subject = arrMetaData.get(i).getDataSet().getSubjectForDisplay();
    	    		subject = subject.substring(0, 2);
    	    		if(arrCode[j].equals(subject)){
    	    			count++;
    	    		}
    	    	}
    	    	if(count>0){
    	    		subject = arrLabel[j];
    	    		arrSubject.add(new StringTriple(subject, Integer.toString(count), arrCode[j]));
    	    	}
        	}
        	
        	String[] arrPub={ 
        			"World Bank", 
        			"European Environment Agency (EEA)",
        			"European Union Open Data Portal",
        			"Central Statistics Office of Ireland", 
        			"TU Wien",
        			"The Scottish Statistics", 
        			"Department for Communities and Local Government", 
        			"United Nations Office on Drugs and Crime", 
        			"United Kingdom - Office for National Statistics",
        			"European Environment Information and Obseration Network (EIONET)",
        			};
        	
        	//list of publisher
        	ArrayList<StringCouple> arrPublisher = new ArrayList<StringCouple>();
        	String publisher="";
        	for(j=0; j<arrPub.length; j++){
        		count=0;
    	    	for(i=1; i<arrMetaData.size(); i++){
    	    		publisher = arrMetaData.get(i).getPublisher();   	    		
    	    		if(arrPub[j].equals(publisher)){
    	    			count++;
    	    		}
    	    	}
    	    	if(count>0){    	    		
    	    		arrPublisher.add(new StringCouple(arrPub[j], Integer.toString(count)));
    	    	}
        	}
        	
        	
           	//return to users           
           	request.setAttribute("result", arrMetaData);
           	request.setAttribute("subject", arrSubject);
    		request.setAttribute("publisher", arrPublisher);
    		RequestDispatcher view = request.getRequestDispatcher("/explorer/dataset.jsp");
    		view.forward(request, response);	
        }else{
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        	response.addHeader("Access-Control-Allow-Origin", "*");
        	response.getWriter().println("No result");
        }
    }
}
