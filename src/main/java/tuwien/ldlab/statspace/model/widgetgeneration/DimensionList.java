package tuwien.ldlab.statspace.model.widgetgeneration;

import tuwien.ldlab.statspace.codelist.StandardDimensions;
import tuwien.ldlab.statspace.model.util.Support;

import java.util.ArrayList;

public class DimensionList {
	ArrayList<Dimension> arrDimension;	
	
	public DimensionList(){
		arrDimension = new ArrayList<Dimension>();		
	}
	
	public void addDimension(String uri, String label){
		int i;		
		for(i=0; i<arrDimension.size(); i++)
			if(arrDimension.get(i).getUri().equalsIgnoreCase(uri))		
				break;		
		if(i==arrDimension.size()){
			Dimension oneDimension = new Dimension(uri, label);
			arrDimension.add(oneDimension);
		}else
			if(label.contains("@en"))
				arrDimension.get(i).setLabel(label);		
	}
	
	public int indexOf(String uri){
		int i;
		for(i=0; i<arrDimension.size(); i++)
			if(arrDimension.get(i).getUri().equalsIgnoreCase(uri))		
				return i;
		return -1;
	}
	
	public void removeDuplicate(){
		int i, j, k;
		boolean bFound;
		for(i=0; i<arrDimension.size(); i++)
			for(j=i+1; j<arrDimension.size(); j++){
				if(Support.getName(arrDimension.get(i).getUri()).equalsIgnoreCase
						(Support.getName(arrDimension.get(j).getUri()))){
					bFound = false;
					for(k=0; k < arrDimension.get(j).getValueSize(); k++){
						if(arrDimension.get(i).haveValue(arrDimension.get(j).getValueUri(k))){
							bFound = true;
							break;
						}
					}
					if(bFound){										
						if(arrDimension.get(i).getValueSize()<arrDimension.get(j).getValueSize()){
							arrDimension.get(i).setUri(arrDimension.get(j).getUri());
							arrDimension.get(i).setLabel(arrDimension.get(j).getLabel());
						}
						for(k=0; k<arrDimension.get(j).getValueSize(); k++)
							arrDimension.get(i).addValue(arrDimension.get(j).getValueUri(k), arrDimension.get(j).getValueLabel(k));		
						arrDimension.remove(j);
						j--;
						
					}
				}
			}
	}	
	
	public void identifyReferenceDimension(){		
		int i;
		String sRef;
		StandardDimensions std = new StandardDimensions();
		
		for(i=0; i<arrDimension.size(); i++){
			sRef = std.identifyReference(arrDimension.get(i).getUri().toLowerCase());
			if(sRef!="")
				arrDimension.get(i).setRefDimension(sRef);
//			else
//				System.out.println("Can not identify coreference dimension for " + arrDimension.get(i).getUri());
		}
		
		for(i=0; i<arrDimension.size(); i++){
			sRef = arrDimension.get(i).getRefDimension();
//			System.out.println(arrDimension.get(i).getUri() + "\t\t" + sRef);
		}
				
		for(i=0; i<arrDimension.size(); i++){
			sRef = arrDimension.get(i).getRefDimension();
			if(sRef!=null && sRef.equals("http://purl.org/linked-data/sdmx/2009/dimension#refArea"))
				break;		
		}
//		if(i==arrDimension.size())
//			System.out.println("No sdmx-dimension:refArea is detected");
		
		for(i=0; i<arrDimension.size(); i++){
			sRef = arrDimension.get(i).getRefDimension();
			if(sRef!=null && sRef.equals("http://purl.org/linked-data/sdmx/2009/dimension#refPeriod"))
				break;		
		}
//		if(i==arrDimension.size())
//			System.out.println("No sdmx-dimension:refPeriod is detected");
		
	}
	
	public void removeDimension(String name){
		int i;
		for(i=0; i<arrDimension.size(); i++)
			if(arrDimension.get(i).getUri().equals(name))		
				arrDimension.remove(i);
	}
	
	public String getDimensionUri(int i){
		if(!arrDimension.isEmpty() && 0<=i && i<arrDimension.size())
			return arrDimension.get(i).getUri();
		else
			return 
				"";
	}
	
	public String getDimensionLabel(int i){
		if(!arrDimension.isEmpty() && 0<=i && i<arrDimension.size())
			return 
					arrDimension.get(i).getLabel();
		else
			return
					"";
	}
	
	public int getSize(){
		return arrDimension.size();
	}
	
	public int getValueSize(int i){
		return arrDimension.get(i).getValueSize();
	}
	
		
	public Dimension getDimension(int i){
		if(!arrDimension.isEmpty() && 0<=i && i<arrDimension.size())
			return arrDimension.get(i);
		else{
			System.out.println("-----------Error - No dimension or out of size------------");
			return new Dimension();
		}
	}	
	
	
	public void addValue(int i, String scode, String slabel){
		arrDimension.get(i).addValue(scode, slabel);
	}
	
	public void clearAll(){
		for(int i=0; i<arrDimension.size(); i++)
			arrDimension.get(i).clearAll();
		arrDimension.clear();
	}
	
	public boolean allEmpty(){
		int i;
		for(i=0; i<arrDimension.size(); i++)
			if(arrDimension.get(i).getValueSize()!=0)
				return false;
		return true;
	}

	public int indexYearDimension(){
		int i;
		for(i=0; i<arrDimension.size(); i++)
			if(arrDimension.get(i).isYearDimension())
				return i;
		return -1;
	}
	
	public int indexDateDimension(){
		int i;
		for(i=0; i<arrDimension.size(); i++)
			if(arrDimension.get(i).isDateDimension())
				return i;
		return -1;
	}

	public int indexDimensionByUri(String s){
		int i;
		for(i=0; i<arrDimension.size(); i++)
			if(arrDimension.get(i).getUri().equalsIgnoreCase(s))
				return i;
		return -1;
	}
	
	public boolean haveValueLabel(){
		int i, n = arrDimension.size();
		for(i=0; i<n; i++)
			if(arrDimension.get(i).haveValueLabel())
				return true;
		return false;
	}
	
}
