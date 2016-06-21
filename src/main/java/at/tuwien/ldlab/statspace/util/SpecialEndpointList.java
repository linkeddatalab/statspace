package at.tuwien.ldlab.statspace.util;

import java.io.File;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class SpecialEndpointList {
	private  ArrayList<SpecialEndpoint> arrEndpoint;
	
	public SpecialEndpointList(String path){
		String sE, sQ, sW, sH, sR, sD, sF;		
		arrEndpoint = new ArrayList<SpecialEndpoint>();		
		try{
			File f = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(f);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("sparql");	
			for (int temp = 0; temp < nList.getLength(); temp++) {	 
				Node nNode = nList.item(temp);					 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {	 
					Element eElement = (Element) nNode;		
					sE = eElement.getAttribute("endpoint");
					
					if(eElement.hasAttribute("query"))
						sQ = eElement.getAttribute("query");
					else
						sQ = "";
					if(eElement.hasAttribute("widget"))
						sW = eElement.getAttribute("widget");
					else
						sW = "";
					if(eElement.hasAttribute("http"))
						sH = eElement.getAttribute("http");
					else
						sH = "";
					if(eElement.hasAttribute("removeDuplicate"))
						sR = eElement.getAttribute("removeDuplicate");
					else
						sR = "";
					if(eElement.hasAttribute("useDistinct"))
						sD = eElement.getAttribute("useDistinct");
					else
						sD = "";
					if(eElement.hasAttribute("findOtherValue"))
						sF = eElement.getAttribute("findOtherValue");
					else
						sF = "";
					
					arrEndpoint.add(new SpecialEndpoint(sE, sQ, sW, sH, sR, sD, sF));						
				}
			}		
		}catch (Exception e) {		
			System.out.println(e.toString());
		}		
	}	
	
	public String getEndpoint(int i){return arrEndpoint.get(i).getEndpoint();}
	public String getEndpointForWidget(int i){return arrEndpoint.get(i).getEndpointForWidget();}
	public String getEndpointForQuery(int i){return arrEndpoint.get(i).getEndpointForQuery();}
	
	public boolean getHTTPRequest(int i){
		if(arrEndpoint.get(i).getHTTPRequest().equalsIgnoreCase("Yes"))
			return true;
		else 
			return false;		
	}
	
	public boolean getRemoveDuplicate(int i){
		if(arrEndpoint.get(i).getRemoveDuplicate().equalsIgnoreCase("Yes"))
			return true;
		else 
			return false;		
	}
	
	public String getUseDistinct(int i){
		if(arrEndpoint.get(i).getDistinct().equalsIgnoreCase("No"))
			return "No";
		else 
			if(arrEndpoint.get(i).getDistinct().equalsIgnoreCase("Yes"))
				return "Yes";
			else
				return "";
	}
	
	public boolean getFindOtherValue(int i){
		if(arrEndpoint.get(i).getFindOtherValue().equalsIgnoreCase("No"))
			return false;
		else 
			return true;		
	}
	
	public int getEndpointIndex(String sEndpoint){
		sEndpoint = sEndpoint.toLowerCase();
		int i;		
		for(i=0; i<arrEndpoint.size(); i++){			
			if(sEndpoint.contains(arrEndpoint.get(i).getEndpoint().toLowerCase())||
					sEndpoint.equals(arrEndpoint.get(i).getEndpointForQuery().toLowerCase())||
					sEndpoint.equals(arrEndpoint.get(i).getEndpointForWidget().toLowerCase()))					
				return i;	
		}
		return -1;
	}
	
	public int getIndexQuery(String sQuery){		
		int i;
		for(i=0; i<arrEndpoint.size(); i++){			
			if(sQuery.equals(arrEndpoint.get(i).getEndpointForQuery()))
				return i;
		}
		return -1;
	}
	
	private class SpecialEndpoint{
		private String endpoint;
		private String endpointForquery;
		private String endpointForwidget;
		private String http;
		private String remove;
		private String distinct;
		private String findOther;
		
		public SpecialEndpoint(String sE, String sQ, String sW, String sH, String sR, String sD, String sF){
			endpoint = sE;
			endpointForquery    = sQ;
			endpointForwidget   = sW;
			http  	 = sH;
			remove	 = sR;
			distinct = sD;
			findOther = sF;
		}
			
		public String getEndpoint(){return endpoint;}
		public String getEndpointForQuery(){return endpointForquery;}
		public String getEndpointForWidget(){return endpointForwidget;}
		public String getHTTPRequest(){return http;}
		public String getRemoveDuplicate(){return remove;}
		public String getDistinct(){return distinct;}
		public String getFindOtherValue(){return findOther;}
	}
}
