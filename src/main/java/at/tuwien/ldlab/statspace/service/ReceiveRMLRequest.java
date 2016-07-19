package at.tuwien.ldlab.statspace.service;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import at.tuwien.ldlab.statspace.metadata.MetaDataForRML;
import be.ugent.mmlab.rml.model.Parameters;

public class ReceiveRMLRequest extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;	
	private static Log log = LogFactory.getLog(ReceiveRMLRequest.class);
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
		doGet(request, response);
	}
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {    
    	String sRMLSource=null; 
    	String sCache = "", sDownload="";    	
    	String sQuery = request.getQueryString();
    	Parameters parameters = new Parameters();
		Enumeration<String> arrNames = request.getParameterNames();	
		while(arrNames.hasMoreElements()){
			String sName = arrNames.nextElement();
			if(sName.equalsIgnoreCase("rmlsource")){
				sRMLSource  = request.getParameter("rmlsource").trim();				
			}else if(sName.equalsIgnoreCase("cache")){
				sCache   	= request.getParameter("cache").trim();				
			}else if(sName.equalsIgnoreCase("download")){
				sDownload 	= request.getParameter("download").trim();				
			}else{
				String sValue = request.getParameter(sName);
				parameters.addParameterValue(sName, sValue);
			}			
		}
		
		if(sRMLSource==null){
			if(parameters.getSize()!=0){
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
	        	response.addHeader("Access-Control-Allow-Origin", "*");
	        	response.getWriter().println("");		
			}
		}else{
			boolean bUseCache = true;
			if(sCache!=null && sCache.toLowerCase().equals("no"))
				bUseCache = false;

			//for queries in generateQuery, we don't need to return rdf file to users
			boolean bDownload = true;
			if(sDownload!=null && sDownload.toLowerCase().equals("no"))
				bDownload = false;
			
			String folderWebApp =  getServletContext().getRealPath("/");
			
			long lStartTime = new Date().getTime();    	
			MetaDataForRML md = new MetaDataForRML(sRMLSource, folderWebApp, sQuery);
			md.createRDF(parameters, bUseCache, sQuery);			
			boolean bStatus = md.getStatus();
			if(!bStatus){	
				log.info("RMLMapping - error in analyzing " + sRMLSource);
				response.addHeader("Access-Control-Allow-Origin", "*");
	        	response.getWriter().println("There was an error in analyzing your mapping");
			}else{
				if(bDownload){				
					//allow user download file
					response.setStatus(HttpServletResponse.SC_OK);
		    		response.addHeader("Access-Control-Allow-Origin", "*");
		    		response.setContentType("application/octet-stream");	
			        File file = new File(md.getOutputRDFFile());	
			        String s = file.getName();
			        s = s.replace(".ttl.", ".");
		    		response.setHeader("Content-Disposition", "attachment; filename=" + s);  	    	
		    		int length=0;	
					ServletOutputStream outStream = response.getOutputStream();
					byte[] byteBuffer = new byte[4096];
				    DataInputStream in = new DataInputStream(new FileInputStream(file));	     
			        while ((in != null) && ((length = in.read(byteBuffer)) != -1))
			        {
			            outStream.write(byteBuffer,0,length);
			        }    	        
			        in.close();
			        outStream.close();
			        long lEndTime = new Date().getTime();
					long difference = lEndTime - lStartTime;
					log.info(difference);
				}else{
					response.setStatus(HttpServletResponse.SC_OK);
					response.addHeader("Access-Control-Allow-Origin", "*");		        	
				}
			}		
		}		
    }
}
