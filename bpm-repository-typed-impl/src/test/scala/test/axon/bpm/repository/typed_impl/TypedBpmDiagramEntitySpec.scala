package test.axon.bpm.repository.typed_impl

import java.util.UUID

import akka.actor.testkit.typed.scaladsl.{LogCapturing, ScalaTestWithActorTestKit}
import akka.persistence.typed.PersistenceId
import axon.bpm.repository.api.model._
import axon.bpm.repository.typed_impl.bpmdiagram.BpmDiagramEntity
import org.scalatest.WordSpecLike

class TypedBpmDiagramEntitySpec extends ScalaTestWithActorTestKit(s"""
    |akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
    |akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
    |akka.persistence.snapshot-store.local.dir = "target/snapshot-${UUID.randomUUID().toString}"
    |""".stripMargin) with WordSpecLike with LogCapturing with BpmDiagramGenerator {

  "BpmDiagramEntity" must {

    "store diagram" in {

      val id = generateId
      val entity = generateBpmDiagram(id = id)
      val probe = createTestProbe[BpmDiagramEntity.Confirmation]()
      val bpmDiagramEntity = spawn(BpmDiagramEntity(PersistenceId("BpmDiagramEntity", id)))
      bpmDiagramEntity ! BpmDiagramEntity.StoreBpmDiagram(entity, probe.ref)

      val result = probe.receiveMessage()
      result shouldBe a[BpmDiagramEntity.Success]
      val resultEntity = result.asInstanceOf[BpmDiagramEntity.Success].entity
      resultEntity shouldBe entity
    }

    "create diagram" in {

      val id = generateId
      val entity = generateBpmDiagramUpdate(id = id)
      val probe = createTestProbe[BpmDiagramEntity.Confirmation]()
      val bpmDiagramEntity = spawn(BpmDiagramEntity(PersistenceId("BpmDiagramEntity", id)))
      bpmDiagramEntity ! BpmDiagramEntity.CreateBpmDiagram(entity, probe.ref)

      val result = probe.receiveMessage()
      result shouldBe a[BpmDiagramEntity.Success]
      val resultEntity = result.asInstanceOf[BpmDiagramEntity.Success].entity
      resultEntity shouldBe BpmDiagram(entity).copy(updatedAt = resultEntity.updatedAt)
    }

    "create diagram with same id" in {

      val id = generateId
      val entity = generateBpmDiagramUpdate(id = id)
      val probe = createTestProbe[BpmDiagramEntity.Confirmation]()
      val bpmDiagramEntity = spawn(BpmDiagramEntity(PersistenceId("BpmDiagramEntity", id)))
      bpmDiagramEntity ! BpmDiagramEntity.CreateBpmDiagram(entity, probe.ref)
      val result = probe.receiveMessage()
      result shouldBe a[BpmDiagramEntity.Success]
      val resultEntity = result.asInstanceOf[BpmDiagramEntity.Success].entity
      resultEntity shouldBe BpmDiagram(entity).copy(updatedAt = resultEntity.updatedAt)

      bpmDiagramEntity ! BpmDiagramEntity.CreateBpmDiagram(entity, probe.ref)
      val result2 = probe.receiveMessage()
      result2 shouldBe a[BpmDiagramEntity.AlreadyExist.type ]
    }
  }

//    "in initial mode" must {
//
//    "handle StoreBpmDiagram" in {
//      val id = generateId
//      val entity = generateBpmDiagramUpdate(id = id)
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val outcome = driver.run(StoreBpmDiagram(entity))
//      val entityToBe = BpmDiagram(entity)
//        .copy(updatedAt = outcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
//      outcome.events should ===(List(BpmDiagramStored(entityToBe)))
//      outcome.state.entity should ===(Some(entityToBe))
//      outcome.replies should ===(List(entityToBe))
//      outcome.issues should be(Nil)
//    }
//
//    "handle CreateBpmDiagram" in {
//      val id = generateId
//      val entity = generateBpmDiagramUpdate(id = id)
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val outcome = driver.run(CreateBpmDiagram(entity))
//      val entityToBe = BpmDiagram(entity)
//        .copy(updatedAt = outcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
//      outcome.events should ===(List(BpmDiagramCreated(entityToBe)))
//      outcome.state.entity should ===(Some(entityToBe))
//      outcome.replies should ===(List(entityToBe))
//      outcome.issues should be(Nil)
//    }
//
//    "handle UpdateBpmDiagram" in {
//      val id = generateId
//      val entity = generateBpmDiagramUpdate(id = id)
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val outcome = driver.run(UpdateBpmDiagram(entity))
//      outcome.replies.head should be(BpmDiagramNotFound(id))
//      outcome.events.size should ===(0)
//    }
//
//    "handle DeleteBpmDiagram" in {
//      val id = generateId
//      val entity = generateBpmDiagramUpdate(id = id)
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val outcome = driver.run(DeleteBpmDiagram(entity.id))
//      outcome.replies.head should be(BpmDiagramNotFound(id))
//      outcome.events.size should ===(0)
//    }
//
//    "handle DeactivateBpmDiagram" in {
//      val id = generateId
//      val entity = generateBpmDiagramUpdate(id = id)
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val outcome = driver.run(DeactivateBpmDiagram(entity.id))
//      outcome.replies.head should be(BpmDiagramNotFound(id))
//      outcome.events.size should ===(0)
//    }
//
//    "handle ActivateBpmDiagram" in {
//      val id = generateId
//      val entity = generateBpmDiagramUpdate(id = id)
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val outcome = driver.run(ActivateBpmDiagram(entity.id))
//      outcome.replies.head should be(BpmDiagramNotFound(id))
//      outcome.events.size should ===(0)
//    }
//
//    "handle GetBpmDiagram" in {
//      val id = generateId
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val outcome = driver.run(GetBpmDiagram)
//      outcome.events should be(Nil)
//      outcome.state.entity should ===(None)
//      outcome.replies should ===(List(None))
//      outcome.issues should be(Nil)
//    }
//  }
//
//  "in active mode" must {
//
//    "handle StoreBpmDiagram" in {
//      val id = generateId
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val storeEntity = generateBpmDiagramUpdate(id = id)
//      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
//      val entity = generateBpmDiagramUpdate(id = id)
//      val outcome = driver.run(StoreBpmDiagram(entity))
//      val entityToBe = BpmDiagram(entity)
//        .copy(updatedAt = outcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
//      outcome.events should ===(List(BpmDiagramStored(entityToBe)))
//      outcome.state.entity should ===(Some(entityToBe))
//      outcome.replies should ===(List(entityToBe))
//      outcome.issues should be(Nil)
//    }
//
//    "handle CreateBpmDiagram" in {
//      val id = generateId
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val storeEntity = generateBpmDiagramUpdate(id = id)
//      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
//      val entity = generateBpmDiagramUpdate(id = id)
//      val outcome = driver.run(CreateBpmDiagram(entity))
//      val entityToBe = BpmDiagram(storeEntity)
//        .copy(updatedAt = storeOutcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
//      outcome.replies.head should be(BpmDiagramAlreadyExist(id))
//      outcome.events.size should ===(0)
//      outcome.state.entity should ===(Some(entityToBe))
//    }
//
//    "handle UpdateBpmDiagram" in {
//      val id = generateId
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val storeEntity = generateBpmDiagramUpdate(id = id)
//      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
//      val entity = generateBpmDiagramUpdate(id = id)
//      val outcome = driver.run(UpdateBpmDiagram(entity))
//      val entityToBe = BpmDiagram(entity)
//        .copy(updatedAt = outcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
//      outcome.events should ===(List(BpmDiagramUpdated(entityToBe)))
//      outcome.state.entity should ===(Some(entityToBe))
//      outcome.replies should ===(List(entityToBe))
//      outcome.issues should be(Nil)
//    }
//
//    "handle DeleteBpmDiagram" in {
//      val id = generateId
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val storeEntity = generateBpmDiagramUpdate(id = id)
//      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
//      val outcome = driver.run(DeleteBpmDiagram(id))
//      outcome.events should ===(List(BpmDiagramDeleted(id)))
//      outcome.state.entity should ===(None)
//      outcome.replies should ===(List(Done))
//      outcome.issues should be(Nil)
//    }
//
//    "handle DeactivateBpmDiagram" in {
//      val id = generateId
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val storeEntity = generateBpmDiagramUpdate(id = id)
//      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
//      val outcome = driver.run(DeactivateBpmDiagram(id))
//      val entityToBe = BpmDiagram(storeEntity)
//        .copy(updatedAt = outcome.replies.head.asInstanceOf[BpmDiagram].updatedAt, active = false)
//      outcome.events should ===(List(BpmDiagramDeactivated(id, entityToBe.updatedAt)))
//      outcome.state.entity should ===(Some(entityToBe))
//      outcome.replies should ===(List(entityToBe))
//      outcome.issues should be(Nil)
//    }
//
//    "handle ActivateBpmDiagram" in {
//      val id = generateId
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val storeEntity = generateBpmDiagramUpdate(id = id)
//      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
//      val outcome = driver.run(ActivateBpmDiagram(id))
//      val entityToBe = BpmDiagram(storeEntity)
//        .copy(updatedAt = storeOutcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
//      outcome.replies.head should be(BpmDiagramAlreadyActive(id))
//      outcome.events.size should ===(0)
//      outcome.state.entity should ===(Some(entityToBe))
//    }
//
//    "handle GetBpmDiagram" in {
//      val id = generateId
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val storeEntity = generateBpmDiagramUpdate(id = id)
//      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity))
//      val outcome = driver.run(GetBpmDiagram)
//      val entityToBe = BpmDiagram(storeEntity)
//        .copy(updatedAt = storeOutcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
//      outcome.events should ===(Nil)
//      outcome.state.entity should ===(Some(entityToBe))
//      outcome.replies should ===(List(Some(entityToBe)))
//      outcome.issues should be(Nil)
//    }
//  }
//
//  "in deactivated mode" must {
//
//    "handle StoreBpmDiagram" in {
//      val id = generateId
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val storeEntity = generateBpmDiagramUpdate(id = id)
//      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity), DeactivateBpmDiagram(id))
//      val entity = generateBpmDiagramUpdate(id = id)
//      val outcome = driver.run(StoreBpmDiagram(entity))
//      val entityToBe = BpmDiagram(entity).copy(updatedAt = outcome.replies.head.asInstanceOf[BpmDiagram].updatedAt)
//      outcome.events should ===(List(BpmDiagramStored(entityToBe)))
//      outcome.state.entity should ===(Some(entityToBe))
//      outcome.replies should ===(List(entityToBe))
//      outcome.issues should be(Nil)
//    }
//
//    "handle CreateBpmDiagram" in {
//      val id = generateId
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val storeEntity = generateBpmDiagramUpdate(id = id)
//      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity), DeactivateBpmDiagram(id))
//      val entity = generateBpmDiagramUpdate(id = id)
//      val outcome = driver.run(CreateBpmDiagram(entity))
//      val entityToBe = BpmDiagram(storeEntity)
//        .copy(
//          updatedAt = storeOutcome.replies.last.asInstanceOf[BpmDiagram].updatedAt,
//          active = false
//        )
//      outcome.replies.head should be(BpmDiagramInDeactivatedState(id))
//      outcome.events.size should ===(0)
//      outcome.state.entity should ===(Some(entityToBe))
//    }
//
//    "handle UpdateBpmDiagram" in {
//      val id = generateId
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val storeEntity = generateBpmDiagramUpdate(id = id)
//      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity), DeactivateBpmDiagram(id))
//      val entity = generateBpmDiagramUpdate(id = id)
//      val outcome = driver.run(UpdateBpmDiagram(entity))
//      val entityToBe = BpmDiagram(storeEntity)
//        .copy(
//          updatedAt = storeOutcome.replies.last.asInstanceOf[BpmDiagram].updatedAt,
//          active = false
//        )
//      outcome.replies.head should be(BpmDiagramInDeactivatedState(id))
//      outcome.events.size should ===(0)
//      outcome.state.entity should ===(Some(entityToBe))
//    }
//
//    "handle DeleteBpmDiagram" in {
//      val id = generateId
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val storeEntity = generateBpmDiagramUpdate(id = id)
//      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity), DeactivateBpmDiagram(id))
//      val outcome = driver.run(DeleteBpmDiagram(id))
//      outcome.events should ===(List(BpmDiagramDeleted(id)))
//      outcome.state.entity should ===(None)
//      outcome.replies should ===(List(Done))
//      outcome.issues should be(Nil)
//    }
//
//    "handle DeactivateBpmDiagram" in {
//      val id = generateId
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val storeEntity = generateBpmDiagramUpdate(id = id)
//      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity), DeactivateBpmDiagram(id))
//      val outcome = driver.run(DeactivateBpmDiagram(id))
//      val entityToBe = BpmDiagram(storeEntity)
//        .copy(
//          updatedAt = storeOutcome.replies.last.asInstanceOf[BpmDiagram].updatedAt,
//          active = false
//        )
//      outcome.replies.head should be(BpmDiagramInDeactivatedState(id))
//      outcome.events.size should ===(0)
//      outcome.state.entity should ===(Some(entityToBe))
//    }
//
//    "handle ActivateBpmDiagram" in {
//      val id = generateId
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val storeEntity = generateBpmDiagramUpdate(id = id)
//      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity), DeactivateBpmDiagram(id))
//      val outcome = driver.run(ActivateBpmDiagram(id))
//      val entityToBe = BpmDiagram(storeEntity)
//        .copy(
//          updatedAt = outcome.replies.head.asInstanceOf[BpmDiagram].updatedAt,
//          active = true
//        )
//      outcome.events should ===(List(BpmDiagramActivated(id, entityToBe.updatedAt)))
//      outcome.state.entity should ===(Some(entityToBe))
//      outcome.replies should ===(List(entityToBe))
//      outcome.issues should be(Nil)
//    }
//
//    "handle GetBpmDiagram" in {
//      val id = generateId
//      val driver = new PersistentEntityTestDriver(system, new BpmDiagramEntity, id)
//      val storeEntity = generateBpmDiagramUpdate(id = id)
//      val storeOutcome = driver.run(StoreBpmDiagram(storeEntity), DeactivateBpmDiagram(id))
//      val outcome = driver.run(GetBpmDiagram)
//      val entityToBe = BpmDiagram(storeEntity)
//        .copy(
//          updatedAt = storeOutcome.replies.last.asInstanceOf[BpmDiagram].updatedAt,
//          active = false
//        )
//      outcome.events should ===(Nil)
//      outcome.state.entity should ===(Some(entityToBe))
//      outcome.replies should ===(List(Some(entityToBe)))
//      outcome.issues should be(Nil)
//    }
//
//  }

}
