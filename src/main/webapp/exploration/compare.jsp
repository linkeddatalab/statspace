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
		<!-- Load c3.css -->
		<link href="exploration/scripts/c3.min.css" rel="stylesheet" type="text/css">

		<!-- Load d3.js and c3.js -->
		<script src="exploration/scripts/d3.v3.min.js" charset="utf-8"></script>
		<script src="exploration/scripts/c3.min.js"></script>
        <style type="text/css">  
			select{
				width:200px;
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
	<script>
		<%
			session = request.getSession();
		    Object idObject = request.getAttribute("idRequest");    
	      	int idRequest = Integer.parseInt(idObject.toString());        	
	        Object obj = request.getServletContext().getAttribute(Integer.toString(idRequest));    
	     	ArrayList<MetaData> arrMetaData = (ArrayList<MetaData>) obj;
	     	MetaData md1 = arrMetaData.get(0);
	     	MetaData md2 = arrMetaData.get(1);
	     	int i, j, n = md1.getNumberofComponent();
	     	ArrayList<String> arrTimes = new ArrayList<String>();
	     	ArrayList<String> arrAreas = new ArrayList<String>();
	     	arrTimes = md1.getDistinctRefValueLabel(3);	     	
	     	arrAreas = md1.getDistinctRefValueLabel(2);
	     	String sTimes="";
	     	String sAreas="";	  
	     	for(i=0; i<arrTimes.size(); i++)
	     		sTimes = sTimes +'"'+arrTimes.get(i)+'"' + ",";
	     	sTimes = sTimes.substring(0, sTimes.length()-1);	     	   	
	     	for(i=0; i<arrAreas.size(); i++)
	     		sAreas = sAreas +'"'+arrAreas.get(i)+'"' + ",";
	     	sAreas = sAreas.substring(0, sAreas.length()-1);
	     %>	   
	    var data1 = JSON.parse('<%= md1.getJSONFormat() %>');   
	    var data2 = JSON.parse('<%= md2.getJSONFormat() %>');
	    var vars  = data1.head.vars;
	    var times = [<%= sTimes %>];
	    var areas =[<%= sAreas %>];	   
		var count = <%= n%>;
		var label1 = '<%= md1.getDataSet().getLabel() %>';
		var label2 = '<%= md2.getDataSet().getLabel() %>';
	    
		function contains(vals, v){
			var i;
			for(i=0; i<vals.length; i++)
				if(vals[i]==v)
					return true;
			return false;
		}
	    
	    function updateChart() {		
			var data, datas, as={}, fm;
			var values=[];
			var i, j, t, n, k, m, index;
			var x=['x'];
			var y=[];
			n = data1.results.bindings.length;
			m = data2.results.bindings.length;			
			for(i=2; i<count; i++){
				var vals = [];
				var id = document.getElementById(vars[i]);         
				for(j=0;j<id.length;j++){
					if(id[j].selected == true){
						vals.push(id[j].value);
					}
				}
				if(vals.length==0){
					for(j=0;j<id.length;j++)
						vals.push(id[j].value);		
				}
				values.push(vals);
			}	
			for(i=0; i<values[1].length; i++)
				x.push(values[1][i]); 	//temporal values
			y = values[0]; 				//spatial values
				
	        data=[];
			datas=[];
			datas.push(x);			
			for(t=0; t<y.length; t++){			
				for(i=1; i<x.length; i++){	
					for(j=0; j<n; j++){			
						for(k=2; k<count; k++){
							if(y[t]!=data1.results.bindings[j][vars[2]].value || x[i]!=data1.results.bindings[j][vars[3]].value ||contains(values[k-2], data1.results.bindings[j][vars[k]].value)==false)
								break;												
						}
						if(k==count){
							if(data.length==0) data.push(y[t]+"," + label1);
							data.push(data1.results.bindings[j][vars[1]].value);
							break;
						}						
					}
					if(j==n){
						if(data.length==0) data.push(y[t]+","+label1);
						data.push(null);
					}
				}
				datas.push(data);
				data=[];
			}
			data=[];
			for(t=0; t<y.length; t++){				
				for(i=1; i<x.length; i++){	
					for(j=0; j<m; j++){						
						for(k=2; k<count; k++){
							if(y[t]!=data2.results.bindings[j][vars[2]].value || x[i]!=data2.results.bindings[j][vars[3]].value ||contains(values[k-2], data2.results.bindings[j][vars[k]].value)==false)
								break;												
						}
						if(k==count){
							if(data.length==0) data.push(y[t] + "," + label2);
							data.push(data2.results.bindings[j][vars[1]].value);
							break;
						}						
					}
					if(j==m){
						if(data.length==0) data.push(y[t]+","+label2);
						data.push(null);
					}
					
				}
				datas.push(data);
				var s = y[t]+"," + label2;
				as[s] = "y2";
				data=[];
			}				
			fm='%Y';			
			drawChart(fm, datas, as);			
		}
	</script>
	 <script type="text/javascript">
		function drawChart(fm, datas, as){
			var chart = c3.generate({
			    bindto: '#chart',
			    data: {
			    	x: 'x',
					xFormat: fm,
					columns: datas,
					axes: as		
				},
				line: {
					connect_null: true
				},
				axis: {
				  x: {
					type: 'timeseries'				
				  },
				  y: {
					label: {
					  text: label1,
					  position: 'outer-middle'
					}
				  },
				  y2: {
					show: true,
					label: {
						 text: label2,
					  position: 'outer-middle'
					}
				  }
				}
				
			});
		}
	</script>	        
    </head>
    <body>
        <h3 align="center">StatSpace Explorer</h3>              	      
			<table border="0">
			     <%
			     	ArrayList<String> arrSelect = new ArrayList<String>();
			     	for(i=2; i<n; i++){
			     		String s = "<select id=\""+md1.getComponent(i).getVariable().substring(1)+ "\" onchange=\"updateChart()\" size=\"5\" multiple>";
			     		ArrayList<String> arrDistinctValue = md1.getDistinctRefValueLabel(i);			     	
			     		for(j=0; j<arrDistinctValue.size(); j++)
			     			s = s + "<option value=\"" + arrDistinctValue.get(j) + "\">" + arrDistinctValue.get(j) + "</option>";
			     		s = s + "</select>";
			     		arrSelect.add(s);
			     	}
			     	for(i=2; i<n; i++){
			     		if(i==2)
					     	out.print(	
					     		"<tr>  "+ 
		         				"	<td bgcolor=\"#DAF7A6\">"+ md1.getComponent(i).getLabel()+"</td>"+
		         				"	<td bgcolor=\"#DAF7A6\">"+ arrSelect.get(i-2) +"</td>"+
		         				"   <td bgcolor=\"#DAF7A6\" rowspan=\"" + n + "\">"+ "Common dimensions and values </td>"+
		         				"</tr>");
			     		else
			     			out.print(	
					     		"<tr>  "+ 
		         				"	<td bgcolor=\"#DAF7A6\">"+ md1.getComponent(i).getLabel()+"</td>"+
		         				"	<td bgcolor=\"#DAF7A6\">"+ arrSelect.get(i-2) +"</td>"+		         				
		         				"</tr>");
			     	}
			     %>	          	
	    </table> 
	    <div id="controls"></div>
		<div id="chart" style="width:100%;position: relative;"></div>
	</body>
</html>