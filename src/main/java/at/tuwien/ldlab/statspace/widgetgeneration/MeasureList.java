package at.tuwien.ldlab.statspace.widgetgeneration;

import java.util.ArrayList;

public class MeasureList {
	private ArrayList<Measure> arrMeasure;
	private boolean bMulipleMeasure;
	
	public MeasureList(){
		arrMeasure = new ArrayList<Measure>();	
		bMulipleMeasure = false;
	}
	
	public void addMeasure(String uri, String label){
		int i;		
		for(i=0; i<arrMeasure.size(); i++)
			if(arrMeasure.get(i).getUri().equalsIgnoreCase(uri))		
				break;		
		if(i==arrMeasure.size()){
			Measure oneMeasure = new Measure(uri, label);
			arrMeasure.add(oneMeasure);
		}else
			if(label.contains("@en"))
				arrMeasure.get(i).setLabel(label);		
	}
	
	public int indexOf(String uri){
		int i;
		for(i=0; i<arrMeasure.size(); i++)
			if(arrMeasure.get(i).getUri().equalsIgnoreCase(uri))		
				return i;
		return -1;
	}
	
	public void removeMeasure(String uri){
		int i;
		for(i=0; i<arrMeasure.size(); i++)
			if(arrMeasure.get(i).getUri().equals(uri))
				arrMeasure.remove(i);
	}
	
	public String getMeasureUri(int i){
		if(!arrMeasure.isEmpty() && 0<=i && i<arrMeasure.size())
			return arrMeasure.get(i).getUri();
		else
			return "";
	}
	
	public String getMeasureLabel(int i){
		if(!arrMeasure.isEmpty() && 0<=i && i<arrMeasure.size())
			return arrMeasure.get(i).getLabel();
		else
			return "";
	}
	
	public boolean getBMultipleMeasure(){return bMulipleMeasure;}
	
	public void setBMultipleMeasure(boolean bM){bMulipleMeasure=bM;}

	public int getSize(){
		return arrMeasure.size();
	}
	
	public void clearAll(){
		arrMeasure.clear();		
	}
	
	public boolean haveMeasureLabel(){
		int i;
		for(i=0; i<arrMeasure.size(); i++)
			if(arrMeasure.get(i).getLabel().equals(""))
				return false;
		return true;
	}

}
