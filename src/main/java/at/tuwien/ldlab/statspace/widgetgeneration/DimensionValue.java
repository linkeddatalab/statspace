package at.tuwien.ldlab.statspace.widgetgeneration;

public class DimensionValue {
	private String uri;
	private String label;

	public DimensionValue(){
		uri = "";
		label = "";
	}
	
	public DimensionValue(String uri, String label){
		if(label.indexOf("@")!=-1 && label.indexOf("@")==label.length()-3)
			label = label.substring(0, label.length()-3);
		this.uri = uri;
		this.label = label;	
	}
	public String getUri(){return uri;}	
	public String getLabel(){return label;}
	public String storeUri(){
		int k = uri.indexOf("^^");
		if(k!=-1)
			return uri.substring(0,k);
		else
			return uri;
	}
	
	public void setUri(String uri){	
		this.uri = uri;
	}	
	public void setLabel(String label){
		if(label.indexOf("@")!=-1 && label.indexOf("@")==label.length()-3)
			label = label.substring(0, label.length()-3);
		this.label = label;
	}
	
	public boolean isValueYear(){		
		if(uri.indexOf("^^")!=-1 && uri.indexOf("#gYear")!=-1)
				return true;		
		return false;
	}
	
	public boolean isValueDate(){		
		if(uri.indexOf("^^")!=-1 && uri.indexOf("#date")!=-1)
				return true;		
		return false;
	}
	
	
}
