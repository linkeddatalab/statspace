package at.tuwien.ldlab.statspace.metadata;

import java.util.ArrayList;

public class GoogleAreaList {
	ArrayList<GoogleArea> arrGoogleArea;
	String sType;               //non-administrative-area or adiministrative-area
	String uriGoogleBroaderArea; //broader area of this area
	
	public GoogleAreaList(){
		arrGoogleArea = new ArrayList<GoogleArea>();
		sType="";
		uriGoogleBroaderArea="";
	}
	
	public void setType(String t){sType = t;}	
	public String getType(){return sType;}
	public void setUriGoogleBroaderArea(String uri){uriGoogleBroaderArea = uri;}	
	public String getUriGoogleBroaderArea(){return uriGoogleBroaderArea;}
	
	public void addGoogleArea(GoogleArea gArea){
		if(arrGoogleArea.size()==0 || containGoogleArea(gArea)==false){
			arrGoogleArea.add(gArea);			
		}		
	}
	
	public void removeAllGoogleArea(){
		arrGoogleArea.clear();
	}
	
	public boolean containGoogleArea(GoogleArea gArea){
		int i;
		for(i=0; i<arrGoogleArea.size(); i++){
			if(arrGoogleArea.get(i).getUri().equals(gArea.getUri()))
				return true;			
		}
		return false;
	}
	
	public boolean containGoogleArea(String uri){
		int i;
		for(i=0; i<arrGoogleArea.size(); i++){
			if(arrGoogleArea.get(i).getUri().equals(uri))
				return true;			
		}
		return false;
	}
	
	public GoogleArea getGoogleArea(int index){
		return arrGoogleArea.get(index);
	}
	
	public void removeGoogleArea(int index){
		arrGoogleArea.remove(index);
	}
	
	public int getSize(){
		return arrGoogleArea.size();
	}
}
