package test.axon.bpm.repository.typed_impl

import java.time.OffsetDateTime

import axon.bpm.repository.api.model.{BpmDiagram, BpmDiagramUpdate}
import test.annette.shared.RandomGenerator

trait BpmDiagramGenerator extends RandomGenerator {

  def generateBpmDiagramUpdate(
      id: String = generateId,
      name: String = generateSentence(),
      description: String = generateText(),
      notation: String = "BPMN",
      xml: String = "<xml>Some xml data</xml>"
  ) = BpmDiagramUpdate(
    id = id,
    name = name,
    description = Some(description),
    notation = notation,
    xml = xml
  )

  def generateBpmDiagram(
      id: String = generateId,
      name: String = generateSentence(),
      description: String = generateText(),
      notation: String = "BPMN",
      xml: String = "<xml>Some xml data</xml>",
      updatedAt: OffsetDateTime = OffsetDateTime.now,
      active: Boolean = true
  ) = BpmDiagram(
    id = id,
    name = name,
    description = Some(description),
    notation = notation,
    xml = xml,
    updatedAt = updatedAt,
    active = active
  )

}
