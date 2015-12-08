package controllers

import java.lang.ProcessBuilder.Redirect
import java.util.{Calendar, Date}
import java.util.concurrent.Future
import java.util.logging.Logger
import _root_.play.api.libs.json.JsResult
import _root_.play.api.libs.json.JsSuccess
import _root_.play.api.libs.json.JsValue
import _root_.play.api.libs.json.Reads
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import _root_.play.api.libs.json.Json
import _root_.play.api.libs.json.Json._
import _root_.play.api.libs.ws.WS
import org.joda.time.Period
import play.api._
import play.api.mvc._
import play.api.i18n._
import play.api.libs.json
import play.api.Play.current
import scala.concurrent.duration._
import uk.gov.dvla.sml.domain._
import org.joda.time.DateTime

import scala.util.Random
;

object Application extends Controller {

  val baseUrl: String = "http://localhost:13000/tokens"

  def index() = smlStub("", "")

  def smlStubJson(data: String) = smlStub((Json.parse(data) \ "token").as[String], data)

  def error(path: String) = smlStub("", "")

  def smlStub(token: String, data: String) = Action {
    implicit request => {
      Ok(views.html.smlStub(token, data))
    }
  }

  def changeLanguage(language: String, endpoint: String) = Action { implicit request => {
      Redirect(endpoint)
        .withLang(Lang(code = language))
    }
  }

  def generateSharingCode() = Action { request =>
    val random = new Random(new DateTime().getMillis())
    val dln = request.body.asFormUrlEncoded.get("dln")(0)
    val documentRef = (random.nextInt(90000000) + 10000000).toString()
    create(dln, documentRef) match {
      case token: JsValue =>
        Redirect("/json/" + token)
      case message: String =>
        Redirect("/sml/" + message + "/error")
      case _ =>
        //ERRORS GO HERE!!!
        Redirect("/")

    }
  }

  def create(driverNumber: String, documentRef: String) = {
    val currentTime: DateTime = new DateTime()
    val response = WS.url(baseUrl).post(Json.toJson(Map(
      "documentRef" -> toJson(documentRef),
      "driverNumber" -> toJson(driverNumber),
      "creationDate" -> toJson(currentTime),
      "expiryDate" -> toJson(currentTime.plusDays(1)))
    )) map { response =>
      response.status match {
        case 201 =>
          Json.parse(response.body)
        case 400 =>
          "There was an error creating your token."
      }
    }
    Await.result(response, 10 seconds)
  }

}