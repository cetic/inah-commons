package be.cetic.inah.commons.database.sql.management

import be.cetic.inah.commons.database.sql.management.model._
import spray.json.{DefaultJsonProtocol, DeserializationException, JsValue, RootJsonFormat}

import scala.util.{Failure, Try}


trait SqlManagementJsonProtocol extends DefaultJsonProtocol {
  implicit val appendixContentDtoJsonFormat : RootJsonFormat[AppendixContentDto] = jsonFormat4(AppendixContentDto)
  implicit val offerAppendixPatternDtoJsonFormat : RootJsonFormat[OfferAppendixPatternDto] = jsonFormat2(OfferAppendixPatternDto)
  implicit val datasourceDtoJsonFormat: RootJsonFormat[DatasourceDto] = jsonFormat5(DatasourceDto)
  implicit val offerQuestionPatternDtoJsonFormat : RootJsonFormat[OfferQuestionPatternDto] = jsonFormat2(OfferQuestionPatternDto)
  implicit val offerSectionPatternDtoJsonFormat : RootJsonFormat[OfferSectionPatternDto] = jsonFormat2(OfferSectionPatternDto)
  implicit val patternDescriptionDtoJsonFormat : RootJsonFormat[PatternDescriptionDto] = jsonFormat3(PatternDescriptionDto)
  implicit val projectDatasourceJsonFormat: RootJsonFormat[ProjectDatasourceDto] = jsonFormat4(ProjectDatasourceDto)
  implicit val projectDtoJsonFormat: RootJsonFormat[ProjectDto] = jsonFormat8(ProjectDto)
  implicit val projectOfferDtoJsonFormat : RootJsonFormat[ProjectOfferDto] = jsonFormat2(ProjectOfferDto)
  implicit val projectResourceDtoJsonFormat: RootJsonFormat[ProjectResourceDto] = jsonFormat2(ProjectResourceDto)
  implicit val projectUserDetailsDtoJsonFormat : RootJsonFormat[ProjectUserDetailsDto] = jsonFormat3 (ProjectUserDetailsDto)
  implicit val questionContentDtoJsonFormat : RootJsonFormat[QuestionContentDto] = jsonFormat3(QuestionContentDto)
  implicit val questionPatternDtoJsonFormat : RootJsonFormat[QuestionPatternDto] = jsonFormat3(QuestionPatternDto)
  implicit val resourceDtoJsonFormat: RootJsonFormat[ResourceDto] = jsonFormat6(ResourceDto)
  implicit val sectionPatternDtoJsonFormat : RootJsonFormat[SectionPatternDto] = jsonFormat2(SectionPatternDto)
  implicit val serviceOfferDtoJsonFormat : RootJsonFormat[ServiceOfferDto] = jsonFormat2(ServiceOfferDto)
  implicit val subsectionContentDtoJsonFormat : RootJsonFormat[SubsectionContentDto] = jsonFormat4(SubsectionContentDto)
  implicit val subsectionPatternDtoJsonFormat : RootJsonFormat[SubsectionPatternDto] = jsonFormat2(SubsectionPatternDto)
  implicit val userDetailsDtoJsonFormat : RootJsonFormat[UserDetailsDto] = jsonFormat7(UserDetailsDto)
  implicit val userDtoJsonFormat: RootJsonFormat[UserDto] = jsonFormat4(UserDto)
  implicit val feedbackDtoJsonFormat : RootJsonFormat[FeedbackDto] = jsonFormat5(FeedbackDto)

  implicit object ManagementResourceJsonFormat extends RootJsonFormat[ManagementResource] {
    override def read(json: JsValue): ManagementResource = {
      val trials = Seq(
        Try(offerAppendixPatternDtoJsonFormat.read(json)),
        Try(appendixContentDtoJsonFormat.read(json)),
        Try(datasourceDtoJsonFormat.read(json)),
        Try(offerQuestionPatternDtoJsonFormat.read(json)),
        Try(offerSectionPatternDtoJsonFormat.read(json)),
        Try(patternDescriptionDtoJsonFormat.read(json)),
        Try(projectDatasourceJsonFormat.read(json)),
        Try(projectDtoJsonFormat.read(json)),
        Try(projectOfferDtoJsonFormat.read(json)),
        Try(projectResourceDtoJsonFormat.read(json)),
        Try(projectUserDetailsDtoJsonFormat.read(json)),
        Try(questionContentDtoJsonFormat.read(json)),
        Try(questionPatternDtoJsonFormat.read(json)),
        Try(resourceDtoJsonFormat.read(json)),
        Try(sectionPatternDtoJsonFormat.read(json)),
        Try(serviceOfferDtoJsonFormat.read(json)),
        Try(subsectionContentDtoJsonFormat.read(json)),
        Try(subsectionPatternDtoJsonFormat.read(json)),
        Try(userDetailsDtoJsonFormat.read(json)),
        Try(userDtoJsonFormat.read(json)),
        Try(feedbackDtoJsonFormat.read(json))
      )
      trials.find(_.isSuccess)
        .getOrElse(Failure[ManagementResource](new DeserializationException(s"Cannot read $json.")))
        .get
    }

    override def write(obj: ManagementResource): JsValue = {
      obj match {
        case o : OfferAppendixPatternDto => offerAppendixPatternDtoJsonFormat.write(o)
        case o : AppendixContentDto => appendixContentDtoJsonFormat.write(o)
        case o: DatasourceDto => datasourceDtoJsonFormat.write(o)
        case o : OfferQuestionPatternDto => offerQuestionPatternDtoJsonFormat.write(o)
        case o : OfferSectionPatternDto => offerSectionPatternDtoJsonFormat.write(o)
        case o : PatternDescriptionDto => patternDescriptionDtoJsonFormat.write(o)
        case o: ProjectDatasourceDto => projectDatasourceJsonFormat.write(o)
        case o: ProjectDto => projectDtoJsonFormat.write(o)
        case o : ProjectOfferDto => projectOfferDtoJsonFormat.write(o)
        case o: ProjectResourceDto => projectResourceDtoJsonFormat.write(o)
        case o : ProjectUserDetailsDto => projectUserDetailsDtoJsonFormat.write(o)
        case o : QuestionContentDto => questionContentDtoJsonFormat.write(o)
        case o : QuestionPatternDto => questionPatternDtoJsonFormat.write(o)
        case o: ResourceDto => resourceDtoJsonFormat.write(o)
        case o : SectionPatternDto => sectionPatternDtoJsonFormat.write(o)
        case o : ServiceOfferDto => serviceOfferDtoJsonFormat.write(o)
        case o : SubsectionContentDto => subsectionContentDtoJsonFormat.write(o)
        case o : SubsectionPatternDto => subsectionPatternDtoJsonFormat.write(o)
        case o : UserDetailsDto => userDetailsDtoJsonFormat.write(o)
        case o: UserDto => userDtoJsonFormat.write(o)
        case o : FeedbackDto => feedbackDtoJsonFormat.write(o)
        case m => throw DeserializationException(s"Support for $m not implemented.")
      }
    }

  }

}
