package test.axon.bpm.repository.typed_impl


import akka.actor.ActorSystem
import axon.bpm.repository.typed_impl.BpmRepositoryApplication
import axon.bpm.repository.typed_impl.bpmdiagram.BpmDiagramSerializerRegistry
import com.lightbend.lagom.scaladsl.api.AdditionalConfiguration
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.typesafe.config.ConfigFactory
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

import scala.concurrent.duration.{FiniteDuration, _}
import scala.concurrent.{Future, Promise}
import scala.util.Random


abstract class AbstractTypedBpmRepositorySpec  extends AsyncWordSpec with BeforeAndAfterAll with Matchers  {

  val system = ActorSystem("BpmDiagramEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(BpmDiagramSerializerRegistry))
  implicit val ec = system.dispatcher

  lazy val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new BpmRepositoryApplication(ctx) with LocalServiceLocator {
      override def additionalConfiguration: AdditionalConfiguration = {
        super.additionalConfiguration ++ ConfigFactory.parseString(
//          s"""
//            |akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
//            |akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
//            |akka.persistence.snapshot-store.local.dir = "target/snapshot-${UUID.randomUUID().toString}"
          s"""
            |cassandra-query-journal.events-by-persistence-id-gap-timeout = 1s
            |cassandra-query-journal.refresh-interval = 1s
            |cassandra-query-journal.events-by-tag.gap-timeout = 1s
            |cassandra-query-journal.events-by-tag.eventual-consistency-delay = 2s
            |lagom.circuit-breaker.default.enabled = off
            |elastic.url = "https://localhost:9200"
            |elastic.prefix = test-${Random.nextInt(99999)}
            |elastic.username = admin
            |elastic.password = admin
            |elastic.allowInsecure = true
            |akka.jvm-exit-on-fatal-error = false""".stripMargin
        )
      }
    }
  }



  override protected def beforeAll() = server

  override def afterAll(): Unit = {
    //Await.ready(system.terminate, 10.seconds)
    server.stop()
  }

  def awaitSuccess[T](maxDuration: FiniteDuration = 30.seconds, checkEvery: FiniteDuration = 1.second)(block: => Future[T]): Future[T] = {
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
