package be.cetic.inah.commons.database.sql.population

import be.cetic.inah.commons.database.sql.management.model._
import be.cetic.inah.commons.database.sql.population.model._
import spray.json.{DefaultJsonProtocol, DeserializationException, JsValue, RootJsonFormat}

import scala.util.{Failure, Try}


trait SqlPopulationJsonProtocol extends DefaultJsonProtocol {

  implicit val coverDtoJsonFormat : RootJsonFormat[CoverDto] = jsonFormat5(CoverDto)
  implicit val personCoverIdJsonFormat: RootJsonFormat[PersonCoverId] = jsonFormat2(PersonCoverId)
  implicit val personCoverDtoJsonFormat: RootJsonFormat[PersonCoverDto] = jsonFormat4(PersonCoverDto)
  implicit val personPopulationIdJsonFormat: RootJsonFormat[PersonPopulationId] = jsonFormat2(PersonPopulationId)
  implicit val personPopulationJsonFormat: RootJsonFormat[PersonPopulationDto] = jsonFormat4(PersonPopulationDto)
  implicit val populationDtoJsonFormat: RootJsonFormat[PopulationDto] = jsonFormat5(PopulationDto)
  implicit val pseudoDtoJsonFormat: RootJsonFormat[PseudoDto] = jsonFormat3(PseudoDto)
  implicit val targetIdJsonFormat: RootJsonFormat[TargetId] = jsonFormat2(TargetId)
  implicit val targetDtoJsonFormat: RootJsonFormat[TargetDto] = jsonFormat3(TargetDto)
  implicit val dataDtoJsonFormat : RootJsonFormat[DataDto] = jsonFormat10(DataDto)
  implicit val dataSetDtoJsonForrmat : RootJsonFormat[DatasetDto]= jsonFormat4(DatasetDto)


  implicit object PopulationResourceJsonFormat extends RootJsonFormat[PopulationResource] {
    override def read(json: JsValue): PopulationResource = {
      val trials = Seq(
        Try(coverDtoJsonFormat.read(json)),
        Try(personCoverDtoJsonFormat.read(json)),
        Try(personPopulationJsonFormat.read(json)),
        Try(populationDtoJsonFormat.read(json)),
        Try(pseudoDtoJsonFormat.read(json)),
        Try(targetDtoJsonFormat.read(json)),
        Try(dataDtoJsonFormat.read(json)),
        Try(dataSetDtoJsonForrmat.read(json))
      )
      trials.find(_.isSuccess)
        .getOrElse(Failure[PopulationResource](new DeserializationException(s"Cannot read $json.")))
        .get
    }

    override def write(obj: PopulationResource): JsValue = {
      obj match {
        case o: CoverDto => coverDtoJsonFormat.write(o)
        case o: PersonCoverDto => personCoverDtoJsonFormat.write(o)
        case o: PersonPopulationDto => personPopulationJsonFormat.write(o)
        case o: PopulationDto => populationDtoJsonFormat.write(o)
        case o: PseudoDto => pseudoDtoJsonFormat.write(o)
        case o: TargetDto => targetDtoJsonFormat.write(o)
        case o : DataDto => dataDtoJsonFormat.write(o)
        case o : DatasetDto => dataSetDtoJsonForrmat.write(o)
        case m => throw DeserializationException(s"Support for $m not implemented.")
      }
    }
  }
}
