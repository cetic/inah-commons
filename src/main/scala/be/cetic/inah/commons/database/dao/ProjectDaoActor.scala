package be.cetic.inah.commons.database.dao

import java.lang.management.ManagementFactory

import akka.actor.{Actor, ActorLogging}
import akka.util.Timeout
import be.cetic.inah.commons.database.dao.ProjectDaoActor._
import be.cetic.inah.commons.database.model._

import scala.concurrent.duration._
import be.cetic.inah.commons.database.sql.SqlDao
import be.cetic.inah.commons.database.sql.management.model._
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContextExecutor, Future}



object ProjectDaoActor {



}

class ProjectDaoActor extends Actor with ActorLogging with ProjectModel{

  implicit val dispatcher: ExecutionContextExecutor = context.dispatcher
  val sqlDao = new SqlDao(slick.jdbc.PostgresProfile)
  val dao = sqlDao.managementDao
  import dao.driver.api._
  implicit val timeout: Timeout = Timeout(5 seconds)

  def receive: Receive = {
    ???
  }

  def readServiceOffer : Future[Seq[ServiceOfferDto]] = {

    dao.db.run(dao.ServiceOfferDao.serviceOffers.result).mapTo[Seq[ServiceOfferDto]]

  }

  def readOfferPattern (offerId : Int) : Future[Offer] = {

//Get Seq of questions
    val questionId = for {

      offerQuestionPattern <- dao.OfferQuestionPatternDao.offerQuestions.filter(_.offerId===offerId)
      questionPattern <- dao.QuestionPatternDao.questionsPattern.filter(_.id === offerQuestionPattern.questionId)

    } yield questionPattern

    val resultQuestionId = dao.db.run(questionId.result).mapTo[QuestionPatternDto]

    val questionName = for
    {

      offerQuestionPattern <- dao.OfferQuestionPatternDao.offerQuestions.filter(_.offerId===offerId)

      questionPattern <- dao.QuestionPatternDao.questionsPattern.filter(_.id === offerQuestionPattern.questionId)

      serviceOffer <- dao.ServiceOfferDao.serviceOffers.filter(_.id === offerQuestionPattern.offerId)

    } yield serviceOffer

    val resultQuestionName = dao.db.run(questionName.result).mapTo[ServiceOfferDto]

    val questionDescription = for {

      offerQuestionPattern <- dao.OfferQuestionPatternDao.offerQuestions.filter(_.offerId===offerId)

      questionPattern <- dao.QuestionPatternDao.questionsPattern.filter(_.id === offerQuestionPattern.questionId)

      serviceOffer <- dao.ServiceOfferDao.serviceOffers.filter(_.id === offerQuestionPattern.offerId)

      patternDescription <- dao.PatternDescriptionDao.patternDescriptions.filter(_.id === questionPattern.descriptionId)
    } yield patternDescription

    val resultQuestionDescription = dao.db.run(questionDescription.result).mapTo[PatternDescriptionDto]

    val options = for {

      offerQuestionPattern <- dao.OfferQuestionPatternDao.offerQuestions.filter(_.offerId===offerId)

      questionPattern <- dao.QuestionPatternDao.questionsPattern.filter(_.id === offerQuestionPattern.questionId)


    } yield questionPattern

    val resultOptions = dao.db.run(options.result).mapTo[QuestionPatternDto]
    val questionValue  = for {
      offerQuestionPattern <- dao.OfferQuestionPatternDao.offerQuestions.filter(_.offerId===offerId)

      questionPattern <- dao.QuestionPatternDao.questionsPattern.filter(_.id === offerQuestionPattern.questionId)

      serviceOffer <- dao.ServiceOfferDao.serviceOffers.filter(_.id === offerQuestionPattern.offerId)

      patternDescription <- dao.PatternDescriptionDao.patternDescriptions.filter(_.id === questionPattern.descriptionId)
      questionContent <- dao.QuestionContentDao.questionContents.filter(_.questionPatternId=== questionPattern.id)

    } yield questionContent

    val resultQuestionContent = dao.db.run(questionValue.result).mapTo[QuestionContentDto]

    val questionFinalRequest = for {
      idQuestion <- resultQuestionId.map(_.id)
      questionName <- resultQuestionName.map(_.name)
      questionDescription <- resultQuestionDescription.map(_.description)
      options <- resultOptions.map(_.options)
      value <- resultQuestionContent.map(_.value)
      question <- Question(idQuestion,questionName, questionDescription,options, Some(value))
    } yield question

    //questionFinalRequest.mapTo[Seq[Question]]

    //Get Seq of appendix

    val appendixDescriptionId = for {

      offerAppendixPattern <- dao.OfferAppendixPatternDao.offersAppendices.filter(_.offerId===offerId)
    } yield offerAppendixPattern

    val resultAppendixDescriptionId = dao.db.run(appendixDescriptionId.result).mapTo[OfferAppendixPatternDto]


    val appendixName = for {
      offerAppendixPattern <- dao.OfferAppendixPatternDao.offersAppendices.filter(_.offerId===offerId)
      serviceOffer <- dao.ServiceOfferDao.serviceOffers.filter(_.id===offerAppendixPattern.descriptionId)

    } yield serviceOffer

    val resultAppendixName = dao.db.run(appendixName.result).mapTo[ServiceOfferDto]

    val descriptionAppendix = for {

      offerAppendixPattern <- dao.OfferAppendixPatternDao.offersAppendices.filter(_.offerId===offerId)
      patternDescription <- dao.PatternDescriptionDao.patternDescriptions.filter(_.id===offerAppendixPattern.descriptionId)
    } yield patternDescription

    val resultDescriptionPattern = dao.db.run(descriptionAppendix.result).mapTo[PatternDescriptionDto]


    val appendixContent = for {

      offerAppendixPattern <- dao.OfferAppendixPatternDao.offersAppendices.filter(_.offerId===offerId)
      patternDescription <- dao.PatternDescriptionDao.patternDescriptions.filter(_.id===offerAppendixPattern.descriptionId)
      appendixContent <- dao.AppendixContentDao.appendixContents.filter(_.descriptionId===patternDescription.id)
    } yield appendixContent

    val resultAppendixContent = dao.db.run(appendixContent.result).mapTo[AppendixContentDto]

    val appendixFinalRequest = for {

      descriptionId <- resultAppendixDescriptionId.map(_.descriptionId)
      name <- resultAppendixName.map(_.name)
      description <- resultDescriptionPattern.map(_.description)
      filename <- resultAppendixContent.map(_.content.toString)
      appendix <- Appendix(descriptionId, name, description, Some(filename))
    } yield appendix

    //appendixFinalRequest.mapTo[Seq[Appendix]]


    //get sequence of sections

    val sectionDescriptionId = for {
      offerSectionPattern <- dao.OfferSectionPatternDao.offerSections.filter(_.offerId === offerId)
    } yield offerSectionPattern

    val resultSectionDescription = dao.db.run(sectionDescriptionId.result).mapTo[OfferSectionPatternDto]


    val sectionNameDescription = for {
      offerSectionPattern <- dao.OfferSectionPatternDao.offerSections.filter(_.offerId === offerId)
      nameDescription <- dao.PatternDescriptionDao.patternDescriptions.filter(_.id===offerSectionPattern.sectionPatternId)
    } yield nameDescription

    val resultSectionNameDescription = dao.db.run(sectionNameDescription.result).mapTo[PatternDescriptionDto]


    //Get Sequence of subsections

    val subsectionDescriptionId = for {
      offerSectionPattern <- dao.OfferSectionPatternDao.offerSections.filter(_.offerId === offerId)
      sectionPattern <- dao.SectionPatternDao.sections.filter(_.id===offerSectionPattern.sectionPatternId)
    } yield sectionPattern

    val resultSubsectionDescription = dao.db.run(subsectionDescriptionId.result).mapTo[OfferSectionPatternDto]

    val subsectionNameDescription = for {
      offerSectionPattern <- dao.OfferSectionPatternDao.offerSections.filter(_.offerId === offerId)
      sectionPattern <- dao.SectionPatternDao.sections.filter(_.id===offerSectionPattern.sectionPatternId)
      nameDescription <- dao.PatternDescriptionDao.patternDescriptions.filter(_.id===sectionPattern.descriptionId)
    } yield nameDescription

    val resultSubsectionNameDescription = dao.db.run(subsectionNameDescription.result).mapTo[PatternDescriptionDto]


    val subsectionContent = for {

      offerSectionPattern <- dao.OfferSectionPatternDao.offerSections.filter(_.offerId === offerId)
      sectionPattern <- dao.SectionPatternDao.sections.filter(_.id===offerSectionPattern.sectionPatternId)
      content <- dao.SectionContentDao.sectionContents.filter(_.sectionPatternId===sectionPattern.id)

    } yield content
    val resultSubsectionContent = dao.db.run(subsectionContent.result).mapTo[SectionContentDto]

    val seqSubsections =for {
      descriptionId <- resultSubsectionDescription.map(_.sectionPatternId)
      name <- resultSubsectionNameDescription.map(_.name)
      description <- resultSubsectionNameDescription.map(_.description)
      content <- resultSubsectionContent.map(_.content)
      subsection <- Subsection(descriptionId, name, description, Some(content))
    } yield subsection

    //case class Section(sectionId: Int, title: String, description: String, content: Seq[Subsection])

    val section = for {
      sectionId <- resultSectionDescription.map(_.sectionPatternId)
      title <- resultSectionNameDescription.map(_.name)
      description <- resultSectionNameDescription.map(_.description)
      content <- seqSubsections
      sectionFinalQuery <- Section (sectionId, title, description, content)

    } yield sectionFinalQuery

   // section.mapTo[Seq[Section]]

    val offer = for {

      requiredMultiChoice <- questionFinalRequest
      requiredAppendices <- appendixFinalRequest
      requiredSections <- section
      offerFinalRequest <- Offer(offerId,requiredMultiChoice, requiredAppendices, requiredSections )
    } yield offerFinalRequest

    offer.mapTo[Offer]
  }

  def createOrUpdateQuestionContent (content : QuestionContentDto) : Future[QuestionContentDto] = {


    val q = for {

      questionExist <- dao.QuestionContentDao.questionContents.filter(_.questionPatternId === content.questionPatternId)
      projectExist <- dao.QuestionContentDao.questionContents.filter(_.projectId ===content.projectId)
      compute <-   if (projectExist == null && questionExist == null)
      {
        dao.QuestionContentDao.create(content)
      }else dao.QuestionContentDao.questionContents.update(content)

    } yield questionExist

    dao.db.run(q.result).mapTo[QuestionContentDto]


  }

  def createOrUpdateAppendixFile (content : AppendixContentDto) : Future[AppendixContentDto] = {

    val q = for {

      appendixFileExist <- dao.AppendixContentDao.appendixContents.filter( x => x.projectId === content.projectId && x.descriptionId=== content.descriptionId)

        compute <- if (appendixFileExist == null)
          {
            dao.AppendixContentDao.create(content)
          } else dao.AppendixContentDao.appendixContents.update(content)


    } yield compute

    dao.db.run(q.result).mapTo[AppendixContentDto]
  }



  def createOrUpdateSectionContent (content : SectionContentDto) : Future[SectionContentDto] = {
      val q = for {


        sectionContentxist <- dao.SectionContentDao.sectionContents.filter(x=>x.projectId === content.projectId && x.descriptionId === content.descriptionId && x.sectionPatternId === content.sectionPatternId)

        compute <- if (sectionContentxist==null)
          {
            dao.SectionContentDao.create(content)
          } else dao.SectionContentDao.sectionContents.update(content)

      } yield compute

    dao.db.run(q.result).mapTo[SectionContentDto]

  }


}
