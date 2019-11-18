package be.cetic.inah.commons.database.janusgraph

import org.janusgraph.core.JanusGraphFactory
import org.janusgraph.graphdb.database.StandardJanusGraph

object Janusgraph {

  private var singleGraph: Option[StandardJanusGraph] = None

  def graph: StandardJanusGraph = {
    if (singleGraph.isEmpty) {
      setGraphInMemory()
      println(Console.RED + "Empty graph setting, using inmemory.")
    }
    singleGraph.get
  }

  def setGraphCassandraEs(cassandraUrl: String, esUrl: String, batchLoad: Boolean = false, bufferSize: Option[Int] = None): Unit = {

    var graphBuilder = JanusGraphFactory.build
      .set("storage.backend", "cql")
      .set("storage.hostname", cassandraUrl)
      .set("index.search.backend", "elasticsearch")
      .set("index.search.hostname", esUrl)
      .set("index.search.index-name", "janusgraph")
      .set("index.search.elasticsearch.client-only", true)
      .set("index.search.elasticsearch.transport-scheme", "http")
      .set("storage.batch-loading", batchLoad)
      .set("storage.lock.wait-time", 300)
      .set("schema.default", "none")
    bufferSize.foreach(bs => graphBuilder = graphBuilder.set("storage.buffer-size", bs))


    val graph = graphBuilder.open
    resetGraph(graph.asInstanceOf[StandardJanusGraph])
  }

  def setGraphFromConf(conf: String): Unit = resetGraph(JanusGraphFactory.open(conf).asInstanceOf[StandardJanusGraph])

  def setGraphInMemory(): Unit = resetGraph {
    JanusGraphFactory.build
      .set("storage.backend", "inmemory")
      .set("storage.batch-loading", "true")
      .set("schema.default", "none")
      .set("storage.buffer-size", "1000")
      .open.asInstanceOf[StandardJanusGraph]
  }


  private def resetGraph(graph: StandardJanusGraph) = {
    singleGraph.foreach(_.close)
    singleGraph = Some(graph)
  }
}
