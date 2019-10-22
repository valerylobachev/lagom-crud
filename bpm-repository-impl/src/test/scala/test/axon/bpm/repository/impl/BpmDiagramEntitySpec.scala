package test.axon.bpm.repository.impl

import akka.Done
import akka.actor.ActorSystem
import axon.bpm.repository.api.model._
import axon.bpm.repository.impl.bpmdiagram._
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._

class BpmDiagramEntitySpec extends WordSpecLike with Matchers with BeforeAndAfterAll with TypeCheckedTripleEquals with BpmDiagramGenerator {

  val system = ActorSystem("BpmDiagramEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(BpmDiagramSerializerRegistry))

  override def afterAll(): Unit = {
    Await.ready(system.terminate, 10.seconds)
  }

  "in initial mode" must {

    "handle StoreBpmDiagram" in {
      val id = generateId
      val entity = generateBpmDiagram(id = id)
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val outcome = driver.run(StoreBpmDiagram(entity))
      val entityToBe = entity.copy(updatedAt = outcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
      outcome.events should ===(List(BpmDiagramStored(entityToBe)))
      outcome.state.entity should  ===(Some(entityToBe))
      outcome.replies should ===(List(entityToBe))
      outcome.issues should be(Nil)
    }

    "handle CreateBpmDiagram" in {
      val id = generateId
      val entity = generateBpmDiagram(id = id)
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val outcome = driver.run(CreateBpmDiagram(entity))
      val entityToBe = entity.copy(updatedAt = outcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
      outcome.events should ===(List(BpmDiagramCreated(entityToBe)))
      outcome.state.entity should  ===(Some(entityToBe))
      outcome.replies should ===(List(entityToBe))
      outcome.issues should be(Nil)
    }

    "handle UpdateBpmDiagram" in {
      val id = generateId
      val entity = generateBpmDiagram(id = id)
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val outcome = driver.run(UpdateBpmDiagram(entity))
      outcome.replies.head should be(BpmDiagramNotFound(id))
      outcome.events.size should ===(0)
    }

    "handle DeleteBpmDiagram" in {
      val id = generateId
      val entity = generateBpmDiagram(id = id)
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val outcome = driver.run(DeleteBpmDiagram(entity.id))
      outcome.replies.head should be(BpmDiagramNotFound(id))
      outcome.events.size should ===(0)
    }

    "handle DeactivateBpmDiagram" in {
      val id = generateId
      val entity = generateBpmDiagram(id = id)
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val outcome = driver.run(DeactivateBpmDiagram(entity.id))
      outcome.replies.head should be(BpmDiagramNotFound(id))
      outcome.events.size should ===(0)
    }

    "handle ActivateBpmDiagram" in {
      val id = generateId
      val entity = generateBpmDiagram(id = id)
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val outcome = driver.run(ActivateBpmDiagram(entity.id))
      outcome.replies.head should be(BpmDiagramNotFound(id))
      outcome.events.size should ===(0)
    }

    "handle GetBpmDiagram" in {
      val id = generateId
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val outcome = driver.run(GetBpmDiagram)
      outcome.events should be(Nil)
      outcome.state.entity should  ===(None)
      outcome.replies should ===(List(None))
      outcome.issues should be(Nil)
    }
  }

  "in active mode" must {

    "handle StoreBpmDiagram" in {
      val id = generateId
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val storeEntity = generateBpmDiagram(id = id)
      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
      val entity = generateBpmDiagram(id = id)
      val outcome = driver.run(StoreBpmDiagram(entity))
      val entityToBe = entity.copy(updatedAt = outcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
      outcome.events should ===(List(BpmDiagramStored(entityToBe)))
      outcome.state.entity should  ===(Some(entityToBe))
      outcome.replies should ===(List(entityToBe))
      outcome.issues should be(Nil)
    }

    "handle CreateBpmDiagram" in {
      val id = generateId
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val storeEntity = generateBpmDiagram(id = id)
      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
      val entity = generateBpmDiagram(id = id)
      val outcome = driver.run(CreateBpmDiagram(entity))
      val entityToBe = storeEntity.copy(updatedAt = storeOutcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
      outcome.replies.head should be(BpmDiagramAlreadyExist(id))
      outcome.events.size should ===(0)
      outcome.state.entity should  ===(Some(entityToBe))
    }

    "handle UpdateBpmDiagram" in {
      val id = generateId
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val storeEntity = generateBpmDiagram(id = id)
      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
      val entity = generateBpmDiagram(id = id)
      val outcome = driver.run(UpdateBpmDiagram(entity))
      val entityToBe = entity.copy(updatedAt = outcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
      outcome.events should ===(List(BpmDiagramUpdated(entityToBe)))
      outcome.state.entity should  ===(Some(entityToBe))
      outcome.replies should ===(List(entityToBe))
      outcome.issues should be(Nil)
    }

    "handle DeleteBpmDiagram" in {
      val id = generateId
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val storeEntity = generateBpmDiagram(id = id)
      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
      val outcome = driver.run(DeleteBpmDiagram(id))
      outcome.events should ===(List(BpmDiagramDeleted(id)))
      outcome.state.entity should  ===(None)
      outcome.replies should ===(List(Done))
      outcome.issues should be(Nil)
    }

    "handle DeactivateBpmDiagram" in {
      val id = generateId
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val storeEntity = generateBpmDiagram(id = id)
      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
      val outcome = driver.run(DeactivateBpmDiagram(id))
      val entityToBe = storeEntity.copy(updatedAt = outcome.replies.head.asInstanceOf[BpmDiagram].updatedAt, active = false)
      outcome.events should ===(List(BpmDiagramDeactivated(id, entityToBe.updatedAt)))
      outcome.state.entity should  ===(Some(entityToBe))
      outcome.replies should ===(List(entityToBe))
      outcome.issues should be(Nil)
    }

    "handle ActivateBpmDiagram" in {
      val id = generateId
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val storeEntity = generateBpmDiagram(id = id)
      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
      val outcome = driver.run(ActivateBpmDiagram(id))
      val entityToBe = storeEntity.copy(updatedAt = storeOutcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
      outcome.replies.head should be(BpmDiagramAlreadyActive(id))
      outcome.events.size should ===(0)
      outcome.state.entity should  ===(Some(entityToBe))
    }

    "handle GetBpmDiagram" in {
      val id = generateId
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val storeEntity = generateBpmDiagram(id = id)
      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
      val outcome = driver.run(GetBpmDiagram)
      val entityToBe = storeEntity.copy(updatedAt = storeOutcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
      outcome.events should ===(Nil)
      outcome.state.entity should  ===(Some(entityToBe))
      outcome.replies should ===(List(Some(entityToBe)))
      outcome.issues should be(Nil)
    }
  }

  "in deactivated mode" must {

    "handle StoreBpmDiagram" in {
      val id = generateId
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val storeEntity = generateBpmDiagram(id = id, active = false)
      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
      val entity = generateBpmDiagram(id = id)
      val outcome = driver.run(StoreBpmDiagram(entity))
      val entityToBe = entity.copy(updatedAt = outcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
      outcome.events should ===(List(BpmDiagramStored(entityToBe)))
      outcome.state.entity should  ===(Some(entityToBe))
      outcome.replies should ===(List(entityToBe))
      outcome.issues should be(Nil)
    }

    "handle CreateBpmDiagram" in {
      val id = generateId
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val storeEntity = generateBpmDiagram(id = id, active = false)
      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
      val entity = generateBpmDiagram(id = id)
      val outcome = driver.run(CreateBpmDiagram(entity))
      val entityToBe = storeEntity.copy(updatedAt = storeOutcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
      outcome.replies.head should be(BpmDiagramInDeactivatedState(id))
      outcome.events.size should ===(0)
      outcome.state.entity should  ===(Some(entityToBe))
    }

    "handle UpdateBpmDiagram" in {
      val id = generateId
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val storeEntity = generateBpmDiagram(id = id, active = false)
      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
      val entity = generateBpmDiagram(id = id)
      val outcome = driver.run(UpdateBpmDiagram(entity))
      val entityToBe = storeEntity.copy(updatedAt = storeOutcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
      outcome.replies.head should be(BpmDiagramInDeactivatedState(id))
      outcome.events.size should ===(0)
      outcome.state.entity should  ===(Some(entityToBe))
    }

    "handle DeleteBpmDiagram" in {
      val id = generateId
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val storeEntity = generateBpmDiagram(id = id, active = false)
      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
      val outcome = driver.run(DeleteBpmDiagram(id))
      outcome.events should ===(List(BpmDiagramDeleted(id)))
      outcome.state.entity should  ===(None)
      outcome.replies should ===(List(Done))
      outcome.issues should be(Nil)
    }

    "handle DeactivateBpmDiagram" in {
      val id = generateId
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val storeEntity = generateBpmDiagram(id = id, active = false)
      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
      val outcome = driver.run(DeactivateBpmDiagram(id))
      val entityToBe = storeEntity.copy(updatedAt = storeOutcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
      outcome.replies.head should be(BpmDiagramInDeactivatedState(id))
      outcome.events.size should ===(0)
      outcome.state.entity should  ===(Some(entityToBe))
    }

    "handle ActivateBpmDiagram" in {
      val id = generateId
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val storeEntity = generateBpmDiagram(id = id, active = false)
      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
      val outcome = driver.run(ActivateBpmDiagram(id))
      val entityToBe = storeEntity.copy(updatedAt = outcome.replies.head.asInstanceOf[BpmDiagram].updatedAt, active = true)
      outcome.events should ===(List(BpmDiagramActivated(id, entityToBe.updatedAt)))
      outcome.state.entity should  ===(Some(entityToBe))
      outcome.replies should ===(List(entityToBe))
      outcome.issues should be(Nil)
    }

    "handle GetBpmDiagram" in {
      val id = generateId
      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
      val storeEntity = generateBpmDiagram(id = id, active = false)
      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
      val outcome = driver.run(GetBpmDiagram)
      val entityToBe = storeEntity.copy(updatedAt = storeOutcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
      outcome.events should ===(Nil)
      outcome.state.entity should  ===(Some(entityToBe))
      outcome.replies should ===(List(Some(entityToBe)))
      outcome.issues should be(Nil)
    }

  }

}
