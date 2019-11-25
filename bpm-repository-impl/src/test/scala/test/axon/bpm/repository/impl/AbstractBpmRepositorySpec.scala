package test.axon.bpm.repository.impl

import akka.actor.ActorSystem
import axon.bpm.repository.impl.BpmRepositoryApplication
import axon.bpm.repository.impl.bpmdiagram.BpmDiagramSerializerRegistry
import com.lightbend.lagom.scaladsl.api.AdditionalConfiguration
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.Configuration

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._


abstract class AbstractBpmRepositorySpec  extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  val system = ActorSystem("BpmDiagramEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(BpmDiagramSerializerRegistry))
  implicit val ec = system.dispatcher

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



  override protected def beforeAll() = server

  override def afterAll(): Unit = {
    //Await.ready(system.terminate, 10.seconds)
    server.stop()
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
