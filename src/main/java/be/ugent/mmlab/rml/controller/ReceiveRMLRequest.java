package be.ugent.mmlab.rml.controller;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;

import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.main.Processor;
import be.ugent.mmlab.rml.model.Parameters;
import be.ugent.mmlab.rml.model.RMLMapping;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLStructureException;
import net.antidot.semantic.rdf.rdb2rdf.r2rml.exception.InvalidR2RMLSyntaxException;

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
			System.out.println(sPath);
			String sSeparator = File.separator;		
			if(RMLEngine.isLocalFile(sRMLSource)){
				 sRMLSource = sPath +  sRMLSource;
			}
			
			Processor processor = new Processor(sRMLSource, sPath, sSeparator);
			processor.run(sRMLSource, parameters);
			boolean bStatus = processor.getStatus();
			if(!bStatus){
				response.addHeader("Access-Control-Allow-Origin", "*");
	        	response.getWriter().println("There was an error with your mapping");
			}else{
				//allow user download file
				File file = new File(processor.getOutputPath());	           	
	    		response.setStatus(HttpServletResponse.SC_OK);
	    		response.addHeader("Access-Control-Allow-Origin", "*");
	    		response.setContentType("application/octet-stream");	    	
	    		response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());    	  	
				
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
			}		
		}		
    }
}
