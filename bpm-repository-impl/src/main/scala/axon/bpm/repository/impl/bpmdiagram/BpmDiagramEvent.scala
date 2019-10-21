package axon.bpm.repository.impl.bpmdiagram

import java.time.OffsetDateTime

import axon.bpm.repository.api.model.{BpmDiagram, BpmDiagramId}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer
import play.api.libs.json.Json

sealed trait BpmDiagramEvent extends AggregateEvent[BpmDiagramEvent] {
  override def aggregateTag = BpmDiagramEvent.Tag
}

object BpmDiagramEvent {
  val NumShards = 4
  val Tag = AggregateEventTag.sharded[BpmDiagramEvent](NumShards)

  val serializers = Vector(
    JsonSerializer(Json.format[BpmDiagramStored]),
    JsonSerializer(Json.format[BpmDiagramCreated]),
    JsonSerializer(Json.format[BpmDiagramUpdated]),
    JsonSerializer(Json.format[BpmDiagramDeleted]),
    JsonSerializer(Json.format[BpmDiagramDeactivated]),
    JsonSerializer(Json.format[BpmDiagramActivated])
  )

}

final case class BpmDiagramStored(bpmDiagram: BpmDiagram) extends BpmDiagramEvent
final case class BpmDiagramCreated(bpmDiagram: BpmDiagram) extends BpmDiagramEvent
final case class BpmDiagramUpdated(bpmDiagram: BpmDiagram) extends BpmDiagramEvent
final case class BpmDiagramDeleted(id: BpmDiagramId) extends BpmDiagramEvent
final case class BpmDiagramDeactivated(id: BpmDiagramId, updatedAt: OffsetDateTime) extends BpmDiagramEvent
final case class BpmDiagramActivated(id: BpmDiagramId, updatedAt: OffsetDateTime) extends BpmDiagramEvent
