package tuwien.ldlab.statspace.codelist;

import java.util.ArrayList;

import tuwien.ldlab.statspace.model.mediator.StringCouple;

public class StandardDimensions {
	private ArrayList<StringCouple> arrDimensions;
	
	public StandardDimensions(){
		arrDimensions = new ArrayList<StringCouple>();	
		
		//current dimensions
		arrDimensions.add(new StringCouple("http://purl.org/linked-data/sdmx/2009/dimension#refArea","Reference Area"));
		arrDimensions.add(new StringCouple("http://purl.org/linked-data/sdmx/2009/dimension#refPeriod","Reference Period"));
		arrDimensions.add(new StringCouple("http://purl.org/linked-data/sdmx/2009/dimension#age","Age"));
		arrDimensions.add(new StringCouple("http://purl.org/linked-data/sdmx/2009/dimension#freq","Frequency"));
		arrDimensions.add(new StringCouple("http://purl.org/linked-data/sdmx/2009/dimension#sex","Sex"));
		arrDimensions.add(new StringCouple("http://purl.org/linked-data/sdmx/2009/dimension#currency","Currency"));
		arrDimensions.add(new StringCouple("http://purl.org/linked-data/sdmx/2009/dimension#civilStatus","Civil Status"));
		arrDimensions.add(new StringCouple("http://purl.org/linked-data/sdmx/2009/dimension#occupation","Occupation"));
		arrDimensions.add(new StringCouple("http://purl.org/linked-data/sdmx/2009/dimension#educationLev","Education Level"));
		
		//new dimensions		
		arrDimensions.add(new StringCouple("http://statisticaldata.linkedwidgets.org/dimension/activity","Economic Activity"));
		arrDimensions.add(new StringCouple("http://statisticaldata.linkedwidgets.org/dimension/expenditure","Expenditure"));
	}
	
	public String getUri(int i){return arrDimensions.get(i).getFirstString();}
	public String getLabel(int i){return arrDimensions.get(i).getSecondString();}
	public int getSize(){return arrDimensions.size();}
	public void display(){
		for(int i=0; i<arrDimensions.size(); i++)
			System.out.println(arrDimensions.get(i).getFirstString() + "\t" + arrDimensions.get(i).getSecondString());
	}
	
	/*
	 * Input: a URI represents a component e.g., dimension, measure, attribute
	 * Output: standard component in the vocabulary
	 */
	
	public String identifyReference(String s){		
		String[] arrRefArea = {"ref-area", "refarea", "country", "refdistrict", "refstate", "place", "geocode", "region"};
		String[] arrRefPeriod = {"ref-period", "ref-date","ref-year","refperiod", "timeperiod", "date", "year", "time-period"};
		String[] arrFreq = {"freq"};
		String[] arrSex = {"sex", "gender"};
		String[] arrStatus= {"civil"};
		String[] arrOccupation = {"occupation"};
		String[] arrEdu = {"educationlev", "education"};
		String[] arrCurr = {"currency"};
		String[] arrAge = {"/age","_age", "#age","refage"};
		String[] arrActivity = {"activity"};
		String[] arrFuncOfGov = {"expenditure", "funcofgov", "function of government", "functions of government" };
		String[] arrIndvCons = {"indvcons", "individual consumption"};
		String[] arrOutlayOfProducer = {"outlayofproducer", "outlay of producer", "outlays of producer"};
		String[] arrPurposeOfNPI = {"purposeofnpi", "purpose of non-profit institution", "purposes of non-profit institution" };

		int i;		
		
		//special case
		if(s.equalsIgnoreCase("http://data.cso.ie/census-2011/property/residence-1-year-b4-census"))
			return null;
	
		//refArea dimension
		for(i=0; i<arrRefArea.length;i++)
			if(s.contains(arrRefArea[i]))
				return "http://purl.org/linked-data/sdmx/2009/dimension#refArea";
					
		//refPeriod dimension
		for(i=0; i<arrRefPeriod.length;i++)
			if(s.contains(arrRefPeriod[i]))
				return "http://purl.org/linked-data/sdmx/2009/dimension#refPeriod";
		
		//Freq dimension
		for(i=0; i<arrFreq.length;i++)
			if(s.contains(arrFreq[i]))
				return "http://purl.org/linked-data/sdmx/2009/dimension#freq";
		
		//Sex dimension
		for(i=0; i<arrSex.length;i++)
			if(s.contains(arrSex[i]))
				return "http://purl.org/linked-data/sdmx/2009/dimension#sex";
		
		//Status dimension
		for(i=0; i<arrStatus.length;i++)
			if(s.contains(arrStatus[i]))
				return "http://purl.org/linked-data/sdmx/2009/dimension#civilStatus";
		
		//Occupation dimension
		for(i=0; i<arrOccupation.length;i++)
			if(s.contains(arrOccupation[i]))
				return "http://purl.org/linked-data/sdmx/2009/dimension#occupation";
		
		//Currency dimension
		for(i=0; i<arrCurr.length;i++)
			if(s.contains(arrCurr[i]))
				return "http://purl.org/linked-data/sdmx/2009/dimension#currency";
		
		//Education dimension
		for(i=0; i<arrEdu.length;i++)
			if(s.contains(arrEdu[i]))
				return "http://purl.org/linked-data/sdmx/2009/dimension#educationLev";
		
		//Age dimension
		for(i=0; i<arrAge.length;i++)
			if(s.contains(arrAge[i]))
				return "http://purl.org/linked-data/sdmx/2009/dimension#age";
		
		//Activity dimension
		for(i=0; i<arrActivity.length;i++)
			if(s.contains(arrActivity[i]))
				return "http://statisticaldata.linkedwidgets.org/dimension/activity";
		
		//FuncOfGov dimension
		for(i=0; i<arrFuncOfGov.length;i++)
			if(s.contains(arrFuncOfGov[i]))
				return "http://statisticaldata.linkedwidgets.org/dimension/expenture";
		
		//Indv Cons dimension
		for(i=0; i<arrIndvCons.length;i++)
			if(s.contains(arrIndvCons[i]))
				return "http://statisticaldata.linkedwidgets.org/dimension/expenture";
		
		//Outlay Of Producer dimension
		for(i=0; i<arrOutlayOfProducer.length;i++)
			if(s.contains(arrOutlayOfProducer[i]))
				return "http://statisticaldata.linkedwidgets.org/dimension/expenture";
		
		//Purpose Of NPI dimension
		for(i=0; i<arrPurposeOfNPI.length;i++)
			if(s.contains(arrPurposeOfNPI[i]))
				return "http://statisticaldata.linkedwidgets.org/dimension/expenture";
		
		return null;
	}
	
	public String getValueReference(String sDimension, String sUri, String sLabel){
		
		if(sDimension.equals("http://purl.org/linked-data/sdmx/2009/dimension#age")){
			CL_Age cl = new CL_Age();
			return cl.identifyReference(sUri, sLabel);
		}
		else if(sDimension.equals("http://purl.org/linked-data/sdmx/2009/dimension#freq")){
			CL_Freq cl = new CL_Freq();
			return cl.identifyReference(sUri, sLabel);
		}
		else if(sDimension.equals("http://purl.org/linked-data/sdmx/2009/dimension#sex")){
			CL_Sex cl = new CL_Sex();
			return cl.identifyReference(sUri, sLabel);
		}
		else if(sDimension.equals("http://purl.org/linked-data/sdmx/2009/dimension#currency")){
			CL_Currency cl = new CL_Currency();
			return cl.identifyReference(sUri, sLabel);
		}
		else if(sDimension.equals("http://purl.org/linked-data/sdmx/2009/dimension#civilStatus")){
			CL_Civil_Status cl = new CL_Civil_Status();
			return cl.identifyReference(sUri, sLabel);
		}
		else if(sDimension.equals("http://purl.org/linked-data/sdmx/2009/dimension#occupation")){
			CL_Occupation cl = new CL_Occupation();
			return cl.identifyReference(sUri, sLabel);
		}
		else if(sDimension.equals("http://purl.org/linked-data/sdmx/2009/dimension#educationLev")){
			CL_Education_Lev cl = new CL_Education_Lev();
			return cl.identifyReference(sUri, sLabel);
		}
		else if(sDimension.equals("http://statisticaldata.linkedwidgets.org/dimension/activity")){
			CL_Activity cl = new CL_Activity();
			return cl.identifyReference(sUri, sLabel);
		}
		else if(sDimension.equals("http://statisticaldata.linkedwidgets.org/dimension/expenditure")){
			CL_COFOG cl = new CL_COFOG();
			String s = cl.identifyReference(sUri, sLabel);
			if(s!=null) return s;
			
			CL_COICOP cl2 = new CL_COICOP();
			s = cl2.identifyReference(sUri, sLabel);
			if(s!=null) return s;
			
			CL_COPNI cl3 = new CL_COPNI();
			s = cl3.identifyReference(sUri, sLabel);
			if(s!=null) return s;
			
			CL_COPP cl4 = new CL_COPP();
			s = cl4.identifyReference(sUri, sLabel);
			if(s!=null) return s;
		}
		
		return null;
	}
	
	public void generateCodeList(){
		CL_Activity cl_activity = new CL_Activity();
		cl_activity.generateCodeList();
		
		CL_Age cl_age = new CL_Age();
		cl_age.generateCodeList();
		
		CL_Area cl_area = new CL_Area();
		cl_area.generateCodeList();
		
		CL_Civil_Status cl_civil = new CL_Civil_Status();
		cl_civil.generateCodeList();
		

		CL_COPP cl_copp = new CL_COPP();
		cl_copp.generateCodeList();
		
		CL_COFOG cl_cofog = new CL_COFOG();
		cl_cofog.generateCodeList();
		
		CL_COICOP cl_coicop = new CL_COICOP();
		cl_coicop.generateCodeList();
		
		CL_COPNI cl_copni = new CL_COPNI();
		cl_copni.generateCodeList();
		
		CL_Currency cl_currency = new CL_Currency();
		cl_currency.generateCodeList();
		
		CL_Education_Lev cl_education = new CL_Education_Lev();
		cl_education.generateCodeList();
		
		CL_Freq cl_freq = new CL_Freq();
		cl_freq.generateCodeList();
		
		CL_Occupation cl_occupation = new CL_Occupation();
		cl_occupation.generateCodeList();
		
		CL_Period cl_period = new CL_Period();
		cl_period.generateCodeList();
		
		CL_Sex cl_sex = new CL_Sex();
		cl_sex.generateCodeList();
		
		CL_Subject cl_subject = new CL_Subject();
		cl_subject.generateCodeList();
		
		CL_Unit_Measure cl_unit = new CL_Unit_Measure();
		cl_unit.generateCodeList();
		
	}
	
}
