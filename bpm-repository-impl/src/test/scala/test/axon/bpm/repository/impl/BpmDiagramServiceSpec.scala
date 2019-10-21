package test.axon.bpm.repository.impl

import akka.Done
import annette.shared.exceptions.AnnetteException
import axon.bpm.repository.api.model.{BpmDiagram, BpmDiagramAlreadyExist, BpmDiagramFindQuery, BpmDiagramNotFound}
import axon.bpm.repository.api.BpmRepositoryService
import axon.bpm.repository.impl.BpmRepositoryApplication
import com.lightbend.lagom.scaladsl.api.AdditionalConfiguration
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.Configuration

import scala.concurrent.duration._
import scala.concurrent.{Future, Promise}
import scala.util.Random

class BpmDiagramServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
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
          ))
      }
    }
  }

  val client = server.serviceClient.implement[BpmRepositoryService]

  override protected def afterAll() = server.stop()

  "bpm repository service" should {

    "create bpmDiagram & find it by id" in {
      val diagram = TestData.bpmDiagram()
      for {
        created <- client.createBpmDiagram.invoke(diagram)
        found <- client.getBpmDiagram(diagram.id, readSide = false).invoke()
      } yield {
        created shouldBe diagram.copy(updatedAt = created.updatedAt)
        found shouldBe diagram.copy(updatedAt = created.updatedAt)
      }
    }

    "create bpmDiagram with existing id" in {
      for {
        created <- client.createBpmDiagram.invoke(TestData.bpmDiagram("id3"))
        created2 <- client.createBpmDiagram
          .invoke(TestData.bpmDiagram("id3"))
          .recover {
            case th: Throwable => th
          }
      } yield {
        created shouldBe a[BpmDiagram]
        created2 shouldBe a[AnnetteException]
        created2.asInstanceOf[AnnetteException].code shouldBe BpmDiagramAlreadyExist.MessageCode
      }
    }

    "update bpmDiagram" in {
      val sch = TestData.bpmDiagram("id4", name = "name1", description = "description1").copy(xml = "xml1")
      for {
        created <- client.createBpmDiagram.invoke(TestData.bpmDiagram("id4").copy(xml = "xml"))
        updated <- client.updateBpmDiagram
          .invoke(sch)
          .recover { case th: Throwable => th }
        bpmDiagram <- client.getBpmDiagram("id4", readSide = false).invoke()
      } yield {
        created shouldBe a[BpmDiagram]
        updated shouldBe a[BpmDiagram]
        bpmDiagram.xml shouldBe sch.xml
        bpmDiagram.name shouldBe sch.name
        bpmDiagram.description shouldBe sch.description
      }

    }

    "update bpmDiagram with non-existing id" in {
      val sch = TestData.bpmDiagram("id5", name = "name1", description = "description1")
      for {
        updated <- client.updateBpmDiagram
          .invoke(sch)
          .recover { case th: Throwable => th }
      } yield {
        updated shouldBe a[AnnetteException]
        updated.asInstanceOf[AnnetteException].code shouldBe BpmDiagramNotFound.MessageCode
      }

    }

    "delete bpmDiagram" in {
      val id = s"id${Random.nextInt()}"
      val diagram = TestData.bpmDiagram(id)
      for {
        created <- client.createBpmDiagram.invoke(diagram)
        found1 <- client
          .getBpmDiagram(id, readSide = false)
          .invoke()
          .recover { case th: Throwable => th }
        deleted <- client.deleteBpmDiagram(id).invoke()
        found2 <- client
          .getBpmDiagram(id, readSide = false)
          .invoke()
          .recover {
            case th: Throwable =>
              th
          }
      } yield {
        created shouldBe a[BpmDiagram]
        found1 shouldBe diagram.copy(updatedAt = created.updatedAt)
        deleted shouldBe Done
        found2 shouldBe a[AnnetteException]
        found2.asInstanceOf[AnnetteException].code shouldBe BpmDiagramNotFound.MessageCode
      }

    }

    "delete bpmDiagram with nonexisting id" in {
      val id = s"id${Random.nextInt()}"
      for {
        deleted <- client
          .deleteBpmDiagram(id)
          .invoke()
          .recover { case th: Throwable => th }
      } yield {
        deleted shouldBe a[AnnetteException]
        deleted.asInstanceOf[AnnetteException].code shouldBe BpmDiagramNotFound.MessageCode
      }

    }

    "find bpmDiagram by non-existing id" in {
      val diagram = TestData.bpmDiagram()
      for {
        found <- client
          .getBpmDiagram(Random.nextInt().toString, readSide = false)
          .invoke()
          .recover { case th: Throwable => th }
      } yield {
        found shouldBe a[AnnetteException]
        found.asInstanceOf[AnnetteException].code shouldBe BpmDiagramNotFound.MessageCode
      }
    }

    "find bpmDiagrams" in {
      val ids = Seq(
        s"id-${Random.nextInt()}",
        s"id-${Random.nextInt()}",
        s"id-${Random.nextInt()}"
      )
      val names = Seq(
        s"name-${Random.nextInt()}",
        s"name-${Random.nextInt()}",
        s"name-${Random.nextInt()}"
      )
      val descriptions = Seq(
        s"description-${Random.nextInt()}",
        s"description-${Random.nextInt()}",
        s"description-${Random.nextInt()}"
      )
      val bpmDiagrams = for (i <- ids.indices) yield TestData.bpmDiagram(ids(i), names(i), descriptions(i))

      val createFuture = Future.traverse(bpmDiagrams)(bpmDiagram => client.createBpmDiagram.invoke(bpmDiagram))

      (for {
        _ <- createFuture.recover { case th: Throwable => th }
      } yield {
        awaitSuccess() {
          for {
            found0 <- client.findBpmDiagrams.invoke(BpmDiagramFindQuery(0, 1000, None))
            found2 <- client.findBpmDiagrams.invoke(BpmDiagramFindQuery(0, 1000, Some(names(1))))
            found4 <- client.findBpmDiagrams.invoke(BpmDiagramFindQuery(0, 1000, Some(Random.nextInt().toString)))
          } yield {

            println(s"found0: $found0")
            println(s"found2: $found2")
            println(s"found4: $found4")

            found0.hits.length shouldBe >=(ids.length)

            found2.hits.length shouldBe 1
            found2.hits.head.id shouldBe ids(1)

            found4.hits.length shouldBe 0

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
