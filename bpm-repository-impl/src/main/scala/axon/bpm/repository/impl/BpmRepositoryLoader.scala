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

package axon.bpm.repository.impl

import annette.shared.elastic.ElasticProvider
import axon.bpm.repository.api.BpmRepositoryService
import axon.bpm.repository.impl.bpmdiagram.{BpmDiagramElastic, BpmDiagramEntity, BpmDiagramEventProcessor, BpmDiagramRepository, BpmDiagramSerializerRegistry, BpmDiagramService}
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

import scala.collection.immutable

class BpmRepositoryLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new BpmRepositoryApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new BpmRepositoryApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[BpmRepositoryService])
}

abstract class BpmRepositoryApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    with CassandraPersistenceComponents
    with AhcWSComponents {

  override lazy val lagomServer = serverFor[BpmRepositoryService](wire[BpmRepositoryServiceImpl])
  lazy val jsonSerializerRegistry = BpmRepositorySerializerRegistry

  lazy val elasticClient = wireWith(ElasticProvider.create _)
  lazy val bpmDiagramElastic = wire[BpmDiagramElastic]
  lazy val bpmDiagramService = wire[BpmDiagramService]
  lazy val bpmDiagramRepository = wire[BpmDiagramRepository]

  persistentEntityRegistry.register(wire[BpmDiagramEntity])
  readSide.register(wire[BpmDiagramEventProcessor])

}

object BpmRepositorySerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = BpmDiagramSerializerRegistry.serializers
}
