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

import axon.bpm.repository.api.model.{BpmDiagram, BpmDiagramId}
import play.api.libs.json.{Format, Json}

case class BpmDiagramIndex(
    id: BpmDiagramId,
    name: String,
    updatedAt: OffsetDateTime,
    active: Boolean
)

object BpmDiagramIndex {
  implicit val format: Format[BpmDiagramIndex] = Json.format

  def apply(entity: BpmDiagram): BpmDiagramIndex = new BpmDiagramIndex(
    id = entity.id,
    name = entity.name,
    updatedAt = entity.updatedAt,
    active = entity.active
  )
}
