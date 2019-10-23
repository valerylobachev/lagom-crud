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

package axon.bpm.repository.api

import akka.{Done, NotUsed}
import annette.shared.elastic.FindResult
import annette.shared.exceptions.AnnetteExceptionSerializer
import axon.bpm.repository.api.model._
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

import scala.collection._

trait BpmRepositoryService extends Service {

  def storeBpmDiagram: ServiceCall[BpmDiagramUpdate, BpmDiagram]
  def createBpmDiagram: ServiceCall[BpmDiagramUpdate, BpmDiagram]
  def updateBpmDiagram: ServiceCall[BpmDiagramUpdate, BpmDiagram]
  def deleteBpmDiagram(id: BpmDiagramId): ServiceCall[NotUsed, Done]
  def deactivateBpmDiagram(id: BpmDiagramId): ServiceCall[NotUsed, BpmDiagram]
  def activateBpmDiagram(id: BpmDiagramId): ServiceCall[NotUsed, BpmDiagram]
  def getBpmDiagram(id: BpmDiagramId, readSide: Boolean = true): ServiceCall[NotUsed, BpmDiagram]
  def getBpmDiagrams(readSide: Boolean = true): ServiceCall[Set[BpmDiagramId], Set[BpmDiagram]]
  def findBpmDiagrams: ServiceCall[BpmDiagramFindQuery, FindResult]

  final override def descriptor = {
    import Service._
    // @formatter:off
    named("bpm-repository")
      .withCalls(
        restCall(Method.POST,   "/api/v1/bpm-repository/bpmDiagram/store",                  storeBpmDiagram),
        restCall(Method.POST,   "/api/v1/bpm-repository/bpmDiagram/create",                 createBpmDiagram),
        restCall(Method.PUT,    "/api/v1/bpm-repository/bpmDiagram/update",                 updateBpmDiagram),
        restCall(Method.DELETE, "/api/v1/bpm-repository/bpmDiagram/activate/:id",           activateBpmDiagram _),
        restCall(Method.DELETE, "/api/v1/bpm-repository/bpmDiagram/deactivate/:id",         deactivateBpmDiagram _),
        restCall(Method.DELETE, "/api/v1/bpm-repository/bpmDiagram/delete/:id",             deleteBpmDiagram _),
        restCall(Method.GET,    "/api/v1/bpm-repository/bpmDiagram/getById/:id/:readSide",  getBpmDiagram _),
        restCall(Method.POST,   "/api/v1/bpm-repository/bpmDiagram/getByIds/:readSide",     getBpmDiagrams _),
        restCall(Method.POST,   "/api/v1/bpm-repository/bpmDiagram/find",                   findBpmDiagrams )
      )
      .withExceptionSerializer(new AnnetteExceptionSerializer())
      .withAutoAcl(true)
    // @formatter:on
  }
}
