package be.ugent.mmlab.rml.model;

import java.util.ArrayList;

public class Parameters {
	
	ArrayList<Parameter> arrPar;
	
	public Parameters(){
		arrPar = new ArrayList<Parameter>();		
	}
	
	public Parameter getParameter(int i){
		return arrPar.get(i);
	}
	
	public void addParameterName(String sVarName){
		int i;
		if(arrPar.size()==0)
			arrPar.add(new Parameter(sVarName, "", ""));
		else{
			for(i=0; i<arrPar.size(); i++){
				if(arrPar.get(i).getName().equalsIgnoreCase(sVarName))
					break;
			}
			if(i==arrPar.size())
				arrPar.add(new Parameter(sVarName, "", ""));
		}			
	}
	
	public void addParameterDefValue(String sVarName, String sDefValue){
		int i;
		if(arrPar.size()==0)
			arrPar.add(new Parameter(sVarName, sDefValue, ""));
		else{
			for(i=0; i<arrPar.size(); i++){
				if(arrPar.get(i).getName().equalsIgnoreCase(sVarName))
					break;
			}
			if(i==arrPar.size())
				arrPar.add(new Parameter(sVarName, sDefValue, ""));
			else
				arrPar.get(i).setDefaultValue(sDefValue);
		}			
	}
	
	public void addParameterValue(String sVarName, String sValue){
		int i;
		if(arrPar.size()==0)
			arrPar.add(new Parameter(sVarName, "", sValue));
		else{
			for(i=0; i<arrPar.size(); i++){
				if(arrPar.get(i).getName().equalsIgnoreCase(sVarName))
					break;
			}
			if(i==arrPar.size())
				arrPar.add(new Parameter(sVarName, "", sValue));
			else
				arrPar.get(i).setValue(sValue);
		}			
	}
	
	public void addParameterValue(String sVarName, String sDefValue, String sValue){
		int i;
		if(arrPar.size()==0)
			arrPar.add(new Parameter(sVarName, sDefValue, sValue));
		else{
			for(i=0; i<arrPar.size(); i++){
				if(arrPar.get(i).getName().equalsIgnoreCase(sVarName))
					break;
			}
			if(i==arrPar.size())
				arrPar.add(new Parameter(sVarName, sDefValue, sValue));
			else{
				arrPar.get(i).setDefaultValue(sDefValue);
				arrPar.get(i).setValue(sValue);
			}
		}			
	}
	
	public int getSize(){return arrPar.size();}
	
	public void display(){
		for(int i=0; i<arrPar.size(); i++)
			arrPar.get(i).display();
	}
	
	
	public class Parameter{	
		String sVarName;
		String sDefaultValue;
		String sValue;
		
		public Parameter(){
			sVarName="";
			sDefaultValue="";
			sValue="";
		}
		public Parameter(String sVar, String sDefValue, String sV){
			sVarName = sVar;
			sDefaultValue = sDefValue;
			sValue = sV;
		}
		public void setName(String s){sVarName=s;}
		public void setDefaultValue(String s){sDefaultValue=s;}
		public void setValue(String s){sValue=s;}
		
		public String getName(){return sVarName;}
		public String getDefaultValue(){return sDefaultValue;}
		public String getValue(){return sValue;}
		
		public void display(){
			System.out.println(sVarName+"\t"+sDefaultValue+"\t"+sValue);
		}
	}
	
}
