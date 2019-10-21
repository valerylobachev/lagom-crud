package test.axon.bpm.repository.impl
import axon.bpm.repository.api.model.{BpmDiagram}

import scala.util.Random

object TestData {

  def bpmDiagram(id: String = "id", name: String = "name", description: String = "description") = {
    BpmDiagram(id, name, Some(description), "BPMN", xml(id, name, description))
  }

  def xml(id: String = "id", name: String = "name", description: String = "description") = s"""|<?xml version="1.0" encoding="UTF-8"?>
                     |<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                     |                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                     |                  xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                     |                  xmlns:camunda="http://camunda.org/schema/1.0/bpmn" id="Definitions_1"
                     |                  targetNamespace="http://bpmn.io/schema/bpmn" exporter="Camunda Modeler" exporterVersion="1.11.3">
                     |    <bpmn:process id="$id" name="$name" isExecutable="true">
                     |        <bpmn:documentation>$description</bpmn:documentation>
                     |        <bpmn:startEvent id="StartEvent_1">
                     |            <bpmn:outgoing>SequenceFlow_07tsabf</bpmn:outgoing>
                     |        </bpmn:startEvent>
                     |        <bpmn:sequenceFlow id="SequenceFlow_07tsabf" sourceRef="StartEvent_1" targetRef="Task_17fss0p"/>
                     |        <bpmn:sequenceFlow id="SequenceFlow_117trsf" sourceRef="Task_17fss0p" targetRef="Task_1l638jw"/>
                     |        <bpmn:sequenceFlow id="SequenceFlow_0voctu5" sourceRef="Task_1l638jw" targetRef="Task_0to6q24"/>
                     |        <bpmn:userTask id="Task_17fss0p" name="Задача проектного процесса 1" camunda:formKey="projectForm"
                     |                       camunda:assignee="$${initiatorId}">
                     |            <bpmn:incoming>SequenceFlow_07tsabf</bpmn:incoming>
                     |            <bpmn:outgoing>SequenceFlow_117trsf</bpmn:outgoing>
                     |        </bpmn:userTask>
                     |        <bpmn:userTask id="Task_1l638jw" name="Задача проектного процесса 2" camunda:formKey="projectForm2"
                     |                       camunda:assignee="$${initiatorId}">
                     |            <bpmn:incoming>SequenceFlow_117trsf</bpmn:incoming>
                     |            <bpmn:outgoing>SequenceFlow_0voctu5</bpmn:outgoing>
                     |        </bpmn:userTask>
                     |        <bpmn:userTask id="Task_0to6q24" name="Задача проектного процесса 3" camunda:formKey="projectForm3"
                     |                       camunda:assignee="$${initiatorId}">
                     |            <bpmn:incoming>SequenceFlow_0voctu5</bpmn:incoming>
                     |            <bpmn:outgoing>SequenceFlow_0fgwq9a</bpmn:outgoing>
                     |        </bpmn:userTask>
                     |        <bpmn:endEvent id="EndEvent_0cl37qe">
                     |            <bpmn:incoming>SequenceFlow_0fgwq9a</bpmn:incoming>
                     |        </bpmn:endEvent>
                     |        <bpmn:sequenceFlow id="SequenceFlow_0fgwq9a" sourceRef="Task_0to6q24" targetRef="EndEvent_0cl37qe"/>
                     |    </bpmn:process>
                     |    <bpmndi:BPMNDiagram id="BPMNDiagram_1">
                     |        <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="projectProcess">
                     |            <bpmndi:BPMNShape id="_BPMNShape_StartEvent_2" bpmnElement="StartEvent_1">
                     |                <dc:Bounds x="173" y="102" width="36" height="36"/>
                     |            </bpmndi:BPMNShape>
                     |            <bpmndi:BPMNEdge id="SequenceFlow_07tsabf_di" bpmnElement="SequenceFlow_07tsabf">
                     |                <di:waypoint x="209" y="120"/>
                     |                <di:waypoint x="259" y="120"/>
                     |                <bpmndi:BPMNLabel>
                     |                    <dc:Bounds x="234" y="98.5" width="0" height="13"/>
                     |                </bpmndi:BPMNLabel>
                     |            </bpmndi:BPMNEdge>
                     |            <bpmndi:BPMNEdge id="SequenceFlow_117trsf_di" bpmnElement="SequenceFlow_117trsf">
                     |                <di:waypoint x="359" y="120"/>
                     |                <di:waypoint x="409" y="120"/>
                     |                <bpmndi:BPMNLabel>
                     |                    <dc:Bounds x="384" y="98.5" width="0" height="13"/>
                     |                </bpmndi:BPMNLabel>
                     |            </bpmndi:BPMNEdge>
                     |            <bpmndi:BPMNEdge id="SequenceFlow_0voctu5_di" bpmnElement="SequenceFlow_0voctu5">
                     |                <di:waypoint x="509" y="120"/>
                     |                <di:waypoint x="559" y="120"/>
                     |                <bpmndi:BPMNLabel>
                     |                    <dc:Bounds x="534" y="98.5" width="0" height="13"/>
                     |                </bpmndi:BPMNLabel>
                     |            </bpmndi:BPMNEdge>
                     |            <bpmndi:BPMNShape id="UserTask_0p0eaqo_di" bpmnElement="Task_17fss0p">
                     |                <dc:Bounds x="259" y="80" width="100" height="80"/>
                     |            </bpmndi:BPMNShape>
                     |            <bpmndi:BPMNShape id="UserTask_02djmjg_di" bpmnElement="Task_1l638jw">
                     |                <dc:Bounds x="409" y="80" width="100" height="80"/>
                     |            </bpmndi:BPMNShape>
                     |            <bpmndi:BPMNShape id="UserTask_1me6zcg_di" bpmnElement="Task_0to6q24">
                     |                <dc:Bounds x="559" y="80" width="100" height="80"/>
                     |            </bpmndi:BPMNShape>
                     |            <bpmndi:BPMNShape id="EndEvent_0cl37qe_di" bpmnElement="EndEvent_0cl37qe">
                     |                <dc:Bounds x="709" y="102" width="36" height="36"/>
                     |                <bpmndi:BPMNLabel>
                     |                    <dc:Bounds x="727" y="141" width="0" height="13"/>
                     |                </bpmndi:BPMNLabel>
                     |            </bpmndi:BPMNShape>
                     |            <bpmndi:BPMNEdge id="SequenceFlow_0fgwq9a_di" bpmnElement="SequenceFlow_0fgwq9a">
                     |                <di:waypoint x="659" y="120"/>
                     |                <di:waypoint x="709" y="120"/>
                     |                <bpmndi:BPMNLabel>
                     |                    <dc:Bounds x="684" y="98" width="0" height="13"/>
                     |                </bpmndi:BPMNLabel>
                     |            </bpmndi:BPMNEdge>
                     |        </bpmndi:BPMNPlane>
                     |    </bpmndi:BPMNDiagram>
                     |</bpmn:definitions>
                  """.stripMargin

}
