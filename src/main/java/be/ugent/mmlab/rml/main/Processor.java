package be.ugent.mmlab.rml.main;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.model.Parameters;
import be.ugent.mmlab.rml.model.RMLMapping;


public class Processor {
	
	private String sOutput;
	private boolean bStatus;
	private static Log log = LogFactory.getLog(Processor.class);
	
	public Processor(String rmlSource, String path, String separator){		
		int i;			
		i=rmlSource.length()-1;
		while(i>=0 && rmlSource.charAt(i)!='\\' && rmlSource.charAt(i)!='/' )
			i--;		
		if(i>0)
			sOutput = rmlSource.substring(i+1);		
		else
			sOutput = "output";
	
		if(sOutput.endsWith(".ttl"))
			sOutput = sOutput.replace(".ttl", ".rdf");
		else
			sOutput = sOutput + ".rdf";					
		sOutput = path + "download_rml" + separator + sOutput;			
	}	
	
	public String getOutputPath(){return sOutput;}
	
	public void run(String sRMLSource, Parameters parameters){  
		bStatus = true;
		log.info("Reading mapping: " + sRMLSource);
		RMLMapping mapping = RMLMappingFactory.extractRMLMapping(sRMLSource, parameters);
		if(mapping == null){
			log.info("Can not read mapping");
			bStatus = false;
		}else{            
			RMLEngine engine = new RMLEngine();	     
	        log.info("Reading data set and writing tranformations to: " + sOutput);	 
			engine.runRMLMapping(mapping, "", sOutput, true);  	   
			if(engine.getStatus()==false){
				log.info("Can not read data set");
				bStatus=false;
			}
			log.info("Finished");
		  }
	}
	public boolean getStatus(){return bStatus;}
}
