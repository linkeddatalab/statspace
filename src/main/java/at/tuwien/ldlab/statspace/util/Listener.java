package at.tuwien.ldlab.statspace.util;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.tuwien.ldlab.statspace.metadata.RDFCache;
import at.tuwien.ldlab.statspace.widgetgeneration.Deletion;
import at.tuwien.ldlab.statspace.widgetgeneration.WidgetCache;
public class Listener implements ServletContextListener {
	/**
	 * 
	 */
	private ScheduledExecutorService serWidgetDeletion;
	private ScheduledExecutorService serWidgetCache;
	private ScheduledExecutorService serRDFCache;
	private static Log log = LogFactory.getLog(Listener.class);
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {		
		log.info("ServletContextListener started");			
		ServletContext serContext = sce.getServletContext();		
		String folderWebApp = serContext.getRealPath("/");
		
		//create folder to store cache
		String folderCache  = folderWebApp.substring(0,  folderWebApp.length()-1) + "_cache" + File.separator;
		File fCache = new File(folderCache);
		if(!fCache.exists())
			fCache.mkdirs();	
		
		//metadata		
		File fMetadata = new File(folderCache + "metadata");
		if(!fMetadata.exists())
			fMetadata.mkdirs();
		
		//widget
		File fWidgetCache = new File(folderCache + "widget");		
		if(!fWidgetCache.exists()){			
			fWidgetCache.mkdir();			
		}
		File fList = new File(folderCache + "widget" + File.separator + "list.csv");
		if(!fList.exists()){
			try {				
				fList.createNewFile();
			} catch (IOException e) {		
				
			}
		}
		File fTemplate = new File(folderCache + "widget" + File.separator + "template");
		if(!fTemplate.exists()){
			fTemplate.mkdir();
			File fsrcTemplate = new File(folderWebApp + "template");
			try {
				FileOperation.doCopy(fsrcTemplate, fTemplate);
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}
		
		//rdf
		File fRDFCache = new File(folderCache + "rdf");
    	if(!fRDFCache.exists()){
    		fRDFCache.mkdirs();         
        }
		
		serWidgetDeletion = Executors.newSingleThreadScheduledExecutor();
		serWidgetCache	  = Executors.newSingleThreadScheduledExecutor();
		serRDFCache		  = Executors.newSingleThreadScheduledExecutor();		
		
		Deletion wDeletion = new Deletion(folderWebApp + "download", serContext);	
		serWidgetDeletion.scheduleAtFixedRate(wDeletion, 0, 6, TimeUnit.HOURS);
//		serWidgetDeletion.scheduleAtFixedRate(wDeletion, 0, 6, TimeUnit.SECONDS);
		
		WidgetCache wCache = new WidgetCache(folderCache + "widget");		
		serWidgetCache.scheduleAtFixedRate(wCache, 30, 30 , TimeUnit.DAYS);
		
		RDFCache rdfCache = new RDFCache(folderCache + "rdf");		
		serRDFCache.scheduleAtFixedRate(rdfCache, 30, 90 , TimeUnit.DAYS);
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		serWidgetDeletion.shutdownNow();
		serWidgetCache.shutdownNow();
		serRDFCache.shutdownNow();
		log.info("ServletContextListener destroyed");
	}
}
    
	
