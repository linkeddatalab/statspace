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
         <script src="http://code.jquery.com/jquery-latest.min.js" type="text/javascript"></script>	
		<link href="exploration/scripts/LinkedWidgetPlatform.css" rel="stylesheet" type="text/css">	       
        <script type="text/javascript">  
	        function validateForm(){	        	     	
	        	$("#btnRun").attr({disabled : true, value : "Searching. Please wait..."});		        	
	        	return true;	        		
	    	} 	        
        </script>
    </head>
    <body>
        <h3>StatSpace Explorer</h3>
        <form method="POST" action="exploration" onsubmit="return validateForm()">
        	<table>
	        	<tr>        	
	        		<td><input type="text" name="keyword" size="60" id="txtInput"></td>        	
					<td align="center"><input id="btnRun" type="SUBMIT" value="Search"></td>
				</tr>
			</table>			
        </form>     
          <%        	
	         Object kwObject = request.getAttribute("keyword");
	    	 String sKeyword = "";
	    	 if(kwObject != null){
	    		 sKeyword = (String) kwObject;
	    	 }
	      %>
	      <script type="text/javascript">       
    	       document.getElementById("txtInput").value = '<%=sKeyword%>';
    	  </script>
	      <%
	         Object idObject = request.getAttribute("idRequest");         	   
	         if(idObject != null){
	          	int idRequest = Integer.parseInt(idObject.toString());
	            Object obj = request.getAttribute(Integer.toString(idRequest));    
	         	ArrayList<MetaData> arrMetaData = (ArrayList<MetaData>) obj;       	        
	       %>		        	      
		    <div>
		    	<h4>Search results:<%=arrMetaData.size()%></h4>
		    </div>
	       <%	
	            int i, n = arrMetaData.size();
	       		out.print("<div class=\"FlexBoxContainer FlexBoxContainer-3column\" style=\"position: relative; overflow: hidden;\">\n");
	            for(i=0; i<n; i++){
	            	out.print("		<div class=\"FlexBox\" style=\"position: relative; overflow: hidden;\">\n"+
	            			  " 	  <div class=\"FlexBoxTitle\">" + arrMetaData.get(i).getDataSet().getLabelForDisplay() +"</div>\n"+	
	            			  "		  <div class=\"FlexBoxContent\">Publisher: "+arrMetaData.get(i).getPublisherForDisplay()+"</br>Subject: " + arrMetaData.get(i).getDataSet().getSubjectForDisplay()+"</div>\n"+
	            			  "		  <div class=\"FlexBoxAction\" style=\"position: absolute; overflow: hidden;\">\n"+	            		
	        				  "			<a class=\"gwt-Anchor\" href=\"http://statspace.linkedwidgets.org/exploreDataSet?idRequest="+idRequest+"&id1="+ arrMetaData.get(i).getUri()+"\" title=\"Find datasets having same structure and common values\">Comparable-datasets</a>\n"+
	        				  "			<a class=\"gwt-Anchor\" href=\"http://statspace.linkedwidgets.org/generateWidget?metadata="+ arrMetaData.get(i).getUri()+"\" title=\"Visualize the dataset\">Visualization</a>\n"+
// 	        				  "			<a class=\"gwt-Anchor\" href=\"http://localhost:8080/statspace/exploreDataSet?idRequest="+idRequest+"&id1="+ arrMetaData.get(i).getUri()+"\" title=\"Find datasets having same structure and common values\">Comparable-datasets</a>\n"+
// 	    	        		  "			<a class=\"gwt-Anchor\" href=\"http://localhost:8080/statspace/generateWidget?metadata="+ arrMetaData.get(i).getUri()+"\" title=\"Visualize the dataset\">Visualization</a>\n"+
	        				  "			<a class=\"gwt-Anchor\" href=\""+ arrMetaData.get(i).getSource()+"\" title=\"Direct to data source\">Source</a>\n"+
	        				  "			<a class=\"gwt-Anchor\" href=\""+ arrMetaData.get(i).getMetaDataOntology()+"\" title=\"Show semantic description\">Metadata</a>\n"+
	        				  "		</div>\n"+
	        				  "    </div>\n");
		  	         }
	            out.print("</div>\n");
		  	  }
	        %>
		  	 
	</body>
</html>