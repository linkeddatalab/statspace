package be.ugent.mmlab.rml.processor.concrete;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Resource;
import be.ugent.mmlab.rml.core.RMLMappingFactory;
import be.ugent.mmlab.rml.core.RMLPerformer;
import be.ugent.mmlab.rml.model.TriplesMap;
import be.ugent.mmlab.rml.processor.AbstractRMLProcessor;
import net.antidot.semantic.rdf.model.impl.sesame.SesameDataSet;


public class NULLProcessor extends AbstractRMLProcessor {

    private static Log log = LogFactory.getLog(RMLMappingFactory.class);

  
	@Override
    public void execute(SesameDataSet dataset, TriplesMap map, RMLPerformer performer, InputStream input) {   
        performer.perform(null, dataset, map);
    }

    @Override
    public List<String> extractValueFromNode(Object node, String expression) {
        //call the right header in the row
        List<String> list = new ArrayList<String>();
        return list;
    }
    
    @Override
    public void execute_node(SesameDataSet dataset, String expression, TriplesMap parentTriplesMap, RMLPerformer performer, Object node, Resource subject) {
        throw new UnsupportedOperationException("Not applicable for sources."); //To change body of generated methods, choose Tools | Templates.
    }
}

