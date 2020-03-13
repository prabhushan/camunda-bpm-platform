{

  <@lib.endpointInfo
      id = "claim"
      tag = "Task"
      desc = "Claims a task for a specific user.

              **Note:** The difference with the
              [Set Assignee](${docsUrl}/reference/rest/task/post-assignee/)
              method is that here a check is performed to see if the task already has a user
              assigned to it." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task to claim."/>

  ],

  <#assign action = "claims the task" >
  <@lib.requestBody
      mediaType = "application/json"
      dto = "UserIdDto"
      examples = ['"example-1": {
                     "summary": "Request Body",
                     "description": "POST `/task/anId/claim`",
                     "value": {
                       "userId": "aUserId"
                     }
                   }'] />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "Task with given id does not exist or claiming was not successful. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}
