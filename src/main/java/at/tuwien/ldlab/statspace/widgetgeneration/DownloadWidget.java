package at.tuwien.ldlab.statspace.widgetgeneration;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DownloadWidget extends HttpServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {		
	
		String sId = request.getParameter("idRequest");		
		Object objRequest = request.getServletContext().getAttribute(sId);	
		
        if(!sId.isEmpty() && objRequest!=null){ 
           	Request req = (Request) objRequest;
	    	String download = req.getDownload(); 
	    	File file = new File(download);
           	
           	//allow user download file
    		int length=0;		
    		response.setContentType("application/octet-stream");
    		response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());    	  	
			
			ServletOutputStream outStream = response.getOutputStream();
			byte[] byteBuffer = new byte[4096];
		    DataInputStream in = new DataInputStream(new FileInputStream(file));	    	        
	        // reads the file's bytes and writes them to the response stream
	        while ((in != null) && ((length = in.read(byteBuffer)) != -1))
	        {
	            outStream.write(byteBuffer,0,length);
	        }    	        
	        in.close();
	        outStream.close();  
        }else{
        	request.setAttribute("errorDownload", 1);
			RequestDispatcher view = request.getRequestDispatcher("/generation/download.jsp");
			view.forward(request, response);
        }				
	}
}
