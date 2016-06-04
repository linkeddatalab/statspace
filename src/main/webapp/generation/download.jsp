<%-- 
    Document   : result
    Created on : Mar 2, 2013, 10:20:06 PM
    Author     : Lamdb
--%>

<%@ page import="java.util.*"%>
<%@ page import="tuwien.ldlab.statspace.model.*"%>

<!DOCTYPE html>
<html>
    <head>   
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="http://code.jquery.com/jquery-latest.min.js" type="text/javascript"></script>	
        <title>Download</title>       
    </head>
    <body>       
        <%
        	Object objIdRequest = request.getAttribute("idRequest");        	
        	Object objError     = request.getAttribute("errorDownload");
        	Object objList      = request.getAttribute("errorList");
        	if(objError != null){
        		out.print("<h4>Sorry. This file is not available or the session is not authenticated</h4>");
        		request.removeAttribute("error_download");
        	}else{        	
	            if(objIdRequest == null){            
	            	out.print("<h4>Sorry. This file is not available</h4>");	            	
	            }else{
	             	int idRequest = Integer.parseInt(objIdRequest.toString());
	          %>	 <h3 align="center">Download widget</h3>           		
				     Click here to download <a href="download?idRequest=<%= idRequest %>">Download</a><br/><br/>				     
			 <% 	
			 		if(objList!=null){
			 			out.print("<br><br><br>Error! the following datasets don't contain Dimension, Measure or value of Dimensions: " + "<br><br>");			 		
			 			out.print(objList.toString());
			 		}       		
	            }	            
            }
         %>               
    </body>
</html>
