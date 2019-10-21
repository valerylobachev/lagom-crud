package axon.bpm.repository.impl.bpmdiagram

import axon.bpm.repository.api.model.BpmDiagram
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer
import play.api.libs.json.{Format, Json}

object BpmDiagramState {
  val empty = BpmDiagramState(None)

  implicit val format: Format[BpmDiagramState] = Json.format[BpmDiagramState]

  val serializers = Vector(
    JsonSerializer[BpmDiagram],
    JsonSerializer[BpmDiagramState]
  )
}

final case class BpmDiagramState(entity: Option[BpmDiagram]) {

  def isEmpty = entity.isEmpty

  def isActive = entity.exists(_.active)

  def processEvent: PartialFunction[BpmDiagramEvent, BpmDiagramState] = {
    case BpmDiagramStored(newEntity)         => BpmDiagramState(Some(newEntity))
    case BpmDiagramCreated(newEntity)        => BpmDiagramState(Some(newEntity))
    case BpmDiagramUpdated(newEntity)        => BpmDiagramState(Some(newEntity))
    case BpmDiagramDeleted(_)                => BpmDiagramState(None)
    case BpmDiagramDeactivated(_, updatedAt) => BpmDiagramState(entity.map(_.copy(active = false, updatedAt = updatedAt)))
    case BpmDiagramActivated(_, updatedAt)   => BpmDiagramState(entity.map(_.copy(active = true, updatedAt = updatedAt)))
  }
}
