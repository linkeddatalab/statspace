package at.tuwien.ldlab.statspace.util;

import java.util.ArrayList;



public class DataSetBlackList {
	private  ArrayList<String> arrExact;
	private  ArrayList<String> arrStartWith;
	
	public DataSetBlackList(){
		arrExact = new ArrayList<String>();
		arrStartWith = new ArrayList<String>();		
		arrExact.add("http://eurostat.linked-statistics.org/data/env_air_emis");
		
		arrStartWith.add("http://ogd.ifs.tuwien.ac.at/vienna/Prognose");	
		arrStartWith.add("http://linkedwidgets.org/ontology/resource/");
		arrStartWith.add("http://finance.data.gov.uk/coins/coins_fact_table");		
	}	
	
	public boolean inBlackList(String s){
		int i;
		for(i=0; i<arrExact.size(); i++)
			if(arrExact.get(i).equalsIgnoreCase(s))
				return true;
		for(i=0; i<arrStartWith.size(); i++)
			if(s.startsWith(arrStartWith.get(i)))
				return true;
		
		return false;
	}
}
