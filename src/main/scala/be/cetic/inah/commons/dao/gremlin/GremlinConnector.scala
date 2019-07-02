package be.cetic.inah.commons.dao.gremlin

import java.io.File

import org.apache.tinkerpop.gremlin.driver.Cluster
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource
import org.apache.tinkerpop.gremlin.structure.util.empty.EmptyGraph


object GremlinConnector {

  def buildCluster(config: String): Cluster = {
    Cluster.build(new File(config)).create()
  }


  def graphTraversalSource(clusterConfig: String, remoteSourceName: String): GraphTraversalSource = {
    val cluster = buildCluster(clusterConfig)
    val graph = EmptyGraph.instance()
    graph.traversal().withRemote(
      DriverRemoteConnection.using(cluster, remoteSourceName)
    )
  }


}
