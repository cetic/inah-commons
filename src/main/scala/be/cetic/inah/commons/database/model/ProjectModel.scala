package be.cetic.inah.commons.database.model

case class Question(questionId: Int, name: String, description: String, options: Seq[String], value: Option[String] = None)
case class Appendix(descriptionId: Int, name: String, description: String, filename: Option[String])
case class Subsection(descriptionId: Int, name: String, description: String, content: Option[String])

case class Section(sectionId: Int, title: String, description: String, content: Seq[Subsection])
case class Offer(offerId: Int, requiredMultichoice: Seq[Question], requiredAppendices: Seq[Appendix], requiredSections: Seq[Section])
case class Application(selectedOfferIds: Seq[Int], multichoice: Seq[Question], appendices: Seq[Appendix], sections: Seq[Section])
