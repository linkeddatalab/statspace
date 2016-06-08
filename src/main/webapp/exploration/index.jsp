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
        <script type="text/javascript">
	        function validateForm(){	        	     	
	        	$("#btnRun").attr({disabled : true, value : "Searching. Please wait..."});		        	
	        	return true;	        		
	    	}       
        </script>
    </head>
    <body>
        <h3 align="center">StatSpace Explorer</h3>
        <form method="POST" action="exploration" onsubmit="return validateForm()">
        	<table align="center">
        	<tr>
        		<td>Keyword</td>
        		<td>
        			<input type="text" name="keyword" size="60" id="txtinput">
        		</td>
        	</tr>		
			<tr>
				<td colspan="2" align="center"><input id="btnRun" type="SUBMIT" value="Search"></td>
			</tr>
			</table>			
        </form>     
         <%
	         session = request.getSession();
	         Object idObject = request.getAttribute("idRequest");      
	         if(idObject != null){
	          	int idRequest = Integer.parseInt(idObject.toString());        	
	            Object obj = request.getServletContext().getAttribute(Integer.toString(idRequest));    
	         	ArrayList<MetaData> arrMetaData = (ArrayList<MetaData>) obj;
       	        
	            %>		        	      
			       <table border="0">
			        	<tr>
			        		<td align="center"><b>No.</b></td>
			        		<td align="center"><b>Data set</b></td>	
			        		<td align="center"><b>Publisher</b></td>		        		
			        		<td align="center"><b>Label</b></td>
			        		<td align="center"><b>Subject</b></td> 
			        		
			        	</tr>
	          <%	
		            int i, n = arrMetaData.size();
		            for(i=0; i<n; i++){
		            	out.print(	"<tr>  "+ 
		                				"<td>  "+ (i+1) + "</td> "+
// 		                				"<td><a href=\"http://localhost:8080/statspace/exploreDataSet?idRequest="+idRequest+"&id1="+ arrMetaData.get(i).getUri() + "\">" + arrMetaData.get(i).getDataSet().getUri() + "</a></td> "+
// 		                				"<td><a href=\"http://statisticaldata.linkedwidgets.org/exploreDataSet?idRequest="+idRequest+"&id1="+ arrMetaData.get(i).getUri() + "\">" + arrMetaData.get(i).getDataSet().getUri() + "</a></td> "+
 		                				"<td><a href=\"http://statspace.linkedwidgets.org/exploreDataSet?idRequest="+idRequest+"&id1="+ arrMetaData.get(i).getUri() + "\">" + arrMetaData.get(i).getDataSet().getUri() + "</a></td> "+ 		                				
			             				"<td>  "+ arrMetaData.get(i).getPublisherForDisplay() + "</td> "+	  
		                				"<td>  "+ arrMetaData.get(i).getDataSet().getLabel() + "</td> "+
		                				"<td>  "+ arrMetaData.get(i).getDataSet().getSubjectCode() + "</td> "+		                				              				
		                			"</tr> ");
		  	         }  	           	    
	          %>
		  	   	</table>	       		
		  	  <% 
		  	  }
	          %>
		  
	</body>
</html>