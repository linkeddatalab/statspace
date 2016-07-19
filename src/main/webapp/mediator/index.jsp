
<%@ page import="java.util.*"%>
<%@ page import="at.tuwien.ldlab.statspace.metadata.*"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Mediator</title>  
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="keywords" content="mediator, linked data, data integration, statistical data"/> 
	<script src="http://code.jquery.com/jquery-latest.min.js" type="text/javascript"></script>	
	<link href="scripts/style.css" rel="stylesheet" type="text/css">	
	<script type="text/javascript">			
		function validateForm(data) {				
			var query = $("#txtQuery").val();
			if(query == ""){
				alert("You must enter a query");
				return false;
			}	
			$("#divResult").hide();
			$("#tblResult").html("").hide();
			$("#txtResult").val("").hide();
			$("#tblProvenance").html("").hide();
			$("#txtProvenance").val("").hide();
			
			$("#btnRun").attr({disabled : true, value : "Running. Please wait..."});	
			return true;
		}
	</script>
</head>
<body>
	<div align="center" style="font-size:15px; font-weight: bold; margin-top:20px;" >		
		A Mediator for Statistical Data Integration	
	</div>
	
	<%      	
         Object objResult = request.getAttribute("result");
    	 String sResult="", sQuery = "", sFormat="", sCache="", sProvenance="";
		 int iNumber=0;
    	 if(objResult != null){
    		 sResult = ((String) objResult).trim();
    		 Object objProvenance = request.getAttribute("provenance");   
    		 Object objQuery  = request.getAttribute("query");  
    		 Object objNumber = request.getAttribute("number"); 
    		 Object objFormat = request.getAttribute("format");
    		 Object objCache  = request.getAttribute("cache");
    		 if(objProvenance != null)  sProvenance = ((String) objProvenance).trim();
    		 if(objQuery != null)  		sQuery 		= ((String) objQuery).trim();
    		 if(objNumber!= null)  		iNumber		= (Integer) objNumber;
        	 if(objFormat!=null)		sFormat 	= (String)objFormat;   
        	 if(sFormat.isEmpty()) 		sFormat		= "html";        	 	
        	 if(objCache!=null)  		sCache  	= (String)objCache;     
    	 }
	 %> 
	
	<div id="divQuery">
	 	<form method="post" action="query" onsubmit="return validateForm()">
			<label>Query</label><br/>	
			<textarea id="query" name="query" rows="15">
<%	
		 if(objResult != null){
				out.print(sQuery);
		 }else{
 %>PREFIX qb:    &lt;http://purl.org/linked-data/cube#&gt;
PREFIX sdmx-dimension:   &lt;http://purl.org/linked-data/sdmx/2009/dimension#&gt;
PREFIX sdmx-measure:     &lt;http://purl.org/linked-data/sdmx/2009/measure#&gt;
PREFIX sdmx-attribute:   &lt;http://purl.org/linked-data/sdmx/2009/attribute#&gt;
SELECT *
WHERE {
	?ds dc:subject  &lt;http://statspace.linkedwidgets.org/codelist/cl_subject/SP.POP.TOTL&gt;.
	?o qb:dataSet ?ds.
	?o sdmx-measure:obsValue  ?obsValue.
	?o sdmx-dimension:refPeriod ?refPeriod.
	?o sdmx-dimension:refArea   ?refArea.
	?o sdmx-attribute:unitMeasure ?unit.
	Filter(?refArea= &lt;http://statspace.linkedwidgets.org/codelist/cl_area/UnitedKingdom&gt;)
}<%
		 }
%>
			</textarea><br/>
			<label>Result Formats </label>
			<select id="format" name="format">	
				<option value="html">HTML</option>	
				<option value="json">JSON</option>		
				<option value="xml">XML</option>							
			</select>			
			<input type='checkbox' id="cache" name="cache" checked>Use cache</input></br></br>
			<input id="btnRun" type="submit" value="Run Query" style="font-weight:bold"/><br/>	
		</form>
	</div> 
	
	<%
		if(objResult != null){
%>
		 <script type="text/javascript">	
	   	   	$("#format").val('<%=sFormat%>');  
<%  
	     if(!sCache.isEmpty()&&sCache.equals("true")){ %>
	   		$("#cache").prop("checked", true);
<%  	 }else{	  %>
			 $("#cache").prop("checked", false);
<% 		 } %>
		  </script>
<%		}
%>
<%
		if(objResult != null){
	 		out.print("		<div id=\"divResult\">\n"+
	 				  "			<label>Number of suitable datasets: " + iNumber + "</label>\n");
	 		
	 		//result of query
	 		if(sFormat.equals("json")||sFormat.equals("xml")){	 		
	 			out.print("		<textarea id=\"txtResult\" rows=\"25\" cols=\"100\">\n" + sResult+ "		</textarea>\n");
	 			if(!sProvenance.isEmpty()){
		 			out.print("		<label>Provenance</label>\n");
		 			out.print("		<textarea id=\"txtProvenance\" rows=\"25\" cols=\"100\" style=\"margin-bottom:50px;\">\n" + sProvenance+ "		</textarea>\n");
		 		}	 		
	 		}else{ 
	 			out.print("		<div id=\"tblResult\" class=\"table-scroll\">\n" + sResult+ "		</div>\n");
	 			if(!sProvenance.isEmpty()){
		 			out.print("		<label>Provenance information regarding owl:sameAs relationships</label>\n");
		 			out.print("		<div id=\"tblProvenance\" class=\"table-scroll\" style=\"margin-bottom:50px;\">\n" + sProvenance+ "		</div>\n");
		 		}
	 		}	
	 		
	 		out.print("		</div>\n");
		}
%>	
	
	

   	  
</body>
</html>
