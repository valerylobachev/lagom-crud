package test.axon.bpm.repository.impl

import axon.bpm.repository.api.model.BpmDiagramUpdate
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

}
