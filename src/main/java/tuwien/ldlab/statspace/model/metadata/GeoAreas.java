package tuwien.ldlab.statspace.model.metadata;

import java.util.ArrayList;

import tuwien.ldlab.statspace.model.mediator.StringCouple;

public class GeoAreas {
	ArrayList<StringCouple> arrArea;
	
	public GeoAreas(){
		arrArea = new ArrayList<StringCouple>();
	}
	
	public void addGeoArea(String sL, String sU){
		int i;		
		for(i=0; i<arrArea.size(); i++)
			if(arrArea.get(i).getSecondString().equalsIgnoreCase(sU))
				return;
		arrArea.add(new StringCouple(sL, sU));
	}
	
	public int getIndex(String sL, String sU){
		int i;
		if(sU=="" || sL=="") return -1;
		for(i=0; i<arrArea.size(); i++)
			if(arrArea.get(i).getFirstString().equalsIgnoreCase(sL) && arrArea.get(i).getSecondString().equalsIgnoreCase(sU))
				return i;
		return -1;
	}
	
	public int getIndex(String sU){
		int i;
		if(sU=="") return -1;
		for(i=0; i<arrArea.size(); i++)
			if(arrArea.get(i).getSecondString().equalsIgnoreCase(sU))
				return i;
		return -1;
	}
	
	public void sortInAscending(){
		int i, j;
		for(i=0; i<arrArea.size(); i++)
			for(j=i+1; j<arrArea.size(); j++)
				if(arrArea.get(i).getSecondString().compareTo(arrArea.get(j).getSecondString())>0){
					String sL = arrArea.get(i).getFirstString();
					String sU = arrArea.get(i).getSecondString();
					arrArea.get(i).setFirstString(arrArea.get(j).getFirstString());
					arrArea.get(i).setSecondString(arrArea.get(j).getSecondString());
					arrArea.get(j).setFirstString(sL);
					arrArea.get(j).setSecondString(sU);
					
			}
	}
	
	public int indexOfBroaderArea(String sUri){
		int i;		
		for(i=0; i<arrArea.size(); i++){
			if(isBroaderArea(arrArea.get(i).getSecondString(), sUri))
				return i;
		}
		return -1;
	}
	
	public String uriOfBroaderArea(String sUri){
		int i;		
		for(i=0; i<arrArea.size(); i++){
			if(isBroaderArea(arrArea.get(i).getSecondString(), sUri))
				return arrArea.get(i).getSecondString();
		}
		return "";
	}
	
	public boolean isBroaderArea(String sUri1, String sUri2){
		//URI
		if(sUri2.startsWith(sUri1+"/") && sUri2.length()> sUri1.length()+1 && sUri2.substring(sUri1.length()+1).indexOf("/")==-1)
				return true;
		
		//NUTS
		if(sUri2.startsWith(sUri1) && sUri2.length()==sUri1.length()+1 )
				return true;
		return false;
	}
	
	
	public void clear(){
		arrArea.clear();
	}
	public String getLabel(int i){return arrArea.get(i).getFirstString();}
	public String getUri(int i){return arrArea.get(i).getSecondString();}
	public int getSize(){return arrArea.size();}
	
}
