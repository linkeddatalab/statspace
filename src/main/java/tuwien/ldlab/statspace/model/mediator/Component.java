package tuwien.ldlab.statspace.model.mediator;

import java.util.ArrayList;

public class Component {
	private String sVariable;
	private String sFilterValue;
	private String sUri;	
	private String sReference;
	private String sType;
	private boolean bHiddenProperty;
	private String sLabel;	
	private ArrayList<StringCouple> arrValue;
	private String sVariableLabel;
	
	public Component(){
		sVariable="";
		sFilterValue="";
		sUri="";
		sReference="";
		sType="";
		sLabel="";
		bHiddenProperty = false;
		arrValue = new ArrayList<StringCouple>();
		sVariableLabel="";
	}
	
	public Component(String uri,String type,String label, String ref){
		sVariable	= "";
		sFilterValue="";
		sUri 		= uri;
		sReference 	= ref;
		sType  		= type;
		sLabel 		= label;	
		bHiddenProperty = false;
		if(sType.endsWith("HiddenProperty"))
			bHiddenProperty = true;
		arrValue 	= new ArrayList<StringCouple>();
		sVariableLabel = "";
	}
	
	public Component(Component cp){
		sVariable	= cp.getVariable();
		sFilterValue= cp.getFilterValue();
		sUri 		= cp.getUri();
		sReference 	= cp.getUriReference();
		sType  		= cp.getType();
		bHiddenProperty = cp.getHiddenStatus();			
		sLabel 		= cp.getLabel();	
		arrValue 	= new ArrayList<StringCouple>();
		sVariableLabel = cp.getVariableLabel();
		for(int i=0; i<cp.getValueSize(); i++){
			arrValue.add(new StringCouple(cp.getValue(i), cp.getValueReference(i)));
		}
	}
	

	public int indexOf(String refValue){
		for(int i=0; i<arrValue.size(); i++)
			if(arrValue.get(i).getSecondString().equalsIgnoreCase(refValue))
				return i;
		return -1;				
	}
	public void setHiddenStatus(boolean b){bHiddenProperty = b;}
	public void setVariable(String s){sVariable=s;}
	public void setFilterValue(String s){sFilterValue=s;}
	public void setUri(String s){sUri=s;}
	public void setReference(String s){sReference=s;}
	public void setType(String s){sType=s;}
	public void setLabel(String s){sLabel=s;}
	public void setVariableLabel(String s){sVariableLabel=s;}
	public void addValue(String value, String vref){arrValue.add(new StringCouple(value, vref));}
	public void setValueRefence(int index, String vref){arrValue.get(index).setSecondString(vref);}
	public void setValue(int index, String v){arrValue.get(index).setFirstString(v);}
	public void removeValue(int index){arrValue.remove(index);}
	
	public String getVariable(){return sVariable;}
	public String getFilterValue(){return sFilterValue;}
	public String getUri(){return sUri;}
	public String getUriReference(){return sReference;}
	public String getType(){return sType;}
	public String getLabel(){return sLabel;}
	public boolean getHiddenStatus(){return bHiddenProperty;}
	public String getVariableLabel(){return sVariableLabel;}	
	public int getValueSize(){return arrValue.size();}
	public String getValue(int i){return arrValue.get(i).getFirstString();}
	public String getValueReference(int i){return arrValue.get(i).getSecondString();}
	
//	public void display(){
//		System.out.println("Variable: " + sVariable);
//		System.out.println("FilterValue: " + sFilterValue);
//		System.out.println("Uri: " + sUri);
//		System.out.println("Reference: " + sReference);
//		System.out.println("Type: " + sType);
//		System.out.println("Label: " + sLabel);
//		System.out.println("FixedValue: " + sFixedValue);		
//	}
}
