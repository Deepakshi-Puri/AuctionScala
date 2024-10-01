package DAO

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject
import models.{UserInfo, LoginInfo}
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

class UserDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val Users = TableQuery[UserInfoTable]

  def insert(user: UserInfo): Future[Unit] = db.run(Users += user).map { _ => () }

  def authenticate(loginInfo: LoginInfo): Future[Boolean] = {
    val query = Users.filter(user => user.username === loginInfo.username && user.password === loginInfo.password)
    db.run(query.result).map(_.nonEmpty)
  }

  def getUserInfo(loginInfo: LoginInfo): Future[Option[(Boolean, String)]] = {
    val query = Users.filter(user => user.username === loginInfo.username && user.password === loginInfo.password)
    db.run(query.result.headOption).map { maybeUser =>
      maybeUser.map(user => (true, user.username))
    }
  }

  private class UserInfoTable(tag: Tag) extends Table[UserInfo](tag, "USERS") {
    def id = column[Option[Long]]("ID", O.PrimaryKey, O.AutoInc)
    def username = column[String]("USERNAME", O.Unique)
    def password = column[String]("PASSWORD")
    def name = column[String]("NAME")
    def email = column[String]("EMAIL")

    def * = (id, username, password, name, email) <> (UserInfo.tupled, UserInfo.unapply)
  }

}
