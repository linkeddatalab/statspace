package tuwien.ldlab.statspace.model.util;

import java.io.File;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class SpecialTypeList {
	private  ArrayList<String> arrYear;
	private  ArrayList<String> arrDate;
	private  ArrayList<String> arrCountry;
	
	public SpecialTypeList(String path){
		String sType;		
		arrYear = new ArrayList<String>();
		arrDate = new ArrayList<String>();	
		arrCountry = new ArrayList<String>();	
		try{
			File f = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(f);
			doc.getDocumentElement().normalize();
			
			//get list of year type
			NodeList nList = doc.getElementsByTagName("year");	
			for (int temp = 0; temp < nList.getLength(); temp++) {	 
				Node nNode = nList.item(temp);					 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {	 
					Element eElement = (Element) nNode;		
					sType = eElement.getAttribute("type");					
					arrYear.add(sType);						
				}
			}
			
			//get list of date type
			nList = doc.getElementsByTagName("date");	
			for (int temp = 0; temp < nList.getLength(); temp++) {	 
				Node nNode = nList.item(temp);					 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {	 
					Element eElement = (Element) nNode;		
					sType = eElement.getAttribute("type");					
					arrDate.add(sType);						
				}
			}	
			
			//get list of country type
			nList = doc.getElementsByTagName("country");	
			for (int temp = 0; temp < nList.getLength(); temp++) {	 
				Node nNode = nList.item(temp);					 
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {	 
					Element eElement = (Element) nNode;		
					sType = eElement.getAttribute("type");					
					arrCountry.add(sType);						
				}
			}	
		}catch (Exception e) {		
			System.out.println(e.toString());
		}		
	}	
		
	public boolean isYear(String s){
		int i;
		if(s.indexOf("^^")!=-1)
			s = s.substring(s.indexOf("^^"));
		
		for(i=0; i<arrYear.size(); i++)
			if(s.equalsIgnoreCase(arrYear.get(i)))
				return true;
		
		return false;
	}
	
	public boolean isDate(String s){
		int i;
		if(s.indexOf("^^")!=-1)
			s = s.substring(s.indexOf("^^"));
		
		for(i=0; i<arrDate.size(); i++)
			if(s.equalsIgnoreCase(arrDate.get(i)))
				return true;
		
		return false;
	}
	
	public boolean isCountry(String s){
		int i;		
		
		for(i=0; i<arrCountry.size(); i++)
			if(s.equalsIgnoreCase(arrCountry.get(i)))
				return true;
		
		return false;
	}
}
