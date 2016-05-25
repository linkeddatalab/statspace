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
        <title>Error Page</title>       
    </head>
    <body>       
        <%
        	Object objErrorPage = request.getAttribute("errorPage");       	
        	if(objErrorPage != null){
        			String sMsg = objErrorPage.toString();	             	
	          %>	 <h4 align="center">Sorry, server cannot finish your request. Please send an email to <a href="mailto:lam@ifs.tuwien.ac.at">lam@ifs.tuwien.ac.at</a> about this request. We will fix error in soon time</h4>           		
				     <p>Detail Error: <%=sMsg%></p>
			 <%           	            
            }
         %>                
    </body>
</html>
