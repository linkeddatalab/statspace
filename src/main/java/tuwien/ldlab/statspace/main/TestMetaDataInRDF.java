package tuwien.ldlab.statspace.main;

import java.io.InputStream;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

public class TestMetaDataInRDF {
	
	static Model model;
	
	public static void main(String[] args){	
		
		
		Model model = ModelFactory.createDefaultModel();
		InputStream is = FileManager.get().open("data/metadata/wb.ttl");	
		model.read(is,null,"TTL");	
		String sQuery;
		Query query;
		QueryExecution qe;
		ResultSet results;
		
		
		//number of metadata
		sQuery =	
				"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
				"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
				"PREFIX dc:   <http://purl.org/dc/terms/> \n"+
				"PREFIX sdt:  <http://statisticaldata.linkedwidgets.org/terms/> \n"+
				"PREFIX sdmx_measure: <http://purl.org/linked-data/sdmx/2009/measure#> \n"+
				"PREFIX sdmx_attribute: <http://purl.org/linked-data/sdmx/2009/attribute#> \n"+
				"PREFIX sdmx_dimension: <http://purl.org/linked-data/sdmx/2009/dimension#> \n"+
				
				"SELECT ?ds ?l \n"+
				"WHERE{ \n"+
					"?md qb:dataSet ?qb. \n"+
				    "optional{?ds rdfs:label ?l.}\n"+
				"}";
		
		query = QueryFactory.create(sQuery);
		qe = QueryExecutionFactory.create(query, model);
		results =  qe.execSelect();
		ResultSetFormatter.out(System.out, results, query);		
		
		sQuery =	
				"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
				"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
				"PREFIX dc:   <http://purl.org/dc/terms/> \n"+
				"PREFIX sdt:  <http://statisticaldata.linkedwidgets.org/terms/> \n"+
				"PREFIX sdmx_measure: <http://purl.org/linked-data/sdmx/2009/measure#> \n"+
				"PREFIX sdmx_attribute: <http://purl.org/linked-data/sdmx/2009/attribute#> \n"+
				"PREFIX sdmx_dimension: <http://purl.org/linked-data/sdmx/2009/dimension#> \n"+
				
				"SELECT (count(?md) as ?c) \n"+
				"WHERE{ \n"+
					"?md qb:dataSet ?ds. \n"+					
				"}";
		
//		query = QueryFactory.create(sQuery);
//		qe = QueryExecutionFactory.create(query, model);
//		results =  qe.execSelect();
//		ResultSetFormatter.out(System.out, results, query);		
		
		

		//list of dimensions, measure, attribute
		sQuery =	
				"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
				"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
				"PREFIX dc:   <http://purl.org/dc/terms/> \n"+
				"PREFIX sdt:  <http://statisticaldata.linkedwidgets.org/terms/> \n"+
				"PREFIX sdmx_measure: <http://purl.org/linked-data/sdmx/2009/measure#> \n"+
				"PREFIX sdmx_attribute: <http://purl.org/linked-data/sdmx/2009/attribute#> \n"+
				"PREFIX sdmx_dimension: <http://purl.org/linked-data/sdmx/2009/dimension#> \n"+
				
				"SELECT DISTINCT ?m  \n"+
				"WHERE{ \n"+
					"?md qb:component ?m. \n"+					
					"?m a qb:MeasureProperty. \n"+				
				"}";
		
//		query = QueryFactory.create(sQuery);
//		qe = QueryExecutionFactory.create(query, model);
//		results =  qe.execSelect();
//		ResultSetFormatter.out(System.out, results, query);		
		
		sQuery =	
				"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
				"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
				"PREFIX dc:   <http://purl.org/dc/terms/> \n"+
				"PREFIX sdt:  <http://statisticaldata.linkedwidgets.org/terms/> \n"+
				"PREFIX sdmx_measure: <http://purl.org/linked-data/sdmx/2009/measure#> \n"+
				"PREFIX sdmx_attribute: <http://purl.org/linked-data/sdmx/2009/attribute#> \n"+
				"PREFIX sdmx_dimension: <http://purl.org/linked-data/sdmx/2009/dimension#> \n"+
				
				"SELECT DISTINCT *\n"+
				"WHERE{ \n"+
					"?ds dc:subject ?sub. \n"+
					"?ds rdfs:label ?l. \n"+	
					"?ds sdt:describes <http://statisticaldata.linkedwidgets.org/cl-refArea/UnitedKingdom>. \n"+
					"?ds sdt:method ?m. \n"+
					"?ds sdt:rml ?r. \n"+
				"}ORDER BY ?ds";
		
//		query = QueryFactory.create(sQuery1);
//		qe = QueryExecutionFactory.create(query, model);
//		results =  qe.execSelect();
//		ResultSetFormatter.out(System.out, results, query);			
		

		sQuery =	
				"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
				"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
				"PREFIX dc:   <http://purl.org/dc/terms/> \n"+
				"PREFIX sdt:  <http://statisticaldata.linkedwidgets.org/terms/> \n"+
				"PREFIX sdmx_measure: <http://purl.org/linked-data/sdmx/2009/measure#> \n"+
				"PREFIX sdmx_attribute: <http://purl.org/linked-data/sdmx/2009/attribute#> \n"+
				"PREFIX sdmx_dimension: <http://purl.org/linked-data/sdmx/2009/dimension#> \n"+
				
				"SELECT DISTINCT ?a ?b\n"+
				"WHERE{ \n"+
					"?a sdt:reference ?b \n"+
				"}ORDER BY ?a";
		
//		query = QueryFactory.create(sQuery);
//		qe = QueryExecutionFactory.create(query, model);
//		results =  qe.execSelect();
//		ResultSetFormatter.out(System.out, results, query);	
		
		sQuery =	
				"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
				"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
				"PREFIX dc:   <http://purl.org/dc/terms/> \n"+
				"PREFIX sdt:  <http://statisticaldata.linkedwidgets.org/terms/> \n"+
				"PREFIX sdmx_measure: <http://purl.org/linked-data/sdmx/2009/measure#> \n"+
				"PREFIX sdmx_attribute: <http://purl.org/linked-data/sdmx/2009/attribute#> \n"+
				"PREFIX sdmx_dimension: <http://purl.org/linked-data/sdmx/2009/dimension#> \n"+		
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"+
				"SELECT *\n"+
				"WHERE{ \n"+
					"?d a qb:DimensionProperty. \n "+
					"?d qb:codeList ?cl. \n "+
				"}ORDER BY ?d";
		
//		query = QueryFactory.create(sQuery);
//		qe = QueryExecutionFactory.create(query, model);
//		results =  qe.execSelect();
//		ResultSetFormatter.out(System.out, results, query);	
		
		sQuery =	
				"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
				"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
				"PREFIX dc:   <http://purl.org/dc/terms/> \n"+
				"PREFIX sdt:  <http://statisticaldata.linkedwidgets.org/terms/> \n"+
				"PREFIX sdmx_measure: <http://purl.org/linked-data/sdmx/2009/measure#> \n"+
				"PREFIX sdmx_attribute: <http://purl.org/linked-data/sdmx/2009/attribute#> \n"+
				"PREFIX sdmx_dimension: <http://purl.org/linked-data/sdmx/2009/dimension#> \n"+		
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"+
				"SELECT *\n"+
				"WHERE{ \n"+
					"?d qb:codeList ?cl. \n "+
					"?cl skos:hasTopConcept ?top. \n "+
				"}ORDER BY ?cl";
		
//		query = QueryFactory.create(sQuery);
//		qe = QueryExecutionFactory.create(query, model);
//		results =  qe.execSelect();
//		ResultSetFormatter.out(System.out, results, query);	
		
		sQuery =	
				"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
				"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
				"PREFIX dc:   <http://purl.org/dc/terms/> \n"+
				"PREFIX sdt:  <http://statisticaldata.linkedwidgets.org/terms/> \n"+
				"PREFIX sdmx_measure: <http://purl.org/linked-data/sdmx/2009/measure#> \n"+
				"PREFIX sdmx_attribute: <http://purl.org/linked-data/sdmx/2009/attribute#> \n"+
				"PREFIX sdmx_dimension: <http://purl.org/linked-data/sdmx/2009/dimension#> \n"+		
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"+
				"SELECT (count(?c) as ?count)\n"+
				"WHERE{ \n"+
					"?c skos:inScheme ?cl. \n "+
				"}Group by ?cl";
		
//		query = QueryFactory.create(sQuery);
//		qe = QueryExecutionFactory.create(query, model);
//		results =  qe.execSelect();
//		ResultSetFormatter.out(System.out, results, query);	
		
		
		sQuery =	
				"PREFIX qb:   <http://purl.org/linked-data/cube#> \n"+
				"PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
				"PREFIX dc:   <http://purl.org/dc/terms/> \n"+
				"PREFIX sdt:  <http://statisticaldata.linkedwidgets.org/terms/> \n"+
				"PREFIX sdmx_measure: <http://purl.org/linked-data/sdmx/2009/measure#> \n"+
				"PREFIX sdmx_attribute: <http://purl.org/linked-data/sdmx/2009/attribute#> \n"+
				"PREFIX sdmx_dimension: <http://purl.org/linked-data/sdmx/2009/dimension#> \n"+		
				"PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"+
				"SELECT *\n"+
				"WHERE{ \n"+
					"<http://statisticaldata.linkedwidgets.org/cl-activity/B> skos:narrower ?c. \n "+
				"}order by ?c";
		
//		query = QueryFactory.create(sQuery);
//		qe = QueryExecutionFactory.create(query, model);
//		results =  qe.execSelect();
//		ResultSetFormatter.out(System.out, results, query);	
	}	
}
