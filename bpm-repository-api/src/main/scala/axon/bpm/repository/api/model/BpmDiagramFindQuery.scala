
package axon.bpm.repository.api.model

import play.api.libs.json.{Format, Json}

case class BpmDiagramFindQuery(
    offset: Int = 0,
    size: Int,
    filter: Option[String] = None, //search by filter in bpmDiagram's names, email and phone
    activeOnly: Boolean = true, //search active bpmDiagrams only (by default)
    sortBy: Option[String] = None, //sort results by field provided
    ascending: Boolean = true
)

object BpmDiagramFindQuery {
  implicit val format: Format[BpmDiagramFindQuery] = Json.format
}
