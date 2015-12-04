package uk.gov.dvla.sml.domain

import java.util
import java.util.Date

import com.fasterxml.jackson.annotation.JsonIgnore
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.mongodb.morphia.annotations._

import scala.beans.BeanProperty

object LicenceAccessToken {

  def create(documentRef: String, token: String, driverNumber: String, created: DateTime, expired: DateTime): LicenceAccessToken =
    new LicenceAccessToken(documentRef, token, driverNumber, created.toDate, expired.toDate, null, null)
}

@Entity(value = "tokens", noClassnameStored = true)
@Indexes(Array(new Index(name = "doc_token_unq", value = "documentRef,token", unique = true)))
case class LicenceAccessToken(@BeanProperty documentRef: String,
                              @BeanProperty token: String,
                              @BeanProperty driverNumber: String,
                              @BeanProperty created: Date,
                              @BeanProperty expired: Date,
                              @BeanProperty cancelled: Date,
                              @BeanProperty redeemed: Date) {

  @Id
  @BeanProperty
  @Indexed(unique = true, name = "id")
  var id: String = ObjectId.get.toString

  private def this() = this(null, null, null, null, null, null, null)

  @JsonIgnore
  def isValid = !isCancelled && !isRedeemed && !hasExpired

  def hasExpired = expired != null && new DateTime(expired).isBeforeNow

  def isCancelled = cancelled != null

  def isRedeemed = redeemed != null

}
