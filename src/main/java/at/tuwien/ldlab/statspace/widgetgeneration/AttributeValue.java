package at.tuwien.ldlab.statspace.widgetgeneration;

public class AttributeValue {
	private String uri;
	private String label;
	
	public AttributeValue(){
		uri = "";
		label = "";
	}
	
	public AttributeValue(String uri, String label){
		if(label.indexOf("@")!=-1 && label.indexOf("@")==label.length()-3)
			label = label.substring(0, label.length()-3);
		this.uri = uri;
		this.label = label;
	}

	public String getUri(){return uri;}	
	public String getLabel(){return label;}
	
	public void setUri(String uri){	
		this.uri = uri;
	}	
	
	public void setLabel(String label){
		if(label.indexOf("@")!=-1 && label.indexOf("@")==label.length()-3)
			label = label.substring(0, label.length()-3);
		this.label = label;
	}	
}
