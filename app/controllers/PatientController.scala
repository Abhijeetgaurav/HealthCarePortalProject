package controllers

import dao.PatientDAO

import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}
import com.vonage.client.VonageClient
import com.vonage.client.sms.messages.TextMessage
import models.Patient

class PatientController @Inject()
(patientDao: PatientDAO, authAction: AuthAction, cc: ControllerComponents)
(implicit executionContext: ExecutionContext) extends AbstractController(cc) {

  // Define form for patient details
  val patientForm: Form[Patient] = Form(
    mapping(
      "id" -> ignored(None: Option[Long]),
      "name" -> nonEmptyText,
      "age" -> number(min = 0),
      "disease" -> nonEmptyText,
      "location" -> nonEmptyText,
      "email" -> email,
      "contactNumber" -> nonEmptyText
    )(Patient.apply)(Patient.unapply)
  )

  // Initialize Vonage client with your API key and secret  93c43011    O8Koc9MvbEtF9LFk
  val vonageClient = VonageClient.builder()
    .apiKey("737c01bf")
    .apiSecret("ax1dQaHAKMLicGqp")
    .build()

  // Show patient details form
  def showPatientForm = Action { implicit request =>
    Ok(views.html.patient())
  }

  // Handle patient form submission
  def addPatient = Action.async { implicit request =>
    val boundForm = patientForm.bindFromRequest()
    boundForm.fold(
      formWithErrors => {
        // Form validation failed, render form again with errors
        Future.successful(BadRequest(views.html.patient()))
      },
      patient => {
        // Form validation passed, insert patient into database
        patientDao.insert(patient).flatMap { _ =>
          // Construct message containing patient details
          val messageText = s"New patient details: Name: ${patient.name}, Age: ${patient.age}, Disease: ${patient.disease}, Location: ${patient.location}, Email: ${patient.email}, Contact Number: ${patient.contactNumber}"

          // Send SMS using Vonage API
          val message = new TextMessage("Health Care Portal", "+919801539818", messageText)
          val response = vonageClient.getSmsClient.submitMessage(message)

          // Handle SMS send response
          if (response != null) {
            // SMS sent successfully
            Future.successful(Redirect(routes.PatientController.addPatient).flashing("success" -> "Patient added sms sent successfully"))
          } else {
            // Failed to send SMS
            Future.successful(InternalServerError("Failed to send SMS notification"))
          }
        }.recover {
          case ex: Exception =>
            // An error occurred while inserting patient
            InternalServerError("An error occurred while adding the patient")
        }
      }
    )
  }

  // Define logout action
  def logout = Action.async { implicit request =>
    Future.successful(Redirect(routes.LoginController.showLogin).withNewSession)
  }
}
