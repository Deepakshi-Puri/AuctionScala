package controllers

import javax.inject._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import DAO.SellItemDAO
import play.api.i18n.{Lang, MessagesImpl}
import play.api.i18n.Messages.implicitMessagesProviderToMessages

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductController @Inject()(val sellItemDAO: SellItemDAO,
                                  mcc: MessagesControllerComponents)(implicit executionContext: ExecutionContext) extends MessagesAbstractController(mcc) {

  val updateSellForm: Form[String] = Form(
    single(
      "newTime" -> nonEmptyText
    )
  )

  val messagesProvider = MessagesImpl(Lang.defaultLang, messagesApi)


  def deleteProduct(listingID: Long): Action[AnyContent] = Action.async { implicit request =>
    sellItemDAO.deletePost(listingID).map { _ =>
      Redirect(routes.ProfileController.showProfile()).flashing("success" -> "Product deleted successfully")
    }
  }

  def updateProductTimeForm(listingID: Long): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.updateTime(updateSellForm, listingID)(messagesProvider, request))
  }

  def processUpdateProductTime(listingID: Long): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    updateSellForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.updateTime(formWithErrors, listingID)(messagesProvider, request)))
      },
      newTime => {
        sellItemDAO.updateProductTime(listingID, newTime).map { _ =>
          Redirect(routes.ProfileController.showProfile()).flashing("success" -> "Product time updated successfully.")
        }.recover {
          case _ => Redirect(routes.ProfileController.showProfile()).flashing("error" -> "Failed to update product time.")
        }
      }
    )
  }
}
