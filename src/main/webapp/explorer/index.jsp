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
        <meta name="keywords" content="linked data, data exploration, statistical data"/>        
		<link href="explorer/scripts/LinkedWidgetPlatform.css" rel="stylesheet" type="text/css">
			
		<!-- CSS file - author: The CSS Ninja-->
		<link href="explorer/scripts/style.css" rel="stylesheet" type="text/css">	
			
		<script src="http://code.jquery.com/jquery-latest.min.js" type="text/javascript"></script>	       
        <script type="text/javascript">  
	        function validateForm(){	        	     	
	        	$("#btnRun").attr({disabled : true, value : "Searching. Please wait..."});		        	
	        	return true;	        		
	    	} 
	        
	        function narrower(id){
				var chk = $(id).is(':checked');			
				if(chk==true){
					$(id).siblings().css({"color": "#D91616"});				
				}else{
					$(id).siblings().css({"color": ""});
				}
				var pubCounter=0, subCounter=0, count, val;
				var pubs = [], subs=[];			
				$("input[name='pub[]']:checked").each(function (){
					val = $(this).val();
					if(val.length>40) val = val.substring(0,40);
					pubs[pubCounter] = val;	
					pubCounter++;
				});
				$("input[name='sub[]']:checked").each(function (){
					val = $(this).val();
					if(val.length>40) val = val.substring(0,40);
					subs[subCounter] = val;	
					subCounter++;
				});
				var results = document.querySelectorAll(".FlexBox");
				if(pubCounter==0 && subCounter==0){
					for(i=0; i<results.length; i++){
						$(results[i]).css("display","block");					
					}				
					$("#txtCount").text("Search results:"+results.length);
				}else{
					for(i=0; i<results.length; i++){
						$(results[i]).css("display","none");					
					}
					count=0;
					var selector;
					for(i=0; i<results.length; i++){
						 selector = $(results[i]).children()[1];
						 val = $(selector).text();
						 if(contains(val, pubs, subs)){
							$(results[i]).css("display","block");
							count++;
						}					
					}				
					$("#txtCount").text("Search results:"+count);
				}			
			}

			function contains(val, pubs, subs){
				var i;
				var bPub=false;			
				
				if(pubs.length>0){
					for(i=0; i<pubs.length; i++)
						if(val.indexOf("Publisher: "+pubs[i])!=-1){
							bPub=true;
							break;
						}
					if(bPub==false)	return false;
				}
				if(subs.length>0){			
					for(i=0; i<subs.length; i++){
						if(val.indexOf("Subject: "+subs[i])!=-1){
							return true;
						}
					}			
					return false;
				}
				return true;
			} 
	        
        </script> 
    </head>
    <body>
    	<div class="Result">		
	        <h3>StatSpace Explorer</h3>
	        <form method="post" action="explorer" onsubmit="return validateForm()">
	        	<table>
		        	<tr>        	
		        		<td><input type="text" name="keyword" size="60" id="txtInput"></td>        	
						<td align="center"><input id="btnRun" type="SUBMIT" value="Search"></td>
					</tr>
				</table>			
	        </form>     
          <%      	
	         Object objKeyword = request.getAttribute("keyword");
	    	 String sKeyword = "";
	    	 if(objKeyword != null){
	    		 sKeyword = (String) objKeyword;
	    	 }
	      %>
	      <script type="text/javascript">       
    	       document.getElementById("txtInput").value = '<%=sKeyword%>';
    	  </script>
    	  
	      <%
	         Object objResult = request.getAttribute("result");       	   
	         if(objResult != null){	          	
	         	ArrayList<MetaData> arrMetaData = (ArrayList<MetaData>) objResult;
	         	Object objPublisher = request.getAttribute("publisher"); 
	         	ArrayList<StringCouple> arrPublisher = new ArrayList<StringCouple>();
	 	        if(objPublisher != null)          	
	 	        	arrPublisher = (ArrayList<StringCouple>) objPublisher;
 	        	Object objSubject = request.getAttribute("subject"); 
	         	ArrayList<StringTriple> arrSubject = new ArrayList<StringTriple>();
	 	        if(objPublisher != null)          	
	 	        	arrSubject = (ArrayList<StringTriple>) objSubject;
	 	        	
	 	        int i, n = arrMetaData.size();
	       %>		        	      
		    
		    	<h4 id="txtCount">Search results:<%=arrMetaData.size()%></h4>
		   </div>  
		   <div style="position: absolute; left: 0px; top: 110px; bottom: 0px; width: 300px;">
		   	 <ol class="tree">	
				<li>
					<label for="publisher">Narrow by Publisher</label> 
					<input type="checkbox" id="publisher" value="" /> 
					<ol>	
			<%
					for(i=0; i<arrPublisher.size(); i++){
						  out.print("						<li>\n"+
									"							<label for=\"pub"+i+"\">\n"+
									"							<div style=\"width:220px; overflow:hidden;\" title=\"" + arrPublisher.get(i).getFirstString() +"\">\n"+
									"								<span class=\"label\">"+arrPublisher.get(i).getFirstString().substring(0, Math.min(arrPublisher.get(i).getFirstString().length(), 24))+"</span>\n"+
									"								<span class=\"count\">"+arrPublisher.get(i).getSecondString()+"</span>\n"+
									"							</div>\n"+
									"							</label>\n"+
									"							<input type=\"checkbox\" id=\"pub"+i+"\" name=\"pub[]\" value=\""+arrPublisher.get(i).getFirstString()+ "\" onclick=\"narrower(this)\"/>\n"+					
									"						</li>\n");
					}
			%>
					</ol>
				</li>	
				<li>
					<label for="subject">Narrow by Subject</label> 
					<input type="checkbox" id="subject" value="" /> 
					<ol>
			<%
					for(i=0; i<arrSubject.size(); i++){
						  out.print("						<li>\n"+
									"							<label for=\"sub"+i+"\">\n"+
									"							<div style=\"width:220px; overflow:hidden;\" title=\"" + arrSubject.get(i).getFirstString() +"\">\n"+
									"								<span class=\"label\">"+arrSubject.get(i).getFirstString().substring(0, Math.min(arrSubject.get(i).getFirstString().length(), 24))+"</span>\n"+
									"								<span class=\"count\">"+arrSubject.get(i).getSecondString()+"</span>\n"+ 
									"							</label>\n"+
									"							<input type=\"checkbox\" id=\"sub"+i+"\" name=\"sub[]\" value=\""+arrSubject.get(i).getThirdString()+ "\" onclick=\"narrower(this)\"/>\n"+					
									"						</li>\n");
					}
			%>
					</ol>
				</li>							
			</ol>
		  </div>									
	       <%		   			
	           
	       		out.print("<div class=\"FlexBoxContainer FlexBoxContainer-4column\" style=\"position: relative; overflow: hidden;\">\n");
	            for(i=0; i<n; i++){	            	
	            	out.print("			<div class=\"FlexBox\" style=\"position: relative; overflow: hidden;\" title=\"" + arrMetaData.get(i).getLabel()+"\">\n"+
	            			  "				<div class=\"FlexBoxTitle\">" + arrMetaData.get(i).getLabelForDisplay() +"</div>\n"+	
	            			  "				<div class=\"FlexBoxContent\">Publisher: "+arrMetaData.get(i).getPublisherForDisplay()+"</br>Subject: " + arrMetaData.get(i).getDataSet().getSubjectForDisplay()+"</div>\n"+
	            			  "			  	<div class=\"FlexBoxAction\" style=\"position: absolute; overflow: hidden;\">\n"+	            		
// 	        				  "					<a class=\"gwt-Anchor\" href=\"http://statspace.linkedwidgets.org/exploreDataSet?&id1="+ arrMetaData.get(i).getUri()+"\" title=\"Find datasets having same structure and common values\">Relatable datasets</a>\n"+
// 	        				  "					<a class=\"gwt-Anchor\" href=\"http://statspace.linkedwidgets.org/generateWidget?metadata="+ arrMetaData.get(i).getUri()+"\" title=\"Visualize the dataset\">Visualization</a>\n"+
	        				  "					<a class=\"gwt-Anchor\" href=\"http://localhost:8080/statspace/exploreDataSet?&id1="+ arrMetaData.get(i).getUri()+"\" title=\"Find datasets having same structure and common values\">Relatable-datasets</a>\n"+
	    	        		  "					<a class=\"gwt-Anchor\" href=\"http://localhost:8080/statspace/generateWidget?metadata="+ arrMetaData.get(i).getUri()+"\" title=\"Visualize the dataset\">Visualization</a>\n"+
	        				  "					<a class=\"gwt-Anchor\" href=\""+ arrMetaData.get(i).getSource()+"\" title=\"Direct to data source\">Source</a>\n"+
	        				  "					<a class=\"gwt-Anchor\" href=\""+ arrMetaData.get(i).getMetaDataOntology()+"\" title=\"Show semantic description\">Metadata</a>\n"+
	        				  "				</div>\n"+
	        				  "			</div>\n");
		  	         }
	       
	     		 out.print("</div>\n");
	 	      }
	     	%>	
	     	
	     	<div style="margin-left:300px;">
	    		 Note:  We use metadata to represent a single-measure data set. Therefore, we split a multi-measure data set into multiple single-measure data sets, and use different metadata to represent each data set.
	     	</div>
	     	  	 
	</body>
</html>