package controllers

import DAO.{ItemsBidDAO, SellItemDAO}
import models.{ItemsBid, SellFormData}
import play.api.data.Form
import javax.inject._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProfileController @Inject()(val mcc: MessagesControllerComponents,
                                  itemsBidDAO: ItemsBidDAO,
                                  itemController: ItemController,
                                  sellItemController: SellItemController,
                                  sellItemDAO: SellItemDAO)(implicit executionContext: ExecutionContext) extends MessagesAbstractController(mcc) {

  val highestBidFuture: Future[Map[Long, Option[ItemsBid]]] = itemsBidDAO.getAllHighestBids

  def showProfile(): Action[AnyContent] = Action.async { implicit request =>
    val sellForm = sellItemController.sellForm
    request.session.get("username") match {
      case Some(username) =>
        val sellFormDataFuture: Future[Seq[SellFormData]] = sellItemDAO.getAllSellFormDataByUsername(username)
        sellFormDataFuture.flatMap { sellFormData =>
          highestBidFuture.map { highestBidOption =>
            Ok(views.html.profile(username, sellFormData, highestBidOption, sellForm))
          }.recover {
            case ex: Exception =>
              InternalServerError("An error occurred: " + ex.getMessage)
          }
        }
      case None =>
        Future.successful(Unauthorized("User not logged in"))
    }
  }

  def showSellersProfile(sellersName: String): Action[AnyContent] = Action.async { implicit request =>
    request.session.get("username") match {
      case Some(username) =>
        val bidForm: Form[ItemsBid] = itemController.bidForm
        val sellFormDataFuture: Future[Seq[SellFormData]] = sellItemDAO.getAllSellFormDataByUsername(sellersName)
        val usernamesFuture: Future[Seq[String]] = sellItemDAO.getAllUsernames
        val combinedFuture = for {
          sellFormData <- sellFormDataFuture
          usernames <- usernamesFuture
        } yield (sellFormData, usernames)
        combinedFuture.flatMap { case (sellFormData, usernames) =>
          highestBidFuture.map { highestBidOption =>
            Ok(views.html.products(username, bidForm, highestBidOption, sellFormData, usernames))
          }.recover {
            case ex: Exception =>
              InternalServerError("An error occurred: " + ex.getMessage)
          }
        }
      case None =>
        Future.successful(Unauthorized("User not logged in"))
    }
  }

  def showCategoryProducts(categoryName: String): Action[AnyContent] = Action.async { implicit request =>
    request.session.get("username") match {
      case Some(username) =>
        val bidForm: Form[ItemsBid] = itemController.bidForm
        val sellFormDataFuture: Future[Seq[SellFormData]] = sellItemDAO.getAllSellFormDataByCategory(categoryName)
        val categoriesFuture: Future[Seq[String]] = sellItemDAO.getAllCategories
        val combinedFuture = for {
          sellFormData <- sellFormDataFuture
          categories <- categoriesFuture
        } yield (sellFormData, categories)
        combinedFuture.flatMap { case (sellFormData, categories) =>
          highestBidFuture.map { highestBidOption =>
            Ok(views.html.categories(username, bidForm, highestBidOption, sellFormData, categories))
          }.recover {
            case ex: Exception =>
              InternalServerError("An error occurred: " + ex.getMessage)
          }
        }
      case None =>
        Future.successful(Unauthorized("User not logged in"))
    }
  }
}
