package be.cetic.inah.commons.dao.janus

import org.apache.commons.configuration.PropertiesConfiguration
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.janusgraph.core.{JanusGraph, JanusGraphFactory}

object JanusConnector {

  def remoteGraphTraversalSource(url: String, port: Int): GraphTraversalSource = {
    val graph = JanusGraphFactory.open("inMemory")
    val configuration = new PropertiesConfiguration()
    configuration.setProperty("clusterConfiguration.hosts",url)
    configuration.setProperty("clusterConfiguration.port", port)
    graph.traversal().withRemote(configuration)
  }


  def graph(config: String): JanusGraph ={
    val configuration = new PropertiesConfiguration(config)
    JanusGraphFactory.open(configuration)
  }

}
