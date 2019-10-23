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

import annette.shared.elastic.{BaseEntityElastic, FindResult}
import axon.bpm.repository.api.model.{BpmDiagram, BpmDiagramFindQuery, BpmDiagramId}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.playjson.playJsonIndexable
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.searches.queries.{Query, QueryStringQuery}
import com.sksamuel.elastic4s.requests.searches.sort.{FieldSort, SortOrder}
import org.slf4j.LoggerFactory
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

class BpmDiagramElastic(configuration: Configuration, elasticClient: ElasticClient, registry: PersistentEntityRegistry)(
    implicit override val ec: ExecutionContext
) extends BaseEntityElastic(configuration, elasticClient) {

  override val log = LoggerFactory.getLogger(classOf[BpmDiagramElastic])

  override def indexSuffix = "bpm_repository-bpm-diagram"

  implicit private val indexable = playJsonIndexable[BpmDiagramIndex]

  def createBpmDiagramIndex = {
    createEntityIndex(
      createIndex(indexName)
        .mapping(
          properties(
            textField("id"),
            keywordField("name"),
            dateField("updatedAt"),
            booleanField("active")
          )
        )
    )
  }

  def indexBpmDiagram(bpmDiagram: BpmDiagram): Future[Unit] = {
    indexEntity(
      indexInto(indexName)
        .id(bpmDiagram.id)
        .doc(BpmDiagramIndex(bpmDiagram))
        .refresh(RefreshPolicy.Immediate)
    )
  }

  def indexBpmDiagram(id: BpmDiagramId): Future[Unit] = {
    for {
      maybeEntity <- getEntity(id)
      res <- maybeEntity.map(indexBpmDiagram).getOrElse(Future.successful(Unit))
    } yield ()
  }

  def deleteBpmDiagram(id: BpmDiagramId): Future[Unit] = {
    deleteEntity(id)
  }

  def findBpmDiagrams(query: BpmDiagramFindQuery): Future[FindResult] = {

    val activeQuery: Seq[Query] =
      if (query.activeOnly) Seq(termQuery("active", true))
      else Seq.empty

    val nameQuery = query.filter
      .map(filterByName => QueryStringQuery(defaultField = Some("name"), query = s"*$filterByName*"))

    val sortBy = query.sortBy
      .map { field =>
        val sortOrder = if (query.ascending) SortOrder.Asc else SortOrder.Desc
        Seq(FieldSort(field, order = sortOrder))
      }
      .getOrElse(Seq.empty)

    val searchRequest = search(indexName)
      .bool(must(nameQuery ++ activeQuery))
      .from(query.offset)
      .size(query.size)
      .sourceInclude("updatedAt")
      .sortBy(sortBy)
      .trackTotalHits(true)

    findEntity(searchRequest)
  }

  private def getEntity(id: BpmDiagramId): Future[Option[BpmDiagram]] = {
    registry.refFor[BpmDiagramEntity](id).ask(GetBpmDiagram)
  }

}
