<!--
To change this template, choose Tools | Templates
and open the template in the editor.
-->

<%@ page import="java.util.*"%>
<%@ page import="tuwien.ldlab.statspace.model.mediator.*"%>

<!DOCTYPE html>
<html>
    <head>
        <title>StatSpace Explorer</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="http://code.jquery.com/jquery-latest.min.js" type="text/javascript"></script>	
       	<!-- C3 Chart -->
		<link rel="stylesheet" type="text/css" href="scripts/c3.min.css"/>
		<script src="scripts/d3.v3.min.js" charset="utf-8"></script>
		<script src="scripts/c3.min.js"></script>
        <style type="text/css">  
			select{
				width:350px;
				color: #000000;
				font-size : 13px;
			}
			body,button{
				color: #000000;
				font-size : 13px;
				font-family: 'Arial Unicode MS', 'Microsoft Sans Serif', Tahoma, Arial, sans-serif;
				margin-left: 250px;
				margin-right:250px;
				padding: 0;
			}			
			div{
				margin-top:10px;
				margin-bottom:10px;
			}
			label{
				font-weight: bold;		
			}
			.table-scroll {
				height:450px;		
				overflow:auto;  
				margin-top:20px;
			}	
			table{		 
			  border-collapse:collapse;
			  font-family: 'Open Sans', Verdana, Helvetica, Arial, sans-serif;
			  font-size:11px;
			}
			td, th {
				height: 30px;	
				padding: 3px;				
			}
	</style>
	<script type="text/javascript">
		function drawChart(datas, as){
			var chart = c3.generate({
			    bindto: '#chart',
			    data: {
			      x: 'x',
			      columns: datas,
				  axes: as			  
			    },				 
			    axis: {
			      x:{
			    	  type: 'timeseries'
			      },
			      y: {
			        label: {
			          text: 'Y Label',
			          position: 'outer-middle'
			        }
			      },
			      y2: {
			        show: true,
			        label: {
			          text: 'Y2 Label',
			          position: 'outer-middle'
			        }
			      }
			    }
			});
		}
	</script>		
	<script>
		<%
			session = request.getSession();
		    Object idObject = request.getAttribute("idRequest");    
	      	int idRequest = Integer.parseInt(idObject.toString());        	
	        Object obj = request.getServletContext().getAttribute(Integer.toString(idRequest));    
	     	ArrayList<MetaData> arrMetaData = (ArrayList<MetaData>) obj;
	     	MetaData md0 = arrMetaData.get(0);
	     	MetaData md1 = arrMetaData.get(1);
	     	int i, j, m, n = md1.reorderComponents(md0);
	     	
	     
	     %>
	    var count = "<%= n %>";
	    var data0 = JSON.parse('<%= md0.getJSONFormat() %>');	   
	    var data1 = JSON.parse('<%= md1.getJSONFormat() %>');	    
		function updateChart() {
			var datas, as;
			var t, k;
		}
	</script>
	 	        
    </head>
    <body>
        <h3 align="center">StatSpace Explorer</h3>              	      
			<table border="0">
			     <%
			     	ArrayList<String> arrSelect = new ArrayList<String>();
			     	for(i=2; i<n+2; i++){
			     		String s = "<select id=\""+md0.getComponent(i).getVariable().substring(1)+ "\" onchange=\"updateChart()\" >";
			     		ArrayList<String> arrDistinctValue = md0.getDistinctValueReference(i);
			     		//add all values
			     		if(i<4 && arrDistinctValue.size()>1)
			     			s = s + "<option value=\"all\">All values</option>";
			     		for(j=0; j<arrDistinctValue.size(); j++)
			     			s = s + "<option value=\"" + arrDistinctValue.get(j) + "\">" + arrDistinctValue.get(j) + "</option>";
			     		s = s + "</select>";
			     		arrSelect.add(s);
			     	}
			     	for(i=2; i<n+2; i++){
			     		if(i==2)
					     	out.print(	
					     		"<tr>  "+ 
		         				"	<td bgcolor=\"#DAF7A6\">"+ md0.getComponent(i).getLabel()+"</td>"+
		         				"	<td bgcolor=\"#DAF7A6\">"+ arrSelect.get(i-2) +"</td>"+
		         				"   <td bgcolor=\"#DAF7A6\" rowspan=\"" + n + "\">"+ "Common dimensions </td>"+
		         				"</tr>");
			     		else
			     			out.print(	
					     		"<tr>  "+ 
		         				"	<td bgcolor=\"#DAF7A6\">"+ md0.getComponent(i).getLabel()+"</td>"+
		         				"	<td bgcolor=\"#DAF7A6\">"+ arrSelect.get(i-2) +"</td>"+		         				
		         				"</tr>");
			     	}
			     	
			     	//metadata0
			     	arrSelect.clear();
			     	for(i=n+2; i<md0.getNumberofComponent(); i++){
			     		String s = "<select id=\""+ md0.getComponent(i).getVariable().substring(1) + "\" onchange=\"updateChart()\" >";
			     		ArrayList<String> arrDistinctValue = md0.getDistinctValueReference(i);
			     		for(j=0; j<arrDistinctValue.size(); j++)
			     			s = s + "<option value=\"" + arrDistinctValue.get(j) + "\">" + arrDistinctValue.get(j) + "</option>";
			     		s = s + "</select>";
			     		arrSelect.add(s);
			     	}
			     	m = md0.getNumberofComponent() - n - 2;
			     	for(i=n+2; i<md0.getNumberofComponent(); i++){  
			     		if(i==n+2)
					     	out.print(	
					     		"<tr>  "+ 
		         				"	<td bgcolor=\"#FFC300\">"+ md0.getComponent(i).getLabel()+"</td>"+
		         				"	<td bgcolor=\"#FFC300\">"+ arrSelect.get(i-n-2) +"</td>"+
		         				"   <td bgcolor=\"#FFC300\" rowspan=\"" + m + "\">"+ "Isolated dimensions of the first data set</td>"+
		         				"</tr>");  
			     		else
			     			out.print(	
					     		"<tr>  "+ 
		         				"	<td bgcolor=\"#FFC300\">"+ md0.getComponent(i).getLabel()+"</td>"+
		         				"	<td bgcolor=\"#FFC300\">"+ arrSelect.get(i-n-2) +"</td>"+		         				
		         				"</tr>"); 
			     	}
			     	
			    	//metadata1
			     	arrSelect.clear();			    	
			     	for(i=n+2; i<md1.getNumberofComponent(); i++){
			     		String s = "<select id=\"" +md1.getComponent(i).getVariable().substring(1) + "\" onchange=\"updateChart()\" >";
			     		ArrayList<String> arrDistinctValue = md1.getDistinctValueReference(i);
			     		for(j=0; j<arrDistinctValue.size(); j++)
			     			s = s + "<option value=\"" + arrDistinctValue.get(j) + "\">" + arrDistinctValue.get(j) + "</option>";
			     		s = s + "</select>";
			     		arrSelect.add(s);
			     	}
			     	m = md1.getNumberofComponent() - n - 2;
			     	for(i=n+2; i<md1.getNumberofComponent(); i++){
			     		if(i==n+2)
					     	out.print(			     		
					     		"<tr>  "+ 
		         				"	<td bgcolor=\"#F1EB35\">"+ md1.getComponent(i).getLabel()+"</td>"+
		         				"	<td bgcolor=\"#F1EB35\">"+ arrSelect.get(i-n-2) +"</td>"+
		         				"   <td bgcolor=\"#F1EB35\" rowspan=\"" + m + "\">"+ "Isolated dimensions of the second data set</td>"+
		         				"</tr>\n");  
			     		else
			     			out.print(			     		
					     		"<tr>  "+ 
		         				"	<td bgcolor=\"#F1EB35\">"+ md1.getComponent(i).getLabel()+"</td>"+
		         				"	<td bgcolor=\"#F1EB35\">"+ arrSelect.get(i-n-2) +"</td>"+			         			
		         				"</tr>\n"); 
			     	}		     	
			     %>	          	
	    </table> 
	    <div id="controls"></div>
		<div id="chart" style="width:100%;position: relative;"></div>
	</body>
</html>