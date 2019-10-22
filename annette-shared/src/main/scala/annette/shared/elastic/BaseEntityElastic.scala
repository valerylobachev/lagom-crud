package annette.shared.elastic

import java.time.OffsetDateTime

import com.sksamuel.elastic4s.ElasticDsl.{deleteById, indexExists, _}
import com.sksamuel.elastic4s.requests.delete.DeleteResponse
import com.sksamuel.elastic4s.requests.indexes.{CreateIndexRequest, IndexRequest}
import com.sksamuel.elastic4s.requests.indexes.admin.IndexExistsResponse
import com.sksamuel.elastic4s.requests.searches.{SearchRequest, SearchResponse}
import com.sksamuel.elastic4s.{ElasticClient, RequestFailure, RequestSuccess}
import org.slf4j.Logger
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

abstract class BaseEntityElastic(configuration: Configuration, elasticClient: ElasticClient)(implicit val ec: ExecutionContext) {

  protected val log: Logger

  protected val indexPrefix: String = configuration.getOptional[String]("elastic.prefix").map(_ + "-").getOrElse("")
  def indexSuffix: String
  protected val indexName = s"${indexPrefix}${indexSuffix}"

  protected def createEntityIndex(createIndexRequest: CreateIndexRequest): Future[Unit] = {
    elasticClient
      .execute(indexExists(indexName))
      .map {
        case failure: RequestFailure =>
          log.error("createEntityIndex: indexExists validation failed", failure.error)
          false
        case res: RequestSuccess[IndexExistsResponse] =>
          res.result.exists
      }
      .flatMap {
        case true =>
          log.debug(s"createEntityIndex: Index ${indexName} exists")
          Future.successful(Unit)
        case false =>
          log.debug(s"createEntityIndex: Index ${indexName} does not exists")
          val createFuture = for {
            res <- elasticClient.execute { createIndexRequest }
          } yield {
            res match {
              case _: RequestSuccess[_] =>
                log.debug("createOrgRolesIndex Success: {}", res.toString)
              case failure: RequestFailure =>
                log.error("createOrgRolesIndex Failure: {}", failure)
                throw new Exception(failure.error.toString)
            }
            ()
          }
          createFuture.failed.map(th => log.error("createEntityIndex: failure", th))
          createFuture
      }
  }

  protected def indexEntity(indexRequest: IndexRequest): Future[Unit] = {
    val indexFuture = for {
      res <- elasticClient.execute { indexRequest }
    } yield {
      res match {
        case _: RequestSuccess[_] =>
          log.debug("indexEntity Success: {}", res.toString)
        case failure: RequestFailure =>
          log.error("indexEntity Failure: {}", failure)
          throw new Exception(failure.error.toString)
      }
      ()
    }
    indexFuture.failed.map(th => log.error("indexEntity: failure", th))
    indexFuture
  }

  protected def deleteEntity(id: String): Future[Unit] = {
    for {
      resp <- elasticClient.execute(deleteById(indexName, id))
    } yield {
      resp match {
        case failure: RequestFailure =>
          log.error("deleteEntity: failed " + failure.error)
          ()
        case results: RequestSuccess[DeleteResponse] =>
          log.debug(s"deleteEntity: id: ${id} results: ${results.toString}")
          ()
      }
    }
  }

  protected def findEntity(searchRequest: SearchRequest): Future[FindResult] = {
    for {
      resp <- elasticClient.execute(searchRequest)
    } yield {
      resp match {
        case failure: RequestFailure =>
          log.error("findEntity: failed " + failure.error)
          FindResult(0, Seq.empty)
        case results: RequestSuccess[SearchResponse] =>
          log.debug(s"findEntity: searchRequest: ${searchRequest.toString} results ${results.toString}")
          val total = results.result.hits.total.value
          val hits = results.result.hits.hits.map { hit =>
            val updatedAt = hit.sourceAsMap
              .get("updatedAt")
              .map(v => OffsetDateTime.parse(v.toString))
              .getOrElse(OffsetDateTime.now)
            HitResult(hit.id, hit.score, updatedAt)
          }.toSeq
          FindResult(total, hits)
      }
    }
  }

}
