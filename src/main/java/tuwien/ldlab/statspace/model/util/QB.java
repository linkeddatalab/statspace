package tuwien.ldlab.statspace.model.util;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author Ba Lam Do
 */
public class QB {

	protected static final String uri = "http://purl.org/linked-data/cube#";

	/**
	 * returns the URI for this schema
	 * 
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	private static Model m = ModelFactory.createDefaultModel();
	
	public static final Resource DimensionProperty = m.createResource(uri+"DimensionProperty");
	public static final Resource MeasureProperty = m.createResource(uri+"MeasureProperty");
	public static final Resource DataSet = m.createResource(uri+"DataSet");
	public static final Resource Observation = m.createResource(uri+"Observation");
	public static final Resource DataStructureDefinition = m.createResource(uri+"DataStructureDefinition");
	public static final Resource AttributeProperty = m.createResource(uri+"AttributeProperty");
	public static final Resource ComponentProperty = m.createResource(uri+"ComponentProperty");

	
	public static final Property attribute = m.createProperty(uri, "attribute");
	public static final Property codeList = m.createProperty(uri,"codeList");
	public static final Property component = m.createProperty(uri,"component");
	public static final Property componentAttachement = m.createProperty(uri, "componentAttachment");
	public static final Property componentProperty = m.createProperty(uri,"componentProperty");
	public static final Property componentRequired = m.createProperty(uri,"componentRequired");
	public static final Property concept = m.createProperty(uri,"concept");
	public static final Property dataSet = m.createProperty(uri,"dataSet");
	public static final Property dimension = m.createProperty(uri, "dimension");
	public static final Property hierarchyRoot= m.createProperty(uri, "hierarchyRoot");
	public static final Property measure = m.createProperty(uri,"measure");
	public static final Property measureDimension = m.createProperty(uri,"measureDimension");
	public static final Property measureType = m.createProperty(uri,"measureType");
	public static final Property observation = m.createProperty(uri,"observation");
	public static final Property observationGroup = m.createProperty(uri, "observationGroup");
	public static final Property order = m.createProperty(uri,"order");
	public static final Property parentChildProperty = m.createProperty(uri, "parentChildProperty");
	public static final Property slice= m.createProperty(uri, "slice");
	public static final Property sliceKey = m.createProperty(uri,"sliceKey");
	public static final Property sliceStructure = m.createProperty(uri,"sliceStructure");
	public static final Property structure = m.createProperty(uri,"structure");	
}
