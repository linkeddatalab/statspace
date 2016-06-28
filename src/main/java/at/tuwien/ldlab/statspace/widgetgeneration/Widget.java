package at.tuwien.ldlab.statspace.widgetgeneration;

import java.io.File;
import java.util.ArrayList;

import at.tuwien.ldlab.statspace.metadata.MetaData;
import at.tuwien.ldlab.statspace.util.*;

public class Widget {
	private DataSet ds = new DataSet(); 
	private String folderTemplate;
	private String folderTarget;
	private String endpoint;
	
	public Widget(DataSet dataset, String sEndpoint, String sFolderTarget, String sFolderTemplate){
		ds = dataset;
		endpoint 		 = sEndpoint;
		folderTarget  	 = sFolderTarget;	
		folderTemplate   = sFolderTemplate;		
	}
	
	public Widget(String sFolderTarget, String sFolderTemplate){
		folderTarget   = sFolderTarget;
		folderTemplate = sFolderTemplate;
	}
	
	public void createWidgetUseSPARQLMethod(ArrayList<String> arrLocation){	
		int i, j, k, sizeD, sizeM, year, date, index;
		boolean bAllMeasure = ds.getMeasure().getBMultipleMeasure();
		String dsName, sDimension, sDimension2, sMeasure, sFilter, sFilter2;
		String sFileTemplate;
		String sTitle="", sId="", sEndpoint="", sQuery="", sQuery2="", sSize="", sBodyDimension="", sBodyMeasure="", sBodyDimension2="", sBodyMeasure2="", sTmp="", sValue2, sValue;
		String sDComponent ="", sCComponent = "", sRowsi="", sRowsi2="";	
		
		//set file name of dataset
		dsName = ds.getUri();
		dsName = Support.extractFileName(dsName);		
		
		sizeD = ds.getDimensionSize();
		sizeM = ds.getMeasureSize();
				
				
		//sTitle, sEndpoint
		sTitle    = dsName;
		sEndpoint = "\""+ endpoint + "\"";
		
		//'@id': 'http://ogd.ifs.tuwien.ac.at/vienna/betriebszweige2012-autobus',
		sId = "'@id': '" + ds.getUri() + "',";
		
		//Dimension component in JSON-LD
		sDComponent = "				'dimension': [\n";
		for(i=0; i<sizeD; i++){
			if(i<sizeD-1){
				sCComponent = sCComponent + "				'" +  Support.getName(ds.getDimensionUri(i)) + "': '" + ds.getDimensionUri(i) + "',\n";
				
				sDComponent = sDComponent + 
							"					{\n"+
							"						'@id': '" + ds.getDimensionUri(i) + "',\n"+
							"						'@type': 'http://purl.org/linked-data/cube#DimensionPropery',\n";
				if(ds.getDimensionLabel(i)!="")
					sDComponent = sDComponent + 
							"						'label': '" + ds.getDimensionLabel(i) + "'\n"+
							"					},\n";
				else
					sDComponent = sDComponent + 
							"						'label': '" + Support.getName(ds.getDimensionUri(i))+ "'\n"+
							"					},\n";	
			}
			else{
				sCComponent = sCComponent + "				'" +  Support.getName(ds.getDimensionUri(i)) + "': '" + ds.getDimensionUri(i) + "'";
				
				sDComponent = sDComponent + 
						"					{\n"+
						"						'@id': '" + ds.getDimensionUri(i) + "',\n"+
						"						'@type': 'http://purl.org/linked-data/cube#DimensionPropery',\n";
				if(ds.getDimensionLabel(i)!="")
					sDComponent = sDComponent + 
						"						'label': '" + ds.getDimensionLabel(i) + "'\n"+
						"					},\n";
				else
					sDComponent = sDComponent + 
						"						'label': '" + Support.getName(ds.getDimensionUri(i)) + "'\n"+
						"					},\n";	
			}
			
		}
		sDComponent = sDComponent + "				],";
		
		if(ds.getDimensionSize()>1)
			sBodyDimension = "<table>\n" +
					"    <tr>\n"+
					"         <td><b>Dimensions</b></td>\n"+
					"    </tr>\n";
		else
			sBodyDimension = "<table>\n" +
					"    <tr>\n"+
					"         <td><b>Dimension</b></td>\n"+
					"    </tr>\n";
		
		sBodyDimension2 = sBodyDimension;
		
		//check Year, Date Dimension
		SpecialTypeList typeList = new SpecialTypeList(folderTemplate + File.separator + "list.xml");
		year=-1; date=-1;
		for(i=0; i<ds.getDimension().getSize(); i++)
			if(typeList.isYear(ds.getDimensionUri(i))){
				year = i;
				break;
			}
		for(i=0; i<ds.getDimension().getSize(); i++)
			if(typeList.isDate(ds.getDimensionUri(i))){
				date = i;
				break;
			}		
		for(i=0; i<ds.getDimension().getSize(); i++)
			if(typeList.isCountry(ds.getDimensionUri(i))){			
				break;
			}
		
		if(arrLocation.size()>0)
			index = ds.getDimension().indexDimensionByUri(arrLocation.get(0));
		else
			index=-1;
		
		if(year!=-1){
			sValue=	"	<tr>\n" +
					"		<td><label>Year: from</label></td>\n" +
					"		<td><select id='" + Support.getName(ds.getDimensionUri(year))+ "_from'>\n";			
			for(j=0; j<ds.getDimension(year).getValueSize();j++)			
				sValue  = sValue  + "			<option value='"+ds.getDimension(year).getValueUri(j)+"'>" + Support.getName(ds.getDimension(year).getValueUri(j))+"</option>\n";
			sValue = sValue +
					"		</select></td>\n" +
					"		<td><label> to </label></td>\n" +
					"		<td><select id='" + Support.getName(ds.getDimensionUri(year))+ "_to'>\n";			
			for(j=0; j<ds.getDimension(year).getValueSize();j++)			
				sValue  = sValue  + "			<option value='"+ds.getDimension(year).getValueUri(j)+"'>" + Support.getName(ds.getDimension(year).getValueUri(j))+"</option>\n";
			sValue = sValue +
					"		</select></td>\n" +
					"	</tr>\n";			
			sBodyDimension = sBodyDimension + sValue;
			sBodyDimension2 = sBodyDimension2 + sValue;
		}
		
		//sBody - Dimension
		
		//if !digital-agenda-data
		if(!endpoint.contains("digital-agenda-data")){
			for(i=0; i<sizeD; i++){
				if(i!=year && ds.getDimension(i).getValueSize()>0){					
					sValue=	"	<tr>\n" +
							"		<td><label>"+ Support.getName(ds.getDimensionUri(i)) +"</label></td>\n";
					if(year==-1){	
						if(i==index)
							sValue = sValue +
								"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "' size='"+ (arrLocation.size()-1) + "' style='height:50px;' multiple>\n";		
						else
							sValue = sValue +
								"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "'>\n";					
					}
					else{
						if(i==index)
							sValue = sValue +
								"		<td colspan=\"3\"><select id='" + Support.getName(ds.getDimensionUri(i))+ "' size='"+ (arrLocation.size()-1) + "' style='height:50px;' multiple>\n";		
						else
							sValue = sValue +
								"		<td colspan=\"3\"><select id='" + Support.getName(ds.getDimensionUri(i)) + "'>\n";						
					}					
					if(ds.getDimension(i).getValueSize()>1 && i!=index)
						sValue  = sValue  + "			<option value=\"any value\">Any value</option>\n";		
					
					if(i==index)
						for(j=1; j<arrLocation.size(); j++)
							sValue  = sValue  + "			<option value=\""+arrLocation.get(j)+"\" selected>" + Support.getName(arrLocation.get(j))+"</option>\n";
					else
						for(j=0; j<ds.getDimension(i).getValueSize();j++)
							sValue  = sValue  + "			<option value=\""+ds.getDimension(i).getValueUri(j)+"\">" + Support.getName(ds.getDimension(i).getValueUri(j))+"</option>\n";
				
					sValue = sValue +
							"		</select></td>\n" +
							"	</tr>\n";			
					sBodyDimension = sBodyDimension + sValue;			
				}			
			}	
		}

		//sBody2 - Dimension
		
		//digital-agenda-data => order the dimension
		if(endpoint.contains("digital-agenda-data")){
			String sIndicator="", sBreakdown="", sUnit="", sCountry="", sTimePeriod="";
			for(i=0; i<sizeD; i++){
				if(ds.getDimension(i).getValueSize()>0){
					if(ds.getDimensionLabel(i).isEmpty())
						sValue2=	
							"	<tr>\n" +
							"		<td><label>"+ Support.getName(ds.getDimensionUri(i)) +"</label></td>\n";
					else
						sValue2 =
							"	<tr>\n" +
							"		<td><label>"+ ds.getDimensionLabel(i) +"</label></td>\n";				
						
					if(i==index){
						sValue2 = sValue2 +
						"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "' size='"+ (arrLocation.size()-1) + "' style='height:50px;' multiple>\n";
					
						for(j=1; j<arrLocation.size(); j++){
							for(k=0; k<ds.getDimension(i).getValueSize();k++)
								if(arrLocation.get(j).equals(ds.getDimension(i).getValueUri(k))){
									if(ds.getDimension(i).haveValueLabel())
										sValue2 = sValue2 + "			<option value=\""+ds.getDimension(i).getValueUri(k)+"\" selected>" + Support.getName(ds.getDimension(i).getValueLabel(k))+"</option>\n";
									else
										sValue2 = sValue2 + "			<option value=\""+ds.getDimension(i).getValueUri(k)+"\" selected>" + Support.getName(ds.getDimension(i).getValueUri(k))+"</option>\n";
									break;
								}
						}						
						
					}else{
						if(ds.getDimensionUri(i).contains("indicator"))
							sValue2 = sValue2 +
								"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "' onchange='changeIndicator();'>\n";
						else
							sValue2 = sValue2 +
								"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "' onchange='changeValue();'>\n";
													
						
						if(ds.getDimension(i).getValueSize()>1)
							sValue2  = sValue2  + "			<option value=\"any value\">Any value</option>\n";	
						
						for(j=0; j<ds.getDimension(i).getValueSize();j++)
							if(ds.getDimension(i).haveValueLabel())
								sValue2 = sValue2 + "			<option value=\""+ds.getDimension(i).getValueUri(j)+"\">" + Support.getName(ds.getDimension(i).getValueLabel(j))+"</option>\n";
							else
								sValue2 = sValue2 + "			<option value=\""+ds.getDimension(i).getValueUri(j)+"\">" + Support.getName(ds.getDimension(i).getValueUri(j))+"</option>\n";
					}		
					
					sValue2 = sValue2 +
							"		</select></td>\n" +
							"	</tr>\n";			
					if(sValue2.contains("<label>Indicator</label>")) sIndicator = sValue2;
					else if(sValue2.contains("<label>Breakdown</label>")) sBreakdown = sValue2;
					else if(sValue2.contains("<label>Unit of measure</label>")) sUnit = sValue2;
					else if(sValue2.contains("<label>Country</label>")) sCountry = sValue2;
					else sTimePeriod = sValue2;	
				}				
			}
			sBodyDimension2 = sBodyDimension2 + sIndicator + sBreakdown + sUnit + sCountry + sTimePeriod;
		}else
		{
			for(i=0; i<sizeD; i++){
				if(i!=year && ds.getDimension(i).getValueSize()>0){
					if(ds.getDimensionLabel(i).isEmpty())
						sValue2=	
							"	<tr>\n" +
							"		<td><label>"+ Support.getName(ds.getDimensionUri(i)) +"</label></td>\n";
					else
						sValue2 =
							"	<tr>\n" +
							"		<td><label>"+ ds.getDimensionLabel(i) +"</label></td>\n";
					if(year==-1){
						if(i==index)
							sValue2 = sValue2 +
								"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "' size='"+ (arrLocation.size()-1) + "' style='height:50px;' multiple>\n";		
						else
							sValue2 = sValue2 +
								"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "'>\n";	
					}else{
						if(i==index)
							sValue2 = sValue2 +
								"		<td colspan=\"3\"><select id='" + Support.getName(ds.getDimensionUri(i)) + "' size='"+ (arrLocation.size()-1) + "' style='height:50px;' multiple>\n";	
						else
							sValue2 = sValue2 +
								"		<td colspan=\"3\"><select id='" + Support.getName(ds.getDimensionUri(i)) + "'>\n";
						
					}
					if(ds.getDimension(i).getValueSize()>1 && i!=index)
						sValue2  = sValue2  + "			<option value=\"any value\">Any value</option>\n";	
					
					if(i==index){
						for(j=1; j<arrLocation.size(); j++){
							for(k=0; k<ds.getDimension(i).getValueSize();k++)
								if(arrLocation.get(j).equals(ds.getDimension(i).getValueUri(k))){
									if(ds.getDimension(i).haveValueLabel())
										sValue2 = sValue2 + "			<option value=\""+ds.getDimension(i).getValueUri(k)+"\" selected>" + Support.getName(ds.getDimension(i).getValueLabel(k))+"</option>\n";
									else
										sValue2 = sValue2 + "			<option value=\""+ds.getDimension(i).getValueUri(k)+"\" selected>" + Support.getName(ds.getDimension(i).getValueUri(k))+"</option>\n";
									break;
								}
						}
							
					}else{
						for(j=0; j<ds.getDimension(i).getValueSize();j++){
							if(ds.getDimension(i).haveValueLabel())
								sValue2 = sValue2 + "			<option value=\""+ds.getDimension(i).getValueUri(j)+"\">" + Support.getName(ds.getDimension(i).getValueLabel(j))+"</option>\n";
							else
								sValue2 = sValue2 + "			<option value=\""+ds.getDimension(i).getValueUri(j)+"\">" + Support.getName(ds.getDimension(i).getValueUri(j))+"</option>\n";
						}
					}
					
					sValue2 = sValue2 +
							"		</select></td>\n" +
							"	</tr>\n";			
					sBodyDimension2 = sBodyDimension2 + sValue2;
				}			
			}				
		}		
		
		sBodyDimension = sBodyDimension + "\n</table>";
		sBodyDimension2 = sBodyDimension2 + "\n</table>";	
		
		//sBody - Measure
		if(sizeM>0){
			if(sizeM>1){
				if(bAllMeasure==true){
					sTmp =  "\n<table>\n" +
							"	<tr>\n" +
							"		<td colspan=\"2\"><b>Measures</b></td>\n"+
							"	</tr>\n" +
							"	<tr>\n" +
							"		<td colspan=\"2\">" + "<input type=\"checkbox\" id=\"allmeasures\" onClick=\"checkAll();\"><label for=\"allmeasures\">All measures</label></td>\n"+
							"	</tr>\n";	
					for(i=0; i<sizeM; i++){
						sValue = 	"	<tr>\n" +
									"		<td><input type=\"checkbox\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + Support.getName(ds.getMeasureUri(i)) +"</label></td>\n";
						if(++i < sizeM)
							sValue = sValue + 
								  	"		<td><input type=\"checkbox\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + Support.getName(ds.getMeasureUri(i)) +"</label></td>\n"+
								  	"	</tr>\n";
						else
							sValue = sValue +
									"	</tr>\n";
						
						sTmp = sTmp + sValue;				
					}
				}
				else{
					sTmp =  "\n<table>\n" +
							"	<tr>\n" +
							"		<td colspan=\"2\"><b>Measures</b></td>\n"+
							"	</tr>\n";
							
					for(i=0; i<sizeM; i++){
						sValue = 	"	<tr>\n" +
									"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\">" + "<label for=\"" + i + "\">" + Support.getName(ds.getMeasureUri(i)) +"</label></td>\n";
						if(++i < sizeM)
							sValue = sValue + 
								  	"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\">" + "<label for=\"" + i + "\">" + Support.getName(ds.getMeasureUri(i)) +"</label></td>\n"+
								  	"	</tr>\n";
						else
							sValue = sValue +
									"	</tr>\n";
						
						sTmp = sTmp + sValue;				
					}
				}
			}
			else{
					sTmp = "\n<table>\n"+
							"	<tr>\n" +
							"		<td colspan=\"2\"><b>Measure</b></td>\n"+
							"	</tr>\n";				
				   sValue =	"	<tr>\n" +
							"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + 0 +"\" value=\""+ ds.getMeasureUri(0)+"\">" + "<label for=\"" + 0 + "\">" + Support.getName(ds.getMeasureUri(0)) +"</label></td>\n"+
							"	</tr>\n";
				   sTmp = sTmp + sValue;					
			}
			sTmp = sTmp + "</table>\n";
		}		
		sBodyMeasure = sBodyMeasure + sTmp;
		
		if(!ds.getMeasure().haveMeasureLabel())
			sBodyMeasure2 = sBodyMeasure2 + sTmp;
		else	
		{
			//sBody - Measure
			if(sizeM>0){
				if(sizeM>1){
					if(bAllMeasure==true){
						sBodyMeasure2 = sBodyMeasure2 +
								"\n<table>\n" +
								"	<tr>\n" +
								"		<td colspan=\"2\"><b>Measures</b></td>\n"+
								"	</tr>\n" +
								"	<tr>\n" +
								"		<td colspan=\"2\">" + "<input type=\"checkbox\" id=\"allmeasures\" onClick=\"checkAll();\"><label for=\"allmeasures\">All measures</label></td>\n"+
								"	</tr>\n";
						
						for(i=0; i<sizeM; i++){
							sValue = 	"	<tr>\n" +
										"		<td><input type=\"checkbox\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + ds.getMeasureLabel(i) +"</label></td>\n";
							if(++i < sizeM)
								sValue = sValue + 
									  	"		<td><input type=\"checkbox\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + ds.getMeasureLabel(i) +"</label></td>\n"+
									  	"	</tr>\n";
							else
								sValue = sValue +
										"	</tr>\n";				
							sBodyMeasure2 = sBodyMeasure2 + sValue;
						}		
					}else{
						sBodyMeasure2 = sBodyMeasure2 +
								"\n<table>\n" +
								"	<tr>\n" +
								"		<td colspan=\"2\"><b>Measures</b></td>\n"+
								"	</tr>\n";								
						
						for(i=0; i<sizeM; i++){
							sValue = 	"	<tr>\n" +
										"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + ds.getMeasureLabel(i) +"</label></td>\n";
							if(++i < sizeM)
								sValue = sValue + 
									  	"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + ds.getMeasureLabel(i) +"</label></td>\n"+
									  	"	</tr>\n";
							else
								sValue = sValue +
										"	</tr>\n";				
							sBodyMeasure2 = sBodyMeasure2 + sValue;
						}		
					}					
				}
				else{
					sBodyMeasure2 = sBodyMeasure2 +
							"\n<table>\n"+
							"	<tr>\n" +
							"		<td colspan=\"2\"><b>Measure</b></td>\n"+
							"	</tr>\n";
					sValue ="	<tr>\n" +
							"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + 0 +"\" value=\""+ ds.getMeasureUri(0)+"\">" + "<label for=\"" + 0 + "\">" + ds.getMeasureLabel(0) +"</label></td>\n"+
							"	</tr>\n";				
					sBodyMeasure2 = sBodyMeasure2 + sValue;							
				}				
				sBodyMeasure2 = sBodyMeasure2 + "</table>\n";
			}
		}	
		
		//sQuery		
		sMeasure="";
		sFilter="";
		
		//sQuery - sMeasure
		sMeasure = 	"var sMeasure=\"\" ;\n" + 
				   	"		$(\"input[name='measures[]']:checked\").each(function () \n" +
				   	"		{ \n"+
				   	"			sMeasure = sMeasure + \"?o <\" + this.value + \"> ?\"+ removeSpecialCharacter(getName(this.value, \"\")) + \".  \" ; \n" +
					"		}); \n" +	
					"		if(sMeasure==\"\"){ \n"+
					"			alert(\"You must choose at least one measure. Widget will run in defaul configuration with all measure(s)\"); \n"+
					"			$(\"input[name='measures[]']\").each(function () \n"+
					"			{ \n"+
					"				sMeasure = sMeasure + \"?o <\"+ this.value + \">  ?\" + removeSpecialCharacter(getName(this.value, \"\")) + \".  \" ; \n" +	
					"				this.checked = true; \n"+				
					"			}); \n";
		if(sizeM > 1)
			sMeasure = sMeasure +
					"			if($('#allmeasures').length>0) \n"+
					"				document.getElementById('allmeasures').checked = true; \n" +
					"		}";
		else
			sMeasure = sMeasure + 
					"		}";
		
		//sQuery - sDimension	
		sDimension = "";		
		sDimension2 = sDimension;
		
		for(i=0; i<sizeD; i++){
			if(ds.getDimension(i).getValueSize()>1){				
				sDimension = sDimension + "					+ \"?o <" + ds.getDimensionUri(i) + "> ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i)))  + ".   \" \n";
				if(ds.getDimension(i).haveValueLabel())
					sDimension2 = sDimension2 + "					+ \"?o <" + ds.getDimensionUri(i) + "> ?" + "uri_"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i)))  + ".   \" \n"
												+ "					+ \"?uri_"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + " <" + ds.getDimension(i).getLabelType() + "> ?"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + ". \"\n";
											
				else
					sDimension2 = sDimension2 + "					+ \"?o <" + ds.getDimensionUri(i) + "> ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i)))  + ".  \" \n";
							
			}
		}
		
		//sQuery - sFilter
		sFilter   = "" ;
		sFilter2 = sFilter; 		
		for(i=0; i<sizeD; i++){
			if(ds.getDimension(i).getValueSize()>1){ 
				if(i==year){				
					sTmp = "\n\t\t\t\t\t+ getYearFilter(\"" +	Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\"," +
							"$(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "_from\").val(), " +
							"$(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "_to\").val())";					
					sFilter   = sFilter   + sTmp;	
					sFilter2 = sFilter2 + sTmp;
				}else{				
					sFilter  = sFilter  + "\n\t\t\t\t\t+ getFilter(\"?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\", \"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\")";
					if(ds.getDimension(i).haveValueLabel())
						sFilter2 = sFilter2 + "\n\t\t\t\t\t+ getFilter(\"?uri_" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\", \"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\")";
					else
						sFilter2 = sFilter2 + "\n\t\t\t\t\t+ getFilter(\"?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\", \"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\")";
				}
			}
		}
		sQuery = sMeasure + "\n\t\t\n" +
				 "		var query =  \"PREFIX qb:    <http://purl.org/linked-data/cube#> \"+ \n"+				
				 "					 \"SELECT * \"+ \n" +
				 "					 \"WHERE {\"+ \n" +
				 "					 \"?o qb:dataSet <" + ds.getUri() + ">. \"+ \n"+		
				 "					 sMeasure  \n" +	
				 					 sDimension + 
				 					 sFilter   + "\n";
		
		sQuery2 = sMeasure + "\n\t\t\n" +
				 "		var query =  \"PREFIX qb:    <http://purl.org/linked-data/cube#> \"+ \n"+				
				 "					 \"SELECT * \"+ \n" +
				 "					 \"WHERE {\"+ \n" +
				 "					 \"?o qb:dataSet <" + ds.getUri() + ">. \"+ \n"+	
				 "					 sMeasure  \n" +	
				 					 sDimension2 + 
				 					 sFilter2   + "\n";
		
		if(year!=-1) {
			sQuery   = sQuery   +  "					+ \"}ORDER BY ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(year)))  + " ?o LIMIT 1000 \";";
			sQuery2 = sQuery2 +  "					+ \"}ORDER BY ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(year)))  + " ?o LIMIT 1000 \";";
		}
		else if(date!=-1) {
			sQuery   = sQuery   +  "					+ \"}ORDER BY ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(date))) + " ?o LIMIT 1000 \";";
			sQuery2 = sQuery2 +  "					+ \"}ORDER BY ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(date))) + " ?o LIMIT 1000 \";";
		}
		else{
			sQuery   = sQuery   +  "					+ \"}ORDER BY ?o LIMIT 1000\";";
			sQuery2 = sQuery2 +  "					+ \"}ORDER BY ?o LIMIT 1000\";";
		}
		
		
		//result['observation'][j]['breakdown'] = getName(binding["breakdown"].value, binding["breakdown"].datatype) ;
		//result['observation'][j]['unit_measure'] = getName(binding["unit_measure"].value, binding["unit_measure"].datatype);
		sRowsi = "";			
		for(i=0; i<sizeD; i++){
			if(ds.getDimension(i).getValueSize()>1){			
				sRowsi =  sRowsi + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"'] = getName(binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\'].value, binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\'].datatype);\n";				
			}else
				sRowsi =  sRowsi + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"'] = $(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\").val();\n";
		}		
		
		sRowsi2 = "";			
		for(i=0; i<sizeD; i++){
			if(ds.getDimension(i).getValueSize()>1){
				sRowsi2 =  sRowsi2 + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"'] = getName(binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\'].value, binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\'].datatype);\n";
			}else
				sRowsi2 =  sRowsi2 + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"'] = $(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\").val();\n";
		}
		if(ds.getDimensionSize()==0 || ds.getMeasureSize()==0)
			sFileTemplate = folderTemplate + File.separator + "index_error.html";
		else
			if(!endpoint.contains("digital-agenda-data"))
				sFileTemplate = folderTemplate + File.separator + "index2.html";
			else{
				sQuery = "\t\t\t\t\t+ \"FILTER(?ds = <" + ds.getUri() +">)  \"" ;
				sQuery2 = "\t\t\t\t\t+ \"FILTER(?ds = <" + ds.getUri() +">) \"" ;
				sFileTemplate = folderTemplate + File.separator + "index_digital2.html";
			}
			
		if(!endpoint.contains("gov.tso.co.uk/coins")
				&& (ds.getDimension().haveValueLabel()||ds.getMeasure().haveMeasureLabel()))
			FileOperation.readFileIndex(folderTarget+ File.separator + dsName + ".html", sFileTemplate,  sTitle, sId, sEndpoint, sQuery2, sSize, sRowsi2, sCComponent, sDComponent, "", sBodyDimension2, sBodyMeasure2 );		
		else
			FileOperation.readFileIndex(folderTarget+ File.separator + dsName + ".html", sFileTemplate,  sTitle, sId, sEndpoint, sQuery, sSize, sRowsi, sCComponent, sDComponent, "", sBodyDimension, sBodyMeasure);

	}	
	

	public void createWidgetUseRMLMethod(MetaData md, String dsName){
		int i, j, n;	
		String sFileTemplate, sFileTarget;
		String sTitle="", sQuery="", sBody="";
				
		//get name of dataset	
		dsName = Support.extractFileName(dsName);	
		
		//Title
		sTitle = dsName;
		
		//Variables 
     	String result = md.getJSONFormat();     	
    	sQuery = 			"    	var data1 = JSON.parse('"+ result + "');\n";   
    	sQuery = sQuery +	"	   	var vars  = data1.head.vars;\n";     	
     	sQuery = sQuery + 	"		var count = " + md.getNumberofComponent() + ";\n";
     	sQuery = sQuery + 	"		var label = '" + md.getDataSet().getLabel() + "';\n";     	
     	
		
		//Body     	
     	n = md.getNumberofComponent();
     	ArrayList<String> arrSelect = new ArrayList<String>();
     	for(i=2; i<n; i++){
     		String s = "\n		<select id=\""+md.getComponent(i).getVariable().substring(1)+ "\" onchange=\"updateChart()\" size=\"5\" multiple>\n";
     		ArrayList<String> arrDistinctValue = md.getDistinctRefValueLabel(i);			     	
     		for(j=0; j<arrDistinctValue.size(); j++)
     			s = s + "\n				<option value=\"" + arrDistinctValue.get(j) + "\">" + arrDistinctValue.get(j) + "</option>\n";
     		s = s + "		</select>\n";
     		arrSelect.add(s);
     	}
     	for(i=2; i<n; i++){
     		if(i==2)
		     	sBody =	
		     		"	<tr>\n"+ 
     				"		<td class='d"+ i%9 +"'>"+ md.getComponent(i).getLabel()+"		</td>\n"+
     				"		<td class='d"+ i%9 +"'>"+ arrSelect.get(i-2) +"		</td>\n"+     		
     				"	</tr>\n";
     		else
     			sBody = sBody + 	
		     		"	<tr>\n"+ 
     				"		<td class='d"+ i%9 +"'>"+ md.getComponent(i).getLabel()+"		</td>\n"+
     				"		<td class='d"+ i%9 +"'>"+ arrSelect.get(i-2) +"		</td>\n"+		         				
     				"	</tr>\n";
     	}
		sFileTemplate 	= folderTemplate + File.separator + "index_rml.html";
		sFileTarget 	= folderTarget+ File.separator + dsName + ".html";
		FileOperation.readFileIndex(sFileTarget, sFileTemplate,  sTitle, "", "", sQuery, "", "", "", "", sBody, "", "");
	}
	
	public void createWidgetFile(){	
		int i, j, sizeD, sizeM, year, date;
		boolean bAllMeasure = ds.getMeasure().getBMultipleMeasure();
		String dsName, sDimension, sDimension2, sMeasure, sFilter, sFilter2;
		String fileTemplate;
		String sTitle="", sId="", sEndpoint="", sQuery="", sQuery2="", sSize="", sBody="", sBody2="", sTmp="", sValue2, sValue;
		String sDComponent ="", sCComponent = "", sRowsi="", sRowsi2="";	
		
		//get name of dataset
		dsName = ds.getUri();
		dsName = Support.extractFileName(dsName);	
			
		sizeD = ds.getDimensionSize();
		sizeM = ds.getMeasureSize();
				
    	//sTitle, sEndpoint
		sTitle    = dsName;
		sEndpoint = "\""+ endpoint + "\"";
		
		//'@id': 'http://ogd.ifs.tuwien.ac.at/vienna/betriebszweige2012-autobus',
		sId = "'@id': '" + ds.getUri() + "',";
		
		//Dimension component in JSON-LD
		sDComponent = "			'dimension': [\n";
		for(i=0; i<sizeD; i++){
			if(i<sizeD-1){
				sCComponent = sCComponent + "				'" +  Support.getName(ds.getDimensionUri(i)) + "': '" + ds.getDimensionUri(i) + "',\n";
				
				sDComponent = sDComponent + 
							"				{\n"+
							"					'@id': '" + ds.getDimensionUri(i) + "',\n"+
							"					'@type': 'http://purl.org/linked-data/cube#DimensionPropery',\n";
				if(ds.getDimensionLabel(i)!="")
					sDComponent = sDComponent + 
							"					'label': '" + ds.getDimensionLabel(i) + "'\n"+
							"				},\n";
				else
					sDComponent = sDComponent + 
							"					'label': '" + Support.getName(ds.getDimensionUri(i))+ "'\n"+
							"				},\n";	
			}
			else{
				sCComponent = sCComponent + "				'" +  Support.getName(ds.getDimensionUri(i)) + "': '" + ds.getDimensionUri(i) + "'";
				
				sDComponent = sDComponent + 
						"				{\n"+
						"					'@id': '" + ds.getDimensionUri(i) + "',\n"+
						"					'@type': 'http://purl.org/linked-data/cube#DimensionPropery',\n";
				if(ds.getDimensionLabel(i)!="")
					sDComponent = sDComponent + 
						"					'label': '" + ds.getDimensionLabel(i) + "'\n"+
						"				},\n";
				else
					sDComponent = sDComponent + 
						"					'label': '" + Support.getName(ds.getDimensionUri(i)) + "'\n"+
						"				},\n";	
			}
			
		}
		sDComponent = sDComponent + "			],";
		
		//Attribute
		if(ds.getAttributeSize()==1){
			sCComponent = sCComponent + ",\n				'" +  Support.getName(ds.getAttributeUri(0)) + "': '" + ds.getAttributeUri(0) + "'\n";
			
			sDComponent = sDComponent +
						"\n			'attribute': [\n"+
						"				{\n"+
						"					'@id': '" + ds.getAttributeUri(0) + "',\n"+
						"					'@type': 'http://purl.org/linked-data/cube#AttributePropery',\n";
			if(ds.getAttributeLabel(0)!="")
				sDComponent = sDComponent + 
						"					'label': '" + ds.getAttributeLabel(0) + "'\n"+
						"				},\n";
			else
				sDComponent = sDComponent + 
						"					'label': '" + Support.getName(ds.getAttributeUri(0))+ "'\n"+
						"				},\n";
			sDComponent = sDComponent + "			],";
		}else{
			sDComponent = sDComponent +
			   		  "\n			'attribute': [],";					
		}
		
		if(ds.getDimensionSize()>1)
			sBody = "<table>\n" +
					"    <tr>\n"+
					"         <td><b>Dimensions</b></td>\n"+
					"    </tr>\n";
		else
			sBody = "<table>\n" +
					"    <tr>\n"+
					"         <td><b>Dimension</b></td>\n"+
					"    </tr>\n";
		
		sBody2 = sBody;
		
		//check Year, Date Dimension
		SpecialTypeList typeList = new SpecialTypeList(folderTemplate + File.separator + "list.xml");
		year=-1; date=-1;
		for(i=0; i<ds.getDimension().getSize(); i++)
			if(typeList.isYear(ds.getDimensionUri(i))){
				year = i;
				break;
			}
		for(i=0; i<ds.getDimension().getSize(); i++)
			if(typeList.isDate(ds.getDimensionUri(i))){
				date = i;
				break;
			}		
		for(i=0; i<ds.getDimension().getSize(); i++)
			if(typeList.isCountry(ds.getDimensionUri(i))){				
				break;
			}
		
		if(year!=-1){
			sValue=	"	<tr>\n" +
					"		<td><label>Year: from</label></td>\n" +
					"		<td><select id='" + Support.getName(ds.getDimensionUri(year))+ "_from'>\n";			
			for(j=0; j<ds.getDimension(year).getValueSize();j++)			
				sValue  = sValue  + "			<option value='"+ds.getDimension(year).getValueUri(j)+"'>" + Support.getName(ds.getDimension(year).getValueUri(j))+"</option>\n";
			sValue = sValue +
					"		</select></td>\n" +
					"		<td><label> to </label></td>\n" +
					"		<td><select id='" + Support.getName(ds.getDimensionUri(year))+ "_to'>\n";			
			for(j=0; j<ds.getDimension(year).getValueSize();j++)			
				sValue  = sValue  + "			<option value='"+ds.getDimension(year).getValueUri(j)+"'>" + Support.getName(ds.getDimension(year).getValueUri(j))+"</option>\n";
			sValue = sValue +
					"		</select></td>\n" +
					"	</tr>\n";			
			sBody = sBody + sValue;
			sBody2 = sBody2 + sValue;
		}
		
		//sBody - Dimension
		
		//if !digital-agenda-data
		if(!folderTarget.contains("digital-agenda-data")){
			for(i=0; i<sizeD; i++){
				if(i!=year && ds.getDimension(i).getValueSize()>0){					
					sValue=	"	<tr>\n" +
							"		<td><label>"+ Support.getName(ds.getDimensionUri(i)) +"</label></td>\n";
					if(year==-1){						
						sValue = sValue +
								"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "'>\n";					
					}
					else{						
						sValue = sValue +
						"		<td colspan=\"3\"><select id='" + Support.getName(ds.getDimensionUri(i)) + "'>\n";						
					}				
					if(ds.getDimension(i).getValueSize()>1)
						sValue  = sValue  + "			<option value=\"any value\">Any value</option>\n";		
					
					for(j=0; j<ds.getDimension(i).getValueSize();j++)	{
						if(j>5000) break;
						sValue  = sValue  + "			<option value=\""+ds.getDimension(i).getValueUri(j)+"\">" + Support.getName(ds.getDimension(i).getValueUri(j))+"</option>\n";
					}
				
					sValue = sValue +
							"		</select></td>\n" +
							"	</tr>\n";			
					sBody = sBody + sValue;			
				}			
			}
			
			//Attribute
			if(ds.getAttributeSize()==1){
				sValue=	"	<tr>\n" +
						"		<td><b><label>"+ Support.getName(ds.getAttributeUri(0)) +"</label></b></td>\n";
				if(year==-1){						
					sValue = sValue +
							"		<td><select id='" + Support.getName(ds.getAttributeUri(0))+ "'>\n";					
				}
				else{						
					sValue = sValue +
					"		<td colspan=\"3\"><select id='" + Support.getName(ds.getAttributeUri(0)) + "'>\n";						
				}
				if(ds.getAttribute(0).getValueSize()>1)
					sValue  = sValue  + "			<option value=\"any value\">Any value</option>\n";		
				
				for(j=0; j<ds.getAttribute(0).getValueSize();j++){					
					sValue  = sValue  + "			<option value=\""+ds.getAttribute(0).getValueUri(j)+"\">" + Support.getName(ds.getAttribute(0).getValueUri(j))+"</option>\n";
				}
			
				sValue = sValue +
						"		</select></td>\n" +
						"	</tr>\n";			
				sBody = sBody + sValue;						
			}
			
		}

		//sBody2 - Dimension
		
		//digital-agenda-data => order the dimension
		if(folderTarget.contains("digital-agenda-data")){
			String sIndicator="", sBreakdown="", sUnit="", sCountry="", sTimePeriod="";
			for(i=0; i<sizeD; i++){
				if(ds.getDimension(i).getValueSize()>0){
					if(ds.getDimensionLabel(i).isEmpty())
						sValue2=	
							"	<tr>\n" +
							"		<td><label>"+ Support.getName(ds.getDimensionUri(i)) +"</label></td>\n";
					else
						sValue2 =
							"	<tr>\n" +
							"		<td><label>"+ ds.getDimensionLabel(i) +"</label></td>\n";				
						
					if(ds.getDimensionUri(i).contains("indicator"))
						sValue2 = sValue2 +
							"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "' onchange='changeIndicator();'>\n";
					else
						sValue2 = sValue2 +
							"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "' onchange='changeValue();'>\n";
						
					
					if(ds.getDimension(i).getValueSize()>1)
						sValue2  = sValue2  + "			<option value=\"any value\">Any value</option>\n";	
					
					for(j=0; j<ds.getDimension(i).getValueSize();j++){
						if(j>5000) break;
						if(ds.getDimension(i).haveValueLabel())
							sValue2 = sValue2 + "			<option value=\""+ds.getDimension(i).getValueUri(j)+"\">" + Support.getName(ds.getDimension(i).getValueLabel(j))+"</option>\n";
						else
							sValue2 = sValue2 + "			<option value=\""+ds.getDimension(i).getValueUri(j)+"\">" + Support.getName(ds.getDimension(i).getValueUri(j))+"</option>\n";
					}
					
					sValue2 = sValue2 +
							"		</select></td>\n" +
							"	</tr>\n";			
					if(sValue2.contains("<label>Indicator</label>")) sIndicator = sValue2;
					else if(sValue2.contains("<label>Breakdown</label>")) sBreakdown = sValue2;
					else if(sValue2.contains("<label>Unit of measure</label>")) sUnit = sValue2;
					else if(sValue2.contains("<label>Country</label>")) sCountry = sValue2;
					else sTimePeriod = sValue2;	
				}				
			}
			sBody2 = sBody2 + sIndicator + sBreakdown + sUnit + sCountry + sTimePeriod;
		}else{
			for(i=0; i<sizeD; i++){
				if(i!=year && ds.getDimension(i).getValueSize()>0){
					if(ds.getDimensionLabel(i).isEmpty())
						sValue2=	
							"	<tr>\n" +
							"		<td><label>"+ Support.getName(ds.getDimensionUri(i)) +"</label></td>\n";
					else
						sValue2 =
							"	<tr>\n" +
							"		<td><label>"+ ds.getDimensionLabel(i) +"</label></td>\n";
					if(year==-1){
						sValue2 = sValue2 +
							"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "'>\n";
						
					}else{
						sValue2 = sValue2 +
							"		<td colspan=\"3\"><select id='" + Support.getName(ds.getDimensionUri(i)) + "'>\n";
						
					}
					if(ds.getDimension(i).getValueSize()>1)
						sValue2  = sValue2  + "			<option value=\"any value\">Any value</option>\n";	
					
					for(j=0; j<ds.getDimension(i).getValueSize();j++){
						if(j>5000) break;
						if(ds.getDimension(i).haveValueLabel())
							sValue2 = sValue2 + "			<option value=\""+ds.getDimension(i).getValueUri(j)+"\">" + Support.getName(ds.getDimension(i).getValueLabel(j))+"</option>\n";
						else
							sValue2 = sValue2 + "			<option value=\""+ds.getDimension(i).getValueUri(j)+"\">" + Support.getName(ds.getDimension(i).getValueUri(j))+"</option>\n";
					}
					
					sValue2 = sValue2 +
							"		</select></td>\n" +
							"	</tr>\n";			
					sBody2 = sBody2 + sValue2;
				}			
			}			

			//Attribute
			if(ds.getAttributeSize()==1){
				if(ds.getAttributeLabel(0).isEmpty())
					sValue2=	
						"	<tr>\n" +
						"		<td><b><label>"+ Support.getName(ds.getAttributeUri(0)) +"</label></b></td>\n";
				else
					sValue2 =
						"	<tr>\n" +
						"		<td><b><label>"+ ds.getAttributeLabel(0) +"</label></b></td>\n";
				if(year==-1){
					sValue2 = sValue2 +
						"		<td><select id='" + Support.getName(ds.getAttributeUri(0))+ "'>\n";
					
				}else{
					sValue2 = sValue2 +
						"		<td colspan=\"3\"><select id='" + Support.getName(ds.getAttributeUri(0)) + "'>\n";
					
				}
				if(ds.getAttribute(0).getValueSize()>1)
					sValue2  = sValue2  + "			<option value=\"any value\">Any value</option>\n";	
				
				for(j=0; j<ds.getAttribute(0).getValueSize();j++)
					if(ds.getAttribute(0).haveValueLabel())
						sValue2 = sValue2 + "			<option value=\""+ds.getAttribute(0).getValueUri(j)+"\">" + Support.getName(ds.getAttribute(0).getValueLabel(j))+"</option>\n";
					else
						sValue2 = sValue2 + "			<option value=\""+ds.getAttribute(0).getValueUri(j)+"\">" + Support.getName(ds.getAttribute(0).getValueUri(j))+"</option>\n";
				
				sValue2 = sValue2 +
						"		</select></td>\n" +
						"	</tr>\n";			
				sBody2 = sBody2 + sValue2;
			}			
		}		
		
		sBody = sBody + "\n</table>";
		sBody2 = sBody2 + "\n</table>";	
		
		//sBody - Measure
		if(sizeM>0){
			if(sizeM>1){
				if(bAllMeasure==true){
					sTmp =  "\n<table>\n" +
							"	<tr>\n" +
							"		<td colspan=\"2\"><b>Measures</b></td>\n"+
							"	</tr>\n" +
							"	<tr>\n" +
							"		<td colspan=\"2\">" + "<input type=\"checkbox\" id=\"allmeasures\" onClick=\"checkAll();\"><label for=\"allmeasures\">All measures</label></td>\n"+
							"	</tr>\n";	
					for(i=0; i<sizeM; i++){
						sValue = 	"	<tr>\n" +
									"		<td><input type=\"checkbox\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + Support.getName(ds.getMeasureUri(i)) +"</label></td>\n";
						if(++i < sizeM)
							sValue = sValue + 
								  	"		<td><input type=\"checkbox\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + Support.getName(ds.getMeasureUri(i)) +"</label></td>\n"+
								  	"	</tr>\n";
						else
							sValue = sValue +
									"	</tr>\n";
						
						sTmp = sTmp + sValue;				
					}
				}
				else{
					sTmp =  "\n<table>\n" +
							"	<tr>\n" +
							"		<td colspan=\"2\"><b>Measures</b></td>\n"+
							"	</tr>\n";
							
					for(i=0; i<sizeM; i++){
						sValue = 	"	<tr>\n" +
									"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\">" + "<label for=\"" + i + "\">" + Support.getName(ds.getMeasureUri(i)) +"</label></td>\n";
						if(++i < sizeM)
							sValue = sValue + 
								  	"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\">" + "<label for=\"" + i + "\">" + Support.getName(ds.getMeasureUri(i)) +"</label></td>\n"+
								  	"	</tr>\n";
						else
							sValue = sValue +
									"	</tr>\n";
						
						sTmp = sTmp + sValue;				
					}
				}
			}
			else{
					sTmp = "\n<table>\n"+
							"	<tr>\n" +
							"		<td colspan=\"2\"><b>Measure</b></td>\n"+
							"	</tr>\n";				
				   sValue =	"	<tr>\n" +
							"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + 0 +"\" value=\""+ ds.getMeasureUri(0)+"\">" + "<label for=\"" + 0 + "\">" + Support.getName(ds.getMeasureUri(0)) +"</label></td>\n"+
							"	</tr>\n";
				   sTmp = sTmp + sValue;					
			}
			sTmp = sTmp + "</table>\n";
		}		
		sBody = sBody + sTmp;
		
		if(!ds.getMeasure().haveMeasureLabel())
			sBody2 = sBody2 + sTmp;
		else	
		{
			//sBody - Measure
			if(sizeM>0){
				if(sizeM>1){
					if(bAllMeasure==true){
						sBody2 = sBody2 +
								"\n<table>\n" +
								"	<tr>\n" +
								"		<td colspan=\"2\"><b>Measures</b></td>\n"+
								"	</tr>\n" +
								"	<tr>\n" +
								"		<td colspan=\"2\">" + "<input type=\"checkbox\" id=\"allmeasures\" onClick=\"checkAll();\"><label for=\"allmeasures\">All measures</label></td>\n"+
								"	</tr>\n";
						
						for(i=0; i<sizeM; i++){
							sValue = 	"	<tr>\n" +
										"		<td><input type=\"checkbox\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + ds.getMeasureLabel(i) +"</label></td>\n";
							if(++i < sizeM)
								sValue = sValue + 
									  	"		<td><input type=\"checkbox\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + ds.getMeasureLabel(i) +"</label></td>\n"+
									  	"	</tr>\n";
							else
								sValue = sValue +
										"	</tr>\n";				
							sBody2 = sBody2 + sValue;
						}		
					}else{
						sBody2 = sBody2 +
								"\n<table>\n" +
								"	<tr>\n" +
								"		<td colspan=\"2\"><b>Measures</b></td>\n"+
								"	</tr>\n";								
						
						for(i=0; i<sizeM; i++){
							sValue = 	"	<tr>\n" +
										"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + ds.getMeasureLabel(i) +"</label></td>\n";
							if(++i < sizeM)
								sValue = sValue + 
									  	"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + ds.getMeasureLabel(i) +"</label></td>\n"+
									  	"	</tr>\n";
							else
								sValue = sValue +
										"	</tr>\n";				
							sBody2 = sBody2 + sValue;
						}		
					}					
				}
				else{
					sBody2 = sBody2 +
							"\n<table>\n"+
							"	<tr>\n" +
							"		<td colspan=\"2\"><b>Measure</b></td>\n"+
							"	</tr>\n";
					sValue ="	<tr>\n" +
							"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + 0 +"\" value=\""+ ds.getMeasureUri(0)+"\">" + "<label for=\"" + 0 + "\">" + ds.getMeasureLabel(0) +"</label></td>\n"+
							"	</tr>\n";				
					sBody2 = sBody2 + sValue;							
				}				
				sBody2 = sBody2 + "</table>\n";
			}
		}		
		
		//sQuery		
		sMeasure="";
		sFilter="";
		
		//sQuery - sMeasure
		sMeasure = 	"var sMeasure=\"\" ;\n" + 
				   	"		$(\"input[name='measures[]']:checked\").each(function () \n" +
				   	"		{ \n"+
				   	"			sMeasure = sMeasure + \"?o <\" + this.value + \"> ?\"+ removeSpecialCharacter(getName(this.value, \"\")) + \".  \" ; \n" +
					"		}); \n" +	
					"		if(sMeasure==\"\"){ \n"+
					"			alert(\"You must choose at least one measure. Widget will run in defaul configuration with all measure(s)\"); \n"+
					"			$(\"input[name='measures[]']\").each(function () \n"+
					"			{ \n"+
					"				sMeasure = sMeasure + \"?o <\"+ this.value + \">  ?\" + removeSpecialCharacter(getName(this.value, \"\")) + \".  \" ; \n" +	
					"				this.checked = true; \n"+				
					"			}); \n";
		if(sizeM > 1)
			sMeasure = sMeasure +
					"			if($('#allmeasures').length>0) \n"+
					"				document.getElementById('allmeasures').checked = true; \n" +
					"		}";
		else
			sMeasure = sMeasure + 
					"		}";
		
		//sQuery - sDimension	
		sDimension = "";		
		sDimension2 = sDimension;
		
		for(i=0; i<sizeD; i++){
			if(ds.getDimension(i).getValueSize()>1){				
				sDimension = sDimension + "					+ \"?o <" + ds.getDimensionUri(i) + "> ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i)))  + ".   \" \n";
				if(ds.getDimension(i).haveValueLabel())
					sDimension2 = sDimension2 + "					+ \"?o <" + ds.getDimensionUri(i) + "> ?" + "uri_"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i)))  + ".   \" \n"
												+ "					+ \"?uri_"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + " <" + ds.getDimension(i).getLabelType()  + "> " + " ?"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + ". \"\n";
											
				else
					sDimension2 = sDimension2 + "					+ \"?o <" + ds.getDimensionUri(i) + "> ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i)))  + ".   \" \n";
			}
		}
		//sQuery - Attribute
		if(ds.getAttributeSize()==1 && ds.getAttribute(0).getValueSize()>1){
			sDimension   = sDimension   + "					+ \"?o <" + ds.getAttributeUri(0) + "> ?" + Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0)))  + ".   \" \n";
			sDimension2 = sDimension2 + "					+ \"?o <" + ds.getAttributeUri(0) + "> ?" + Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0)))  + ".   \" \n";
		}		
		
		//sQuery - sFilter
		sFilter   = "" ;
		sFilter2 = sFilter; 	
	
		for(i=0; i<sizeD; i++){
			if(ds.getDimension(i).getValueSize()>1){ 
				if(i==year){				
					sTmp = "\n\t\t\t\t\t+ getYearFilter(\"?" +	Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\"," +
							"$(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "_from\").val(), " +
							"$(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "_to\").val())";					
					sFilter   = sFilter   + sTmp;	
					sFilter2 = sFilter2 + sTmp;
				}else{				
					sFilter  = sFilter  + "\n\t\t\t\t\t+ getFilter(\"?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\", \"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\")";
					if(ds.getDimension(i).haveValueLabel())
						sFilter2 = sFilter2 + "\n\t\t\t\t\t+ getFilter(\"?uri_" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\", \"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\")";
					else
						sFilter2 = sFilter2 + "\n\t\t\t\t\t+ getFilter(\"?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\", \"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\")";
				}
			}
		}
		if(ds.getAttributeSize()==1 && ds.getAttribute(0).getValueSize()>1){
			sFilter = sFilter + "\n\t\t\t\t\t+ getFilter(\"?" + Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) + "\", \"#" + Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) + "\")";
			sFilter2 = sFilter2 + "\n\t\t\t\t\t+ getFilter(\"?" + Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) + "\", \"#" + Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) + "\")";
		}
			
		sQuery = sMeasure + "\n\t\t\n" +
				 "		var query =  \"PREFIX qb:    <http://purl.org/linked-data/cube#> \"+ \n"+				
				 "					 \"SELECT * \"+ \n" +
				 "					 \"WHERE {\"+ \n" +
				 "					 \"?o qb:dataSet <" + ds.getUri() + ">. \"+ \n"+		
				 "					 sMeasure  \n" +	
				 					 sDimension + 
				 					 sFilter   + "\n";
		
		sQuery2 = sMeasure + "\n\t\t\n" +
				 "		var query =  \"PREFIX qb:    <http://purl.org/linked-data/cube#> \"+ \n"+				
				 "					 \"SELECT * \"+ \n" +
				 "					 \"WHERE {\"+ \n" +
				 "					 \"?o qb:dataSet <" + ds.getUri() + ">. \"+ \n"+		
				 "					 sMeasure  \n" +	
				 					 sDimension2 + 
				 					 sFilter2   + "\n";
		
		if(ds.getBUseDistinct()){
			if(year!=-1) {
				sQuery   = sQuery   +  "					+ \"}ORDER BY ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(year)))  + " ?o LIMIT 1000 \";";
				sQuery2 = sQuery2 +  "					+ \"}ORDER BY ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(year)))  + " ?o LIMIT 1000 \";";
			}
			else if(date!=-1) {
				sQuery   = sQuery   +  "					+ \"}ORDER BY ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(date))) + " ?o LIMIT 1000 \";";
				sQuery2 = sQuery2 +  "					+ \"}ORDER BY ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(date))) + " ?o LIMIT 1000 \";";
			}
			else{
				sQuery   = sQuery   +  "					+ \"}ORDER BY ?o LIMIT 1000\";";
				sQuery2 = sQuery2 +  "					+ \"}ORDER BY ?o LIMIT 1000\";";
			}
		}else{
			sQuery   = sQuery   +  "					+ \"}LIMIT 1000\";";
			sQuery2 = sQuery2 +  "					+ \"}LIMIT 1000\";";
		}
				
		
		//result['observation'][j]['breakdown'] = getName(binding["breakdown"].value, binding["breakdown"].datatype) ;
		//result['observation'][j]['unit_measure'] = getName(binding["unit_measure"].value, binding["unit_measure"].datatype);
		sRowsi = "";			
		for(i=0; i<sizeD; i++){
			if(ds.getDimension(i).getValueSize()>1){			
				sRowsi =  sRowsi + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"'] = getName(binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\'].value, binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\'].datatype);\n";				
			}else
				sRowsi =  sRowsi + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"'] = $(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\").val();\n";
		}		
		
		sRowsi2 = "";			
		for(i=0; i<sizeD; i++){
			if(ds.getDimension(i).getValueSize()>1){
				sRowsi2 =  sRowsi2 + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"'] = getName(binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\'].value, binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\'].datatype);\n";
			}else
				sRowsi2 =  sRowsi2 + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"'] = $(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\").val();\n";
		}
		
		if(ds.getAttributeSize()==1){
			if(ds.getAttribute(0).getValueSize()>1){
				sRowsi   =  sRowsi   + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) +"'] = getName(binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) +"\'].value, binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) +"\'].datatype);\n";
				sRowsi2 =  sRowsi2 + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) +"'] = getName(binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) +"\'].value, binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) +"\'].datatype);\n";
			}else{
				sRowsi   =  sRowsi   + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) +"'] = $(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) + "\").val();\n";
				sRowsi2 =  sRowsi2 + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) +"'] = $(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) + "\").val();\n";
			}
		}
		
		if(ds.getDimensionSize()==0 || ds.getMeasureSize()==0)
			fileTemplate = folderTemplate + File.separator + "index_error.html";
		else
			if(!folderTarget.contains("digital-agenda-data"))
				fileTemplate = folderTemplate + File.separator + "index.html";
			else{
				sQuery = "\t\t\t+ \"FILTER(?ds = <" + ds.getUri() +">)\"" ;
				sQuery2 = "\t\t\t+ \"FILTER(?ds = <" + ds.getUri() +">)\"" ;
				fileTemplate = folderTemplate + File.separator + "index_digital.html";
			}
			
		if(!endpoint.contains("gov.tso.co.uk/coins")
				&& (ds.getDimension().haveValueLabel()||ds.getMeasure().haveMeasureLabel()))
			FileOperation.readFileIndex(folderTarget+ File.separator + dsName + ".html", fileTemplate,  sTitle, sId, sEndpoint, sQuery2, sSize, sRowsi2, sCComponent, sDComponent, sBody2, "", "");		
		else
			FileOperation.readFileIndex(folderTarget+ File.separator + dsName + ".html", fileTemplate,  sTitle, sId, sEndpoint, sQuery, sSize, sRowsi, sCComponent, sDComponent, sBody, "", "");
		
		//RDFDescription.createDescription(ds, folder_endpoint+"\\"+dsName+"\\data.rdf", endpoint);
	}	
}
