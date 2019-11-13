package be.cetic.inah.commons.database.janusgraph


import org.apache.tinkerpop.gremlin.structure.Element
import org.janusgraph.core.schema.{JanusGraphIndex, JanusGraphManagement, SchemaAction}
import org.janusgraph.core.{Cardinality, EdgeLabel, Multiplicity, PropertyKey, VertexLabel}
import org.janusgraph.graphdb.database.StandardJanusGraph


class SchemaMaker(graph: StandardJanusGraph) {
  var management: JanusGraphManagement = _

  def init(): Unit = {
    graph.getOpenTransactions.forEach(_.rollback())
    management = graph.openManagement()

    management.getOpenInstances.forEach { instance =>
      if (!instance.contains("current")) {
        management.forceCloseInstance(instance)
      }
    }
    graph.tx().rollback()
    commitAndReopenManagement()
  }

  def close(): Unit = {
    if (management != null) {
      management.commit()
    }
  }

  def buildMixedIndex(indexName: String, key: PropertyKey, elementClass: Class[_ <: Element]): JanusGraphIndex = {
    var index = management.getGraphIndex(indexName)
    if (index == null) {
      index = management.buildIndex(indexName, elementClass).addKey(key).buildMixedIndex("search")
    }
    index
  }

  def buildCompositeIndex(indexName: String, key: PropertyKey, elementClass: Class[_ <: Element]): JanusGraphIndex = {
    var index = management.getGraphIndex(indexName)
    if (index == null) {
      index = management.buildIndex(indexName, elementClass).addKey(key).buildCompositeIndex()
    }
    index
  }

  def buildVertexLabel(labelName: String): (String, VertexLabel) = {
    var lbl = management.getVertexLabel(labelName)
    if (lbl == null) {
      lbl = management.makeVertexLabel(labelName).make()
    }
    labelName -> lbl
  }

  def buildEdgeLabel(labelName: String, multiplicity: Multiplicity = Multiplicity.MULTI): (String, EdgeLabel) = {
    var lbl = management.getEdgeLabel(labelName)
    if (lbl == null) {
      lbl = management.makeEdgeLabel(labelName).multiplicity(multiplicity).make()
    }
    labelName -> lbl
  }


  def buildPropertyKey(keyName: String, dataType: Class[_], cardinality: Cardinality = Cardinality.LIST): (String, PropertyKey) = {
    var key = management.getPropertyKey(keyName)
    if (key == null) {
      if (key == null) {
        key = management.makePropertyKey(keyName).dataType(dataType).cardinality(cardinality).make()
      }
    }
    keyName -> key
  }

  def enableIndex(indexName: String): Unit = {
    commitAndReopenManagement()
    management.updateIndex(management.getGraphIndex(indexName), SchemaAction.ENABLE_INDEX)
  }

  def reindex(indexName: String): Unit = {
    commitAndReopenManagement()
    management.updateIndex(management.getGraphIndex(indexName), SchemaAction.REINDEX)
    management.commit()
  }


  def commitAndReopenManagement() {
    if (management.isOpen) management.commit()
    management = graph.openManagement()
  }
}