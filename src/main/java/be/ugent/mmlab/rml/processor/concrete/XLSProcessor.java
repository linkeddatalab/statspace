package be.ugent.mmlab.rml.processor.concrete;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.openrdf.model.Resource;

import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.core.RMLPerformer;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.AbstractRMLProcessor;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;


public class XLSProcessor extends AbstractRMLProcessor {

    private static Log log = LogFactory.getLog(RMLMappingFactory.class);

    public String extractSheetName(String iterator){    	
    	int k = iterator.indexOf("!");
    	if(k!=-1)
    		return iterator.substring(0, k);
    	else
    		return "";   	
    } 
    
    public String getCellPosition(String s, int c){
		  char ch = (char) (s.charAt(0) + c);
		  s = ch + s.substring(1);	  
		  return s;
	}
	  
	public String getCellPosition(String s, int r, int c){
		  char ch = (char) (s.charAt(0) + c);
		  int k = Integer.parseInt(s.substring(1)) + r;
		  s = ch + Integer.toString(k);	  
		  return s;
	}
	  
    @Override
    public void execute(SesameDataSet dataset, TriplesMap map, RMLPerformer performer, InputStream input) {
    	int i, j, k, n;
    	
        try {
            String iterator = getIterator(map.getLogicalSource());	 
            String sheetName = extractSheetName(iterator);
			org.apache.poi.ss.usermodel.Workbook workbook = WorkbookFactory.create(input);
			org.apache.poi.ss.usermodel.Sheet sheet;
			if(sheetName==""){
				sheet = workbook.getSheetAt(0);
			}
			else{
				sheet = workbook.getSheet(sheetName);
        	}			
        
		    String[] headerPosition = new String[2];
		    String[] dataPosition = new String[2];
		    k = iterator.indexOf("!");
	    	if(k!=-1)
	    		iterator = iterator.substring(k+1);
	    	
	    	k = iterator.indexOf(":");
	    	String[] positions = iterator.split(":");
	    	if(positions.length==4){ 	    		
	    		dataPosition[0] = positions[0];
	    		dataPosition[1] = positions[1];
	    		headerPosition[0] = positions[2];
	    		headerPosition[1] = positions[3];
	    	}else{
	    		System.out.println("Error in iterator parameter");
	    		return;
	    	}
	    	
	    	//Check validity
	    	if(!(headerPosition[0].substring(1).equalsIgnoreCase(headerPosition[1].substring(1)) && 
	    	   headerPosition[0].charAt(0)==dataPosition[0].charAt(0) && 
	    	   headerPosition[1].charAt(0)==dataPosition[1].charAt(0))){
	    		System.out.println("Error in checking validity");
	    		return;
	    	}
	    	
		     
		    n = headerPosition[1].charAt(0) - headerPosition[0].charAt(0) + 1;	
		    k = Integer.parseInt(dataPosition[1].substring(1)) - Integer.parseInt(dataPosition[0].substring(1)) + 1;
		    ArrayList<String> headers = new ArrayList<String>();	   
		    
		    for(i=0; i<n; i++){			    	
		    	CellReference ref = new CellReference(getCellPosition(headerPosition[0], i));
		    	Row r = sheet.getRow(ref.getRow());
			    if (r != null) {
			       Cell c = r.getCell(ref.getCol());
			       headers.add(c.getStringCellValue());			    	
			    }			    	
		    }
		    
		    for(i=0; i<k; i++){
		    	HashMap<String, String> val = new HashMap<>();
		    	ArrayList<String> data = new ArrayList<String>();
		    	
		    	for(j=0; j<n; j++){
		    		CellReference ref = new CellReference(getCellPosition(dataPosition[0], i, j));
			    	Row r = sheet.getRow(ref.getRow());
				    if (r != null) {
				       Cell cell = r.getCell(ref.getCol());
				       switch(cell.getCellType()) {		               
		                case Cell.CELL_TYPE_NUMERIC:
		                	Double d=cell.getNumericCellValue();
		                	if(d % 1 ==0){
		                		int v = d.intValue();		                		
		                		data.add(Integer.toString(v));
		                	}
		                	else
		                		data.add(d.toString());
		                	val.put(headers.get(j), data.get(j));	
		                	break;
		                case Cell.CELL_TYPE_STRING:
		                	data.add(cell.getStringCellValue());
		                	val.put(headers.get(j), data.get(j));	
		                    break;		                
				       }				      	
				    }				   		    
		    	}
		    	if(data.size() == headers.size())
		    		performer.perform(val, dataset, map);
		    } 
		    workbook.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JSONPathProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JSONPathProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EncryptedDocumentException e) {			
			e.printStackTrace();
		} catch (InvalidFormatException e) {			
			e.printStackTrace();
		} 
    }

    @Override
    public List<String> extractValueFromNode(Object node, String expression) {
        HashMap<String, String> row = (HashMap<String, String>) node;
        for(String key : row.keySet())
            key = new String(key.getBytes(), UTF_8);
        //call the right header in the row
        List<String> list = new ArrayList<String>();
        if (row.containsKey(expression)){
            list.add(row.get(expression));
        }
        return list;
    }
    
    @Override
    public void execute_node(SesameDataSet dataset, String expression, TriplesMap parentTriplesMap, RMLPerformer performer, Object node, Resource subject) {
        throw new UnsupportedOperationException("Not applicable for XLS sources."); //To change body of generated methods, choose Tools | Templates.
    }
}

