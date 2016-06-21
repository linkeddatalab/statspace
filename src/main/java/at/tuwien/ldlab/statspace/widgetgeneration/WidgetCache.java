package at.tuwien.ldlab.statspace.widgetgeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import at.tuwien.ldlab.statspace.util.SpecialEndpointList;
import at.tuwien.ldlab.statspace.util.Support;

public class WidgetCache implements Runnable{
	/**
	 * 
	 */
	private String folderWidgetCache="";	
	private Log log = LogFactory.getLog(WidgetCache.class);
	
	public WidgetCache (String sFolder){
		folderWidgetCache = sFolder;		
	}
	
	@Override
	public void run() {		
		Date d1 = new Date();	
		ArrayList<String> arrEndpoint = new ArrayList<String>();
		int i;			
		BufferedReader br = null;
		String sEndpoint, folderEndpoint;	
		File fList = new File(folderWidgetCache + File.separator + "list.csv");
		if(fList.exists()){
			try {	 
				br = new BufferedReader(new InputStreamReader(new FileInputStream(folderWidgetCache + File.separator + "list.csv")));		
				while ((sEndpoint = br.readLine()) != null) {				
					sEndpoint = sEndpoint.trim();
					if(arrEndpoint.size()==0 || arrEndpoint.indexOf(sEndpoint)==-1)
						arrEndpoint.add(sEndpoint);					
				}	 
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			for(i=0; i<arrEndpoint.size(); i++){
				sEndpoint = arrEndpoint.get(i);
				folderEndpoint = Support.extractFolderName(sEndpoint);
				log.info("Creating cache for " + sEndpoint);
	    		createCache(sEndpoint, folderEndpoint);
			}
			Date d2 = new Date();				
			long diff = d2.getTime() - d1.getTime();
			long diffMin = diff /(60 * 1000);
			log.info("Time to create widgets: " + diffMin + " minutes");	
		}
	}
	
	public void createCache(String sEndPoint, String folderEndpoint){
		int i, n;		
		boolean bHTTP, bRemove, bFindOther;
		String sUseDistinct;
		String sEndpointForWidget;
		SpecialEndpointList specialList = new SpecialEndpointList(folderWidgetCache + File.separator + "template" + File.separator + "list.xml"); 
    	int k=specialList.getEndpointIndex(sEndPoint);
    	if(k!=-1){
    		if(!specialList.getEndpointForQuery(k).equals(""))
    			sEndPoint = specialList.getEndpointForQuery(k);
    		if(!specialList.getEndpointForWidget(k).equals(""))
    			sEndpointForWidget = specialList.getEndpointForWidget(k);
    		else
    			sEndpointForWidget = sEndPoint;    	

    		bHTTP = specialList.getHTTPRequest(k);
			bRemove = specialList.getRemoveDuplicate(k);
			sUseDistinct = specialList.getUseDistinct(k);
			bFindOther = specialList.getFindOtherValue(k);
    	}else{
    		sEndpointForWidget = sEndPoint;
    		bHTTP = false;
    		bRemove = false;
    		sUseDistinct = "";
    		bFindOther = true;
    	}    	
    	Endpoint endpoint = new Endpoint(sEndPoint, sEndpointForWidget, bHTTP, bRemove, sUseDistinct, bFindOther);
		endpoint.queryDataSet();
		n = endpoint.getDataSet().size();	
		if(n>0){
			for(i=0; i<n; i++){					
	    		System.out.println(endpoint.getDataSet(i).getUri());	    			
				endpoint.getDataSet(i).queryComponent(endpoint.getEndpointForQuery(), endpoint.getHTTP(), endpoint.getUseDistict());
	     	    endpoint.getDataSet(i).queryValue(endpoint.getEndpointForQuery(), endpoint.getHTTP(), endpoint.getFindOther(), endpoint.getRemove());	
				Widget widget = new Widget(endpoint.getDataSet(i), sEndpointForWidget, folderWidgetCache + File.separator + folderEndpoint, folderWidgetCache + File.separator +"template");	
		    	widget.createWidgetFile();   			
				
				if(endpoint.getDataSet(i).getDimensionSize()==0 || endpoint.getDataSet(i).getMeasureSize()==0){
					System.out.println("--------------------------\n");
		    		System.out.println(endpoint.getDataSet(i).getUri());
		    		System.out.println("\n--------------------------");
				}
					
			}	
		}
		endpoint.removeAll();
	}	
}

