package be.cetic.inah.commons.database.model

trait ProjectModel

case class Question(questionId: Option[Int], name: String, description: String, options: Seq[String], value: Option[String] = None)
case class Appendix(descriptionId: Option[Int], name: String, description: String, filename: Option[String])
case class Subsection(descriptionId: Option[Int], name: String, description: String, content: Option[String])

case class Section(sectionId: Option[Int], title: String, description: String, content: Seq[Subsection])
case class Offer(offerId: Int, requiredMultichoice: Seq[Question], requiredAppendices: Seq[Appendix], requiredSections: Seq[Section])
case class Application(selectedOfferIds: Seq[Int], multichoice: Seq[Question], appendices: Seq[Appendix], sections: Seq[Section])
