package at.tuwien.ldlab.statspace.metadata.concrete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Endpoints {
	private  ArrayList<EndpointMetaData> arrEndpoint;
	
	public Endpoints(){
		arrEndpoint = new ArrayList<EndpointMetaData>();	
//		arrEndpoint.add(new EndpointMetaData("http://data.europa.eu/euodp/en/linked-data",  
//		Arrays.asList(""), "", "European Union Open Data Portal"));		
		arrEndpoint.add(new EndpointMetaData("http://semantic.eea.europa.eu/sparql",  
		Arrays.asList(""), "", "European Environment Agency (EEA)"));
//		arrEndpoint.add(new EndpointMetaData("http://cr.eionet.europa.eu/sparql", 
//		Arrays.asList(	"http://rdfdata.eionet.europa.eu/eurostat/data/crim_gen", 
//					  	"http://rdfdata.eionet.europa.eu/eurostat/data/demo_pjanbroad"), "", "European Environment Information and Obseration Network (EIONET)"));
//		arrEndpoint.add(new EndpointMetaData("http://unodc.publicdata.eu/sparql", 
//		Arrays.asList(""), "", "United Nations Office on Drugs and Crime"));
//		arrEndpoint.add(new EndpointMetaData("http://data.cso.ie/sparql", 
//		Arrays.asList(""), "Ireland", "Central Statistics Office of Ireland"));
//		arrEndpoint.add(new EndpointMetaData("http://ogd.ifs.tuwien.ac.at/sparql",  
//		Arrays.asList(""), "Vienna", "TU Wien"));
//		arrEndpoint.add(new EndpointMetaData("http://statistics.gov.scot/sparql",  
//		Arrays.asList(	"http://statistics.gov.scot/data/alcohol-related-discharge",
//						"http://statistics.gov.scot/data/business-births-deaths-and-survival-rates",
//						"http://statistics.gov.scot/data/child-dental-health",
//						"http://statistics.gov.scot/data/crime-clear-up-rates",
//						"http://statistics.gov.scot/data/crime-survey",
//						"http://statistics.gov.scot/data/employment",
//						"http://statistics.gov.scot/data/full-time-employment",
//						"http://statistics.gov.scot/data/greenhouse-gas",
//						"http://statistics.gov.scot/data/healthy-life-expectancy",
//						"http://statistics.gov.scot/data/household-waste",
//						"http://statistics.gov.scot/data/life-expectancy",
//						"http://statistics.gov.scot/data/municipal-waste",
//						"http://statistics.gov.scot/data/poverty",
//						"http://statistics.gov.scot/data/public-transport",
//						"http://statistics.gov.scot/data/recorded-crime",
//						"http://statistics.gov.scot/data/renewable-electricity",
//						"http://statistics.gov.scot/data/road-safety",
//						"http://statistics.gov.scot/data/road-transport-expenditure",
//						"http://statistics.gov.scot/data/road-vehicles",
//						"http://statistics.gov.scot/data/scottish-health-survey",
//						"http://statistics.gov.scot/data/scottish-house-condition-survey",
//						"http://statistics.gov.scot/data/scottish-household-survey",
//						"http://statistics.gov.scot/data/earnings"), "Scotland", "The Scottish Statistics"));
//		arrEndpoint.add(new EndpointMetaData("http://opendatacommunities.org/sparql", 
//		Arrays.asList(	"http://opendatacommunities.org/data/homelessness-decisions",
//						"http://opendatacommunities.org/data/homelessness-acceptances",
//						"http://opendatacommunities.org/data/household-projections",
//						"http://opendatacommunities.org/data/housing-market/ratio/house-prices-ratio/med-house-price-to-earnings",
//						"http://opendatacommunities.org/data/households/projections2012/households"), "", "Department for Communities and Local Government"));			
	}	
	
	public EndpointMetaData getEndpointMetaData(int i){
		return arrEndpoint.get(i);
	}
		
	public String getEndpoint(int i){return arrEndpoint.get(i).getEndpointUri();}
	public String getCountryName(int i){return arrEndpoint.get(i).getCountryName();}
	public String getDataProvider(int i){return arrEndpoint.get(i).getDataProvider();}
	public int getSize(){return arrEndpoint.size();}
	
	public class EndpointMetaData{
		private String sUri;
		private String sCountryName;		
		private String sDataProvider;
		private ArrayList<String> arrDataSet;
		
		public EndpointMetaData(String sEndpoint, List<String> arrDs, String sCountry, String sProvider){
			sUri = sEndpoint;
			sCountryName = sCountry;	
			sDataProvider = sProvider;
			arrDataSet = new ArrayList<String>();
			int i=0;
			while(i<arrDs.size()){
				if(arrDs.get(i)!="")
					arrDataSet.add(arrDs.get(i));					
				i++;				
			}
		}
    	public String getEndpointUri(){return sUri;}
		public String getCountryName(){return sCountryName;}
		public String getDataProvider(){return sDataProvider;}
		public int getNumberofDS(){return arrDataSet.size();}
		public String getDataSetUri(int i){return arrDataSet.get(i);}
		
		public boolean containDataSet(String sUri){
			int j;
			if(arrDataSet.size()==0) return true;
			for(j=0; j<arrDataSet.size(); j++)
				if(sUri.equalsIgnoreCase(arrDataSet.get(j)))
					return true;
			return false;		
		}
	}
	
	
}
