package tuwien.ldlab.statspace.model.widgetgeneration;

import java.io.File;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tuwien.ldlab.statspace.model.metadata.RDFCache;
public class WidgetListener implements ServletContextListener {
	/**
	 * 
	 */
	private ScheduledExecutorService serWidgetDeletion;
	private ScheduledExecutorService serWidgetCache;
	private ScheduledExecutorService serRDFCache;
	private static Log log = LogFactory.getLog(WidgetListener.class);
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {		
		log.info("ServletContextListener started");			
		ServletContext serContext = sce.getServletContext();		
		String folder = serContext.getRealPath("/");			
		serWidgetDeletion = Executors.newSingleThreadScheduledExecutor();
		serWidgetCache	  =  Executors.newSingleThreadScheduledExecutor();
		serRDFCache		  =  Executors.newSingleThreadScheduledExecutor();		
		
		WidgetDeletion wDeletion = new WidgetDeletion(folder + "download", serContext);	
		serWidgetDeletion.scheduleAtFixedRate(wDeletion, 0, 1, TimeUnit.DAYS);
//		serWidgetDelete.scheduleAtFixedRate(wDeletion, 2, 10, TimeUnit.SECONDS);
		
		WidgetCache wCache = new WidgetCache(folder + "download" + File.separator+ "list_endpoint");		
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH); 
		serWidgetCache.scheduleAtFixedRate(wCache, 31-day, 30 , TimeUnit.DAYS);
		
		RDFCache rdfCache = new RDFCache(folder + "download_rdf");		
		serRDFCache.scheduleAtFixedRate(rdfCache, 31-day, 60 , TimeUnit.DAYS);
//		serRDFCache.scheduleAtFixedRate(rdfCache, 1, 30 , TimeUnit.DAYS);	
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		serWidgetDeletion.shutdownNow();
		serWidgetCache.shutdownNow();
		serRDFCache.shutdownNow();
		log.info("ServletContextListener destroyed");
	}
}
    
	
