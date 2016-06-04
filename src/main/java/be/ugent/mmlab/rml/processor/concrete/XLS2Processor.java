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
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.openrdf.model.Resource;
import be.ugent.mmlab.rml.core.RMLPerformer;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.AbstractRMLProcessor;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;


public class XLS2Processor extends AbstractRMLProcessor {

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
		  int k = Integer.parseInt(splitRow(s)) + r;		 
		  if(ch <='Z')
			  s = ch + Integer.toString(k);
		  else{
			  char ch1 = (char) (ch - 'Z' - 1 + 'A');
			  s = ch1 + Integer.toString(k);
			  s = 'A' + s;
		  }
		  return s;
	}
	
	/* Input: e.g., AU54
	 * Output: e.g., AU
	 */
	public String splitColumn(String s){
		int i;
		char ch;
		for(i=s.length()-1; i>=0; i--){
			ch = s.charAt(i);
			if('9'<ch || ch<'0')
				break;			
		}
		if(i>=0 && i<s.length()-1)
			return s.substring(0, i+1);
		else
			return "";	
	}
	
	public String splitRow(String s){
		int i;
		char ch;
		for(i=s.length()-1; i>=0; i--){
			ch = s.charAt(i);
			if('9'<ch || ch<'0')
				break;			
		}
		if(i>=0 && i<s.length()-1)
			return s.substring(i+1);
		else
			return "";	
	}
	
	/* (D2, E2) => 2; (D2, AU2) =>?
	 */
	public int numberOfColumn(String s1, String s2){
		int n, m;
		n = splitColumn(s1).length();
		m = splitColumn(s2).length();
		if(n==1 && m==1){			
			return s2.charAt(0) - s1.charAt(0) + 1;			
		}else if(n==1 && m==2){			
			return (s2.charAt(0) - 'A' + 1) * 26 + (s2.charAt(1)-s1.charAt(0) + 1);
		}
		return 0;
	}
	
	  
	@Override
    public void execute(SesameDataSet dataset, TriplesMap map, RMLPerformer performer, InputStream input) {
    	int i, j, k, n, m;
    	Double d;
    	
        try {
            String iterator = getIterator(map.getLogicalSource());	
            
            //Get data from selected sheet in the workbook
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
		    String[] measurePosition = new String[2];
		    String[] dataPosition = new String[2];		    
		    
		    k = iterator.indexOf("!");
	    	if(k!=-1)
	    		iterator = iterator.substring(k+1);
	    	
	    	k = iterator.indexOf(":");
	    	String[] positions = iterator.split(":");
	    	if(positions.length==6){ 	    		
	    		dataPosition[0] = positions[0];
	    		dataPosition[1] = positions[1];
	    		headerPosition[0] = positions[2];
	    		headerPosition[1] = positions[3];
	    		measurePosition[0] = positions[4];
	    		measurePosition[1] = positions[5];
	    	}else{
	    		System.out.println("Error in iterator parameter");
	    		return;
	    	}
	    	
	    	//Check validity
	    	if(!(splitRow(headerPosition[0]).equalsIgnoreCase(splitRow(headerPosition[1])) &&	    	  
	 	    	 splitRow(headerPosition[0]).equalsIgnoreCase(splitRow(measurePosition[0])) && 
	 	    	 splitRow(measurePosition[0]).equalsIgnoreCase(splitRow(measurePosition[1])) &&
	 	    	 splitColumn(headerPosition[0]).equalsIgnoreCase(splitColumn(dataPosition[0])) &&
	 	    	 splitColumn(measurePosition[1]).equalsIgnoreCase(splitColumn(dataPosition[1])))){
	 	    		System.out.println("Error in checking validity");
	 	    		return;
	 	    	}
	    	
		    n = headerPosition[1].charAt(0) - headerPosition[0].charAt(0) + 1;	
		    m = numberOfColumn(measurePosition[0], measurePosition[1]);
		    k = Integer.parseInt(splitRow(dataPosition[1])) - Integer.parseInt(splitRow(dataPosition[0])) + 1;
		    ArrayList<String> headers = new ArrayList<String>();	   
		    
		    for(i=0; i<n; i++){			    	
		    	CellReference ref = new CellReference(getCellPosition(headerPosition[0], i));
		    	Row r = sheet.getRow(ref.getRow());
			    if (r != null) {
			       Cell c = r.getCell(ref.getCol());
			       headers.add(c.getStringCellValue());			    	
			    }			    	
		    }
		    //add special headers
		    headers.add("ql:Spreadsheet2!Header");
		    headers.add("ql:Spreadsheet2!Value");
		    
		    //loop by rows
		    for(i=0; i<k; i++){
		    	HashMap<String, String> val = new HashMap<>();	    	
		    	
		    	//loop by dimensions list
		    	for(j=0; j<n; j++){
		    		CellReference ref = new CellReference(getCellPosition(dataPosition[0], i, j));
			    	Row r = sheet.getRow(ref.getRow());
				    if (r != null) {
				       Cell cell = r.getCell(ref.getCol());
				       switch(cell.getCellType()) {		               
		                case Cell.CELL_TYPE_NUMERIC:
		                	d=cell.getNumericCellValue();
		                	if(d % 1 ==0){
		                		int v = d.intValue();	
		                		val.put(headers.get(j), Integer.toString(v));		                		
		                	}
		                	else		                	
		                		val.put(headers.get(j), d.toString());	
		                	break;
		                case Cell.CELL_TYPE_STRING:		               
		                	val.put(headers.get(j), cell.getStringCellValue());	
		                    break;		                
				       }				      	
				    }				   		    
		    	}
		    	
		    	
		    	//loop by measures list
		    	for(j=0; j<m; j++){
		    		
		    		//add header
		    		CellReference ref = new CellReference(getCellPosition(measurePosition[0], 0, j));
			    	Row r = sheet.getRow(ref.getRow());
				    if (r != null) {
				       Cell cell = r.getCell(ref.getCol());
				       switch(cell.getCellType()) {		               
		                case Cell.CELL_TYPE_NUMERIC:
		                	d=cell.getNumericCellValue();
		                	if(d % 1 ==0){
		                		int v = d.intValue();	
		                		val.put(headers.get(n), Integer.toString(v));		                		
		                	}
		                	else
		                		val.put(headers.get(n), d.toString());		                
		                	break;
		                case Cell.CELL_TYPE_STRING:		                	
		                	val.put(headers.get(n), cell.getStringCellValue());	
		                    break;		                
				       }				       
				    }
				    
				    //add value
				    ref = new CellReference(getCellPosition(dataPosition[0], i, j+n));
			    	r = sheet.getRow(ref.getRow());
				    if (r != null) {
				       Cell cell = r.getCell(ref.getCol());
				       switch(cell.getCellType()) {		               
		                case Cell.CELL_TYPE_NUMERIC:
		                	d=cell.getNumericCellValue();
		                	if(d % 1 ==0){
		                		int v = d.intValue();		                		
		                		val.put(headers.get(n+1), Integer.toString(v));		  
		                	}
		                	else
		                		val.put(headers.get(n+1), d.toString());            	
		                	break;
		                case Cell.CELL_TYPE_STRING:
		                	val.put(headers.get(n+1), cell.getStringCellValue());		                		
		                    break;		                
				       }				      	
				    }				 
				    
				    //call processor
				    if(val.size() == n+2)
			    		performer.perform(val, dataset, map);
		    	}		    	
		    	
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
    
    public double convert2Double(String s){
    	int i;
    	char ch;
    	for(i=0; i<s.length(); i++){
    		ch = s.charAt(i);
    		if(!(ch=='.'||ch==','||('0'<=ch && ch<='9')))
    			return -1;
    	}
    	s = s.replace(",", "");
    	return Double.parseDouble(s);    	
    }
}

