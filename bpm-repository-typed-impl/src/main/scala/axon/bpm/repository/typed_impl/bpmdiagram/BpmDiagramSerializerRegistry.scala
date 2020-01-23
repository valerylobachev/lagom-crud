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

import axon.bpm.repository.api.model.{BpmDiagram, BpmDiagramFindQuery, BpmDiagramUpdate}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.Json

import scala.collection._

object BpmDiagramSerializerRegistry extends JsonSerializerRegistry {
  import BpmDiagramEntity._
  override val serializers = immutable.Seq(
        // events
        JsonSerializer[BpmDiagramStored],
        JsonSerializer[BpmDiagramCreated],
        JsonSerializer[BpmDiagramUpdated],
        JsonSerializer[BpmDiagramDeleted],
        JsonSerializer[BpmDiagramDeactivated],
        JsonSerializer[BpmDiagramActivated],

        // responses
        JsonSerializer[Confirmation],
        JsonSerializer[Success],
        JsonSerializer[DeleteSuccess.type],
        JsonSerializer[NotFound.type],
        JsonSerializer[AlreadyExist.type],
        JsonSerializer[AlreadyActive.type],
        JsonSerializer[InDeactivatedState.type],

        // entities
        JsonSerializer[BpmDiagramEntity],
        JsonSerializer[BpmDiagram],
        JsonSerializer[BpmDiagramUpdate],
        JsonSerializer[BpmDiagramFindQuery]
      )
}
