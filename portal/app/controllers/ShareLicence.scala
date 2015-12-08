package controllers

import play.api.mvc._

import scala.language.postfixOps

import uk.gov.dvla.vdl.report.pdf.{A1AConformance, Generator}
import uk.gov.dvla.vdl.report.JsonReport
import play.libs.Json
import _root_.net.sf.jasperreports.engine.JRParameter
import play.api.Play.current
import play.api.libs.MimeTypes
import play.api.libs.iteratee.Enumerator
import play.api.mvc.{Controller, ResponseHeader, Result}
import play.api.i18n.Lang
import play.api.Logger
import org.joda.time.LocalDate
import play.api.Play
import java.util.Date

trait ShareLicenceController extends Controller {
  val pDFGenerator: Generator

  def generateSharingPDF(data: String)(implicit lang : Lang) = Action {
    implicit request => {
      val reportName = "sample"

      val model = data.replaceAll(""""""", "'") //converts " chars to ' chars in the model string

      val fileName = s"${reportName}.pdf"

      pDFGenerator.compile(reportName, Play.resourceAsStream(s"reports/$reportName.jrxml").get)

      val pdfByteArray = pDFGenerator.generate(
        new JsonReport(reportName, model,
          "GENERATED_BY" -> "Driver and Vehicle Licensing Agency",
          "GENERATION_DATE" -> new Date(),
          "IMAGES_DIR" -> "reports/img"
          //JRParameter.REPORT_LOCALE -> lang.toLocale
        ),
        A1AConformance
      )

      Result(
        header = ResponseHeader(200, Map(
          CONTENT_LENGTH -> pdfByteArray.length.toString,
          CONTENT_TYPE -> MimeTypes.forExtension("pdf").get,
          CONTENT_DISPOSITION -> s"attachment; filename=$fileName"
        )),
        body = Enumerator(pdfByteArray)
      )
    }
  }
}

object ShareLicenceController extends ShareLicenceController {
  lazy val pDFGenerator = new Generator
}


