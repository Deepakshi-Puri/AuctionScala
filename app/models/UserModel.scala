package models

case class LoginInfo(username: String, password: String)

case class UserInfo(id: Option[Long], username: String, password: String, name: String, email: String)

case class ItemsBid(listingID: Long, username: String, bidPrice: Int)

case class SellFormData(listingID: Option[Long], username: String,
                        title: String, description: String,
                        featureA: String, featureB: String,
                        featureC: String, featureD: String,
                        featureE: String, biddingEndDate: String, category: String)
