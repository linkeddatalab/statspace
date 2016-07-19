<!--
To change this template, choose Tools | Templates
and open the template in the editor.
-->

<%@ page import="java.util.*"%>
<%@ page import="at.tuwien.ldlab.statspace.metadata.*"%>

<!DOCTYPE html>
<html>
    <head>
        <title>StatSpace Explorer</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="http://code.jquery.com/jquery-latest.min.js" type="text/javascript"></script>
		<!-- Load c3.css -->
		<link href="explorer/scripts/c3.min.css" rel="stylesheet" type="text/css">

		<!-- Load d3.js and c3.js -->
		<script src="explorer/scripts/d3.v3.min.js" charset="utf-8"></script>
		<script src="explorer/scripts/c3.min.js"></script>
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
			.d0{
				background-color:#A68BC4 
			}
			.d1{
				background-color:#D7D063
			}
			.d3{
				background-color:#FAD6D6
			}
			.d4{
				background-color:#CCFFCC
			}
			.d5{
				background-color:#BAF4F7
			}
			.d2{
				background-color:#D5FCF5
			}
			.d6{
				background-color:#CCFF66
			}
			.d7{
				background-color:#FFCCFF
			}
			.d8{
				background-color:#DAF7A6
			}
	</style>			
	<script>
	 <%				  
	    Object obj = request.getAttribute("result");    
	    ArrayList<MetaData> arrMetaData = (ArrayList<MetaData>) obj;
	    MetaData md1 = arrMetaData.get(0);
	    MetaData md2 = arrMetaData.get(1);
	    int i, j, n = md1.getNumberofComponent();	 
	 %>	   
	    var data1 = JSON.parse('<%= md1.getJSONSimpleResult() %>');   
	    var data2 = JSON.parse('<%= md2.getJSONSimpleResult() %>');
	    var vars  = data1.head.vars;	
		var count = <%= n-1 %>;
		var label1 = "<%= md1.getDataSet().getLabel().replace('"', ' ') %>";
		var label2 = "<%= md2.getDataSet().getLabel().replace('"', ' ') %>";
	    
		function contains(vals, v){
			var i;
			for(i=0; i<vals.length; i++)
				if(vals[i]==v)
					return true;
			return false;
		}
	    
	    function updateChart() {		
			var dataCL, columnsLC, as={}, dataSP, columnsSP, sp={};
			var v, values=[];
			var k, i, j, n;
			var x=['x'];		
			n = data1.results.bindings.length;
			m = data2.results.bindings.length;			
			for(i=1; i<count; i++){
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
				
	        dataCL=[];
			dataSP=[];
			columnsLC=[];
			columnsLC.push(x);
			columnsSP=[];
			<%
			out.print(md1.getJSFunction(1));
			%>			
			
			<%
			out.print(md2.getJSFunction(2));
			%>	
			if(columnsSP.length>1 && columnsSP.length%2==0){
				for(i=0; i<columnsSP.length/2; i++){				
					lx = columnsSP[i][0];
					ly = columnsSP[i+columnsSP.length/2][0];				
					sp[ly] = lx;					
				}		
			}
			
			drawChart(columnsLC, as, columnsSP, sp);			
		}
	 </script>
	 <script type="text/javascript">
		function drawChart(columnsLC, as, columnsSP, sp){
			var chart = c3.generate({
			    bindto: '#chartLC',
			    data: {
			    	x: 'x',				
					columns: columnsLC,
					axes: as		
				},
				line: {
					connect_null: true
				},
				axis: {				 
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
			
			if(columnsSP.length>1 && columnsSP.length%2==0){
				$("#chartSP").show();
				var chart2 = c3.generate({
					bindto: '#chartSP',
					point: {
						r:5
					},
					data: {				
						// iris data from R
						xs : sp,					
						columns: columnsSP,							
						type: 'scatter'
					},
					axis: {
						x: {
							label: label1,
							tick: {
								rotate: 75,
								culling: {
									max: 10 // the number of tick texts will be adjusted to less than this value
								}								
							},
							height: 100																	
						},
						y: {
							label: label2
						}
					}
				});
			}else{
				$("#chartSP").hide();
			}
		}
	</script>	        
    </head>
    <body>
        <h3 align="center">StatSpace Explorer</h3>              	      
		<table border="0">
			     <%
			     	ArrayList<String> arrSelect = new ArrayList<String>();
			     	for(i=2; i<n; i++){
			     		String s = "\n		<select id=\""+md1.getComponent(i).getVariable().substring(1)+ "\" onchange=\"updateChart()\" size=\"5\" multiple>\n";
			     		ArrayList<String> arrDistinctValue = md1.getDistinctRefValueLabel(i);			     	
			     		for(j=0; j<arrDistinctValue.size(); j++)
			     			s = s + "			<option value=\"" + arrDistinctValue.get(j) + "\">" + arrDistinctValue.get(j) + "</option>\n";
			     		s = s + "		</select>\n";
			     		arrSelect.add(s);
			     	}
			     	for(i=2; i<n; i++){
			     		if(i==2)
					     	out.print(	
					     		"	<tr>\n"+ 
		         				"		<td class='d"+ i%9 +"'>"+ md1.getComponent(i).getLabel()+"		</td>\n"+
		         				"		<td class='d"+ i%9 +"'>"+ arrSelect.get(i-2) +"		</td>\n"+
		         				"   	<td class='d8'"+" rowspan='" + n + "'>"+ "Common dimensions and values </td>\n"+
		         				"	</tr>\n");
			     		else
			     			out.print(	
					     		"	<tr>\n"+ 
		         				"		<td class='d"+ i%9 +"'>"+ md1.getComponent(i).getLabel()+"		</td>\n"+
		         				"		<td class='d"+ i%9 +"'>"+ arrSelect.get(i-2) +"		</td>\n"+		         				
		         				"	</tr>\n");
			     	}
			     %>	          	
	    </table> 
	    <div id="controls"></div>
		<div id="chartLC" style="width:60%;position: relative; max-height: 500px;"></div>
		<div id="chartSP" style="width:60%;position: relative; max-height: 600px;"></div>
	</body>
</html>