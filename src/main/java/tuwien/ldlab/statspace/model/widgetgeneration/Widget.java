package tuwien.ldlab.statspace.model.widgetgeneration;

import java.io.File;
import java.util.ArrayList;

import tuwien.ldlab.statspace.model.mediator.MetaData;
import tuwien.ldlab.statspace.model.util.*;

public class Widget {
	private DataSet ds = new DataSet(); 
	private String folder_template;
	private String folder_target;
	private String endpoint;
	private int index;
	
	public Widget(DataSet dataset, int i_position, String s_endpoint, String f_target, String f_template){
		ds = dataset;
		endpoint 		 = s_endpoint;
		folder_target  	 = f_target;	
		folder_template  = f_template;
		index			 = i_position; //index of this dataset in the datasource
	}
	
	public Widget(String f_target, String f_template){
		folder_target   = f_target;
		folder_template = f_template;
	}
	
	public void createWidgetUseSPARQLMethod(ArrayList<String> arrLocation){	
		int i, j, k, size_d, size_m, year, date, index;
		boolean bAllMeasure = ds.getMeasure().getBMultipleMeasure();
		String dsName, sDimension, sDimension_2, sMeasure, sFilter, sFilter_2;
		String file_template;
		String sTitle="", sId="", sEndpoint="", sQuery="", sQuery_2="", sSize="", sBody_Dimension="", sBody_Measure="", sBody2_Dimension="", sBody2_Measure="", sTmp="", sValue_2, sValue;
		String sDComponent ="", sCComponent = "", sRowsi="", sRowsi_2="";	
		
		//get name of dataset
		dsName = ds.getUri();
		dsName = Support.getName(dsName);	
		dsName = dsName.replaceAll("%2F", "_");
//		dsName = index + "_"+ dsName;
		
		
		size_d = ds.getDimensionSize();
		size_m = ds.getMeasureSize();
				
				
		//sTitle, sEndpoint
		sTitle    = dsName;
		sEndpoint = "\""+ endpoint + "\"";
		
		//'@id': 'http://ogd.ifs.tuwien.ac.at/vienna/betriebszweige2012-autobus',
		sId = "'@id': '" + ds.getUri() + "',";
		
		//Dimension component in JSON-LD
		sDComponent = "				'dimension': [\n";
		for(i=0; i<size_d; i++){
			if(i<size_d-1){
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
			sBody_Dimension = "<table>\n" +
					"    <tr>\n"+
					"         <td><b>Dimensions</b></td>\n"+
					"    </tr>\n";
		else
			sBody_Dimension = "<table>\n" +
					"    <tr>\n"+
					"         <td><b>Dimension</b></td>\n"+
					"    </tr>\n";
		
		sBody2_Dimension = sBody_Dimension;
		
		//check Year, Date Dimension
		SpecialTypeList typeList = new SpecialTypeList(folder_template + File.separator + "list.xml");
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
			sBody_Dimension = sBody_Dimension + sValue;
			sBody2_Dimension = sBody2_Dimension + sValue;
		}
		
		//sBody - Dimension
		
		//if !digital-agenda-data
		if(!endpoint.contains("digital-agenda-data")){
			for(i=0; i<size_d; i++){
				if(i!=year && ds.getDimension(i).getValueSize()>0){					
					sValue=	"	<tr>\n" +
							"		<td><label>"+ Support.getName(ds.getDimensionUri(i)) +"</label></td>\n";
					if(year==-1){	
						if(i==index)
							sValue = sValue +
								"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "' size='"+ (arrLocation.size()-1) + "' style='height:30px;' multiple disabled>\n";		
						else
							sValue = sValue +
								"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "'>\n";					
					}
					else{
						if(i==index)
							sValue = sValue +
								"		<td colspan=\"3\"><select id='" + Support.getName(ds.getDimensionUri(i))+ "' size='"+ (arrLocation.size()-1) + "' style='height:30px;' multiple disabled>\n";		
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
					sBody_Dimension = sBody_Dimension + sValue;			
				}			
			}	
		}

		//sBody2 - Dimension
		
		//digital-agenda-data => order the dimension
		if(endpoint.contains("digital-agenda-data")){
			String sIndicator="", sBreakdown="", sUnit="", sCountry="", sTimePeriod="";
			for(i=0; i<size_d; i++){
				if(ds.getDimension(i).getValueSize()>0){
					if(ds.getDimensionLabel(i).isEmpty())
						sValue_2=	
							"	<tr>\n" +
							"		<td><label>"+ Support.getName(ds.getDimensionUri(i)) +"</label></td>\n";
					else
						sValue_2 =
							"	<tr>\n" +
							"		<td><label>"+ ds.getDimensionLabel(i) +"</label></td>\n";				
						
					if(i==index){
						sValue_2 = sValue_2 +
						"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "' size='"+ (arrLocation.size()-1) + "' style='height:30px;' multiple disabled>\n";
					
						for(j=1; j<arrLocation.size(); j++){
							for(k=0; k<ds.getDimension(i).getValueSize();k++)
								if(arrLocation.get(j).equals(ds.getDimension(i).getValueUri(k))){
									if(ds.getDimension(i).haveValueLabel())
										sValue_2 = sValue_2 + "			<option value=\""+ds.getDimension(i).getValueUri(k)+"\" selected>" + Support.getName(ds.getDimension(i).getValueLabel(k))+"</option>\n";
									else
										sValue_2 = sValue_2 + "			<option value=\""+ds.getDimension(i).getValueUri(k)+"\" selected>" + Support.getName(ds.getDimension(i).getValueUri(k))+"</option>\n";
									break;
								}
						}						
						
					}else{
						if(ds.getDimensionUri(i).contains("indicator"))
							sValue_2 = sValue_2 +
								"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "' onchange='changeIndicator();'>\n";
						else
							sValue_2 = sValue_2 +
								"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "' onchange='changeValue();'>\n";
													
						
						if(ds.getDimension(i).getValueSize()>1)
							sValue_2  = sValue_2  + "			<option value=\"any value\">Any value</option>\n";	
						
						for(j=0; j<ds.getDimension(i).getValueSize();j++)
							if(ds.getDimension(i).haveValueLabel())
								sValue_2 = sValue_2 + "			<option value=\""+ds.getDimension(i).getValueUri(j)+"\">" + Support.getName(ds.getDimension(i).getValueLabel(j))+"</option>\n";
							else
								sValue_2 = sValue_2 + "			<option value=\""+ds.getDimension(i).getValueUri(j)+"\">" + Support.getName(ds.getDimension(i).getValueUri(j))+"</option>\n";
					}		
					
					sValue_2 = sValue_2 +
							"		</select></td>\n" +
							"	</tr>\n";			
					if(sValue_2.contains("<label>Indicator</label>")) sIndicator = sValue_2;
					else if(sValue_2.contains("<label>Breakdown</label>")) sBreakdown = sValue_2;
					else if(sValue_2.contains("<label>Unit of measure</label>")) sUnit = sValue_2;
					else if(sValue_2.contains("<label>Country</label>")) sCountry = sValue_2;
					else sTimePeriod = sValue_2;	
				}				
			}
			sBody2_Dimension = sBody2_Dimension + sIndicator + sBreakdown + sUnit + sCountry + sTimePeriod;
		}else
		{
			for(i=0; i<size_d; i++){
				if(i!=year && ds.getDimension(i).getValueSize()>0){
					if(ds.getDimensionLabel(i).isEmpty())
						sValue_2=	
							"	<tr>\n" +
							"		<td><label>"+ Support.getName(ds.getDimensionUri(i)) +"</label></td>\n";
					else
						sValue_2 =
							"	<tr>\n" +
							"		<td><label>"+ ds.getDimensionLabel(i) +"</label></td>\n";
					if(year==-1){
						if(i==index)
							sValue_2 = sValue_2 +
								"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "' size='"+ (arrLocation.size()-1) + "' style='height:30px;' multiple disabled>\n";		
						else
							sValue_2 = sValue_2 +
								"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "'>\n";	
					}else{
						if(i==index)
							sValue_2 = sValue_2 +
								"		<td colspan=\"3\"><select id='" + Support.getName(ds.getDimensionUri(i)) + "' size='"+ (arrLocation.size()-1) + "' style='height:30px;' multiple disabled>\n";	
						else
							sValue_2 = sValue_2 +
								"		<td colspan=\"3\"><select id='" + Support.getName(ds.getDimensionUri(i)) + "'>\n";
						
					}
					if(ds.getDimension(i).getValueSize()>1 && i!=index)
						sValue_2  = sValue_2  + "			<option value=\"any value\">Any value</option>\n";	
					
					if(i==index){
						for(j=1; j<arrLocation.size(); j++){
							for(k=0; k<ds.getDimension(i).getValueSize();k++)
								if(arrLocation.get(j).equals(ds.getDimension(i).getValueUri(k))){
									if(ds.getDimension(i).haveValueLabel())
										sValue_2 = sValue_2 + "			<option value=\""+ds.getDimension(i).getValueUri(k)+"\" selected>" + Support.getName(ds.getDimension(i).getValueLabel(k))+"</option>\n";
									else
										sValue_2 = sValue_2 + "			<option value=\""+ds.getDimension(i).getValueUri(k)+"\" selected>" + Support.getName(ds.getDimension(i).getValueUri(k))+"</option>\n";
									break;
								}
						}
							
					}else{
						for(j=0; j<ds.getDimension(i).getValueSize();j++){
							if(ds.getDimension(i).haveValueLabel())
								sValue_2 = sValue_2 + "			<option value=\""+ds.getDimension(i).getValueUri(j)+"\">" + Support.getName(ds.getDimension(i).getValueLabel(j))+"</option>\n";
							else
								sValue_2 = sValue_2 + "			<option value=\""+ds.getDimension(i).getValueUri(j)+"\">" + Support.getName(ds.getDimension(i).getValueUri(j))+"</option>\n";
						}
					}
					
					sValue_2 = sValue_2 +
							"		</select></td>\n" +
							"	</tr>\n";			
					sBody2_Dimension = sBody2_Dimension + sValue_2;
				}			
			}				
		}		
		
		sBody_Dimension = sBody_Dimension + "\n</table>";
		sBody2_Dimension = sBody2_Dimension + "\n</table>";	
		
		//sBody - Measure
		if(size_m>0){
			if(size_m>1){
				if(bAllMeasure==true){
					sTmp =  "\n<table>\n" +
							"	<tr>\n" +
							"		<td colspan=\"2\"><b>Measures</b></td>\n"+
							"	</tr>\n" +
							"	<tr>\n" +
							"		<td colspan=\"2\">" + "<input type=\"checkbox\" id=\"allmeasures\" onClick=\"checkAll();\"><label for=\"allmeasures\">All measures</label></td>\n"+
							"	</tr>\n";	
					for(i=0; i<size_m; i++){
						sValue = 	"	<tr>\n" +
									"		<td><input type=\"checkbox\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + Support.getName(ds.getMeasureUri(i)) +"</label></td>\n";
						if(++i < size_m)
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
							
					for(i=0; i<size_m; i++){
						sValue = 	"	<tr>\n" +
									"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\">" + "<label for=\"" + i + "\">" + Support.getName(ds.getMeasureUri(i)) +"</label></td>\n";
						if(++i < size_m)
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
		sBody_Measure = sBody_Measure + sTmp;
		
		if(!ds.getMeasure().haveMeasureLabel())
			sBody2_Measure = sBody2_Measure + sTmp;
		else	
		{
			//sBody - Measure
			if(size_m>0){
				if(size_m>1){
					if(bAllMeasure==true){
						sBody2_Measure = sBody2_Measure +
								"\n<table>\n" +
								"	<tr>\n" +
								"		<td colspan=\"2\"><b>Measures</b></td>\n"+
								"	</tr>\n" +
								"	<tr>\n" +
								"		<td colspan=\"2\">" + "<input type=\"checkbox\" id=\"allmeasures\" onClick=\"checkAll();\"><label for=\"allmeasures\">All measures</label></td>\n"+
								"	</tr>\n";
						
						for(i=0; i<size_m; i++){
							sValue = 	"	<tr>\n" +
										"		<td><input type=\"checkbox\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + ds.getMeasureLabel(i) +"</label></td>\n";
							if(++i < size_m)
								sValue = sValue + 
									  	"		<td><input type=\"checkbox\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + ds.getMeasureLabel(i) +"</label></td>\n"+
									  	"	</tr>\n";
							else
								sValue = sValue +
										"	</tr>\n";				
							sBody2_Measure = sBody2_Measure + sValue;
						}		
					}else{
						sBody2_Measure = sBody2_Measure +
								"\n<table>\n" +
								"	<tr>\n" +
								"		<td colspan=\"2\"><b>Measures</b></td>\n"+
								"	</tr>\n";								
						
						for(i=0; i<size_m; i++){
							sValue = 	"	<tr>\n" +
										"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + ds.getMeasureLabel(i) +"</label></td>\n";
							if(++i < size_m)
								sValue = sValue + 
									  	"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + ds.getMeasureLabel(i) +"</label></td>\n"+
									  	"	</tr>\n";
							else
								sValue = sValue +
										"	</tr>\n";				
							sBody2_Measure = sBody2_Measure + sValue;
						}		
					}					
				}
				else{
					sBody2_Measure = sBody2_Measure +
							"\n<table>\n"+
							"	<tr>\n" +
							"		<td colspan=\"2\"><b>Measure</b></td>\n"+
							"	</tr>\n";
					sValue ="	<tr>\n" +
							"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + 0 +"\" value=\""+ ds.getMeasureUri(0)+"\">" + "<label for=\"" + 0 + "\">" + ds.getMeasureLabel(0) +"</label></td>\n"+
							"	</tr>\n";				
					sBody2_Measure = sBody2_Measure + sValue;							
				}				
				sBody2_Measure = sBody2_Measure + "</table>\n";
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
		if(size_m > 1)
			sMeasure = sMeasure +
					"			if($('#allmeasures').length>0) \n"+
					"				document.getElementById('allmeasures').checked = true; \n" +
					"		}";
		else
			sMeasure = sMeasure + 
					"		}";
		
		//sQuery - sDimension	
		sDimension = "";		
		sDimension_2 = sDimension;
		
		for(i=0; i<size_d; i++){
			if(ds.getDimension(i).getValueSize()>1){				
				sDimension = sDimension + "					+ \"?o <" + ds.getDimensionUri(i) + "> ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i)))  + ".   \" \n";
				if(ds.getDimension(i).haveValueLabel())
					sDimension_2 = sDimension_2 + "					+ \"?o <" + ds.getDimensionUri(i) + "> ?" + "uri_"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i)))  + ".   \" \n"
												+ "					+ \"?uri_"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + " <" + ds.getDimension(i).getLabelType() + "> ?"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + ". \"\n";
											
				else
					sDimension_2 = sDimension_2 + "					+ \"?o <" + ds.getDimensionUri(i) + "> ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i)))  + ".  \" \n";
							
			}
		}
		
		//sQuery - sFilter
		sFilter   = "" ;
		sFilter_2 = sFilter; 		
		for(i=0; i<size_d; i++){
			if(ds.getDimension(i).getValueSize()>1){ 
				if(i==year){				
					sTmp = "\n\t\t\t\t\t+ getYearFilter(\"" +	Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\"," +
							"$(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "_from\").val(), " +
							"$(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "_to\").val())";					
					sFilter   = sFilter   + sTmp;	
					sFilter_2 = sFilter_2 + sTmp;
				}else{				
					sFilter  = sFilter  + "\n\t\t\t\t\t+ getFilter(\"?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\", \"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\")";
					if(ds.getDimension(i).haveValueLabel())
						sFilter_2 = sFilter_2 + "\n\t\t\t\t\t+ getFilter(\"?uri_" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\", \"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\")";
					else
						sFilter_2 = sFilter_2 + "\n\t\t\t\t\t+ getFilter(\"?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\", \"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\")";
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
		
		sQuery_2 = sMeasure + "\n\t\t\n" +
				 "		var query =  \"PREFIX qb:    <http://purl.org/linked-data/cube#> \"+ \n"+				
				 "					 \"SELECT * \"+ \n" +
				 "					 \"WHERE {\"+ \n" +
				 "					 \"?o qb:dataSet <" + ds.getUri() + ">. \"+ \n"+	
				 "					 sMeasure  \n" +	
				 					 sDimension_2 + 
				 					 sFilter_2   + "\n";
		
		if(year!=-1) {
			sQuery   = sQuery   +  "					+ \"}ORDER BY ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(year)))  + " ?o LIMIT 1000 \";";
			sQuery_2 = sQuery_2 +  "					+ \"}ORDER BY ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(year)))  + " ?o LIMIT 1000 \";";
		}
		else if(date!=-1) {
			sQuery   = sQuery   +  "					+ \"}ORDER BY ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(date))) + " ?o LIMIT 1000 \";";
			sQuery_2 = sQuery_2 +  "					+ \"}ORDER BY ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(date))) + " ?o LIMIT 1000 \";";
		}
		else{
			sQuery   = sQuery   +  "					+ \"}ORDER BY ?o LIMIT 1000\";";
			sQuery_2 = sQuery_2 +  "					+ \"}ORDER BY ?o LIMIT 1000\";";
		}
		
		
		//result['observation'][j]['breakdown'] = getName(binding["breakdown"].value, binding["breakdown"].datatype) ;
		//result['observation'][j]['unit_measure'] = getName(binding["unit_measure"].value, binding["unit_measure"].datatype);
		sRowsi = "";			
		for(i=0; i<size_d; i++){
			if(ds.getDimension(i).getValueSize()>1){			
				sRowsi =  sRowsi + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"'] = getName(binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\'].value, binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\'].datatype);\n";				
			}else
				sRowsi =  sRowsi + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"'] = $(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\").val();\n";
		}		
		
		sRowsi_2 = "";			
		for(i=0; i<size_d; i++){
			if(ds.getDimension(i).getValueSize()>1){
				sRowsi_2 =  sRowsi_2 + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"'] = getName(binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\'].value, binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\'].datatype);\n";
			}else
				sRowsi_2 =  sRowsi_2 + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"'] = $(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\").val();\n";
		}
		if(ds.getDimensionSize()==0 || ds.getMeasureSize()==0)
			file_template = folder_template + File.separator + "index_error.html";
		else
			if(!endpoint.contains("digital-agenda-data"))
				file_template = folder_template + File.separator + "index2.html";
			else{
				sQuery = "\t\t\t\t\t+ \"FILTER(?ds = <" + ds.getUri() +">)  \"" ;
				sQuery_2 = "\t\t\t\t\t+ \"FILTER(?ds = <" + ds.getUri() +">) \"" ;
				file_template = folder_template + File.separator + "index_digital2.html";
			}
			
		if(!endpoint.contains("gov.tso.co.uk/coins")
				&& (ds.getDimension().haveValueLabel()||ds.getMeasure().haveMeasureLabel()))
			FileOperation.readFileIndex(folder_target+ File.separator + dsName + ".html", file_template,  sTitle, sId, sEndpoint, sQuery_2, sSize, sRowsi_2, sCComponent, sDComponent, "", sBody2_Dimension, sBody2_Measure );		
		else
			FileOperation.readFileIndex(folder_target+ File.separator + dsName + ".html", file_template,  sTitle, sId, sEndpoint, sQuery, sSize, sRowsi, sCComponent, sDComponent, "", sBody_Dimension, sBody_Measure);

	}	
	

	public void createWidgetUseRMLMethod(MetaData md, String dsName){
		int i, j, n;	
		String file_template, file_dest;
		String sTitle="", sQuery="", sBody="";
				
		//get name of dataset
		dsName = Support.getName(dsName);	
		dsName = dsName.replaceAll("%2F", "_");
		
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
		file_template 	= folder_template + File.separator + "index_rml.html";
		file_dest 		= folder_target+ File.separator + dsName + ".html";
		FileOperation.readFileIndex(file_dest, file_template,  sTitle, "", "", sQuery, "", "", "", "", sBody, "", "");
	}
	
	public void createWidgetFile(){	
		int i, j, size_d, size_m, year, date;
		boolean bAllMeasure = ds.getMeasure().getBMultipleMeasure();
		String dsName, sDimension, sDimension_2, sMeasure, sFilter, sFilter_2;
		String file_template;
		String sTitle="", sId="", sEndpoint="", sQuery="", sQuery_2="", sSize="", sBody="", sBody_2="", sTmp="", sValue_2, sValue;
		String sDComponent ="", sCComponent = "", sRowsi="", sRowsi_2="";	
		
		//get name of dataset
		dsName = ds.getUri();
		dsName = Support.getName(dsName);		
		dsName = index + "_"+ dsName;
			
		size_d = ds.getDimensionSize();
		size_m = ds.getMeasureSize();
				
    	//sTitle, sEndpoint
		sTitle    = dsName;
		sEndpoint = "\""+ endpoint + "\"";
		
		//'@id': 'http://ogd.ifs.tuwien.ac.at/vienna/betriebszweige2012-autobus',
		sId = "'@id': '" + ds.getUri() + "',";
		
		//Dimension component in JSON-LD
		sDComponent = "			'dimension': [\n";
		for(i=0; i<size_d; i++){
			if(i<size_d-1){
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
		
		sBody_2 = sBody;
		
		//check Year, Date Dimension
		SpecialTypeList typeList = new SpecialTypeList(folder_template + File.separator + "list.xml");
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
			sBody_2 = sBody_2 + sValue;
		}
		
		//sBody - Dimension
		
		//if !digital-agenda-data
		if(!folder_target.contains("digital-agenda-data")){
			for(i=0; i<size_d; i++){
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
		if(folder_target.contains("digital-agenda-data")){
			String sIndicator="", sBreakdown="", sUnit="", sCountry="", sTimePeriod="";
			for(i=0; i<size_d; i++){
				if(ds.getDimension(i).getValueSize()>0){
					if(ds.getDimensionLabel(i).isEmpty())
						sValue_2=	
							"	<tr>\n" +
							"		<td><label>"+ Support.getName(ds.getDimensionUri(i)) +"</label></td>\n";
					else
						sValue_2 =
							"	<tr>\n" +
							"		<td><label>"+ ds.getDimensionLabel(i) +"</label></td>\n";				
						
					if(ds.getDimensionUri(i).contains("indicator"))
						sValue_2 = sValue_2 +
							"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "' onchange='changeIndicator();'>\n";
					else
						sValue_2 = sValue_2 +
							"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "' onchange='changeValue();'>\n";
						
					
					if(ds.getDimension(i).getValueSize()>1)
						sValue_2  = sValue_2  + "			<option value=\"any value\">Any value</option>\n";	
					
					for(j=0; j<ds.getDimension(i).getValueSize();j++){
						if(j>5000) break;
						if(ds.getDimension(i).haveValueLabel())
							sValue_2 = sValue_2 + "			<option value=\""+ds.getDimension(i).getValueUri(j)+"\">" + Support.getName(ds.getDimension(i).getValueLabel(j))+"</option>\n";
						else
							sValue_2 = sValue_2 + "			<option value=\""+ds.getDimension(i).getValueUri(j)+"\">" + Support.getName(ds.getDimension(i).getValueUri(j))+"</option>\n";
					}
					
					sValue_2 = sValue_2 +
							"		</select></td>\n" +
							"	</tr>\n";			
					if(sValue_2.contains("<label>Indicator</label>")) sIndicator = sValue_2;
					else if(sValue_2.contains("<label>Breakdown</label>")) sBreakdown = sValue_2;
					else if(sValue_2.contains("<label>Unit of measure</label>")) sUnit = sValue_2;
					else if(sValue_2.contains("<label>Country</label>")) sCountry = sValue_2;
					else sTimePeriod = sValue_2;	
				}				
			}
			sBody_2 = sBody_2 + sIndicator + sBreakdown + sUnit + sCountry + sTimePeriod;
		}else{
			for(i=0; i<size_d; i++){
				if(i!=year && ds.getDimension(i).getValueSize()>0){
					if(ds.getDimensionLabel(i).isEmpty())
						sValue_2=	
							"	<tr>\n" +
							"		<td><label>"+ Support.getName(ds.getDimensionUri(i)) +"</label></td>\n";
					else
						sValue_2 =
							"	<tr>\n" +
							"		<td><label>"+ ds.getDimensionLabel(i) +"</label></td>\n";
					if(year==-1){
						sValue_2 = sValue_2 +
							"		<td><select id='" + Support.getName(ds.getDimensionUri(i))+ "'>\n";
						
					}else{
						sValue_2 = sValue_2 +
							"		<td colspan=\"3\"><select id='" + Support.getName(ds.getDimensionUri(i)) + "'>\n";
						
					}
					if(ds.getDimension(i).getValueSize()>1)
						sValue_2  = sValue_2  + "			<option value=\"any value\">Any value</option>\n";	
					
					for(j=0; j<ds.getDimension(i).getValueSize();j++){
						if(j>5000) break;
						if(ds.getDimension(i).haveValueLabel())
							sValue_2 = sValue_2 + "			<option value=\""+ds.getDimension(i).getValueUri(j)+"\">" + Support.getName(ds.getDimension(i).getValueLabel(j))+"</option>\n";
						else
							sValue_2 = sValue_2 + "			<option value=\""+ds.getDimension(i).getValueUri(j)+"\">" + Support.getName(ds.getDimension(i).getValueUri(j))+"</option>\n";
					}
					
					sValue_2 = sValue_2 +
							"		</select></td>\n" +
							"	</tr>\n";			
					sBody_2 = sBody_2 + sValue_2;
				}			
			}			

			//Attribute
			if(ds.getAttributeSize()==1){
				if(ds.getAttributeLabel(0).isEmpty())
					sValue_2=	
						"	<tr>\n" +
						"		<td><b><label>"+ Support.getName(ds.getAttributeUri(0)) +"</label></b></td>\n";
				else
					sValue_2 =
						"	<tr>\n" +
						"		<td><b><label>"+ ds.getAttributeLabel(0) +"</label></b></td>\n";
				if(year==-1){
					sValue_2 = sValue_2 +
						"		<td><select id='" + Support.getName(ds.getAttributeUri(0))+ "'>\n";
					
				}else{
					sValue_2 = sValue_2 +
						"		<td colspan=\"3\"><select id='" + Support.getName(ds.getAttributeUri(0)) + "'>\n";
					
				}
				if(ds.getAttribute(0).getValueSize()>1)
					sValue_2  = sValue_2  + "			<option value=\"any value\">Any value</option>\n";	
				
				for(j=0; j<ds.getAttribute(0).getValueSize();j++)
					if(ds.getAttribute(0).haveValueLabel())
						sValue_2 = sValue_2 + "			<option value=\""+ds.getAttribute(0).getValueUri(j)+"\">" + Support.getName(ds.getAttribute(0).getValueLabel(j))+"</option>\n";
					else
						sValue_2 = sValue_2 + "			<option value=\""+ds.getAttribute(0).getValueUri(j)+"\">" + Support.getName(ds.getAttribute(0).getValueUri(j))+"</option>\n";
				
				sValue_2 = sValue_2 +
						"		</select></td>\n" +
						"	</tr>\n";			
				sBody_2 = sBody_2 + sValue_2;
			}			
		}		
		
		sBody = sBody + "\n</table>";
		sBody_2 = sBody_2 + "\n</table>";	
		
		//sBody - Measure
		if(size_m>0){
			if(size_m>1){
				if(bAllMeasure==true){
					sTmp =  "\n<table>\n" +
							"	<tr>\n" +
							"		<td colspan=\"2\"><b>Measures</b></td>\n"+
							"	</tr>\n" +
							"	<tr>\n" +
							"		<td colspan=\"2\">" + "<input type=\"checkbox\" id=\"allmeasures\" onClick=\"checkAll();\"><label for=\"allmeasures\">All measures</label></td>\n"+
							"	</tr>\n";	
					for(i=0; i<size_m; i++){
						sValue = 	"	<tr>\n" +
									"		<td><input type=\"checkbox\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + Support.getName(ds.getMeasureUri(i)) +"</label></td>\n";
						if(++i < size_m)
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
							
					for(i=0; i<size_m; i++){
						sValue = 	"	<tr>\n" +
									"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\">" + "<label for=\"" + i + "\">" + Support.getName(ds.getMeasureUri(i)) +"</label></td>\n";
						if(++i < size_m)
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
			sBody_2 = sBody_2 + sTmp;
		else	
		{
			//sBody - Measure
			if(size_m>0){
				if(size_m>1){
					if(bAllMeasure==true){
						sBody_2 = sBody_2 +
								"\n<table>\n" +
								"	<tr>\n" +
								"		<td colspan=\"2\"><b>Measures</b></td>\n"+
								"	</tr>\n" +
								"	<tr>\n" +
								"		<td colspan=\"2\">" + "<input type=\"checkbox\" id=\"allmeasures\" onClick=\"checkAll();\"><label for=\"allmeasures\">All measures</label></td>\n"+
								"	</tr>\n";
						
						for(i=0; i<size_m; i++){
							sValue = 	"	<tr>\n" +
										"		<td><input type=\"checkbox\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + ds.getMeasureLabel(i) +"</label></td>\n";
							if(++i < size_m)
								sValue = sValue + 
									  	"		<td><input type=\"checkbox\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + ds.getMeasureLabel(i) +"</label></td>\n"+
									  	"	</tr>\n";
							else
								sValue = sValue +
										"	</tr>\n";				
							sBody_2 = sBody_2 + sValue;
						}		
					}else{
						sBody_2 = sBody_2 +
								"\n<table>\n" +
								"	<tr>\n" +
								"		<td colspan=\"2\"><b>Measures</b></td>\n"+
								"	</tr>\n";								
						
						for(i=0; i<size_m; i++){
							sValue = 	"	<tr>\n" +
										"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + ds.getMeasureLabel(i) +"</label></td>\n";
							if(++i < size_m)
								sValue = sValue + 
									  	"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + i +"\" value=\""+ ds.getMeasureUri(i)+"\" onClick=\"check();\">" + "<label for=\"" + i + "\">" + ds.getMeasureLabel(i) +"</label></td>\n"+
									  	"	</tr>\n";
							else
								sValue = sValue +
										"	</tr>\n";				
							sBody_2 = sBody_2 + sValue;
						}		
					}					
				}
				else{
					sBody_2 = sBody_2 +
							"\n<table>\n"+
							"	<tr>\n" +
							"		<td colspan=\"2\"><b>Measure</b></td>\n"+
							"	</tr>\n";
					sValue ="	<tr>\n" +
							"		<td><input type=\"radio\" name=\"measures[]\" id=\"" + 0 +"\" value=\""+ ds.getMeasureUri(0)+"\">" + "<label for=\"" + 0 + "\">" + ds.getMeasureLabel(0) +"</label></td>\n"+
							"	</tr>\n";				
					sBody_2 = sBody_2 + sValue;							
				}				
				sBody_2 = sBody_2 + "</table>\n";
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
		if(size_m > 1)
			sMeasure = sMeasure +
					"			if($('#allmeasures').length>0) \n"+
					"				document.getElementById('allmeasures').checked = true; \n" +
					"		}";
		else
			sMeasure = sMeasure + 
					"		}";
		
		//sQuery - sDimension	
		sDimension = "";		
		sDimension_2 = sDimension;
		
		for(i=0; i<size_d; i++){
			if(ds.getDimension(i).getValueSize()>1){				
				sDimension = sDimension + "					+ \"?o <" + ds.getDimensionUri(i) + "> ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i)))  + ".   \" \n";
				if(ds.getDimension(i).haveValueLabel())
					sDimension_2 = sDimension_2 + "					+ \"?o <" + ds.getDimensionUri(i) + "> ?" + "uri_"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i)))  + ".   \" \n"
												+ "					+ \"?uri_"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + " <" + ds.getDimension(i).getLabelType()  + "> " + " ?"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + ". \"\n";
											
				else
					sDimension_2 = sDimension_2 + "					+ \"?o <" + ds.getDimensionUri(i) + "> ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i)))  + ".   \" \n";
			}
		}
		//sQuery - Attribute
		if(ds.getAttributeSize()==1 && ds.getAttribute(0).getValueSize()>1){
			sDimension   = sDimension   + "					+ \"?o <" + ds.getAttributeUri(0) + "> ?" + Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0)))  + ".   \" \n";
			sDimension_2 = sDimension_2 + "					+ \"?o <" + ds.getAttributeUri(0) + "> ?" + Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0)))  + ".   \" \n";
		}		
		
		//sQuery - sFilter
		sFilter   = "" ;
		sFilter_2 = sFilter; 	
	
		for(i=0; i<size_d; i++){
			if(ds.getDimension(i).getValueSize()>1){ 
				if(i==year){				
					sTmp = "\n\t\t\t\t\t+ getYearFilter(\"?" +	Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\"," +
							"$(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "_from\").val(), " +
							"$(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "_to\").val())";					
					sFilter   = sFilter   + sTmp;	
					sFilter_2 = sFilter_2 + sTmp;
				}else{				
					sFilter  = sFilter  + "\n\t\t\t\t\t+ getFilter(\"?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\", \"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\")";
					if(ds.getDimension(i).haveValueLabel())
						sFilter_2 = sFilter_2 + "\n\t\t\t\t\t+ getFilter(\"?uri_" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\", \"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\")";
					else
						sFilter_2 = sFilter_2 + "\n\t\t\t\t\t+ getFilter(\"?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\", \"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\")";
				}
			}
		}
		if(ds.getAttributeSize()==1 && ds.getAttribute(0).getValueSize()>1){
			sFilter = sFilter + "\n\t\t\t\t\t+ getFilter(\"?" + Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) + "\", \"#" + Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) + "\")";
			sFilter_2 = sFilter_2 + "\n\t\t\t\t\t+ getFilter(\"?" + Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) + "\", \"#" + Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) + "\")";
		}
			
		sQuery = sMeasure + "\n\t\t\n" +
				 "		var query =  \"PREFIX qb:    <http://purl.org/linked-data/cube#> \"+ \n"+				
				 "					 \"SELECT * \"+ \n" +
				 "					 \"WHERE {\"+ \n" +
				 "					 \"?o qb:dataSet <" + ds.getUri() + ">. \"+ \n"+		
				 "					 sMeasure  \n" +	
				 					 sDimension + 
				 					 sFilter   + "\n";
		
		sQuery_2 = sMeasure + "\n\t\t\n" +
				 "		var query =  \"PREFIX qb:    <http://purl.org/linked-data/cube#> \"+ \n"+				
				 "					 \"SELECT * \"+ \n" +
				 "					 \"WHERE {\"+ \n" +
				 "					 \"?o qb:dataSet <" + ds.getUri() + ">. \"+ \n"+		
				 "					 sMeasure  \n" +	
				 					 sDimension_2 + 
				 					 sFilter_2   + "\n";
		
		if(ds.getBUseDistinct()){
			if(year!=-1) {
				sQuery   = sQuery   +  "					+ \"}ORDER BY ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(year)))  + " ?o LIMIT 1000 \";";
				sQuery_2 = sQuery_2 +  "					+ \"}ORDER BY ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(year)))  + " ?o LIMIT 1000 \";";
			}
			else if(date!=-1) {
				sQuery   = sQuery   +  "					+ \"}ORDER BY ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(date))) + " ?o LIMIT 1000 \";";
				sQuery_2 = sQuery_2 +  "					+ \"}ORDER BY ?" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(date))) + " ?o LIMIT 1000 \";";
			}
			else{
				sQuery   = sQuery   +  "					+ \"}ORDER BY ?o LIMIT 1000\";";
				sQuery_2 = sQuery_2 +  "					+ \"}ORDER BY ?o LIMIT 1000\";";
			}
		}else{
			sQuery   = sQuery   +  "					+ \"}LIMIT 1000\";";
			sQuery_2 = sQuery_2 +  "					+ \"}LIMIT 1000\";";
		}
				
		
		//result['observation'][j]['breakdown'] = getName(binding["breakdown"].value, binding["breakdown"].datatype) ;
		//result['observation'][j]['unit_measure'] = getName(binding["unit_measure"].value, binding["unit_measure"].datatype);
		sRowsi = "";			
		for(i=0; i<size_d; i++){
			if(ds.getDimension(i).getValueSize()>1){			
				sRowsi =  sRowsi + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"'] = getName(binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\'].value, binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\'].datatype);\n";				
			}else
				sRowsi =  sRowsi + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"'] = $(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\").val();\n";
		}		
		
		sRowsi_2 = "";			
		for(i=0; i<size_d; i++){
			if(ds.getDimension(i).getValueSize()>1){
				sRowsi_2 =  sRowsi_2 + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"'] = getName(binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\'].value, binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"\'].datatype);\n";
			}else
				sRowsi_2 =  sRowsi_2 + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) +"'] = $(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getDimensionUri(i))) + "\").val();\n";
		}
		
		if(ds.getAttributeSize()==1){
			if(ds.getAttribute(0).getValueSize()>1){
				sRowsi   =  sRowsi   + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) +"'] = getName(binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) +"\'].value, binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) +"\'].datatype);\n";
				sRowsi_2 =  sRowsi_2 + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) +"'] = getName(binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) +"\'].value, binding[\'"+ Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) +"\'].datatype);\n";
			}else{
				sRowsi   =  sRowsi   + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) +"'] = $(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) + "\").val();\n";
				sRowsi_2 =  sRowsi_2 + "\t\t\t\t\tresult[\'observation\'][k][\'"+ Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) +"'] = $(\"#" + Support.removeSpecialCharacter(Support.getName(ds.getAttributeUri(0))) + "\").val();\n";
			}
		}
		
		if(ds.getDimensionSize()==0 || ds.getMeasureSize()==0)
			file_template = folder_template + File.separator + "index_error.html";
		else
			if(!folder_target.contains("digital-agenda-data"))
				file_template = folder_template + File.separator + "index.html";
			else{
				sQuery = "\t\t\t+ \"FILTER(?ds = <" + ds.getUri() +">)\"" ;
				sQuery_2 = "\t\t\t+ \"FILTER(?ds = <" + ds.getUri() +">)\"" ;
				file_template = folder_template + File.separator + "index_digital.html";
			}
			
		if(!endpoint.contains("gov.tso.co.uk/coins")
				&& (ds.getDimension().haveValueLabel()||ds.getMeasure().haveMeasureLabel()))
			FileOperation.readFileIndex(folder_target+ File.separator + dsName + ".html", file_template,  sTitle, sId, sEndpoint, sQuery_2, sSize, sRowsi_2, sCComponent, sDComponent, sBody_2, "", "");		
		else
			FileOperation.readFileIndex(folder_target+ File.separator + dsName + ".html", file_template,  sTitle, sId, sEndpoint, sQuery, sSize, sRowsi, sCComponent, sDComponent, sBody, "", "");
		
		//RDFDescription.createDescription(ds, folder_endpoint+"\\"+dsName+"\\data.rdf", endpoint);
	}	
}
