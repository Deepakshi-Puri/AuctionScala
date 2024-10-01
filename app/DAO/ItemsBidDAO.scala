package DAO

import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import models.ItemsBid
import slick.lifted.ProvenShape

import scala.concurrent.{ExecutionContext, Future}

class ItemsBidDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val Bids = TableQuery[ItemsBidTable]

  def insert(bid: ItemsBid): Future[Unit] = db.run(Bids += bid).map { _ => () }

  def getAllHighestBids: Future[Map[Long, Option[ItemsBid]]] = {
    val allListingIdsQuery = Bids.map(_.listingID).distinct.result
    val listingIdsFuture: Future[Seq[Long]] = db.run(allListingIdsQuery)

    listingIdsFuture.flatMap { listingIds =>
      val highestBidsFutures: Seq[Future[(Long, Option[ItemsBid])]] = listingIds.map { listingId =>
        getHighestBidForListing(listingId).map(highestBid => (listingId, highestBid))
      }
      Future.sequence(highestBidsFutures).map(_.toMap)
    }
  }

  private def getHighestBidForListing(listingId: Long): Future[Option[ItemsBid]] = {
    val query = Bids.filter(_.listingID === listingId).sortBy(_.bid.desc).result.headOption
    db.run(query)
  }

  class ItemsBidTable(tag: Tag) extends Table[ItemsBid](tag, "ITEMS_BID") {
    def listingID: Rep[Long] = column[Long]("LISTING_ID")
    def username: Rep[String] = column[String]("USERNAME")
    def bid: Rep[Int] = column[Int]("BID")

    def * : ProvenShape[ItemsBid] = (listingID, username, bid) <> (ItemsBid.tupled, ItemsBid.unapply)
  }
}