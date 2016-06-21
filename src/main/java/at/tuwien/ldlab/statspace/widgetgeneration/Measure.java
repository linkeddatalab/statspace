package at.tuwien.ldlab.statspace.widgetgeneration;

import at.tuwien.ldlab.statspace.util.Support;

public class Measure{
	private String uri;	
	private String label;
	
	public Measure(){		
		uri = "";
		label = "";
	}
	
	public Measure(String uri, String label){
		if(label=="")
			label=Support.getName(uri);
		if(label.indexOf("@")!=-1 && label.indexOf('@')==label.length()-3)
			label = label.substring(0, label.length()-3);
		label = label.substring(0, 1).toUpperCase() + label.substring(1);
		this.uri = uri;
		this.label = label;
	}
	
	public void setUri(String name){		
		this.uri = name;
	}
	public void setLabel(String label){
		if(label.indexOf("@")!=-1 && label.indexOf('@')==label.length()-3)
			label = label.substring(0, label.length()-3);
		label = label.substring(0, 1).toUpperCase() + label.substring(1);
		this.label = label;
	}
	public String getUri(){return this.uri;}
	public String getLabel(){return this.label;}	
}
	