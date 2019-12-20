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

package axon.bpm.repository.typed_impl

import akka.{Done, NotUsed}
import annette.shared.elastic.FindResult
import axon.bpm.repository.api._
import axon.bpm.repository.api.model._
import axon.bpm.repository.typed_impl.bpmdiagram._
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.collection._

class BpmRepositoryServiceImpl(bpmDiagramService: BpmDiagramService) extends BpmRepositoryService {

  override def storeBpmDiagram: ServiceCall[BpmDiagramUpdate, BpmDiagram] = ServiceCall { entity =>
    bpmDiagramService.storeBpmDiagram(entity)
  }

  override def createBpmDiagram: ServiceCall[BpmDiagramUpdate, BpmDiagram] = ServiceCall { entity =>
    bpmDiagramService.createBpmDiagram(entity)
  }

  override def updateBpmDiagram: ServiceCall[BpmDiagramUpdate, BpmDiagram] = ServiceCall { entity =>
    bpmDiagramService.updateBpmDiagram(entity)
  }

  override def deactivateBpmDiagram(id: BpmDiagramId): ServiceCall[NotUsed, BpmDiagram] = ServiceCall { _ =>
    bpmDiagramService.deactivateBpmDiagram(id)
  }

  override def activateBpmDiagram(id: BpmDiagramId): ServiceCall[NotUsed, BpmDiagram] = ServiceCall { _ =>
    bpmDiagramService.activateBpmDiagram(id)
  }

  override def deleteBpmDiagram(id: BpmDiagramId): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    bpmDiagramService.deleteBpmDiagram(id)
  }

  override def getBpmDiagram(id: BpmDiagramId, readSide: Boolean): ServiceCall[NotUsed, BpmDiagram] = ServiceCall { _ =>
    bpmDiagramService.getBpmDiagram(id, readSide)
  }

  override def getBpmDiagrams(readSide: Boolean): ServiceCall[Set[BpmDiagramId], Set[BpmDiagram]] = ServiceCall { ids =>
    bpmDiagramService.getBpmDiagrams(ids, readSide)
  }

  override def findBpmDiagrams: ServiceCall[BpmDiagramFindQuery, FindResult] = ServiceCall { query =>
    bpmDiagramService.findBpmDiagrams(query)
  }

}
