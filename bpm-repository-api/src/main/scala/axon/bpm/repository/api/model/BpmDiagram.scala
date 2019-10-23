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

package axon.bpm.repository.api.model

import java.time.OffsetDateTime

import play.api.libs.json._

case class BpmDiagram(
    id: BpmDiagramId,
    name: String,
    description: Option[String],
    notation: String,
    xml: String,
    updatedAt: OffsetDateTime = OffsetDateTime.now,
    active: Boolean = true
)

object BpmDiagram {
  implicit val format: Format[BpmDiagram] = Json.format

  def apply(entity: BpmDiagramUpdate): BpmDiagram = new BpmDiagram(
    id = entity.id,
    name = entity.name,
    description = entity.description,
    notation = entity.notation,
    xml = entity.xml
  )
}
