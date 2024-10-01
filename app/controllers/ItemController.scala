package controllers

import DAO.ItemsBidDAO

import javax.inject.Inject
import play.api.data._
import play.api.data.Forms._
import models.ItemsBid
import play.api.mvc.{Action, AnyContent, MessagesAbstractController, MessagesControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

class ItemController @Inject()(
                                itemsBidDAO: ItemsBidDAO,
                                mcc: MessagesControllerComponents
                              )(implicit executionContext: ExecutionContext) extends MessagesAbstractController(mcc) {

  val bidForm: Form[ItemsBid] = Form (
    mapping(
      "listingID" -> longNumber,
      "username" -> text,
      "bidPrice" -> number
    )(ItemsBid.apply)(ItemsBid.unapply)
  )

  def saveBid(): Action[AnyContent] = Action.async { implicit request =>
    val bidInfo = bidForm.bindFromRequest().get

    itemsBidDAO.getAllHighestBids.flatMap { allHighestBids =>
      val highestBidForListing: Option[ItemsBid] = allHighestBids.getOrElse(bidInfo.listingID, None)

      highestBidForListing match {
        case Some(highestBid) if bidInfo.bidPrice <= highestBid.bidPrice =>
          Future.successful(
            Redirect(routes.AuthenticationController.index())
              .flashing("error" -> "Your bid is lower than the highest bid.")
          )
        case _ =>
          itemsBidDAO.insert(bidInfo).map { _ =>
            Redirect(routes.AuthenticationController.index())
              .flashing("success" -> "Bid placed successfully. Will contact you after the results")
          }.recover {
            case _: Throwable =>
              Redirect(routes.AuthenticationController.index())
                .flashing("error" -> "An error occurred while processing your bid.")
          }
      }
    }
  }

}
