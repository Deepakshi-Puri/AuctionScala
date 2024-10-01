package DAO

import models.SellFormData
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SellItemDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, val itemsBidDAO: ItemsBidDAO)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._
  import itemsBidDAO.Bids

  private val Items = TableQuery[SellingItemsTable]

  def insert(item: SellFormData): Future[Unit] = db.run(Items += item).map(_ => ())

  def getAllSellFormData: Future[Seq[SellFormData]] = db.run(Items.result)

  def getAllUsernames: Future[Seq[String]] = {
    val query = Items.map(_.username).distinct.result
    db.run(query)
  }

  def getAllCategories: Future[Seq[String]] = {
    val query = Items.map(_.category).distinct.result
    db.run(query)
  }

  def getAllSellFormDataByUsername(currentUser: String): Future[Seq[SellFormData]] = {
    val query = Items.filter(_.username === currentUser).result
    db.run(query)
  }

  def getAllSellFormDataByCategory(selectedCategory: String): Future[Seq[SellFormData]] = {
    val query = Items.filter(_.category === selectedCategory).result
    db.run(query)
  }

  def deletePost(productId: Long): Future[Unit] = {
    val deleteBidsAction = Bids.filter(_.listingID === productId).delete
    val deleteItemAction = Items.filter(_.id === productId).delete

    db.run(deleteBidsAction.flatMap(_ => deleteItemAction)).map(_ => ())
  }

  def updateProductTime(listingID: Long, newTime: String): Future[Unit] =
    db.run(Items.filter(_.id === listingID).map(_.biddingEndDate).update(newTime)).map(_ => ())

  private class SellingItemsTable(tag: Tag) extends Table[SellFormData](tag, "SELL_FORM_DATA") {

    def id = column[Long]("LISTING_ID", O.PrimaryKey, O.AutoInc)
    def username = column[String]("USERNAME")
    private def title = column[String]("TITLE")
    private def description = column[String]("DESCRIPTION")
    private def featureA = column[String]("FEATURE_A")
    private def featureB = column[String]("FEATURE_B")
    private def featureC = column[String]("FEATURE_C")
    private def featureD = column[String]("FEATURE_D")
    private def featureE = column[String]("FEATURE_E")
    def biddingEndDate = column[String]("BIDDING_END_DATE")
    def category = column[String]("CATEGORY")

    def * = (id.?, username, title, description, featureA, featureB, featureC, featureD, featureE, biddingEndDate, category) <> (SellFormData.tupled, SellFormData.unapply)
  }
}
