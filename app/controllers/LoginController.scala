package controllers

import dao.LoginDAO

import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}
import  models.User
class LoginController @Inject()
(loginDao: LoginDAO, authAction: AuthAction, cc: ControllerComponents)
(implicit executionContext: ExecutionContext) extends AbstractController(cc) {




  val loginForm = Form("email" -> text)

  def showLogin = Action { implicit request =>
    if (request.session.get("connected").isEmpty)
      Ok(views.html.login())
    else
      Redirect(routes.HomeController.index())
  }

  def login = Action.async { implicit request =>
    val boundForm = loginForm.bindFromRequest()
    val email = boundForm("email").value.getOrElse("")
    val password = boundForm("password").value.getOrElse("")

    loginDao.get(email).flatMap { userOption =>
      if (userOption.isDefined) {
        // Email exists, authenticate the user
        loginDao.authenticate(email, password).flatMap { userOption =>
          if (userOption.isDefined) {
            // Authentication successful, redirect
            val user = userOption.get
            Future.successful(
              Redirect(routes.DashboardController.index).withSession(
                request.session + ("connected" -> email) + ("id" -> user.id.toString)
              )
            )
          } else {
            // Authentication failed, show error message
            Future.successful(Ok("Wrong email or password"))
          }
        }
      } else {
        // Email not found, add to database and authenticate
        loginDao.insert1(email, password).flatMap { id =>
          Future.successful(
            Redirect(routes.HomeController.index()).withSession(
              request.session + ("connected" -> email) + ("id" -> id.toString)
            )
          )
        }
      }
    }
  }


  def logout = Action.async { implicit request =>
    Future.successful(Redirect(routes.LoginController.showLogin).withNewSession)
  }

}