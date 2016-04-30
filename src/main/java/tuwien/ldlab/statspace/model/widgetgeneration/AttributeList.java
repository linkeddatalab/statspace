package tuwien.ldlab.statspace.model.widgetgeneration;

import java.util.ArrayList;

public class AttributeList {
	ArrayList<Attribute> arrAttribute;	
	
	public AttributeList(){
		arrAttribute = new ArrayList<Attribute>();		
	}
	
	public void addAttribute(String uri, String label){
		int i;		
		for(i=0; i<arrAttribute.size(); i++)
			if(arrAttribute.get(i).getUri().equalsIgnoreCase(uri))		
				break;		
		if(i==arrAttribute.size()){
			Attribute oneAttribute = new Attribute(uri, label);
			arrAttribute.add(oneAttribute);
		}else
			if(label.contains("@en"))
				arrAttribute.get(i).setLabel(label);			
	}
	
	public int indexOf(String uri){
		int i;
		for(i=0; i<arrAttribute.size(); i++)
			if(arrAttribute.get(i).getUri().equalsIgnoreCase(uri))		
				return i;
		return -1;
	}
	
	public void removeAttribute(int i){		
		arrAttribute.remove(i);
	}
	
	public String getAttributeUri(int i){
		if(!arrAttribute.isEmpty() && 0<=i && i<arrAttribute.size())
			return arrAttribute.get(i).getUri();
		else
			return 
				"";
	}
	
	public String getAttributeLabel(int i){
		if(!arrAttribute.isEmpty() && 0<=i && i<arrAttribute.size())
			return 
					arrAttribute.get(i).getLabel();
		else
			return
					"";
	}
	
	public int getSize(){
		return arrAttribute.size();
	}
	
	public int getValueSize(int i){
		return arrAttribute.get(i).getValueSize();
	}
	
		
	public Attribute getAttribute(int i){
		if(!arrAttribute.isEmpty() && 0<=i && i<arrAttribute.size())
			return arrAttribute.get(i);
		else{
			System.out.println("-----------Error - No Attribute or out of size------------");
			return new Attribute();
		}
	}	
	
	
	public void addValue(int i, String sUri, String sLabel){
		arrAttribute.get(i).addValue(sUri, sLabel);
	}
	
	public void clearAll(){
		for(int i=0; i<arrAttribute.size(); i++)
			arrAttribute.get(i).clearAll();
		arrAttribute.clear();
	}
	
	public boolean allEmpty(){
		int i;
		for(i=0; i<arrAttribute.size(); i++)
			if(arrAttribute.get(i).getValueSize()!=0)
				return false;
		return true;
	}

	public int indexAttributeByUri(String sUri){
		int i;
		for(i=0; i<arrAttribute.size(); i++)
			if(arrAttribute.get(i).getUri().equalsIgnoreCase(sUri))
				return i;
		return -1;
	}
	
	public boolean haveValueLabel(){
		int i, n = arrAttribute.size();
		for(i=0; i<n; i++)
			if(arrAttribute.get(i).haveValueLabel())
				return true;
		return false;
	}
	
}
