package at.tuwien.ldlab.statspace.util;

import at.tuwien.ldlab.statspace.metadata.GoogleArea;

public class Support {
	
	public static String getName(String sUri){
		if(sUri=="")
			return "";
		while(sUri.endsWith("#")||sUri.endsWith("/"))
			sUri = sUri.substring(0, sUri.length()-1);
		
		int n, k;
		if(sUri.contains("http:")){
			/* Date, Year
			 * 2002^^http://..../XMLSchema#date
			 */			
			k = sUri.indexOf("^^");
			if(k!=-1){
				if(sUri.indexOf("#gYear")!=-1)
					sUri = sUri.substring(0,4);
				else
					sUri = sUri.substring(0,k);			
				return sUri;
			}			
			/* Sex
			 * http://..../code#sex-F
			 */
			k = sUri.indexOf("#");
			if(k!=-1 && k!=sUri.length()-1){
				sUri = sUri.substring(k+1);				
				while((k=sUri.indexOf("-"))!=-1)
					sUri = sUri.substring(0, k) + "_" + sUri.substring(k+1);			
				while((k=sUri.indexOf("+"))!=-1)
					sUri = sUri.substring(0, k) + "_" + sUri.substring(k+1);			
				while((k=sUri.indexOf(")"))!=-1)
					sUri = sUri.substring(0, k) + "_" + sUri.substring(k+1);			
				while((k=sUri.indexOf("("))!=-1)
					sUri = sUri.substring(0, k) + "_" + sUri.substring(k+1);
				return sUri;
			}
			
			/*
			 * http://..../refDistrict
			 *Special case: http://reference.data.gov.uk/id/gregorian-interval/2013-02-25T00:00:00/PT1D 
			 */
			if(sUri.endsWith("/PT1D")) sUri = sUri.substring(0, sUri.length()-5);
			
			n = sUri.length()-2;
			while(n>=0 && sUri.charAt(n)!='/') n--;		
			sUri = sUri.substring(n+1);	
			while((k=sUri.indexOf("-"))!=-1)
				sUri = sUri.substring(0, k) + "_" + sUri.substring(k+1);
			while((k=sUri.indexOf("+"))!=-1)
				sUri = sUri.substring(0, k) + "_" + sUri.substring(k+1);
			while((k=sUri.indexOf(")"))!=-1)
				sUri = sUri.substring(0, k) + "_" + sUri.substring(k+1);			
			while((k=sUri.indexOf("("))!=-1)
				sUri = sUri.substring(0, k) + "_" + sUri.substring(k+1);	
		}else{
			/* Other - label
			 * Austria@en
			 */
			k = sUri.indexOf("@");
			if(k!=-1 && k==sUri.length()-3)
				sUri = sUri.substring(0, k);
			
			sUri = sUri.replace("%2F", "_");
		}		
		return sUri;
	}
	
	public static String removeSpecialCharacter(String sUri){
		int i;
		String s="";
		for(i=0; i<sUri.length(); i++)
			if(0<sUri.charAt(i) && sUri.charAt(i) <127)
				s = s + sUri.charAt(i);
		return s;
	}
	
	public static String getFilter(String sUri, String sValue){
		/*?refDistrict = <http://..../District.01>
		*/
		int k;
		k = sValue.indexOf("http");
		if(k==0){
			return ("FILTER( "+ sUri + "!= <" + sValue + ">) ");
		}else if(k>0){
			/*
			   refYear        2010-01-01 00:00:00^^<http://www.w3.org/2001/XMLSchema#gYear>
			*/
			k = sValue.indexOf("^^");
			if(k>0){
				String stemp = sValue.substring(0,k);
				sValue = sValue.substring(k+2);
				if(sValue.indexOf("XMLSchema#gYear")!=-1)
					stemp = stemp.substring(0,4);
				return ("FILTER( "+ sUri + "!='" + stemp + "'^^<" + sValue + ">) ");
			}
			else{
				return "";
			}
		}else{
			/*?refBrand = 'Toyota'
			*/
			k = sValue.indexOf("@");
			if(k>0){
				return ("FILTER( " + sUri + "!='" + sValue.substring(0,k) +"'" + sValue.substring(k) +") ");
			}
			else
				return ("FILTER( " + sUri + "!='" + sValue +"') ");			
			
		}
		
	}
	
	public static boolean ignoreAreaLabel(String sLabel){
		String[] sArray = {"other ", "not ", "rest ", "all "};
		int i, n = sArray.length;
		for(i=0; i<n; i++)
			if(sLabel.toLowerCase().startsWith(sArray[i])){
//				System.out.println("Remove area "+ sLabel);
				return true;		
			}
		
		String[] sArray2 = {"eu", "all", "africa"};
		n = sArray2.length;
		for(i=0; i<n; i++)
			if(sLabel.toLowerCase().equals(sArray2[i])){
//				System.out.println("Remove area: "+ sLabel);
				return true;
			}
		
		String[] sArray3 = {"north america", "south america", "asia", "europe",  "antarctica", "eurasia", "afro-eurasia"};
		n = sArray3.length;
		for(i=0; i<n; i++)
			if(sLabel.toLowerCase().contains(sArray3[i])){
//				System.out.println("Remove area: "+ sLabel);
				return true;
			}
		
		return false;
	}
	
	public static boolean ignoreComponent(String sUri){
		String[] sArray = {"http://purl.org/dc", "#type", "#dataset", "decimal", "md5", "#label", "#comment", "publishstate"};
		int i, n = sArray.length;
		for(i=0; i<n; i++)
			if(sUri.contains(sArray[i]))
				return true;	
		
		return false;
	}
	
	
	public static boolean ignoreType(String sUri){
		String[] sArray = {"annotation", "objectproperty"};
		int i, n = sArray.length;
		for(i=0; i<n; i++)
			if(sUri.contains(sArray[i]))
				return true;	
		
		return false;
	}
	
	public static String removeSpecialCharacterInFileName(String sUri){
		if(sUri.startsWith("http"))
			sUri = sUri.substring(7);
		sUri = sUri.replaceAll("[^a-zA-Z0-9.-]", "_");
		return sUri;
	}
	
//	public static String extractFolderName(String endpointURI){	
//		int i=0;
//		endpointURI = endpointURI.substring(7);		
//		while(i<endpointURI.length() && endpointURI.charAt(i)!='.' && endpointURI.charAt(i)!='-') 
//			i++;
//		i++;
//		while(i<endpointURI.length() && endpointURI.charAt(i)!='.' && endpointURI.charAt(i)!='-'&& endpointURI.charAt(i)!='/') 
//			i++;
//		
//		return endpointURI.substring(0, i);		
//	}
	
	public static String extractFolderName(String sUri){	
		return removeSpecialCharacterInFileName(sUri);
	}
	
	public static String extractFileName(String sUri){	
		int i=sUri.length()-1;			
		while(i>0 && sUri.charAt(i)!='/') 
			i--;
		i--;
		while(i>0 && sUri.charAt(i)!='/') 
			i--;
		String s = sUri.substring(i+1);
		s = removeSpecialCharacterInFileName(s);
		return s;		
	}
		
	/* Input: name and type
	 * Output: This is a dimension or not?
 	 * For example 
 	 *      scomponent:  <http://purl.org/linked-data/sdmx/2009/dimension#refPeriod>
 	 *      stype:		 <http://purl.org/linked-data/cube#DimensionProperty>
 	 *      svalue: 	 <http://finance.data.gov.uk/def/coins/time/o2009m>
 	 *      
 	 *      scomponent:  <http://finance.data.gov.uk/dsd/coins/measure/amount>
 	 *      stype 		 <http://purl.org/linked-data/cube#MeasureProperty>
 	 *      svalue:  	"927"^^<http://www.w3.org/2001/XMLSchema#long>
 	 *      
 	 *      
 	 *         
 	 *         Return		D		M		A
	 *         0			100%	0%		0%
 	 *         1			0%		100%	0%
 	 *         2			0%		0%		100%
 	 *         3			50%		0%		0%
 	 *         4			0%		50%		0%
 	 *         5			50%		50%		0%
 	 *         6			0%		0%		0%

	 */
	public static int detectProperty(String sComponent, String sType, String sValue){	
		sType = sType.toLowerCase();
		sComponent = sComponent.toLowerCase();
		sValue = sValue.toLowerCase();
		int d=0, m=0;	
		
		if(Support.ignoreType(sType) ||	Support.ignoreComponent(sComponent))
			return 6;
		
		if(sType.contains("dimension") || sComponent.contains("dimension"))				
			return 0;	
		
		if(sType.contains("#measureproperty") || (sComponent.contains("measure") && !sComponent.contains("attribute")))
			return 1;	
		
		if(sType.contains("#attributeproperty") || sComponent.contains("attribute") || sComponent.contains("/unit")|| sComponent.contains("unitmeasure"))
			return 2;
		
		/*Special cases */
		if(sComponent.equals("http://rdfdata.eionet.europa.eu/lrtap/ontology/pollutantname")
//				||	sComponent.equals("http://rdfdata.eionet.europa.eu/eurostat/property#dest")||
//				sComponent.equals("http://rdfdata.eionet.europa.eu/eurostat/property#natvessr")||
//				sComponent.equals("http://rdfdata.eionet.europa.eu/eurostat/property#pres")||
//				sComponent.equals("http://rdfdata.eionet.europa.eu/eurostat/property#species")
				)
			return 0;
		
		//review value of dimension
		if(sComponent.contains("year") || sComponent.contains("date") ||sComponent.contains("time") ||
				sValue.contains("year") || sValue.contains("#date") || sValue.contains("time") ||
				(sValue.contains("http:") && !sValue.contains("^^http:"))) 
			d=1;
		
		//review value of measure
		if(sValue.contains("#int")||sValue.contains("#long")||sValue.contains("#float")||sValue.contains("#double")||sValue.contains("#decimal"))
			m=1;
			
		if(m==0){
			int i;
			char ch;
			for(i=0; i<sValue.length(); i++){
				ch = sValue.charAt(i);
				if(!((ch>='0' && ch<='9')||(ch=='-')||(ch=='.')||(ch=='+')))
					break;
			}
			if(i==sValue.length())
				m= 1;
		}
		
		if(d==1 && m==0) return 3;
		if(d==0 && m==1) return 4;
		if(d==1 && m==1) return 5;		
		System.out.println("Can not suggest: " + sComponent + ";" + sType + ";" + sValue);
		return 6;
	}
	
	
	public static boolean isLong(String sValue){
		int i;
		char ch;
		for(i=0; i<sValue.length(); i++){
			ch = sValue.charAt(i);
			if(!((i==0 && ch=='-') ||('0'<=ch && ch<='9')))
					return false;
		}
		return true;
	}
	
	public static boolean isNumber(String sValue){
		int i;
		char ch;
		for(i=0; i<sValue.length(); i++){
			ch = sValue.charAt(i);
			if(!((i==0 && ch=='-') ||('0'<=ch && ch<='9') || ch=='.'))
					return false;
		}
		return true;
	}
	
	public static int compare(String sValue1, String sValue2){	
		sValue1 = Support.getName(sValue1);
		sValue2 = Support.getName(sValue2);
		try{
			if(Support.isLong(sValue1) && Support.isLong(sValue2)){
				long l1, l2;
				l1 = Long.parseLong(sValue1);
				l2 = Long.parseLong(sValue2);
				if(l1>l2) return 1;
				if(l1==l2) return 0;
				return -1;
			}			
			if(Support.isNumber(sValue1) && Support.isNumber(sValue2)){
				double d1, d2;
				d1=Double.parseDouble(sValue1);
				d2=Double.parseDouble(sValue2);
				if(d1>d2) return 1;
				if(d1==d2) return 0;
				return -1;
			}
		}catch(Exception e){
			return sValue1.compareToIgnoreCase(sValue2);
		}
		
		return sValue1.compareToIgnoreCase(sValue2);		
		
	}
	
	public static String removeExtraInfo(String sName){
		int k;
		for(k=sName.length()-1; k>=0; k--)
			if(sName.charAt(k)==';'){
				sName=sName.substring(0, k);
				return sName;
			}	
		System.out.println("Error in remove Random Number" + sName);
		return "";
	}
	
	public static double distance(GoogleArea ga1, GoogleArea ga2) {
		double lat1, lng1, lat2, lng2;
		lat1 = ga1.getLat(); lng1 = ga1.getLng();
		lat2 = ga2.getLat(); lng2 = ga2.getLng();
	    double earthRadius = 3958.75; // miles (or 6371.0 kilometers)
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double sindLat = Math.sin(dLat / 2);
	    double sindLng = Math.sin(dLng / 2);
	    double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
	            * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;

	    return dist;
	 }
	
	
}
