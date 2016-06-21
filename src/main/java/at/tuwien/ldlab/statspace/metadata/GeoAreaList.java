package at.tuwien.ldlab.statspace.metadata;

import java.util.ArrayList;

public class GeoAreaList {
	ArrayList<StringCouple> arrArea;
	
	public GeoAreaList(){
		arrArea = new ArrayList<StringCouple>();
	}
	
	public void addGeoArea(String label, String uri){
		int i;		
		for(i=0; i<arrArea.size(); i++)
			if(arrArea.get(i).getSecondString().equalsIgnoreCase(uri))
				return;
		arrArea.add(new StringCouple(label, uri));
	}
	
	public int getIndex(String label, String uri){
		int i;
		if(uri=="" || label=="") return -1;
		for(i=0; i<arrArea.size(); i++)
			if(arrArea.get(i).getFirstString().equalsIgnoreCase(label) && arrArea.get(i).getSecondString().equalsIgnoreCase(uri))
				return i;
		return -1;
	}
	
	public int getIndex(String uri){
		int i;
		if(uri=="") return -1;
		for(i=0; i<arrArea.size(); i++)
			if(arrArea.get(i).getSecondString().equalsIgnoreCase(uri))
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
	
	public int indexOfBroaderArea(String uri){
		int i;		
		for(i=0; i<arrArea.size(); i++){
			if(isBroaderArea(arrArea.get(i).getSecondString(), uri))
				return i;
		}
		return -1;
	}
	
	public String uriOfBroaderArea(String uri){
		int i;		
		for(i=0; i<arrArea.size(); i++){
			if(isBroaderArea(arrArea.get(i).getSecondString(), uri))
				return arrArea.get(i).getSecondString();
		}
		return "";
	}
	
	public boolean isBroaderArea(String uri1, String uri2){
		//URI
		if(uri2.startsWith(uri1+"/") && uri2.length()> uri1.length()+1 && uri2.substring(uri1.length()+1).indexOf("/")==-1)
				return true;
		
		//NUTS
		if(uri2.startsWith(uri1) && uri2.length()==uri1.length()+1 )
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
