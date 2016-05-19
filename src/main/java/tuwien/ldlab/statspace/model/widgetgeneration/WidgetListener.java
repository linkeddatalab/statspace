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




public class WidgetListener implements ServletContextListener {
	/**
	 * 
	 */
	private ScheduledExecutorService s_delete;
	private ScheduledExecutorService s_cache;
	private static Log log = LogFactory.getLog(WidgetListener.class);
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {		
		log.info("ServletContextListener started");			
		ServletContext c = sce.getServletContext();
		
		String folder = c.getRealPath("/");
		folder = folder + "download_widgets";
		
		s_delete = Executors.newSingleThreadScheduledExecutor();
		s_cache =  Executors.newSingleThreadScheduledExecutor();
		
		WidgetDeletion wDelete = new WidgetDeletion(folder, c);	
		s_delete.scheduleAtFixedRate(wDelete, 0, 1, TimeUnit.DAYS);
//		s_delete.scheduleAtFixedRate(wDelete, 30, 15, TimeUnit.SECONDS);
		
		WidgetCache wCache = new WidgetCache(folder + File.separator+ "list_endpoint");		
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH); 
		s_cache.scheduleAtFixedRate(wCache, 31-day, 30 , TimeUnit.DAYS);		
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		s_delete.shutdownNow();
		s_cache.shutdownNow();
		log.info("ServletContextListener destroyed");
	}
}
    
	
