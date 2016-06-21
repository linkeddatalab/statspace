package be.ugent.mmlab.rml.main;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
			String folderWebApp =  getServletContext().getRealPath("/");			
			String sSeparator = File.separator;		
			Processor processor = new Processor(sRMLSource, folderWebApp, sSeparator);
			processor.run(sRMLSource, parameters);
			boolean bStatus = processor.getStatus();
			if(!bStatus){	
				log.info("RMLMapping - error in analyzing " + sRMLSource);
				response.addHeader("Access-Control-Allow-Origin", "*");
	        	response.getWriter().println("There was an error in analyzing your mapping");
			}else{
				//allow user download file
				response.setStatus(HttpServletResponse.SC_OK);
	    		response.addHeader("Access-Control-Allow-Origin", "*");
	    		response.setContentType("application/octet-stream");	
		        File file = new File(processor.getOutputPath());	
		        String s = file.getName();
	    		s = s.substring(s.indexOf("_")+1);
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
			}		
		}		
    }
}
