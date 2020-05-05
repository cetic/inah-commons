package be.cetic.inah.commons.database.dao

import be.cetic.inah.commons.database.model.{Application, Offer, Section}
import be.cetic.inah.commons.database.sql.management.ManagementDaoFactory

import scala.concurrent.{ExecutionContextExecutor, Future}

trait ContentReader extends PatternReader {

  implicit val dispatcher: ExecutionContextExecutor
  val dao: ManagementDaoFactory

  import dao.driver.api._

  def mergeOfferPattern(offers: Seq[Offer]): Application = {
    val offerIds = offers.map(_.offerId)
    val appendices = offers.flatMap(_.requiredAppendices).distinct
    val questions = offers.flatMap(_.requiredMultichoice).distinct
    val sections = offers.flatMap(_.requiredSections).distinct
    Application(offerIds, questions, appendices, sections)
  }

  def readApplication(projectId: String): Future[Application] = {
    val selecteOfferQuery = for {
      offerId <- dao.ProjectOfferDao.projectOffers.filter(_.projectId === projectId).map(_.serviceOfferId)
    } yield {
      offerId
    }
    for {
      offerIds <- dao.db.run(selecteOfferQuery.result)
      offerPattern <- Future sequence offerIds.map(readOfferPattern)
      pattern = mergeOfferPattern(offerPattern)
      application <- fillApplicationContent(projectId, pattern)
    } yield application
  }


  def fillSectionContent(projectId: String, section: Section): Future[Section] = {
    val subsFuture = section.content.map { sub =>
      for {
        content <- dao.db.run(dao.SubsectionContentDao.read((projectId, section.sectionId, sub.descriptionId)))
      } yield {
        val value = content.headOption.flatMap(_.content)
        sub.copy(content = value)
      }
    }

    for {
      subs <- Future sequence subsFuture
    } yield {
      section.copy(content = subs)
    }
  }

  def fillApplicationContent(projectId: String, application: Application): Future[Application] = {
    val filledMultichoices = Future sequence application.multichoice.map { question =>
      for {
        content <- dao.db.run(dao.QuestionContentDao.read((projectId, question.questionId)))
      } yield {
        val value = content.headOption.flatMap(_.value)
        question.copy(value = value)
      }
    }

    val filledSection = Future sequence application.sections.map { section =>
      fillSectionContent(projectId, section)
    }

    val filledAppendices = Future sequence application.appendices.map { app =>
      for {
        filename <- dao.db.run(dao.AppendixContentDao.appendixContents.filter(a => a.projectId === projectId && a.descriptionId === app.descriptionId).map(_.filename).result)
      } yield {
        val value = filename.headOption.flatMap(_.value)
        app.copy(filename = value)
      }
    }

    for {
      apps <- filledAppendices
      secs <- filledSection
      multis <- filledMultichoices
    } yield {
      application.copy(multichoice = multis, sections = secs, appendices = apps)
    }

  }
}
