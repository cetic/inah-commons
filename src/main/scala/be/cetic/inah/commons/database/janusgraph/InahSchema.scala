package be.cetic.inah.commons.database.janusgraph


import org.apache.tinkerpop.gremlin.structure.Vertex
import org.janusgraph.core.{Cardinality, Multiplicity}
import org.janusgraph.graphdb.database.StandardJanusGraph
import org.janusgraph.graphdb.database.management.ManagementSystem

class InahSchema(graph: StandardJanusGraph, waitForStatus: Boolean = false, mixed: Boolean = true, reindexation: Boolean = false, enabling: Boolean = true) {

  private val schemaMakerMaker = new SchemaMaker(graph)
  private val medicalVertices = Seq("Person", "Event", "Drug", "Act", "Disease", "MeasurableQuantity",
    "MeasurableQuality", "Measure",
    "Symptom", "Observation", "Activeubstance", "Procedure", "Biometry")
  private val targetingVertices = Seq("Population", "OrNode", "Target")
  private val vLabels = medicalVertices ++ targetingVertices

  private val medicalEdges = Seq(
    ("CONCERNS", Multiplicity.MANY2ONE),
    ("HAS_VALUE", Multiplicity.MULTI),
    ("MEASURED", Multiplicity.MULTI),
    ("PRESCRIBED", Multiplicity.MULTI),
    ("DONE", Multiplicity.MULTI),
    ("OBSERVED", Multiplicity.MULTI),
    ("INVOLVES", Multiplicity.MULTI),
    ("IS_A", Multiplicity.MULTI),
    ("HAS_REF", Multiplicity.MULTI),
    ("PARENT_OF", Multiplicity.MULTI),
    ("CONTAINS", Multiplicity.MULTI)
  )
  private val targetingEdges = Seq(
    ("COVERS", Multiplicity.MANY2ONE),
    ("LOCAL_CONTAINS", Multiplicity.MULTI)
  )

  private val eLabels = medicalEdges ++ targetingEdges

  private val props = Seq(
    ("iid", classOf[String], Cardinality.SINGLE),
    ("ts", classOf[java.lang.Long], Cardinality.SINGLE),
    ("niss", classOf[String], Cardinality.SINGLE),
    ("description", classOf[String], Cardinality.LIST),
    ("codeInami", classOf[String], Cardinality.LIST),
    ("codeCnk", classOf[String], Cardinality.LIST),
    ("codeAtc", classOf[String], Cardinality.LIST),
    ("type", classOf[String], Cardinality.LIST),
    ("name", classOf[String], Cardinality.LIST),
    ("start", classOf[java.lang.Long], Cardinality.LIST),
    ("end", classOf[java.lang.Long], Cardinality.LIST),
    ("position", classOf[java.lang.Integer], Cardinality.LIST),
    ("delay", classOf[java.lang.Long], Cardinality.LIST),
    ("name", classOf[java.lang.String], Cardinality.LIST),
    ("hierarchyName", classOf[java.lang.String], Cardinality.SINGLE),
  )

  private val compositeIndices = Seq(
    ("byIid", "iid", classOf[Vertex]),
    ("byTs", "ts", classOf[Vertex]),
    ("byCodeInamiComposite", "codeInami", classOf[Vertex]),
    ("byCodeCnkComposite", "codeCnk", classOf[Vertex]),
    ("byCodeAtcComposite", "codeAtc", classOf[Vertex]),
    ("byNameComposite", "name", classOf[Vertex]),
    ("byDescriptionComposite", "description", classOf[Vertex]),
    ("byNissComposite", "niss", classOf[Vertex])
  )

  private val mixedIndices = if (mixed) {
    Seq(
      ("byCodeInamiMixed", "codeInami", classOf[Vertex]),
      ("byCodeCnkMixed", "codeCnk", classOf[Vertex]),
      ("byCodeAtcMixed", "codeAtc", classOf[Vertex]),
      ("byNameMixed", "name", classOf[Vertex]),
      ("byDescriptionMixed", "description", classOf[Vertex]),
      ("byNissMixed", "niss", classOf[Vertex])
    )
  } else Seq()


  private val allIndices = compositeIndices ++ mixedIndices

  def build() = {
    import schemaMakerMaker._
    schemaMakerMaker.init()

    vLabels.map(buildVertexLabel).toMap
    eLabels.map { case (lbl, mult) => buildEdgeLabel(lbl, mult) }.toMap
    val propertyKeyMap = props.map { case (name, klass, card) => buildPropertyKey(name, klass, card) }.toMap

    compositeIndices.foreach { case (indName, keyName, klazz) => buildCompositeIndex(indName, propertyKeyMap(keyName), klazz) }

    mixedIndices.foreach { case (indName, keyName, klazz) => buildMixedIndex(indName, propertyKeyMap(keyName), klazz) }


    management.commit()

    if (waitForStatus) {
      for (ind <- allIndices) {
        val indexName = ind._1
        ManagementSystem.awaitGraphIndexStatus(graph, indexName).call()
      }
    }
    if (reindexation) {
      allIndices.map(_._1).foreach(reindex)
    }

    if (enabling) {
      allIndices.map(_._1).foreach(enableIndex)
    }
  }
}
