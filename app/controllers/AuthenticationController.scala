package controllers

import DAO.{ItemsBidDAO, SellItemDAO, UserDao}

import javax.inject.Inject
import java.sql.{SQLException, SQLIntegrityConstraintViolationException}
import models.{ItemsBid, LoginInfo, SellFormData, UserInfo}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class AuthenticationController @Inject()(
                                          userDao: UserDao,
                                          itemController: ItemController,
                                          sellItemDAO: SellItemDAO,
                                          itemsBidDAO: ItemsBidDAO,
                                          mcc: MessagesControllerComponents
                                        )(implicit executionContext: ExecutionContext) extends MessagesAbstractController(mcc) {

  private val userForm: Form[UserInfo] = Form(
    mapping(
      "id" -> optional(longNumber),
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "name" -> nonEmptyText,
      "email" -> email
    )(UserInfo.apply)(UserInfo.unapply)
  )

  private val loginForm: Form[LoginInfo] = Form(
    mapping(
      "username" -> text(),
      "password" -> text()
    )(LoginInfo.apply)(LoginInfo.unapply)
  )

  val bidForm: Form[ItemsBid] = itemController.bidForm
  private val sellFormData: Future[Seq[SellFormData]] = sellItemDAO.getAllSellFormData

  def index(): Action[AnyContent] = Action.async { implicit request =>
    val highestBidFuture: Future[Map[Long, Option[ItemsBid]]] = itemsBidDAO.getAllHighestBids
    highestBidFuture.flatMap { highestBidOption =>
      request.session.get("username") match {
        case Some(username) =>
          sellFormData.map { data =>
            Ok(views.html.index(username, bidForm, highestBidOption, data))
          }.recover {
            case ex: Exception =>
              InternalServerError("An error occurred: " + ex.getMessage)
          }
        case None =>
          Future.successful(Unauthorized("User not logged in"))
      }
    }
  }

  def insertUser(): Action[AnyContent] = Action.async { implicit request =>
    val userInfo = userForm.bindFromRequest().get
    userDao.insert(userInfo).map { _ =>
      Redirect(routes.AuthenticationController.showLoginForm())
        .flashing("success" -> "Registration successful. Please log in.")
    }.recover {
      case _: SQLIntegrityConstraintViolationException =>
        Redirect(routes.AuthenticationController.showLoginForm())
          .flashing("error" -> "Username already exists. Please choose a different username.")
      case sqlEx: SQLException if sqlEx.getSQLState == "22001" =>
        Redirect(routes.AuthenticationController.showLoginForm())
          .flashing("error" -> "Data too long. Please enter shorter values.")
      case _: SQLException =>
        Redirect(routes.AuthenticationController.showLoginForm())
          .flashing("error" -> "An error occurred while processing your request.")
    }
  }


  def showLoginForm: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.login(userForm, loginForm))
  }

  def authenticateUser(): Action[AnyContent] = Action.async { implicit request =>
    loginForm.bindFromRequest().fold(
      _ => {
        Future.successful(Unauthorized("Cannon Login"))
      },
      loginInfo => {
        userDao.authenticate(loginInfo).flatMap { isAuthenticated =>
          if (isAuthenticated) {
            userDao.getUserInfo(loginInfo).map {
              case Some((authStatus, username)) =>
                Redirect(routes.AuthenticationController.index()).withSession(
                  "authenticated" -> authStatus.toString,
                  "username" -> username
                )
              case None =>
                Unauthorized("User not found")
            }
          } else {
            Future.successful(Unauthorized("Invalid credentials"))
          }
        }
      }
    )
  }

  def logout: Action[AnyContent] = Action { implicit request =>
    Redirect(routes.AuthenticationController.showLoginForm()).withNewSession
  }

}
