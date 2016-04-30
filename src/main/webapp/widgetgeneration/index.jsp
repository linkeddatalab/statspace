<!--
To change this template, choose Tools | Templates
and open the template in the editor.
-->
<!DOCTYPE html>
<html>
    <head>
        <title>Widget Generation</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="http://code.jquery.com/jquery-latest.min.js" type="text/javascript"></script>	
        <script type="text/javascript">
	        function validateForm(){
	        	if($("h4").length>0)
	        		$("h4").hide();
	           	if($("#txtinput").val()==""){
	        		alert("You must enter a sparql endpoint");
	        		return false;
	        	}
	           	if($("#txtinput").val().indexOf(" ")!=-1){
	        		alert("The sparql endpoint contains space character");
	        		return false;
	        	}
	        	$("#btnRun").attr({disabled : true, value : "Running. Please wait..."});		        	
	        	return true;	        		
	    	}       
        </script>
    </head>
    <body>
        <h3 align="center">Generating widgets for statistical datasets which are published with the use of RDF Data Cube Vocabulary</h3>
        <form method="POST" action="endpoint" onsubmit="return validateForm()">
        	<table>
        	<tr>
        		<td>Sparql endpoint</td>
        		<td>
        			<input type="text" name="endpoint" size="60" id="txtinput" value="http://digital-agenda-data.eu/data/sparql">
        		</td>
        	</tr>		
			<tr>
				<td colspan="2" align="center"><input id="btnRun" type="SUBMIT" value="RUN"></td>
			</tr>
			</table>			
        </form>
       <div id="infor"></div>
         <%
       		Object sError = request.getAttribute("error");         
            if(sError != null){            	
            	int error = Integer.parseInt(sError.toString());            	
            	if(error==0){
            		%>
               	 	<script type="text/javascript">            	 	
   						$("#infor").html("<h4>Sorry. No dataset is detected</h4>");
   					</script>
   					<%     
    			}else if(error==1){
    				%>
               	 	<script type="text/javascript">            	 	
   						$("#infor").html("<h4>Sorry. The sparql endpoint is not availabe or there is an request timeout error </h4>");
   					</script>
   					<%     				
	            }else if(error==2){
	            	%>
               	 	<script type="text/javascript">            	 	
   						$("#infor").html("<h4>Sorry. There was a problem in parsing result </h4>");
   					</script>
   					<% 			
				}else if(error==3){
					%>
               	 	<script type="text/javascript">            	 	
   						$("#infor").html("<h4>Sorry. There was a problem in analyzing the sparql endpoint</h4>");
   					</script>
   					<%     			
    			}            	
            	request.removeAttribute("error");
            }else{
            	%>
       			 <script type="text/javascript">
					$("#infor").text("");
				</script>
       	<%  
            }
		%> 
		  
	</body>
</html>