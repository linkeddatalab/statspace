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
        <link href="explorer/scripts/LinkedWidgetPlatform.css" rel="stylesheet" type="text/css">	  
        <!-- CSS file - author: The CSS Ninja-->
		<link href="explorer/scripts/style.css" rel="stylesheet" type="text/css">			
		<script type="text/javascript">  
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
					for(i=1; i<results.length; i++){
						$(results[i]).css("display","block");					
					}				
					$("#txtCount").text("Search results:"+results.length);
				}else{
					for(i=1; i<results.length; i++){
						$(results[i]).css("display","none");					
					}
					count=0;
					var selector;
					for(i=1; i<results.length; i++){
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
        <h3 align="center">StatSpace Explorer</h3>    
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
	 	        
	         	if(arrMetaData.size()>0){
	        %>
	         	<div style="position: absolute; left: 0px; top: 175px; bottom: 0px; width: 300px;">
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
	 				out.print(" <div id=\"metadata0\" class=\"FlexBoxContainer FlexBoxContainer-4column\" style=\"position: relative; overflow: hidden;\">\n");
	            	out.print("		<div class=\"FlexBox\" style=\"position: relative; overflow: hidden; background-color: #DEE2BE;\" title=\"" + arrMetaData.get(0).getLabel()+"\">\n"+	  
	            			  " 	  <div class=\"FlexBoxTitle\">" + arrMetaData.get(0).getLabelForDisplay() +"</div>\n"+	
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
			    	<h4 id="txtCount">Number of relatable datasets:<%=arrMetaData.size()-1%></h4>
			    </div>    
			    
		     <%	
			        out.print("</div>\n");
		          
		       		out.print("<div class=\"FlexBoxContainer FlexBoxContainer-4column\" style=\"position: relative; overflow: hidden;\">\n");
		            for(i=1; i<n; i++){
		            	out.print("		<div class=\"FlexBox\" style=\"position: relative; overflow: hidden;\" title=\"" + arrMetaData.get(i).getLabel()+"\">\n"+
		            			  " 	  	<div class=\"FlexBoxTitle\">" + arrMetaData.get(i).getLabelForDisplay() +"</div>\n"+	
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