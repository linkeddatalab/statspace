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
        <link href="exploration/scripts/LinkedWidgetPlatform.css" rel="stylesheet" type="text/css">	             
    </head>
    <body>
        <h3 align="center">StatSpace Explorer</h3>    
         <%
        	 Object obj = request.getAttribute("result");  
	         if(obj != null){	          
	         	ArrayList<MetaData> arrMetaData = (ArrayList<MetaData>) obj;
	         	if(arrMetaData.size()>0){
		         	out.print(" <div class=\"FlexBoxContainer FlexBoxContainer-3column\" style=\"position: relative; overflow: hidden;\">\n");
	            	out.print("		<div class=\"FlexBox\" style=\"position: relative; overflow: hidden; background-color: #DEE2BE;\" title=\"" + arrMetaData.get(0).getDataSet().getLabel()+"\">\n"+	  
	            			  " 	  <div class=\"FlexBoxTitle\">" + arrMetaData.get(0).getDataSet().getLabelForDisplay() +"</div>\n"+	
	            			  "		  <div class=\"FlexBoxContent\">Publisher: "+arrMetaData.get(0).getPublisherForDisplay()+"</br>Subject: " + arrMetaData.get(0).getDataSet().getSubjectForDisplay()+"</div>\n"+
	            			  "		  <div class=\"FlexBoxAction\" style=\"position: absolute; overflow: hidden;\">\n"+        			        						  
	       					  "			<a class=\"gwt-Anchor\" href=\"http://statspace.linkedwidgets.org/generateWidget?metadata="+ arrMetaData.get(0).getUri()+"\" title=\"Visualize the dataset\">Visualization</a>\n"+
// 	      	         	  	  "			<a class=\"gwt-Anchor\" href=\"http://localhost:8080/statspace/generateWidget?metadata="+ arrMetaData.get(0).getUri()+"\" title=\"Visualize the dataset\">Visualization</a>\n"+
	           				  "			<a class=\"gwt-Anchor\" href=\""+ arrMetaData.get(0).getSource()+"\" title=\"Direct to data source\">Source</a>\n"+
	           				  "			<a class=\"gwt-Anchor\" href=\""+ arrMetaData.get(0).getMetaDataOntology()+"\" title=\"Show semantic description\">Metadata</a>\n"+        			
	        				  "		</div>\n"+
	        				  " </div>\n");		  	       
	            
	         %>	
	         	 <div>
			    	<h4>Number of comparable datasets:<%=arrMetaData.size()-1%></h4>
			    </div>
		     <%	
			        out.print("</div>\n");
		            int i, n = arrMetaData.size();
		       		out.print("<div class=\"FlexBoxContainer FlexBoxContainer-3column\" style=\"position: relative; overflow: hidden;\">\n");
		            for(i=1; i<n; i++){
		            	out.print("		<div class=\"FlexBox\" style=\"position: relative; overflow: hidden;\" title=\"" + arrMetaData.get(i).getDataSet().getLabel()+"\">\n"+
		            			  " 	  	<div class=\"FlexBoxTitle\">" + arrMetaData.get(i).getDataSet().getLabelForDisplay() +"</div>\n"+	
		            			  "		 	 <div class=\"FlexBoxContent\">Publisher: "+arrMetaData.get(i).getPublisherForDisplay()+"</br>Subject: " + arrMetaData.get(i).getDataSet().getSubjectForDisplay()+"</div>\n"+
		            			  "		  	<div class=\"FlexBoxAction\" style=\"position: absolute; overflow: hidden;\">\n"+	            		
		            			  "				<a class=\"gwt-Anchor\" href=\"http://statspace.linkedwidgets.org/compareDataSet?&id1="+ arrMetaData.get(0).getUri()+"&id2="+ arrMetaData.get(i).getUri()+"\" title=\"Compare two datasets\">Comparison</a>\n"+
		        				  "				<a class=\"gwt-Anchor\" href=\"http://statspace.linkedwidgets.org/generateWidget?metadata="+ arrMetaData.get(i).getUri()+"\" title=\"Visualize the dataset\">Visualization</a>\n"+
// 		        				  "				<a class=\"gwt-Anchor\" href=\"http://localhost:8080/statspace/compareDataSet?&id1="+ arrMetaData.get(0).getUri()+"&id2="+ arrMetaData.get(i).getUri()+"\" title=\"Compare two datasets\">Comparison</a>\n"+
// 		    	        		  "				<a class=\"gwt-Anchor\" href=\"http://localhost:8080/statspace/generateWidget?metadata="+ arrMetaData.get(i).getUri()+"\" title=\"Visualize the dataset\">Visualization</a>\n"+
		        				  "				<a class=\"gwt-Anchor\" href=\""+ arrMetaData.get(i).getSource()+"\" title=\"Direct to data source\">Source</a>\n"+
		        				  "				<a class=\"gwt-Anchor\" href=\""+ arrMetaData.get(i).getMetaDataOntology()+"\" title=\"Show semantic description\">Metadata</a>\n"+
		        				  "		 	</div>\n"+
		        				  "   </div>\n");
		            }
		            out.print("</div>\n");
	         	}else{
	         		out.print("Sorry, metadata in your request is not found in our repository. Please update to the lastest URIs in our SPARQL endpoint");
	         	}
	         }
	        %>	        
	</body>
</html>