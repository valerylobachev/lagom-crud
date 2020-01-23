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

package axon.bpm.repository.typed_impl.bpmdiagram

import java.time.OffsetDateTime
import akka.Done
import axon.bpm.repository.api.model.{BpmDiagram, BpmDiagramId}
import com.datastax.driver.core.PreparedStatement
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}

import scala.concurrent.{ExecutionContext, Future}

private[typed_impl] class BpmDiagramEventProcessor(session: CassandraSession, readSide: CassandraReadSide, elastic: BpmDiagramElastic)(
    implicit ec: ExecutionContext
) extends ReadSideProcessor[BpmDiagramEntity.Event] {
  private var insertStatement: PreparedStatement = _
  private var updateStatement: PreparedStatement = _
  private var deleteStatement: PreparedStatement = _
  private var changeActiveStatusStatement: PreparedStatement = _

  def buildHandler = {
    readSide
      .builder[BpmDiagramEntity.Event]("bpmDiagramEventOffset")
      .setGlobalPrepare(createTables)
      .setPrepare(_ => prepareStatements())
      .setEventHandler[BpmDiagramEntity.BpmDiagramStored](e => updateEntity(e.event.bpmDiagram))
      .setEventHandler[BpmDiagramEntity.BpmDiagramCreated](e => insertEntity(e.event.bpmDiagram))
      .setEventHandler[BpmDiagramEntity.BpmDiagramUpdated](e => updateEntity(e.event.bpmDiagram))
      .setEventHandler[BpmDiagramEntity.BpmDiagramDeleted](e => deleteEntity(e.event.id))
      .setEventHandler[BpmDiagramEntity.BpmDiagramDeactivated](e => changeActiveStatus(e.event.id, e.event.updatedAt, active = false))
      .setEventHandler[BpmDiagramEntity.BpmDiagramActivated](e => changeActiveStatus(e.event.id, e.event.updatedAt, active = true))
      .build
  }

  def aggregateTags = BpmDiagramEntity.Event.Tag.allTags

  private def createTables() = {
    println("createTables started")
    for {
      index <- elastic.createBpmDiagramIndex
      create <- session.executeCreateTable("""
        CREATE TABLE IF NOT EXISTS bpm_diagrams (
          id text PRIMARY KEY,
          name text,
          description text,
          notation text,
          xml text,
          updated_at text,
          active boolean
        )
      """)

    } yield {
      println("createTables done")
      println(index)
      println(create)
      Done
    }
  }

  private def prepareStatements() = {
    println("prepareStatements started")
    for {
      insertStmt <- session.prepare("""
        | INSERT INTO bpm_diagrams(id, name, description, notation, xml, updated_at, active)
        |    VALUES (:id, :name, :description, :notation, :xml, :updated_at, :active)
      """.stripMargin)
      updateStmt <- session.prepare("""
        | UPDATE bpm_diagrams SET
        |   name = :name,
        |   description = :description,
        |   notation = :notation,
        |   xml = :xml,
        |   updated_at = :updated_at,
        |   active = :active
        | WHERE id = :id
        |""".stripMargin)
      changeActiveStatusStmt <- session.prepare("""
        | UPDATE bpm_diagrams SET
        |    updated_at = :updated_at,
        |   active = :active
        | WHERE id = :id
        |""".stripMargin)
      deleteStmt <- session.prepare("DELETE FROM bpm_diagrams WHERE id = :id")
    } yield {
      println("prepareStatements done")
      insertStatement = insertStmt
      updateStatement = updateStmt
      deleteStatement = deleteStmt
      changeActiveStatusStatement = changeActiveStatusStmt
      Done
    }
  }

  private def insertEntity(entity: BpmDiagram) = {
    println(s"insertEntity: ${entity.id}")
    for {
      _ <- elastic.indexBpmDiagram(entity)
    } yield {
      List(
        insertStatement
          .bind()
          .setString("id", entity.id)
          .setString("name", entity.name)
          .setString("description", entity.description.orNull)
          .setString("notation", entity.notation)
          .setString("xml", entity.xml)
          .setString("updated_at", entity.updatedAt.toString)
          .setBool("active", entity.active)
      )
    }
  }

  private def updateEntity(entity: BpmDiagram) = {
    println(s"updateEntity: ${entity.id}")
    for {
      _ <- elastic.indexBpmDiagram(entity)
    } yield {
      List(
        updateStatement
          .bind()
          .setString("id", entity.id)
          .setString("name", entity.name)
          .setString("description", entity.description.orNull)
          .setString("notation", entity.notation)
          .setString("xml", entity.xml)
          .setString("updated_at", entity.updatedAt.toString)
          .setBool("active", entity.active)
      )
    }
  }

  private def changeActiveStatus(id: BpmDiagramId, updatedAt: OffsetDateTime, active: Boolean) = {
    for {
      _ <- elastic.indexBpmDiagram(id)
    } yield {
      List(
        changeActiveStatusStatement
          .bind()
          .setString("id", id)
          .setString("updated_at", updatedAt.toString)
          .setBool("active", active)
      )
    }
  }

  private def deleteEntity(id: BpmDiagramId) = {
    for {
      _ <- elastic.deleteBpmDiagram(id)
    } yield {
      List(
        deleteStatement
          .bind()
          .setString("id", id)
      )
    }
  }

}
