package be.cetic.inah.commons.database.model
import spray.json.{DefaultJsonProtocol, RootJsonFormat}


trait ProjectModelJsonProtocol extends DefaultJsonProtocol {

  implicit val questionJsonProtocol : RootJsonFormat[Question] = jsonFormat5(Question)
  implicit val appendixJsonProtocol : RootJsonFormat[Appendix] = jsonFormat4(Appendix)
  implicit val subsectionJsonProtocol : RootJsonFormat[Subsection] =jsonFormat4(Subsection)
  implicit val sectionJsonProtocol : RootJsonFormat[Section] = jsonFormat4(Section)
  implicit val offerJsonProtocol : RootJsonFormat[Offer] = jsonFormat4(Offer)
  implicit val applicationJsonProtocol : RootJsonFormat[Application] = jsonFormat4(Application)


}
