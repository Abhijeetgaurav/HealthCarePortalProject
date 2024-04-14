package dao

import javax.inject.Inject
import models.User
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

trait LoginTrait { self: HasDatabaseConfigProvider[JdbcProfile] =>
  import profile.api._

  class LoginTable(tag: Tag) extends Table[User](tag, "LOGIN") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def email = column[String]("EMAIL", O.Unique)
    def password = column[String]("PASSWORD")

    def * = (id, email, password) <> (User.tupled, User.unapply)
  }
}

class LoginDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] with LoginTrait {

  import profile.api._

  private val Logins = TableQuery[LoginTable]

  def all(): Future[Seq[User]] = db.run(Logins.result)

  def get(email: String): Future[Option[User]] = db.run(Logins.filter(_.email === email).result.headOption)

  def exists(email: String): Future[Boolean] = db.run(Logins.filter(_.email === email).exists.result)

  def insert(login: User): Future[Int] = db.run(Logins returning Logins.map(_.id) += login)

  def insert1(email: String, password: String): Future[Int] = {
    println(s"Inserting login with email: $email and password: $password")
    val query = (Logins.map(l => (l.email, l.password)) returning Logins.map(_.id)) += (email, password)
    db.run(query)
  }

  def authenticate(email: String, password: String): Future[Option[User]] =
    db.run(Logins.filter(u => u.email === email && u.password === password).result.headOption)
}