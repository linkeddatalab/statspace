package tuwien.ldlab.statspace.controller.metadata;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.ugent.mmlab.rml.core.RMLEngine;

import be.ugent.mmlab.rml.model.Parameters;
import tuwien.ldlab.statspace.model.metadata.MetaDataGenerator;

public class GenerateMetaData extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(GenerateMetaData.class);

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String sRMLSource=null;   			
    	Parameters parameters = new Parameters();
		Enumeration<String> arrNames = request.getParameterNames();
		while(arrNames.hasMoreElements()){
			String sName = arrNames.nextElement();
			if(sName.equalsIgnoreCase("rmlsource")){
				sRMLSource    = request.getParameter("rmlsource").trim();				
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
			String sPath =  getServletContext().getRealPath("/");
			String sSeparator = File.separator;		
			if(RMLEngine.isLocalFile(sRMLSource)){
				 sRMLSource = sPath +  sRMLSource;
			}
			MetaDataGenerator md = new MetaDataGenerator();
			md.runRMLProcessor(sRMLSource, sPath, sSeparator, parameters);
			boolean bStatus = md.getStatus();
			if(!bStatus){
				response.addHeader("Access-Control-Allow-Origin", "*");
	        	response.getWriter().println("There was an error with your mapping");
			}else{
				File file = new File(md.getOutputFile());	           	
	    		response.setStatus(HttpServletResponse.SC_OK);
	    		response.setContentType("application/octet-stream");
	    		String s = file.getName();
	    		s = s.substring(s.indexOf("_")+1);
	    		response.setHeader("Content-Disposition", "attachment; filename=" + s);    	  	
				
	    		int length=0;	
				OutputStream outStream = response.getOutputStream();
				byte[] byteBuffer = new byte[4096];
			    FileInputStream in = new FileInputStream(file);
			    try{					
			        while ((length = in.read(byteBuffer))!=-1){
			            outStream.write(byteBuffer,0,length);
			        } 
			        outStream.flush();				
				}catch (Exception ex) {
				    log.info(ex.toString());
				} finally {
					outStream.close();
				    in.close();
				}
			}
		}    
	}
}
