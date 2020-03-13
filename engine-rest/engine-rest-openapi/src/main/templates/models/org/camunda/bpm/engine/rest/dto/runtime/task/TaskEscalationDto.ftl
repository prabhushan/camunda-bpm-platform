{
  "type": "object",
  "properties": {

  <@lib.property
      name = "escalationCode"
      type = "string"
      desc = "An escalation code that indicates the predefined escalation. It is used to identify
              the BPMN escalation handler." />

    <@lib.property
        name = "variables"
        type = "object"
        dto = "VariableValueDto"
        additionalProperties = true
        desc = "A JSON object containing variable key-value pairs." />

  }
}
