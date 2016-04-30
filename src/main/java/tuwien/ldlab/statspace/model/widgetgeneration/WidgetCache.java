package tuwien.ldlab.statspace.model.widgetgeneration;

import java.io.File;
import java.util.Date;

import tuwien.ldlab.statspace.model.util.SpecialEndpointList;

public class WidgetCache implements Runnable{
	/**
	 * 
	 */
	static String folder_list="";	
	
	public WidgetCache (String sFolder){
		folder_list = sFolder;
	}
	
	@Override
	public void run() {	
		File directory = new File(folder_list);
		System.out.println("Run Widget Cache");
		if(!directory.exists()){
			System.out.println("Can not run Widget Cache - Directory does not exist");
		}else{
			String files[] = directory.list();
			String endpoint;
			Date d1 = new Date();		
    		for (String file : files) {    			
    			endpoint = file;
    			if(!endpoint.equalsIgnoreCase("template")){    				
	    			endpoint = endpoint.replaceAll("\\+", "/");
	    			endpoint = endpoint.replaceAll("=", ":");
	    			endpoint = "http://" + endpoint;
	    			System.out.println("END ---------------------------- END");
	 	    		System.out.println("Create cache of " + endpoint);
	 	    		createCache(endpoint, file);
    			}
    		}    		
    		Date d2 = new Date();				
			long diff = d2.getTime() - d1.getTime();
			long diffMin = diff /(60 * 1000);
			System.out.println("Time to create widgets: " + diffMin + " minutes");
		}    	
	}
	
	public static void createCache(String sEndPoint, String file){
		int i, n;		
		boolean bHTTP, bRemove, bFindOther;
		String sUseDistinct;
		String sEndpointForWidget;
		SpecialEndpointList specialList = new SpecialEndpointList( folder_list + File.separator + "template" + File.separator + "list.xml"); 
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
				Widget widget = new Widget(endpoint.getDataSet(i), i, sEndpointForWidget, folder_list + File.separator + file, folder_list + File.separator +"template");	
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

