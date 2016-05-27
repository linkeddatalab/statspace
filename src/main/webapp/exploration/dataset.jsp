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
    </head>
    <body>
        <h3 align="center">StatSpace Explorer</h3>    
         <%
	         session = request.getSession();
	         Object idObject = request.getAttribute("idRequest");      
	         if(idObject != null){
	          	int idRequest = Integer.parseInt(idObject.toString());        	
	            Object obj = request.getServletContext().getAttribute(Integer.toString(idRequest));    
	         	ArrayList<MetaData> arrMetaData = (ArrayList<MetaData>) obj;
       	        
	            %>		        	      
			       <table border="0">
			         <table border="0">
			     <%
			     	out.print(	
			     		"<tr>  "+ 
         				"	<td><b>Uri</b></td>"+
         				"	<td>" + arrMetaData.get(0).getUri() + "</td> "+
         				"	<td width=\"150px\"></td>"+
         				"   <td><b>Note</b>: European Union Data Portal is temporarily unavailable to 30.05.2016. <a href=\"http://open-data.europa.eu/\">Detail</a></td>" +
         				"</tr>" +
         				"<tr>"+
         				"	<td><b>Publisher</b></td>"+ 
         				"   <td>" + arrMetaData.get(0).getPublisher() + "</td> "+
         				"</tr>"+
         				"<tr>"+
         				"	<td><b>Label</b></td>"+ 
         				"   <td>" + arrMetaData.get(0).getDataSet().getLabel() + "</td> "+
         				"</tr>"+
         				" 	<td><b>Subject</b></td>"+
         				"	<td>"+ arrMetaData.get(0).getDataSet().getSubject() + "</td> "+
         				"</tr>");         				
			     %>	     
			     	</table>     	
	          	  </table>        	      
		       	  <table border="0">
		        	<tr>		        	
		        		<th align="center">No.</th>		        		
		        		<th align="center">Comparable data sets of structure or subject</th>	
		        		<th align="center">Publisher</th>		        		
		        		<th align="center">Label</th>
		        		<th align="center">Subject</th>		        		
		        	</tr>
         		<%	
		            int i, n = arrMetaData.size();
		            for(i=1; i<n; i++){
		            	out.print(		"\n	<tr>\n"+ 
		                				"	<td>"+ (i) + "</td>\n"+
// 		                				"	<td><a href=\"http://localhost:8080/statspace/compareDataSet?idRequest="+idRequest+"&id1="+ arrMetaData.get(0).getUri()+"&id2="+ arrMetaData.get(i).getUri() + "\">" + arrMetaData.get(i).getDataSet().getUri() + "</a></td>\n"+	
		                				"	<td><a href=\"http://statspace.linkedwidgets.org/compareDataSet?idRequest="+idRequest+"&id1="+ arrMetaData.get(0).getUri()+"&id2="+ arrMetaData.get(i).getUri() + "\">" + arrMetaData.get(i).getDataSet().getUri() + "</a></td>\n"+
		                				"	<td>  "+ arrMetaData.get(i).getPublisherForDisplay() + "</td>\n"+	  
		                				"	<td>  "+ arrMetaData.get(i).getDataSet().getLabel() + "</td>\n"+
		                				"	<td>  "+ arrMetaData.get(i).getDataSet().getSubjectCode() + "</td>\n"+		                				              				
		                				"	</tr>\n");
		  	         }  	           	    
	          	%>
		  	   	 </table>	       		
		  	  <% 
		  	  }
	          %>
		  
	</body>
</html>