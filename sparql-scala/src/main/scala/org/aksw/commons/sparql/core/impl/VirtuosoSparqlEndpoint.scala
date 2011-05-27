package org.aksw.commons.sparql.core.impl

import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.sparql.engine.binding.Binding
import com.hp.hpl.jena.query.{QueryExecution, ResultSet}
import virtuoso.jena.driver.{VirtuosoQueryEngine, VirtGraph, VirtuosoQueryExecutionFactory}
import org.aksw.commons.sparql.core.SparqlEndpoint


/**
 * Created by Claus Stadler
 * Date: Oct 20, 2010
 * Bugs: Does not take defaultGraphNames into account!
 *
 */
class VirtuosoSparqlEndpoint(val virtGraph: VirtGraph, override val defaultGraphNames: Set[String])
        extends QueryExecutionSparqlEndpoint
{
  def this(virtGraph: VirtGraph) = this (virtGraph, Set[String]())
  def this(virtGraph: VirtGraph, graphName: String) = this (virtGraph, if (graphName == null) Set[String]() else Set(graphName))


  override def createQueryExecution(query: String): QueryExecution = VirtuosoQueryExecutionFactory.create(query, virtGraph);

  override def id() = defaultGraphNames.mkString("_")
}