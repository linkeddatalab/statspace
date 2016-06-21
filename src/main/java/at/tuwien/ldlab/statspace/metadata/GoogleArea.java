package at.tuwien.ldlab.statspace.metadata;

import java.util.ArrayList;

public class GoogleArea {
	private ArrayList<String> types;
	private String uri;
	private String fullname;
	private double lat;
	private double lng;
	
	public GoogleArea(GoogleArea googleArea){
		types = new ArrayList<String>();
		for(int i=0; i<googleArea.getTypeSize(); i++)
			types.add(googleArea.getType(i));		
		uri = googleArea.getUri();
		fullname = googleArea.getFullname();
		lat=googleArea.getLat();
		lng=googleArea.getLng();
	}
	
	public GoogleArea(){
		types = new ArrayList<String>();
		uri = "";
		fullname = "";		
	}
	
	public void addType(String type){types.add(type);}
	public void setUri(String s){uri = s;}
	public void setFullname(String s){fullname = s;}
	public void setLat(double l){lat = l;}
	public void setLng(double l){lng = l;}
		
	public ArrayList<String> getAllTypes(){return types;}
	public int getTypeSize(){return types.size();}
	public String getType(int i){return types.get(i);}
	public String getUri(){return uri;}
	public String getFullname(){return fullname;}
	public double getLat(){return lat;}
	public double getLng(){return lng;}
	
	public void display(){
		System.out.println(fullname+ ";" + uri);
		for(int i=0; i<types.size();i++)
			System.out.print(types.get(i)+";");
		System.out.println();
	}

	public int getUriLength(){
		return uri.split("\\/").length;
	}
}


