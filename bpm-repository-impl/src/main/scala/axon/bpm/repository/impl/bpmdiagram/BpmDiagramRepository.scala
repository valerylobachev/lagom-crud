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

import axon.bpm.repository.api.model.{BpmDiagram, BpmDiagramId}
import com.datastax.driver.core.Row
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import scala.collection._
//import scala.collection.JavaConverters._
import scala.jdk.CollectionConverters._
import scala.concurrent.{ExecutionContext, Future}

private[impl] class BpmDiagramRepository(session: CassandraSession)(implicit ec: ExecutionContext) {

  def getBpmDiagram(id: BpmDiagramId): Future[Option[BpmDiagram]] = {
    for {
      stmt <- session.prepare("SELECT * FROM bpm_diagrams WHERE id = ?")
      result <- session.selectOne(stmt.bind(id)).map(_.map(convert))
    } yield result
  }

  def getBpmDiagrams(ids: Set[BpmDiagramId]): Future[Set[BpmDiagram]] = {
    for {
      stmt <- session.prepare("SELECT * FROM bpm_diagrams WHERE id IN ?")
      result <- session.selectAll(stmt.bind(ids.toList.asJava)).map(_.map(convert))
    } yield result.toSet
  }

  private def convert(row: Row): BpmDiagram = {
    BpmDiagram(
      id = row.getString("id"),
      name = row.getString("name"),
      description = Option(row.getString("description")),
      notation = row.getString("notation"),
      xml = row.getString("xml"),
      updatedAt = OffsetDateTime.parse(row.getString("updated_at")),
      active = row.getBool("active")
    )
  }

}
