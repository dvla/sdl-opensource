package uk.gov.dvla.auditing.serialization

import scala.xml.NodeSeq

trait XmlSerializable {
  def toXml: NodeSeq
}
