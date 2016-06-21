package at.tuwien.ldlab.statspace.util;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * @author Ba Lam Do
 */
public class OGD {

	protected static final String uri = "http://location-based-catalogue.ifs.tuwien.ac.at/";

	/**
	 * returns the URI for this schema
	 * 
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	private static Model m = ModelFactory.createDefaultModel();
	
	public static final Resource Endpoint = m.createResource(uri+"SPARQLEndPoint");
	public static final Resource AdministrativeArea = m.createResource(uri+"AdministrativeArea");
	public static final Property describes = m.createProperty(uri, "describes");
	public static final Property hasDataSet = m.createProperty(uri,"hasDataSet");
	public static final Property hasDimension = m.createProperty(uri,"hasDimension");
	public static final Property hasMeasure = m.createProperty(uri, "hasMeasure");
	public static final Property hasValue = m.createProperty(uri, "hasValue");
	
	

}
