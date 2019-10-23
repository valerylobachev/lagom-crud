package axon.bpm.repository.impl.bpmdiagram

import akka.Done
import axon.bpm.repository.api.model.{BpmDiagram, BpmDiagramId, BpmDiagramUpdate}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer
import play.api.libs.json._
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer.emptySingletonFormat

sealed trait BpmDiagramCommand

object BpmDiagramCommand {
  val serializers = Vector(
    JsonSerializer(Json.format[StoreBpmDiagram]),
    JsonSerializer(Json.format[CreateBpmDiagram]),
    JsonSerializer(Json.format[UpdateBpmDiagram]),
    JsonSerializer(Json.format[DeleteBpmDiagram]),
    JsonSerializer(Json.format[DeactivateBpmDiagram]),
    JsonSerializer(Json.format[ActivateBpmDiagram]),
    JsonSerializer(emptySingletonFormat(GetBpmDiagram))
  )
}

final case class StoreBpmDiagram(entity: BpmDiagramUpdate) extends BpmDiagramCommand with ReplyType[BpmDiagram]
final case class CreateBpmDiagram(entity: BpmDiagramUpdate) extends BpmDiagramCommand with ReplyType[BpmDiagram]
final case class UpdateBpmDiagram(entity: BpmDiagramUpdate) extends BpmDiagramCommand with ReplyType[BpmDiagram]
final case class DeleteBpmDiagram(id: BpmDiagramId) extends BpmDiagramCommand with ReplyType[Done]
final case class DeactivateBpmDiagram(id: BpmDiagramId) extends BpmDiagramCommand with ReplyType[BpmDiagram]
final case class ActivateBpmDiagram(id: BpmDiagramId) extends BpmDiagramCommand with ReplyType[BpmDiagram]
case object GetBpmDiagram extends BpmDiagramCommand with ReplyType[Option[BpmDiagram]]
