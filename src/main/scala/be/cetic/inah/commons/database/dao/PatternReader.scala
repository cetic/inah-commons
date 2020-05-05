package be.cetic.inah.commons.database.dao

import be.cetic.inah.commons.database.model._
import be.cetic.inah.commons.database.sql.management.ManagementDaoFactory
import be.cetic.inah.commons.database.sql.management.model.{OfferSectionPatternDto, PatternDescriptionDto, QuestionPatternDto}
import scala.concurrent.{ExecutionContextExecutor, Future}

trait PatternReader {
  implicit val dispatcher: ExecutionContextExecutor
  val dao: ManagementDaoFactory

  import dao.driver.api._

  def readOfferQuestionPattern(offerId: Int): Future[Seq[Question]] = {
    val querryQuestionPattern = for {
      offerQuestionPattern <- dao.OfferQuestionPatternDao.offerQuestions.filter(_.offerId === offerId)
      questionPattern <- dao.QuestionPatternDao.questionsPattern.filter(_.id === offerQuestionPattern.questionId)
      questionDescription <- dao.PatternDescriptionDao.patternDescriptions.filter(_.id === questionPattern.descriptionId)
    } yield {
      (questionPattern, questionDescription)
    }

    for {
      questionDescriptions <- dao.db.run(querryQuestionPattern.result)
    } yield {
      questionDescriptions.map { case (questionPattern: QuestionPatternDto, questionDescription: PatternDescriptionDto) =>
        Question(questionPattern.id.get, questionDescription.name, questionDescription.description, questionPattern.options, None)
      }
    }
  }

  def readAppendixPattern(offerId: Int): Future[Seq[Appendix]] = {
    val appQuery = for {
      offerAppendixPattern <- dao.OfferAppendixPatternDao.offersAppendices.filter(_.offerId === offerId)
      patternDescription <- dao.PatternDescriptionDao.patternDescriptions.filter(_.id === offerAppendixPattern.descriptionId)
    } yield {
      patternDescription
    }

    for {
      appPD <- dao.db.run(appQuery.result)
    } yield {
      appPD.map { desc => Appendix(desc.id.get, desc.name, desc.description, None) }
    }
  }

  def readSubsectionPattern(sectionId: Int): Future[Seq[Subsection]] = {
    val subQuery = for {
      pattern <- dao.SubsectionPatternDao.subsectionPatterns.filter(_.sectionPatternId === sectionId)
      descrs <- dao.PatternDescriptionDao.patternDescriptions.filter(_.id === pattern.descriptionId)
    } yield {
      descrs
    }
    dao.db.run(subQuery.result).map(_.map(d => Subsection(d.id.get, d.name, d.description, None)))
  }

  def readSectionPattern(offerId: Int): Future[Seq[Section]] = {
    val sectionQuery = for {
      section <- dao.OfferSectionPatternDao.offerSections.filter(_.offerId === offerId)
      description <- dao.PatternDescriptionDao.patternDescriptions.filter(_.id === section.sectionPatternId)
    } yield (section, description)

    for {
      sectionDescs <- dao.db.run(sectionQuery.result)
      subs <- Future sequence sectionDescs.map(sD => readSubsectionPattern(sD._1.sectionPatternId))
    } yield {
      sectionDescs.zip(subs)
        .map { case ((sec: OfferSectionPatternDto, desc: PatternDescriptionDto), subsections: Seq[Subsection]) =>
          Section(sec.sectionPatternId, desc.name, desc.description, subsections)
        }
    }
  }

  def readOfferPattern(offerId: Int): Future[Offer] = {
    for {
      questions <- readOfferQuestionPattern(offerId)
      appendices <- readAppendixPattern(offerId)
      sections <- readSectionPattern(offerId)
    } yield Offer(offerId, questions, appendices, sections)
  }

}
