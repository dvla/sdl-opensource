package uk.gov.dvla.testing.matchers

import java.util.Date

import org.joda.time.DateTime
import org.scalatest.matchers.{MatchResult, Matcher}

trait DateMatchers {

  def beBeforeNow = Matcher { (left: Date) =>
    val date = new DateTime(left)
    MatchResult(
      matches = date.isBeforeNow,
      rawFailureMessage = s"$left was not before now",
      rawNegatedFailureMessage = s"$left is before now"
    )
  }

}

object DateMatchers extends DateMatchers
