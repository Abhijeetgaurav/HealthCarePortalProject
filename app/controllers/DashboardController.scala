package controllers

import javax.inject._
import play.api.mvc._
import play.api.i18n.I18nSupport

@Singleton
class DashboardController @Inject()(val controllerComponents: ControllerComponents) extends BaseController with I18nSupport {

  def index: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val logoutUrl = routes.LoginController.showLogin // Provide the logout URL
    Ok(views.html.dashboard(logoutUrl))
  }

  def redirectToMobileClinic: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Redirect(routes.PatientController.addPatient)
  }
  def redirectToScheduleAppointment: Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Redirect(routes.AppointmentController.scheduleAppointment)
  }
}
