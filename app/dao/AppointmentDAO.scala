package dao

import javax.inject.Inject
import models.Appointment
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait AppointmentTrait { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class AppointmentTable(tag: Tag) extends Table[Appointment](tag, "appointments") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def patientId = column[Long]("patient_id")
    def doctorId = column[Long]("doctor_id")

    def * = (id.?, patientId, doctorId) <> (Appointment.tupled, Appointment.unapply)
  }
}

class AppointmentDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with AppointmentTrait {

  import profile.api._

  private val Appointments = TableQuery[AppointmentTable]

  def all(): Future[Seq[Appointment]] = db.run(Appointments.result)

  def get(id: Long): Future[Option[Appointment]] = db.run(Appointments.filter(_.id === id).result.headOption)

  def insert(appointment: Appointment): Future[Long] = db.run(Appointments returning Appointments.map(_.id) += appointment)

  def delete(id: Long): Future[Int] = db.run(Appointments.filter(_.id === id).delete)
}
