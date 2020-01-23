/*
 * Copyright 2018 Valery Lobachev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package axon.bpm.repository.typed_impl.bpmdiagram

import java.time.OffsetDateTime

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl._
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.RetentionCriteria
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.scaladsl.ReplyEffect
import com.lightbend.lagom.scaladsl.persistence.AggregateEvent
import com.lightbend.lagom.scaladsl.persistence.AggregateEventShards
import com.lightbend.lagom.scaladsl.persistence.AggregateEventTag
import com.lightbend.lagom.scaladsl.persistence.AggregateEventTagger
import com.lightbend.lagom.scaladsl.persistence.AkkaTaggerAdapter
import play.api.libs.json.Format
import play.api.libs.json._

import axon.bpm.repository.api.model.{BpmDiagram, BpmDiagramId, BpmDiagramUpdate}

object BpmDiagramEntity {
  trait CommandSerializable

  sealed trait Command extends CommandSerializable
  final case class StoreBpmDiagram(entity: BpmDiagram, replyTo: ActorRef[Confirmation]) extends Command
  final case class CreateBpmDiagram(entity: BpmDiagramUpdate, replyTo: ActorRef[Confirmation]) extends Command
  final case class UpdateBpmDiagram(entity: BpmDiagramUpdate, replyTo: ActorRef[Confirmation]) extends Command
  final case class DeleteBpmDiagram(id: BpmDiagramId, replyTo: ActorRef[Confirmation]) extends Command
  final case class DeactivateBpmDiagram(id: BpmDiagramId, replyTo: ActorRef[Confirmation]) extends Command
  final case class ActivateBpmDiagram(id: BpmDiagramId, replyTo: ActorRef[Confirmation]) extends Command
  final case class GetBpmDiagram(id: BpmDiagramId, replyTo: ActorRef[Confirmation]) extends Command

  sealed trait Confirmation
  final case class Success(entity: BpmDiagram) extends Confirmation
  final case object DeleteSuccess extends Confirmation
  final case object NotFound extends Confirmation
  final case object AlreadyExist extends Confirmation
  final case object AlreadyActive extends Confirmation
  final case object InDeactivatedState extends Confirmation

  implicit val confirmationSuccessFormat: Format[Success] = Json.format
  implicit val confirmationDeleteSuccessFormat: Format[DeleteSuccess.type] = Json.format
  implicit val confirmationNotFoundFormat: Format[NotFound.type] = Json.format
  implicit val confirmationAlreadyExistFormat: Format[AlreadyExist.type] = Json.format
  implicit val confirmationAlreadyActiveFormat: Format[AlreadyActive.type] = Json.format
  implicit val confirmationInDeactivatedStateFormat: Format[InDeactivatedState.type] = Json.format

  implicit val confirmationFormat: OFormat[Confirmation] = {
    implicit val config = JsonConfiguration(discriminator = "confirmationType", typeNaming = JsonNaming { fullName =>
      fullName.split("\\.").toSeq.last
    })
    Json.format[Confirmation]
  }

  sealed trait Event extends AggregateEvent[Event] {
    override def aggregateTag: AggregateEventTagger[Event] = Event.Tag
  }

  object Event {
    val Tag: AggregateEventShards[Event] = AggregateEventTag.sharded[Event](numShards = 10)
  }

  final case class BpmDiagramStored(bpmDiagram: BpmDiagram) extends Event
  final case class BpmDiagramCreated(bpmDiagram: BpmDiagram) extends Event
  final case class BpmDiagramUpdated(bpmDiagram: BpmDiagram) extends Event
  final case class BpmDiagramDeleted(id: BpmDiagramId) extends Event
  final case class BpmDiagramDeactivated(id: BpmDiagramId, updatedAt: OffsetDateTime) extends Event
  final case class BpmDiagramActivated(id: BpmDiagramId, updatedAt: OffsetDateTime) extends Event

  implicit val bpmDiagramStoredFormat: Format[BpmDiagramStored] = Json.format
  implicit val bpmDiagramCreatedFormat: Format[BpmDiagramCreated] = Json.format
  implicit val bpmDiagramUpdatedFormat: Format[BpmDiagramUpdated] = Json.format
  implicit val bpmDiagramDeletedFormat: Format[BpmDiagramDeleted] = Json.format
  implicit val bpmDiagramDeactivatedFormat: Format[BpmDiagramDeactivated] = Json.format
  implicit val bpmDiagramActivatedFormat: Format[BpmDiagramActivated] = Json.format

  val empty = BpmDiagramEntity(None)
  val typeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("BpmDiagram")

  def apply(persistenceId: PersistenceId): EventSourcedBehavior[Command, Event, BpmDiagramEntity] = {
    EventSourcedBehavior
      .withEnforcedReplies[Command, Event, BpmDiagramEntity](
        persistenceId = persistenceId,
        emptyState = BpmDiagramEntity.empty,
        commandHandler = (cart, cmd) => cart.applyCommand(cmd),
        eventHandler = (cart, evt) => cart.applyEvent(evt)
      )
  }

  def apply(entityContext: EntityContext[Command]): Behavior[Command] =
    apply(PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId))
      .withTagger(AkkaTaggerAdapter.fromLagom(entityContext, Event.Tag))
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 2))

  implicit val bpmDiagramEntityFormat: Format[BpmDiagramEntity] = Json.format
}

final case class BpmDiagramEntity(maybeEntity: Option[BpmDiagram]) {
  import BpmDiagramEntity._

  def applyCommand(cmd: Command): ReplyEffect[Event, BpmDiagramEntity] = {
    cmd match {
      case StoreBpmDiagram(e, replyTo)       => onStore(e, replyTo)
      case CreateBpmDiagram(e, replyTo)      => onCreate(e, replyTo)
      case UpdateBpmDiagram(e, replyTo)      => onUpdate(e, replyTo)
      case DeleteBpmDiagram(id, replyTo)     => onDelete(id, replyTo)
      case ActivateBpmDiagram(id, replyTo)   => onActivate(id, replyTo)
      case DeactivateBpmDiagram(id, replyTo) => onDeactivate(id, replyTo)
      case GetBpmDiagram(id, replyTo)        => onGet(id, replyTo)
    }
  }

  def onStore(e: BpmDiagram, replyTo: ActorRef[Confirmation]): ReplyEffect[Event, BpmDiagramEntity] = {
    Effect
      .persist(BpmDiagramStored(e))
      .thenReply(replyTo)(_ => Success(e))
  }

  def onCreate(e: BpmDiagramUpdate, replyTo: ActorRef[Confirmation]): ReplyEffect[Event, BpmDiagramEntity] = {
    if (maybeEntity.isDefined) {
      Effect.reply(replyTo)(AlreadyExist)
    } else {
      println(s"entity created: ${e.id}")
      val newEntity = BpmDiagram(e)
      Effect
        .persist(BpmDiagramCreated(newEntity))
        .thenReply(replyTo)(_ => Success(newEntity))
    }
  }

  def onUpdate(e: BpmDiagramUpdate, replyTo: ActorRef[Confirmation]): ReplyEffect[Event, BpmDiagramEntity] = {
    if (maybeEntity.isEmpty) {
      Effect.reply(replyTo)(NotFound)
    } else if (maybeEntity.get.active) {
      val newEntity = BpmDiagram(e)
      Effect
        .persist(BpmDiagramUpdated(newEntity))
        .thenReply(replyTo)(_ => Success(newEntity))
    } else {
      Effect.reply(replyTo)(InDeactivatedState)
    }
  }

  def onDelete(id: BpmDiagramId, replyTo: ActorRef[Confirmation]): ReplyEffect[Event, BpmDiagramEntity] = {
    if (maybeEntity.isDefined) {
      Effect
        .persist(BpmDiagramDeleted(id))
        .thenReply(replyTo)(_ => DeleteSuccess)
    } else {
      Effect.reply(replyTo)(NotFound)
    }
  }

  def onDeactivate(id: BpmDiagramId, replyTo: ActorRef[Confirmation]): ReplyEffect[Event, BpmDiagramEntity] = {
    if (maybeEntity.isEmpty) {
      Effect.reply(replyTo)(NotFound)
    } else if (maybeEntity.get.active) {
      val newEntity = maybeEntity.get.copy(active = false, updatedAt = OffsetDateTime.now)
      Effect
        .persist(BpmDiagramDeactivated(id, newEntity.updatedAt))
        .thenReply(replyTo)(_ => Success(newEntity))
    } else {
      Effect.reply(replyTo)(InDeactivatedState)
    }
  }

  def onActivate(id: BpmDiagramId, replyTo: ActorRef[Confirmation]): ReplyEffect[Event, BpmDiagramEntity] = {
    if (maybeEntity.isEmpty) {
      Effect.reply(replyTo)(NotFound)
    } else if (maybeEntity.get.active) {
      Effect.reply(replyTo)(AlreadyActive)
    } else {
      val newEntity = maybeEntity.get.copy(active = true, updatedAt = OffsetDateTime.now)
      Effect
        .persist(BpmDiagramActivated(id, newEntity.updatedAt))
        .thenReply(replyTo)(_ => Success(newEntity))
    }
  }

  def onGet(id: BpmDiagramId, replyTo: ActorRef[Confirmation]): ReplyEffect[Event, BpmDiagramEntity] = {
    if (maybeEntity.isDefined) {
      Effect.reply(replyTo)(Success(maybeEntity.get))
    } else {
      Effect.reply(replyTo)(NotFound)
    }
  }

  def applyEvent(evt: Event): BpmDiagramEntity = {
    evt match {
      case BpmDiagramStored(newEntity)         => BpmDiagramEntity(Some(newEntity))
      case BpmDiagramCreated(newEntity)        => BpmDiagramEntity(Some(newEntity))
      case BpmDiagramUpdated(newEntity)        => BpmDiagramEntity(Some(newEntity))
      case BpmDiagramDeleted(_)                => BpmDiagramEntity(None)
      case BpmDiagramDeactivated(_, updatedAt) => BpmDiagramEntity(maybeEntity.map(_.copy(active = false, updatedAt = updatedAt)))
      case BpmDiagramActivated(_, updatedAt)   => BpmDiagramEntity(maybeEntity.map(_.copy(active = true, updatedAt = updatedAt)))
    }
  }

}
