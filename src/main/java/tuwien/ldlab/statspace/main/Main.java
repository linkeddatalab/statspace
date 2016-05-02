package tuwien.ldlab.statspace.main;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import be.ugent.mmlab.rml.core.RMLEngine;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.model.Parameters;
import be.ugent.mmlab.rml.model.RMLMapping;
import tuwien.ldlab.statspace.codelist.*;


public class Main {

	public static void main(String[] args) {
		mergeMetaData();
			
	}
	
	public static void rml() {       
    	String[]args = new String[10];
        Parameters parameters = new Parameters();       
        parameters.addParameterValue("indicator", "SP.POP.TOTL");
        parameters.addParameterValue("refArea", "AT");
        
        for(int i=0; i<8; i++){
        	args[0]= "data/mapping/uk"+i+".ttl";
        	args[1]= "data/mapping/uk"+i+"_output.rdf";             	
        	
        	System.out.println("Start: reading mapping " + args[0]);
            RMLMapping mapping = RMLMappingFactory.extractRMLMapping(args[0], parameters);
            if(mapping == null){
            	System.out.println("Can not read mapping");
            }else{            
	            RMLEngine engine = new RMLEngine();	     
	            System.out.println("Start: reading data set and writing transformations to " + args[0]);	 
	            engine.runRMLMapping(mapping, "", args[1], true);
	            if(engine.getStatus()==false){
	            	System.out.println("Can not read data set");
	            }
	            System.out.println("Finished");
            }
        }  
    }
	

	public static void mergeMetaData(){
		try{
			//generate code lists
			StandardDimensions sd = new StandardDimensions();
			sd.generateCodeList();
			
			//merge with metadata
			Model model = ModelFactory.createDefaultModel();
			InputStream is;
			is = FileManager.get().open("data/codelist/cl_activity.ttl");	
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/codelist/cl_age.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/codelist/cl_area.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/codelist/cl_civilStatus.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/codelist/cl_cofog.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/codelist/cl_coicop.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/codelist/cl_copni.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/codelist/cl_copp.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/codelist/cl_currency.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/codelist/cl_educationLev.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/codelist/cl_freq.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/codelist/cl_occupation.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/codelist/cl_period.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/codelist/cl_sex.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/codelist/cl_subject.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/codelist/cl_unitMeasure.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/metadata/cr.eionet.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/metadata/opendatacommunities.org.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/metadata/unodc.publicdata.ttl");		
			model.read(is,null,"TTL");	
			is.close();			
			is = FileManager.get().open("data/metadata/statistics.gov.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/metadata/ogd.ifs.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/metadata/data.cso.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/metadata/semantic.eea.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/metadata/open-data.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/metadata/wb.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			is = FileManager.get().open("data/metadata/uk.ttl");		
			model.read(is,null,"TTL");	
			is.close();
			
			OutputStream out = new FileOutputStream( new File( "data/metadata/statspace.ttl" ));
		    model.write( out, "Turtle", null );
		    
		    System.out.println("Done!");
		}catch(Exception e){
			System.out.println(e.toString());
		}
	}
		
	
}
