package controllers

import javax.inject._
import play.api.mvc.{MessagesControllerComponents, _}
import play.api.data._
import play.api.data.Forms._
import DAO.{ItemsBidDAO, SellItemDAO}
import models.{ItemsBid, SellFormData}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SellItemController @Inject()(val sellItemDAO: SellItemDAO,
                                   itemController: ItemController,
                                   itemsBidDAO: ItemsBidDAO,
                                   mcc: MessagesControllerComponents)(implicit executionContext: ExecutionContext) extends MessagesAbstractController(mcc) {

  val sellForm: Form[SellFormData] = Form(
    mapping(
      "listingID" -> ignored(None: Option[Long]),
      "username" -> nonEmptyText,
      "title" -> nonEmptyText,
      "description" -> nonEmptyText,
      "featureA" -> nonEmptyText,
      "featureB" -> nonEmptyText,
      "featureC" -> nonEmptyText,
      "featureD" -> nonEmptyText,
      "featureE" -> nonEmptyText,
      "biddingEndDate" -> nonEmptyText,
      "category" -> nonEmptyText
    )(SellFormData.apply)(SellFormData.unapply)
  )

  def sellItem(): Action[AnyContent] = Action { implicit request =>
    request.session.get("username") match {
      case Some(username) =>
        Ok(views.html.sellItem(username, sellForm))
      case None =>
        Unauthorized("User not logged in")
    }
  }

  def createNewListing(): Action[AnyContent] = Action.async { implicit request =>
    val sellInfo = sellForm.bindFromRequest().get
    val bidForm: Form[ItemsBid] = itemController.bidForm
    val insertionFuture = sellItemDAO.insert(sellInfo)

    insertionFuture.flatMap { _ =>
      val sellFormDataFuture: Future[Seq[SellFormData]] = sellItemDAO.getAllSellFormData
      val highestBidFuture: Future[Map[Long, Option[ItemsBid]]] = itemsBidDAO.getAllHighestBids

      val combinedFuture: Future[(Seq[SellFormData], Map[Long, Option[ItemsBid]])] =
        for {
          sellFormData <- sellFormDataFuture
          highestBids <- highestBidFuture
        } yield (sellFormData, highestBids)

      combinedFuture.map { case (sellFormData, highestBidOption) =>
        request.session.get("username") match {
          case Some(username) =>
            Ok(views.html.index(username, bidForm, highestBidOption, sellFormData))
              .flashing("success" -> "Your product has been listed!!")
          case None =>
            Unauthorized("User not logged in")
        }
      }.recover {
        case ex: Throwable =>
          Redirect(routes.SellItemController.sellItem())
            .flashing("error" -> s"An error occurred while processing your request: ${ex.getMessage}")
      }
    }
  }

}
