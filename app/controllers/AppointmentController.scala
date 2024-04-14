package controllers

import dao.AppointmentDAO
import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{AbstractController, ControllerComponents}
import scala.concurrent.{ExecutionContext, Future}
import com.vonage.client.VonageClient
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Date
import models.Appointment

class AppointmentController @Inject()
(appointmentDao: AppointmentDAO, authAction: AuthAction, cc: ControllerComponents)
(implicit executionContext: ExecutionContext) extends AbstractController(cc) {

  // Define form for appointment details
  val appointmentForm: Form[Appointment] = Form(
    mapping(
      "id" -> ignored(None: Option[Long]),
      "patientId" -> longNumber,
      "doctorId" -> longNumber
    )(Appointment.apply)(Appointment.unapply)
  )

  // Initialize Vonage client with your API key and secret
  val vonageClient = VonageClient.builder()
    .apiKey("93c43011")
    .apiSecret("O8Koc9MvbEtF9LFk")
    .build()

  // Show appointment scheduling form
  def showAppointmentForm = Action { implicit request =>
    Ok(views.html.appointment())
  }

  // Handle appointment form submission
  def scheduleAppointment = Action.async { implicit request =>
    val boundForm = appointmentForm.bindFromRequest()
    boundForm.fold(
      formWithErrors => {
        // Form validation failed, render form again with errors
        Future.successful(BadRequest(views.html.appointment()))
      },
      appointment => {
        // Form validation passed, insert appointment into database
        appointmentDao.insert(appointment).map { _ =>
          // Generate Vonage Video API session ID and token
          val sessionId = "1_MX5lMTQ4MzA2Mi03ZDUyLTRlMGUtODU2Yy0wOGY5NWE4ZmMyN2R-fjE3MTE4ODA1ODk1MjV-QmtVS0duRDlzay9zcDFZanUyaktLYXE1fn5-"
          val token = "eyJhbGciOiJSUzI1NiJ9.eyJhcHBsaWNhdGlvbl9pZCI6ImUxNDgzMDYyLTdkNTItNGUwZS04NTZjLTA4Zjk1YThmYzI3ZCIsImlhdCI6MTcxMTg4MDU4OSwianRpIjoiZTE0ODMwNjItN2Q1Mi00ZTBlLTg1NmMtMDhmOTVhOGZjMjdkIiwic2NvcGUiOiJzZXNzaW9uLmNvbm5lY3QiLCJzZXNzaW9uX2lkIjoiMV9NWDVsTVRRNE16QTJNaTAzWkRVeUxUUmxNR1V0T0RVMll5MHdPR1k1TldFNFptTXlOMlItZmpFM01URTRPREExT0RrMU1qVi1RbXRWUzBkdVJEbHpheTl6Y0RGWmFuVXlha3RMWVhFMWZuNS0iLCJyb2xlIjoicHVibGlzaGVyIiwiaW5pdGlhbF9sYXlvdXRfY2xhc3NfbGlzdCI6IiJ9.MYahkrsGqCxQ5G2YB0H1PzVWI4CS13hNbNJdYYAttrklG3WSJoCAL_wA59aLPrhpxX6_b6gaJs4_Yew3Rl_e6ssVq212pINJ07hX7zTvNDerKvwJOpXBOgF3NMn01hWf1tqCIFsNXBt_gvsIrFX1ezJdBfL72oGfqAHJh8w2O0khR31YHlQO59hQrSZxfPGTWBfrcecgSVYwCepry2JBK3NjLJwhZYAx321Ralf1mZ47qhfpdMfDxaV1O_pIWUeWwGO901TiyjgeRzaWxZBmn_ppj4uxbGvEFM45HmMWBBHQKsquHL-7bX73BUTqHZ9uu1uO8klW84ppHpsuUHx4Jg"

          // Redirect to video call page with session ID and token
          Redirect(routes.AppointmentController.videoCall(sessionId, token))
            .flashing("success" -> "Appointment scheduled successfully")
        }.recover {
          case ex: Exception =>
            // An error occurred while inserting appointment
            InternalServerError("An error occurred while scheduling the appointment")
        }
      }
    )
  }

  // Show video call interface
  def showVideoCall(sessionId: String, token: String) = Action { implicit request =>
    Ok(views.html.videoCall(sessionId, token))
  }

  // Generate Vonage Video API session ID
  private def generateSessionId(): String = {
    // Implement session ID generation logic here (e.g., using UUID)
    // Return a unique session ID for each video call
    java.util.UUID.randomUUID().toString
  }

  // Generate Vonage Video API token
  private def generateToken(sessionId: String): String = {
    // Generate token using Vonage Video API SDK or JWT library
    // For example, using JWT library to create a JSON Web Token
    val key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256)
    val expirationTime = System.currentTimeMillis() + 1000 * 60 * 30 // Token expiration time (e.g., 30 minutes)
    Jwts.builder()
      .setSubject(sessionId)
      .setExpiration(new Date(expirationTime))
      .signWith(key)
      .compact()
  }

  // Initiate video call with session ID and token
  def videoCall(sessionId: String, token: String) = Action { implicit request =>
    // Pass session ID and token to the video call view
    Ok(views.html.videoCall(sessionId, token))
  }
}
