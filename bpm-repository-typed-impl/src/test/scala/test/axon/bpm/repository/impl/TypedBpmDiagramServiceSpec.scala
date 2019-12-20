package test.axon.bpm.repository.impl

import java.time.OffsetDateTime

import annette.shared.exceptions.AnnetteException
import axon.bpm.repository.api.BpmRepositoryService
import axon.bpm.repository.api.model.{BpmDiagram, BpmDiagramFindQuery, BpmDiagramNotFound}
import play.api.Configuration

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.util.Random

class TypedBpmDiagramServiceSpec extends AbstractTypedBpmRepositorySpec with BpmDiagramGenerator {
  final val N = 10

  lazy val client = server.serviceClient.implement[BpmRepositoryService]

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
        awaitSuccess(15.seconds, 1.second) {
          for {
            found <- client.getBpmDiagram(entity.id, readSide = true).invoke()
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
            found <- client.getBpmDiagrams(readSide = true).invoke(entities.map(_.id).toSet)
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
            found.total shouldBe entities1.length
            found.hits.map(_.id).toSet shouldBe entitiesToBe
          }
        }
      }).flatMap(identity)
    }

  }

}
