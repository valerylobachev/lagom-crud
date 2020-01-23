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

import akka.Done
import akka.actor.ActorSystem
import akka.stream.Materializer
import annette.shared.elastic.FindResult
import axon.bpm.repository.api.model.{BpmDiagram, BpmDiagramFindQuery, BpmDiagramId, BpmDiagramNotFound, BpmDiagramUpdate}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.{ExecutionContext, Future}
import scala.collection._

class BpmDiagramService(registry: PersistentEntityRegistry, system: ActorSystem, repository: BpmDiagramRepository, elastic: BpmDiagramElastic)(
    implicit ec: ExecutionContext,
    mat: Materializer
) {

  def storeBpmDiagram(bpmDiagram: BpmDiagram): Future[BpmDiagram] = {
    refFor(bpmDiagram.id).ask(StoreBpmDiagram(bpmDiagram))
  }

  def createBpmDiagram(bpmDiagram: BpmDiagramUpdate): Future[BpmDiagram] = {
    refFor(bpmDiagram.id).ask(CreateBpmDiagram(bpmDiagram))
  }

  def updateBpmDiagram(bpmDiagram: BpmDiagramUpdate): Future[BpmDiagram] = {
    refFor(bpmDiagram.id).ask(UpdateBpmDiagram(bpmDiagram))
  }

  def deactivateBpmDiagram(id: BpmDiagramId): Future[BpmDiagram] = {
    refFor(id).ask(DeactivateBpmDiagram(id))
  }

  def activateBpmDiagram(id: BpmDiagramId): Future[BpmDiagram] = {
    refFor(id).ask(ActivateBpmDiagram(id))
  }

  def deleteBpmDiagram(id: BpmDiagramId): Future[Done] = {
    refFor(id).ask(DeleteBpmDiagram(id))
  }

  def getBpmDiagram(id: BpmDiagramId, readSide: Boolean = true): Future[BpmDiagram] = {
    for {
      maybeBpmDiagram <- if (readSide) {
        repository.getBpmDiagram(id)
      } else {
        refFor(id).ask(GetBpmDiagram)
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
        .traverse(ids)(id => refFor(id).ask(GetBpmDiagram))
        .map(seq => seq.flatten)
    }
  }

  def findBpmDiagrams(query: BpmDiagramFindQuery): Future[FindResult] = {
    elastic.findBpmDiagrams(query)
  }

  private def refFor(id: BpmDiagramId) = {
    registry.refFor[BpmDiagramEntity](id)

  }

}
