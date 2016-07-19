/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package at.tuwien.ldlab.statspace.service;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import at.tuwien.ldlab.statspace.metadata.MetaData;
import at.tuwien.ldlab.statspace.metadata.StringCouple;
import at.tuwien.ldlab.statspace.metadata.StringTriple;
public class RequestExplorer extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Log log = LogFactory.getLog(RequestExplorer.class);
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
		doGet(request, response);
	}
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {    	
    	String sKeyword = request.getParameter("keyword");
    	if(sKeyword==null) 
    		sKeyword = "";
    	else {
    		sKeyword = sKeyword.trim().replace("'", "");
    		sKeyword = sKeyword.trim().replace("\"", "");
    	}
    	
    	MetaData md = new MetaData();
    	md.setKeyword(sKeyword);
    	ArrayList<MetaData> arrMetaData =  new ArrayList<MetaData>();	
    	arrMetaData = md.searchMetaDataOrderByPublisher();    		
    	
    	//list of topic
    	int i, j, count;
    	ArrayList<StringTriple> arrSubject = new ArrayList<StringTriple>();
    	String subject;
    	String[] arrCode={"AG", "BG", "BM", "BN", "BX", "CM", "DC", "DT", "EA", "EG", "EN", "EP", "ER", "FB", "FD", "FI", "FM", "FP", "FR", "FS", "GB", "GC", "IC", "IE", "IP", "IQ", "IS", "IT", "LP", "MS", "NE", "NV", "NY", "PA", "PX", "SE", "SG", "SH", "SI", "SL", "SM", "SN", "SP", "ST", "TG", "TM", "TT", "TX", "VC", "WP", "pe", "SO", "UN", "CT", "NA"};
    	String[] arrLabel={"Agriculture", "Balance of payments: gross", "Balance of payments: imports, payments (credit)", "Balance of payments: net", "Balance of payments: exports, receipts (debit)", "Capital markets", "Debt: aid flows from DAC", "Debt: external", "Environment: agriculture", "Environment: energy", "Environment: general", "Environment: prices", "Environment: resources", "Financial: bank (miscellaneous)", "Financial: deposit money banks", "Financial: international liquidity", "Financial: monetary survey", "Financial: prices", "Financial: interest rates", "Financial: banking survey", "Government finance: other sources", "Government finance: IMF", "Investment climate", "Infrastructure: expenditure", "Intellectual property", "Index quality assessment", "Infrastructure: transportation", "Infrastructure: telecommunications", "Logistics performance", "Military statistics", "National accounts: expenditure", "National accounts: value added", "National accounts: income", "Prices: period average", "Prices: index", "Social: education", "Social: gender", "Social: health", "Social: income", "Social: labor", "Social: migration", "Social: nutrition", "Social: population", "Social: tourism", "Trade: gross", "Trade: imports", "Trade: terms of trade", "Trade: exports", "Violence and conflict", "Mobile account", "Percentage", "Social:Politics", "Undefined", "ICT", "National Account"};
    	
    	for(j=0; j<arrCode.length; j++){
    		count=0;
	    	for(i=0; i<arrMetaData.size(); i++){
	    		subject = arrMetaData.get(i).getDataSet().getSubjectForDisplay();
	    		if(subject.length()>2)
	    			subject = subject.substring(0, 2);
	    		else{
	    			log.info(i + "\t" + subject);
	    		}	    			
	    		if(arrCode[j].equals(subject)){
	    			count++;
	    		}
	    	}
	    	if(count>0){
	    		subject = arrLabel[j];
	    		arrSubject.add(new StringTriple(subject, Integer.toString(count), arrCode[j]));
	    	}
    	}
    	
    	
    	//list of providers
//    	ArrayList<StringCouple> arrPublisher = new ArrayList<StringCouple>();
//    	String pre="", provider;
//    	count=0;
//    	for(i=0; i<arrMetaData.size(); i++){
//    		provider = arrMetaData.get(i).getPublisher();
//    		if(i==0) pre=provider;
//    		if(provider.equals(pre)) count++;
//    		else{
//    			arrPublisher.add(new StringCouple(pre, Integer.toString(count)));
//    			pre=provider;
//    			count=1;
//    		}
//    	}
    	
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
	    	for(i=0; i<arrMetaData.size(); i++){
	    		publisher = arrMetaData.get(i).getPublisher();   	    		
	    		if(arrPub[j].equals(publisher)){
	    			count++;
	    		}
	    	}
	    	if(count>0){    	    		
	    		arrPublisher.add(new StringCouple(arrPub[j], Integer.toString(count)));
	    	}
    	}
	
		request.setAttribute("result", arrMetaData);
		request.setAttribute("subject", arrSubject);
		request.setAttribute("publisher", arrPublisher);
		request.setAttribute("keyword", sKeyword);
		RequestDispatcher view = request.getRequestDispatcher("/explorer/index.jsp");		
		view.forward(request, response);		
				
	}  	    	
}
