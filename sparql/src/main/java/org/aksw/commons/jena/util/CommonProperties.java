package org.aksw.commons.jena.util;

import java.util.LinkedHashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.oval.constraint.Min;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;
import net.sf.oval.constraint.Range;
import net.sf.oval.guard.Guarded;

import org.aksw.commons.collections.ValueComparator;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/** @author Konrad Höffner */
@Guarded
public class CommonProperties
{
	/**@param endpoint the URL of the SPARQL endpoint to be queried
	 * @param where the contents of a SPARQL select "where" clause <em>which may only use 
	 * ?s, ?p and ?o as variable names for subject, predicate and object.</em>
	 * @param threshold a value between 0 and 1, specifying what fraction of the instances must have this property 
	 * for it to be counted as common property. Set to null if you want no restriction on this. 
	 * @param maxResultSize a non-negative integer value, specifying the maximum amount of properties to return.
	 * If there are more than {@link limit} after the exclusion with {@threshold}, the most common properties of those are returned. 
	 * @param sampleSize the number of instances whose triples are examined. Set to null to look at all triples (may take a long time).
	 * On the other hand, using a sample instead of all data may give a wrong result even for a big sample size because the sample is not random
	 * but the selection depends on the SPARQL server (uses Virtuoso SPARQL for subqueries).
	 * @return the most common properties sorted by occurrence in descending order.
	 * Each property p is counted at most once for each instance s, even if there are multiple triples (s,p,o).
	 * Example: getCommonProperties(0.5) will only return properties which are used by at least half of the uris in the cache.
	 */	
	public static LinkedHashMap<String,Integer> getCommonProperties
	(
		@NotEmpty @NotNull String endpoint,
		@NotEmpty @NotNull String where,
		@Range(min=0, max=1) Double threshold,
		@Min(1) Integer maxResultSize,
		@Min(1) Integer sampleSize
	)
	{		
		final String sampleSizeLimit	= sampleSize	==null?"":("limit "+sampleSize);
		final String maxResultSizeLimit	= maxResultSize	==null?"":("limit "+maxResultSize);
		
		final String innerSubquery = "select ?s where {"+where+"}"+sampleSizeLimit;
		final String outerSubquery = "select distinct ?s ?p where {?s ?p ?o. {"+innerSubquery+"}}";		
		final String query = "select ?p, count(?p) as ?count where {{"+outerSubquery+"}} ORDER BY DESC(?count) "+maxResultSizeLimit;
		System.out.println(query);
		final ResultSet rs = new QueryEngineHTTP(endpoint, query).execSelect();
		final LinkedHashMap<String,Integer> commonProperties = new LinkedHashMap<String,Integer>();
		//		final ValueComparator<String,Integer> valueComparator = new ValueComparator<String,Integer>(null);
		//		final SortedMap<String,Integer> commonProperties = new TreeMap<String,Integer>(valueComparator);
		//		valueComparator.setMap(commonProperties);		

		final int minCount = (int)Math.ceil(threshold * sampleSize);
		while(rs.hasNext()&&commonProperties.size()<maxResultSize)
		{
			QuerySolution qs = rs.next();
			int count = Integer.valueOf(qs.getLiteral("count").getLexicalForm());
			if(count<minCount) {break;}
			commonProperties.put(qs.getResource("p").getURI(), count);
		}
		return commonProperties;
	}
}