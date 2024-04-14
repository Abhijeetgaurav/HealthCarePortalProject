package dao

import javax.inject.Inject
import models.Patient
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait PatientTrait { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class PatientTable(tag: Tag) extends Table[Patient](tag, "patients") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def age = column[Int]("age")
    def disease = column[String]("disease")
    def location = column[String]("location")
    def email = column[String]("email")
    def contactNumber = column[String]("contact_number")

    def * = (id.?, name, age, disease, location, email, contactNumber) <> (Patient.tupled, Patient.unapply)
  }
}

class PatientDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with PatientTrait {

  import profile.api._

  private val Patients = TableQuery[PatientTable]

  def all(): Future[Seq[Patient]] = db.run(Patients.result)

  def get(id: Long): Future[Option[Patient]] = db.run(Patients.filter(_.id === id).result.headOption)

  def insert(patient: Patient): Future[Long] = db.run(Patients returning Patients.map(_.id) += patient)

  def delete(id: Long): Future[Int] = db.run(Patients.filter(_.id === id).delete)
}
