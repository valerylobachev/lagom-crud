package test.axon.bpm.repository.impl

import akka.actor.ActorSystem
import axon.bpm.repository.impl.BpmRepositoryApplication
import axon.bpm.repository.impl.bpmdiagram.BpmDiagramSerializerRegistry
import com.lightbend.lagom.scaladsl.api.AdditionalConfiguration
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import com.typesafe.config.{Config, ConfigFactory}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.Configuration

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.util.Random


abstract class AbstractBpmRepositorySpec  extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  val system = ActorSystem("BpmDiagramEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(BpmDiagramSerializerRegistry))
  implicit val ec = system.dispatcher

  lazy val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new BpmRepositoryApplication(ctx) with LocalServiceLocator {
      override def additionalConfiguration: AdditionalConfiguration = {
        super.additionalConfiguration ++ ConfigFactory.parseString(
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
