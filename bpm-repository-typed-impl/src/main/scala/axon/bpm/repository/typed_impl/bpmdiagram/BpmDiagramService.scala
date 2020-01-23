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

import akka.Done
import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.stream.Materializer

import scala.concurrent.duration._
import akka.util.Timeout
import annette.shared.elastic.FindResult
import axon.bpm.repository.api.model.{
  BpmDiagram,
  BpmDiagramAlreadyActive,
  BpmDiagramAlreadyExist,
  BpmDiagramFindQuery,
  BpmDiagramId,
  BpmDiagramInDeactivatedState,
  BpmDiagramNotFound,
  BpmDiagramUpdate
}
import axon.bpm.repository.typed_impl.bpmdiagram.BpmDiagramEntity._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.{ExecutionContext, Future}
import scala.collection._

class BpmDiagramService(
    clusterSharding: ClusterSharding,
    registry: PersistentEntityRegistry,
    system: ActorSystem,
    repository: BpmDiagramRepository,
    elastic: BpmDiagramElastic
)(
    implicit ec: ExecutionContext,
    mat: Materializer
) {

  implicit val timeout = Timeout(5.seconds)

  private def refFor(id: BpmDiagramId): EntityRef[Command] = {
    clusterSharding.entityRefFor(BpmDiagramEntity.typeKey, id)
  }

  private def confirmationToResult(id: BpmDiagramId, confirmation: Confirmation): BpmDiagram =
    confirmation match {
      case Success(entity)    => entity
      case NotFound           => throw BpmDiagramNotFound(id)
      case AlreadyActive      => throw BpmDiagramAlreadyActive(id)
      case AlreadyExist       => throw BpmDiagramAlreadyExist(id)
      case InDeactivatedState => throw BpmDiagramInDeactivatedState(id)
      case _                  => throw new RuntimeException("Match fail")
    }

  def storeBpmDiagram(bpmDiagram: BpmDiagram): Future[BpmDiagram] = {
    refFor(bpmDiagram.id)
      .ask[Confirmation](reply => StoreBpmDiagram(bpmDiagram, reply))
      .map(res => confirmationToResult(bpmDiagram.id, res))
  }

  def createBpmDiagram(bpmDiagram: BpmDiagramUpdate): Future[BpmDiagram] = {
    refFor(bpmDiagram.id)
      .ask[Confirmation](reply => CreateBpmDiagram(bpmDiagram, reply))
      .map(res => confirmationToResult(bpmDiagram.id, res))
  }

  def updateBpmDiagram(bpmDiagram: BpmDiagramUpdate): Future[BpmDiagram] = {
    refFor(bpmDiagram.id)
      .ask[Confirmation](reply => UpdateBpmDiagram(bpmDiagram, reply))
      .map(res => confirmationToResult(bpmDiagram.id, res))
  }

  def deactivateBpmDiagram(id: BpmDiagramId): Future[BpmDiagram] = {
    refFor(id)
      .ask[Confirmation](reply => DeactivateBpmDiagram(id, reply))
      .map(res => confirmationToResult(id, res))
  }

  def activateBpmDiagram(id: BpmDiagramId): Future[BpmDiagram] = {
    refFor(id)
      .ask[Confirmation](reply => ActivateBpmDiagram(id, reply))
      .map(res => confirmationToResult(id, res))
  }

  def deleteBpmDiagram(id: BpmDiagramId): Future[Done] = {
    refFor(id)
      .ask[Confirmation](reply => DeleteBpmDiagram(id, reply))
      .map {
        case DeleteSuccess => Done
        case NotFound      => throw BpmDiagramNotFound(id)
        case _             => throw new RuntimeException("Match fail")
      }
  }

  def getBpmDiagram(id: BpmDiagramId, readSide: Boolean = true): Future[BpmDiagram] = {
    for {
      maybeBpmDiagram <- if (readSide) {
        repository.getBpmDiagram(id)
      } else {
        getBpmDiagram(id)
      }
    } yield {
      maybeBpmDiagram match {
        case Some(bpmDiagram) => bpmDiagram
        case None             => throw BpmDiagramNotFound(id)
      }
    }
  }

  def getBpmDiagrams(ids: Set[BpmDiagramId], readSide: Boolean = true): Future[Set[BpmDiagram]] = {
    if (readSide) {
      repository.getBpmDiagrams(ids)
    } else {
      Future
        .traverse(ids)(getBpmDiagram)
        .map(seq => seq.flatten)
    }
  }

  private def getBpmDiagram(id: BpmDiagramId) = {
    refFor(id)
      .ask[Confirmation](reply => GetBpmDiagram(id, reply))
      .map {
        case Success(entity) => Some(entity)
        case _               => None
      }
  }

  def findBpmDiagrams(query: BpmDiagramFindQuery): Future[FindResult] = {
    elastic.findBpmDiagrams(query)
  }

}
