package test.axon.bpm.repository.impl

import java.time.OffsetDateTime

import annette.shared.exceptions.AnnetteException
import axon.bpm.repository.api.BpmRepositoryService
import axon.bpm.repository.api.model.{BpmDiagram, BpmDiagramFindQuery, BpmDiagramNotFound}
import axon.bpm.repository.impl.BpmRepositoryApplication
import com.lightbend.lagom.scaladsl.api.AdditionalConfiguration
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.Configuration

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.util.Random

class BpmDiagramServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll with BpmDiagramGenerator {
  final val N = 10

  lazy val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new BpmRepositoryApplication(ctx) with LocalServiceLocator {
      override def additionalConfiguration: AdditionalConfiguration = {
        super.additionalConfiguration ++ Configuration.from(
          Map(
            "cassandra-query-journal.eventual-consistency-delay" -> "0",
            "lagom.circuit-breaker.default.enabled" -> "off",
            "elastic.url" -> "https://localhost:9200",
            "elastic.prefix" -> "test",
            "elastic.username" -> "admin",
            "elastic.password" -> "admin",
            "elastic.allowInsecure" -> "true"
          )
        )
      }
    }
  }

  lazy val client = server.serviceClient.implement[BpmRepositoryService]

  override protected def beforeAll() = server

  override protected def afterAll() = server.stop()

  "bpm repository service" should {

    "getBpmDiagram for existing entity" in {
      val entity = generateBpmDiagramUpdate()
      for {
        created <- client.createBpmDiagram.invoke(entity)
        found <- client.getBpmDiagram(entity.id, readSide = false).invoke()
      } yield {
        created shouldBe BpmDiagram(entity).copy(updatedAt = created.updatedAt)
        found shouldBe BpmDiagram(entity).copy(updatedAt = created.updatedAt)
      }
    }

    "getBpmDiagram for existing entity (read side)" in {
      val entity = generateBpmDiagramUpdate()
      (for {
        created <- client.createBpmDiagram.invoke(entity)
      } yield {
        awaitSuccess() {
          for {
            found <- client.getBpmDiagram(entity.id, readSide = false).invoke()
          } yield {
            created shouldBe BpmDiagram(entity).copy(updatedAt = created.updatedAt)
            found shouldBe BpmDiagram(entity).copy(updatedAt = created.updatedAt)
          }
        }
      }).flatMap(identity)
    }

    "getBpmDiagram for non-existing entity" in {
      val entity = generateBpmDiagramUpdate()
      for {
        found <- client.getBpmDiagram(entity.id, readSide = false).invoke().recover { case th: Throwable => th }
      } yield {
        found shouldBe a[AnnetteException]
        found.asInstanceOf[AnnetteException].code shouldBe BpmDiagramNotFound.MessageCode
      }
    }

    "getBpmDiagrams" in {
      val entities = (1 to N).map(_ => generateBpmDiagramUpdate())
      val now = OffsetDateTime.now
      val entitiesToBe = entities.map(BpmDiagram(_).copy(updatedAt = now)).toSet
      for {
        _ <- Future.traverse(entities)(entity => client.createBpmDiagram.invoke(entity))
        found <- client.getBpmDiagrams(readSide = false).invoke(entities.map(_.id).toSet)
      } yield {
        found.map(_.copy(updatedAt = now)) shouldBe entitiesToBe
      }
    }

    "getBpmDiagrams (read side)" in {
      val entities = (1 to N).map(_ => generateBpmDiagramUpdate())
      val now = OffsetDateTime.now
      val entitiesToBe = entities.map(BpmDiagram(_).copy(updatedAt = now)).toSet
      (for {
        _ <- Future.traverse(entities)(entity => client.createBpmDiagram.invoke(entity))
      } yield {
        awaitSuccess() {
          for {
            found <- client.getBpmDiagrams(readSide = false).invoke(entities.map(_.id).toSet)
          } yield {
            found.map(_.copy(updatedAt = now)) shouldBe entitiesToBe
          }
        }
      }).flatMap(identity)
    }

    "findBpmDiagrams" in {
      val searchWord = generateWord(8)
      val entities1 = (1 to N).map(_ => generateBpmDiagramUpdate()).map(e => e.copy(name = s"${e.name} $searchWord"))
      val entities2 = (1 to N).map(_ => generateBpmDiagramUpdate())
      val entitiesToBe = entities1.map(_.id).toSet
      val query = BpmDiagramFindQuery(0, 1000, Some(searchWord.substring(2, 6)), activeOnly = false)
      (for {
        _ <- Future.traverse(entities1 ++ entities2)(entity => client.createBpmDiagram.invoke(entity))
        _ <- Future.traverse(entities1 ++ entities2) { entity =>
          if (Random.nextBoolean()) {
            client.deactivateBpmDiagram(entity.id).invoke()
          } else {
            Future.successful()
          }
        }
      } yield {
        awaitSuccess() {
          for {
            found <- client.findBpmDiagrams.invoke(query)
          } yield {
            found.total shouldBe entities1.length
            found.hits.map(_.id).toSet shouldBe entitiesToBe
          }
        }
      }).flatMap(identity)
    }

    "findBpmDiagrams active only" in {
      val searchWord = generateWord(8)
      val entities1 = (1 to N).map(_ => generateBpmDiagramUpdate()).map(e => e.copy(name = s"${e.name} $searchWord"))
      val entities2 = (1 to N).map(_ => generateBpmDiagramUpdate()).map(e => e.copy(name = s"${e.name} $searchWord"))
      val entitiesToBe = entities1.map(_.id).toSet
      val query = BpmDiagramFindQuery(0, 1000, Some(searchWord.substring(2, 6)))
      (for {
        _ <- Future.traverse(entities1 ++ entities2)(entity => client.createBpmDiagram.invoke(entity))
        _ <- Future.traverse(entities2)(entity => client.deactivateBpmDiagram(entity.id).invoke())
      } yield {
        awaitSuccess() {
          for {
            found <- client.findBpmDiagrams.invoke(query)
          } yield {
            println(found)
            found.total shouldBe entities1.length
            found.hits.map(_.id).toSet shouldBe entitiesToBe
          }
        }
      }).flatMap(identity)
    }

  }

  def awaitSuccess[T](maxDuration: FiniteDuration = 10.seconds, checkEvery: FiniteDuration = 100.milliseconds)(block: => Future[T]): Future[T] = {
    val checkUntil = System.currentTimeMillis() + maxDuration.toMillis

    def doCheck(): Future[T] = {
      block.recoverWith {
        case recheck if checkUntil > System.currentTimeMillis() =>
          val timeout = Promise[T]()
          server.application.actorSystem.scheduler.scheduleOnce(checkEvery) {
            timeout.completeWith(doCheck())
          }(server.executionContext)
          timeout.future
      }
    }

    doCheck()
  }

}
