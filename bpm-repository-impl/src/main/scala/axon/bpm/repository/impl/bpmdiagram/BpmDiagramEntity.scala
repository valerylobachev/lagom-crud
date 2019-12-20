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

package axon.bpm.repository.impl.bpmdiagram

import java.time.OffsetDateTime

import akka.Done
import axon.bpm.repository.api.model.{BpmDiagram, BpmDiagramAlreadyActive, BpmDiagramAlreadyExist, BpmDiagramInDeactivatedState, BpmDiagramNotFound}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

class BpmDiagramEntity extends PersistentEntity {
  override type Command = BpmDiagramCommand
  override type Event = BpmDiagramEvent
  override type State = BpmDiagramState
  override def initialState: State = BpmDiagramState.empty

  override def behavior: Behavior = {
    case state if state.isEmpty                     => initial
    case state if !state.isEmpty && state.isActive  => active
    case state if !state.isEmpty && !state.isActive => deactivated
  }

  private val storeCommand: Actions = {
    Actions()
      .onCommand[StoreBpmDiagram, BpmDiagram] {
        case (StoreBpmDiagram(entity), ctx, _) =>
          val newEntity = BpmDiagram(entity)
          ctx.thenPersist(BpmDiagramStored(newEntity))(_ => ctx.reply(newEntity))
      }
  }

  private val getStateCommand: Actions = {
    Actions()
      .onReadOnlyCommand[GetBpmDiagram.type, Option[BpmDiagram]] {
        case (GetBpmDiagram, ctx, state) => ctx.reply(state.entity)
      }
  }

  private val processEvent: Actions = {
    Actions()
      .onEvent {
        case (event, state) => state.processEvent(event)
      }
  }

  private val initial: Actions = {
    Actions()
      .onCommand[CreateBpmDiagram, BpmDiagram] {
        case (CreateBpmDiagram(entity), ctx, _) =>
          println(s"entity created: ${entity.id}")
          val newEntity = BpmDiagram(entity)
          ctx.thenPersist(BpmDiagramCreated(newEntity))(_ => {
            println(s"entity event posted: ${entity.id}")
            ctx.reply(newEntity)
          })
      }
      .onReadOnlyCommand[UpdateBpmDiagram, BpmDiagram] {
        case (UpdateBpmDiagram(entity), ctx, _) => ctx.commandFailed(BpmDiagramNotFound(entity.id))
      }
      .onReadOnlyCommand[DeleteBpmDiagram, Done] {
        case (DeleteBpmDiagram(id), ctx, _) => ctx.commandFailed(BpmDiagramNotFound(id))
      }
      .onReadOnlyCommand[DeactivateBpmDiagram, BpmDiagram] {
        case (DeactivateBpmDiagram(id), ctx, _) => ctx.commandFailed(BpmDiagramNotFound(id))
      }
      .onReadOnlyCommand[ActivateBpmDiagram, BpmDiagram] {
        case (ActivateBpmDiagram(id), ctx, _) => ctx.commandFailed(BpmDiagramNotFound(id))
      }
      .orElse(storeCommand)
      .orElse(processEvent)
      .orElse(getStateCommand)
  }

  private val active = {
    Actions()
      .onReadOnlyCommand[CreateBpmDiagram, BpmDiagram] {
        case (CreateBpmDiagram(entity), ctx, _) =>
          ctx.commandFailed(BpmDiagramAlreadyExist(entity.id))
      }
      .onCommand[UpdateBpmDiagram, BpmDiagram] {
        case (UpdateBpmDiagram(entity), ctx, _) =>
          val newEntity = BpmDiagram(entity)
          ctx.thenPersist(BpmDiagramUpdated(newEntity))(_ => ctx.reply(newEntity))
      }
      .onCommand[DeleteBpmDiagram, Done] {
        case (DeleteBpmDiagram(id), ctx, _) =>
          ctx.thenPersist(BpmDiagramDeleted(id))(_ => ctx.reply(Done))
      }
      .onCommand[DeactivateBpmDiagram, BpmDiagram] {
        case (DeactivateBpmDiagram(id), ctx, state) =>
          val newEntity = state.entity.get.copy(active = false, updatedAt = OffsetDateTime.now)
          ctx.thenPersist(BpmDiagramDeactivated(id, newEntity.updatedAt))(_ => ctx.reply(newEntity))
      }
      .onReadOnlyCommand[ActivateBpmDiagram, BpmDiagram] {
        case (ActivateBpmDiagram(id), ctx, _) =>
          ctx.commandFailed(BpmDiagramAlreadyActive(id))
      }
      .orElse(storeCommand)
      .orElse(processEvent)
      .orElse(getStateCommand)
  }

  private val deactivated: Actions = {
    Actions()
      .onReadOnlyCommand[CreateBpmDiagram, BpmDiagram] {
        case (CreateBpmDiagram(entity), ctx, _) =>
          ctx.commandFailed(BpmDiagramInDeactivatedState(entity.id))
      }
      .onReadOnlyCommand[UpdateBpmDiagram, BpmDiagram] {
        case (UpdateBpmDiagram(entity), ctx, _) =>
          ctx.commandFailed(BpmDiagramInDeactivatedState(entity.id))
      }
      .onCommand[DeleteBpmDiagram, Done] {
        case (DeleteBpmDiagram(id), ctx, _) =>
          ctx.thenPersist(BpmDiagramDeleted(id))(_ => ctx.reply(Done))
      }
      .onReadOnlyCommand[DeactivateBpmDiagram, BpmDiagram] {
        case (DeactivateBpmDiagram(id), ctx, _) => ctx.commandFailed(BpmDiagramInDeactivatedState(id))
      }
      .onCommand[ActivateBpmDiagram, BpmDiagram] {
        case (ActivateBpmDiagram(id), ctx, state) =>
          val newEntity = state.entity.get.copy(active = true, updatedAt = OffsetDateTime.now)
          ctx.thenPersist(BpmDiagramActivated(id, newEntity.updatedAt))(_ => ctx.reply(newEntity))
      }
      .orElse(storeCommand)
      .orElse(processEvent)
      .orElse(getStateCommand)
  }

}
