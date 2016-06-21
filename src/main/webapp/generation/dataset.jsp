<%-- 
    Document   : result
    Created on : Mar 2, 2013, 10:20:06 PM
    Author     : Lamdb
--%>

<%@ page import="java.util.*"%>
<%@ page import="at.tuwien.ldlab.statspace.widgetgeneration.*"%>

<!DOCTYPE html>
<html>
    <head>   
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="http://code.jquery.com/jquery-latest.min.js" type="text/javascript"></script>	
        <title>List of datasets</title>
        <script type="text/javascript">
	        function checkAll(){
	    		var check = document.getElementById('chkall').checked;
	    		$("input[name='chk[]']").each(function ()
	    		{
	    			this.checked = check;
	    		});	
	    	}	    	
	    	function check(){
	    		n = $("input[name='chk[]']:checked").length;
	    		if(n< $("input[name='chk[]']").length) 
	    			document.getElementById('chkall').checked = false;
	    		else
	    			document.getElementById('chkall').checked = true;
	    	} 
	    	function validateForm(){
	        	if($("input[name='chk[]']:checked").length ==0){
	        		alert("You must choose at least one dataset in the list");
	        		return false;
	        	}
	        	var sValue="";
	        	$("input[name='chk[]']:checked").each(function () 
				{				
					if(sValue=="")
						sValue = this.value;
					else
						sValue = sValue + ";" + this.value;					
				}); 		
	        	$("input[name='chkValue']").val(sValue);
				$("input[name='chk[]']").attr({disabled : true});				
	        	$("#chkall").attr({disabled : true});
	        	$("#btnRun").attr({disabled : true, value : "Running. Please wait..."});	
	        	return true;	        		
	    	}	     	     
        </script>
    </head>
    <body>       
          <%	
//             session = request.getSession();
            Object objectId = request.getAttribute("idRequest");      
            if(objectId != null){
            	int idRequest = Integer.parseInt(objectId.toString());        	
	            Object objectRequest = request.getServletContext().getAttribute(Integer.toString(idRequest));        
//  	        Object objectRequest = session.getAttribute(Integer.toString(idRequest));  
                Request req = (Request)objectRequest;                
           		ArrayList<DataSet> ds = req.getEndpoint().getDataSet();           		
          %>
	           <h3 align="center">List of datasets</h3>
		       Check datasets that you want to generate, then click RUN button<br><br>
		  	   <%  
		       if(req.getMetaDataPurpose()==false){
		       %>		       
		        <form method="POST" action="dataset"  onsubmit="return validateForm()">
		       <%
		       }else{
		       %>
		        <form method="POST" action="generation/dataset"  onsubmit="return validateForm()">   
		       <%
		       	}
		       %>
		       	<input type="SUBMIT" id="btnRun" value="RUN"><br><br>               
		        <table border="1">
		        	<tr>
		        		<td align="center">No</td>
		        		<td align="center">URL</td> 
		        		<td>All <input type="checkbox" id="chkall" onClick="checkAll();" checked="checked"></td>       		
		        	</tr>
          <%	
            int i, n = ds.size();
            for(i=0; i<n; i++){
            	out.print(	"<tr>  "+ 
                				"<td>  "+ (i+1) +
                				"</td> "+
                				"<td>  "+ ds.get(i).getUri() +
                				"</td> "+
                				"<td align=\"center\"> " + "<input type=\"checkbox\"  name=\"chk[]\" value='" + i + "' onClick=\"check();\" checked=\"checked\">" +
                				"</td>"+
                			"</tr> ");
  	         } 
  	         out.print("<input type=\"hidden\" id=\"chkValue\" name=\"chkValue\" value=\"\">");
  	         out.print("<input type=\"hidden\" id=\"idRequest\" name=\"idRequest\" value=\""+idRequest +"\">");  	    
          %>
	  	   	</table> 
       		</form>   
	  	 
         <br><br><br><br><br><br><br>
         <%
         	if(req.getMetaDataPurpose()==false){
         %>
	         <div>Notification:<br>To avoid long waiting times for users, we store all widgets, which have already been generated previously. If you select a dataset, which has
	         already been analysed before, we will return the corresponding widget from our cache. Otherwise, our server will analyse this dataset to generate a new widget, and store it in our cache.
	         Widgets are updated to the newest data automatically after each 30 days.
	         </div>
         <% 
         	}else{        	  
         %>
	         <div>Notification:<br>To avoid long waiting times for users, we store all metadata. If you select a dataset, which has
	         already been analysed before, we will return the its metadata from our cache. Otherwise, our server will analyse this dataset to generate a new metadata, and store it in our cache.
	         Please note that, due to the limitation of free requests of Google, metadata can lack of co-reference relationships for values of refArea dimension.
	         </div>
        <%	}  
	  	  }
         %>
    </body>
</html>
