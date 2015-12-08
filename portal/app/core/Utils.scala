package core

import java.net.InetAddress
import java.text.{DateFormatSymbols, DecimalFormat}
import java.util.Locale
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import com.google.common.net.InetAddresses
import org.joda.time.format.{DateTimeFormat, PeriodFormat}
import org.joda.time.{DateTime, Period}
import org.slf4j.LoggerFactory
import play.api.Play
import play.api.i18n._

import scala.Predef._
import scala.collection.JavaConverters._
import scala.util.Try

object Utils {

  val welshSymbols = {
    val symbols = DateFormatSymbols.getInstance(new Locale("cy"))

    symbols.setMonths(Array(
      Messages("wMonths.long.1"),
      Messages("wMonths.long.2"),
      Messages("wMonths.long.3"),
      Messages("wMonths.long.4"),
      Messages("wMonths.long.5"),
      Messages("wMonths.long.6"),
      Messages("wMonths.long.7"),
      Messages("wMonths.long.8"),
      Messages("wMonths.long.9"),
      Messages("wMonths.long.10"),
      Messages("wMonths.long.11"),
      Messages("wMonths.long.12")
    ))

    symbols.setShortMonths(Array(
      Messages("wMonths.short.1"),
      Messages("wMonths.short.2"),
      Messages("wMonths.short.3"),
      Messages("wMonths.short.4"),
      Messages("wMonths.short.5"),
      Messages("wMonths.short.6"),
      Messages("wMonths.short.7"),
      Messages("wMonths.short.8"),
      Messages("wMonths.short.9"),
      Messages("wMonths.short.10"),
      Messages("wMonths.short.11"),
      Messages("wMonths.short.12")
    ))

    symbols
  }

  private val shortMonthFormatter = DateTimeFormat.forPattern("d MMM yyyy")
  private val longMonthFormatter = DateTimeFormat.forPattern("d MMMM yyyy")

  def formatDate(dateTime: DateTime) = {
    shortMonthFormatter.print(dateTime)
  }

  def formatDateWithLongMonth(dateTime: DateTime)(implicit lang: Lang) = {
    val longMonth = longMonthFormatter.print(dateTime)
    val formatter = DateTimeFormat.forPattern("MMMM")
    val month = formatter.print(dateTime)
    val welshMonths = Map(Messages("eMonths.1") -> Messages("wMonths.long.1"), Messages("eMonths.2") -> Messages("wMonths.long.2"), Messages("eMonths.3") -> Messages("wMonths.long.3"), Messages("eMonths.4") -> Messages("wMonths.long.4"), Messages("eMonths.5") -> Messages("wMonths.long.5"), Messages("eMonths.6") -> Messages("wMonths.long.6"), Messages("eMonths.7") -> Messages("wMonths.long.7"), Messages("eMonths.8") -> Messages("wMonths.long.8"), Messages("eMonths.9") -> Messages("wMonths.long.9"), Messages("eMonths.10") -> Messages("wMonths.long.10"), Messages("eMonths.11") -> Messages("wMonths.long.11"), Messages("eMonths.12") -> Messages("wMonths.long.12"))
    if (lang.code == "cy") {
      longMonth.replaceAll(month, welshMonths(month))
    } else {
      longMonth
    }
  }

  def formatDateWithShortMonth(dateTime: DateTime) (implicit lang: Lang) = {
    val longMonth = longMonthFormatter.print(dateTime)
    val fmt = DateTimeFormat.forPattern("MMMM")
    val mnth = fmt.print(dateTime)
    val welshMonths = Map(Messages("eMonths.1") -> Messages("wMonths.short.1"), Messages("eMonths.2") -> Messages("wMonths.short.2"), Messages("eMonths.3") -> Messages("wMonths.short.3"), Messages("eMonths.4") -> Messages("wMonths.short.4"), Messages("eMonths.5") -> Messages("wMonths.short.5"), Messages("eMonths.6") -> Messages("wMonths.short.6"), Messages("eMonths.7") -> Messages("wMonths.short.7"), Messages("eMonths.8") -> Messages("wMonths.short.8"), Messages("eMonths.9") -> Messages("wMonths.short.9"), Messages("eMonths.10") -> Messages("wMonths.short.10"), Messages("eMonths.11") -> Messages("wMonths.short.11"), Messages("eMonths.12") -> Messages("wMonths.short.12"))
    if (lang.code == "cy") {
      longMonth.replaceAll(mnth, welshMonths(mnth))
    } else {
      formatDate(dateTime)
    }
  }

  def formatNumberToCurrencyString(num: Number) = {
    val formatter = new DecimalFormat("#,###,##0.00")
    formatter.format(num)
  }

  def dropTrailingSlash(str: String) = if (str.last == '/') str.dropRight(1) else str

  def formatBooleanToYesNo(bool: Boolean) = {
    val boolMap = Map(true -> Messages("views.common.yes"), false -> Messages("views.common.no"))
    boolMap.get(bool)
  }

  def formatIsoDurationAsString(duration: String): String =
    Try {
      val interim = Period.parse(duration)
      PeriodFormat.getDefault.print(interim)
    } getOrElse ""

  def formatIsoDurationAsStringWithZeroAs(zero: String)(duration: String): String =
    Try {
      val interim = Period.parse(duration)
      if (interim == Period.ZERO)
        zero
      else
        PeriodFormat.getDefault.print(interim)
    } getOrElse ""

  def classToMap(cc: AnyRef) =
    (Map[String, String]() /: cc.getClass.getDeclaredFields) {
      (a, f) =>
        f.setAccessible(true)
        a + (f.getName -> f.get(cc).toString)
    }

  //imho prettier
  def getConfigString(property: String) = Play.current.configuration.getString(property)

  def getConfigStringEx(property: String) = Play.current.configuration.getString(property).orElse(throw ConfigurationMissingException(property)).get

  def getConfigInt(property: String) = Play.current.configuration.getInt(property)

  def getConfigIntEx(property: String) = Play.current.configuration.getInt(property).orElse(throw ConfigurationMissingException(property)).get

  def getConfigMilliseconds(property: String) = Play.current.configuration.getMilliseconds(property)

  def getConfigMillisecondsEx(property: String) = Play.current.configuration.getMilliseconds(property).orElse(throw ConfigurationMissingException(property)).get

  def getConfigBoolean(property: String) = Play.current.configuration.getBoolean(property)

  def getConfigBooleanEx(property: String) = Play.current.configuration.getBoolean(property).orElse(throw ConfigurationMissingException(property)).get

  def getConfigURL(property: String): Option[String] = getConfigString(property).map(dropTrailingSlash)

  def mergeMap[A, B](ms: List[Map[A, B]])(f: (B, B) => B): Map[A, B] =
    (Map[A, B]() /: (for (m <- ms; kv <- m) yield kv)) {
      (a, kv) =>
        a + (if (a.contains(kv._1)) kv._1 -> f(a(kv._1), kv._2) else kv)
    }

  def getConfiglist[T](property: String): List[T] =
    Play.current.configuration.getList(property) match {
      case Some(list) => list.unwrapped.asScala.toList.asInstanceOf[List[T]]
      case _ => Nil
    }

  def decode64(str: String) = {
    val decoder = new sun.misc.BASE64Decoder()
    new String(decoder.decodeBuffer(str))
  }

  def encode64(str: String) = {
    val decoder = new sun.misc.BASE64Encoder()
    decoder.encode(str.getBytes)
  }

  def formatPostCode(postCode: String) = {
    if (!postCode.isEmpty) {
      postCode.replace(postCode.substring(postCode.length() - 3),
        " " + postCode.substring(postCode.length() - 3))
    } else postCode
  }

  def ipToLong(ip: String): Long = InetAddresses.coerceToInteger(InetAddress.getByName(ip))

  def ipInRange(range: (String, String), ip: String): Boolean = {
    ipToLong(range._1) <= ipToLong(ip) && ipToLong(ip) <= ipToLong(range._2)
  }

  def isAllowed(ip: String) = {
    val ipRegex = "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b".r
    val verifyAllowedIPMatchers: List[(String => Boolean)] = getConfiglist[String]("verify.allowedIP").map(ip => {
      val matches = ipRegex.findAllIn(ip).toList
      matches match {
        case head :: Nil => (t: String) => ipToLong(t) == ipToLong(head)
        case head :: tail => (t: String) => ipInRange((head, tail.head), t)
        case _ => throw new IllegalArgumentException
      }
    })

    verifyAllowedIPMatchers.exists(f => f(ip))
  }

  def timestamp = DateTime.now().getMillis

  object MapSerializer {
    val delimiter = "//"

    def serializeMap(map: Map[String, String]) =
      map mkString delimiter

    def deserializeMap(s: String): Map[String, String] = {
      s.split(delimiter).toSet.map {
        set: String =>
          val tupleAsSet = set.split(" -> ").toSet
          if (tupleAsSet.size == 1)
            tupleAsSet.head -> ""
          else
            tupleAsSet.head -> tupleAsSet.last
      }.groupBy(_._1)
        .map {
        case (k, v) => (k, v.map(_._2).head)
      }
    }
  }

  case class ConfigurationMissingException(missingField: String) extends Exception(missingField)

}

trait Logging {

  val logger = LoggerFactory.getLogger(this.getClass)
}
