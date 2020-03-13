{

  <@lib.endpointInfo
      id = "updateTask"
      tag = "Task"
      desc = "Updates a task." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "TaskDto" />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful."
        examples = ['"example-1": {
                         "summary": "PUT /task/aTaskId/ Response",
                         "value": {
                           "name": "My Task",
                           "description": "This have to be done very urgent",
                           "priority" : 30,
                           "assignee" : "peter",
                           "owner" : "mary",
                           "delegationState" : "PENDING",
                           "due" : "2014-08-30T10:00:00.000+0200",
                           "followUp" : "2014-08-25T10:00:00.000+0200",
                           "parentTaskId" : "aParentTaskId",
                           "caseInstanceId" : "aCaseInstanceId",
                           "tenantId" : "tenantId"
                         }
                       }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if a not valid `delegationState` is supplied. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "If the corresponding task cannot be found." />

  }
}
