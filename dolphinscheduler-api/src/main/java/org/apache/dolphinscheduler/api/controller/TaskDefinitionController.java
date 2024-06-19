/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.api.controller;

import static org.apache.dolphinscheduler.api.enums.Status.CREATE_TASK_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_TASK_DEFINE_BY_CODE_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.DELETE_TASK_DEFINITION_VERSION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_DETAIL_OF_TASK_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_DEFINITION_LIST_PAGING_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.QUERY_TASK_DEFINITION_VERSIONS_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.RELEASE_TASK_DEFINITION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.SWITCH_TASK_DEFINITION_VERSION_ERROR;
import static org.apache.dolphinscheduler.api.enums.Status.UPDATE_TASK_DEFINITION_ERROR;

import org.apache.dolphinscheduler.api.audit.OperatorLog;
import org.apache.dolphinscheduler.api.audit.enums.AuditType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.TaskDefinitionService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * task definition controller
 */
@Tag(name = "TASK_DEFINITION_TAG")
@RestController
@RequestMapping("projects/{projectCode}/task-definition")
public class TaskDefinitionController extends BaseController {

    @Autowired
    private TaskDefinitionService taskDefinitionService;

    /**
     * create task definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param taskDefinitionJson task definition json
     * @return create result code
     */
    @Operation(summary = "save", description = "CREATE_TASK_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = long.class)),
            @Parameter(name = "taskDefinitionJson", description = "TASK_DEFINITION_JSON", required = true, schema = @Schema(implementation = String.class))
    })
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_TASK_DEFINITION_ERROR)
    @OperatorLog(auditType = AuditType.TASK_CREATE)
    public Result createTaskDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                       @RequestParam(value = "taskDefinitionJson", required = true) String taskDefinitionJson) {
        Map<String, Object> result =
                taskDefinitionService.createTaskDefinition(loginUser, projectCode, taskDefinitionJson);
        return returnDataList(result);
    }

    /**
     * create single task definition that binds the workflow
     *
     * @param loginUser             login user
     * @param projectCode           project code
     * @param processDefinitionCode process definition code
     * @param taskDefinitionJsonObj task definition json object
     * @param upstreamCodes         upstream task codes, sep comma
     * @return create result code
     */
    @Operation(summary = "saveSingle", description = "CREATE_SINGLE_TASK_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = long.class)),
            @Parameter(name = "processDefinitionCode", description = "PROCESS_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class)),
            @Parameter(name = "taskDefinitionJsonObj", description = "TASK_DEFINITION_JSON", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "upstreamCodes", description = "UPSTREAM_CODES", required = false, schema = @Schema(implementation = String.class))
    })
    @PostMapping("/save-single")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_TASK_DEFINITION_ERROR)
    @OperatorLog(auditType = AuditType.TASK_CREATE)
    public Result createTaskBindsWorkFlow(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                          @RequestParam(value = "processDefinitionCode", required = true) long processDefinitionCode,
                                          @RequestParam(value = "taskDefinitionJsonObj", required = true) String taskDefinitionJsonObj,
                                          @RequestParam(value = "upstreamCodes", required = false) String upstreamCodes) {
        Map<String, Object> result = taskDefinitionService.createTaskBindsWorkFlow(loginUser, projectCode,
                processDefinitionCode, taskDefinitionJsonObj, StringUtils.defaultString(upstreamCodes));
        return returnDataList(result);
    }

    /**
     * update task definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param code task definition code
     * @param taskDefinitionJsonObj task definition json object
     * @return update result code
     */
    @Operation(summary = "update", description = "UPDATE_TASK_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = long.class)),
            @Parameter(name = "code", description = "TASK_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "1")),
            @Parameter(name = "taskDefinitionJsonObj", description = "TASK_DEFINITION_JSON", required = true, schema = @Schema(implementation = String.class))
    })
    @PutMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_TASK_DEFINITION_ERROR)
    @OperatorLog(auditType = AuditType.TASK_UPDATE)
    public Result updateTaskDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                       @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                       @PathVariable(value = "code") long code,
                                       @RequestParam(value = "taskDefinitionJsonObj", required = true) String taskDefinitionJsonObj) {
        Map<String, Object> result =
                taskDefinitionService.updateTaskDefinition(loginUser, projectCode, code, taskDefinitionJsonObj);
        return returnDataList(result);
    }

    /**
     * update task definition
     *
     * @param loginUser             login user
     * @param projectCode           project code
     * @param code                  task definition code
     * @param taskDefinitionJsonObj task definition json object
     * @param upstreamCodes         upstream task codes, sep comma
     * @return update result code
     */
    @Operation(summary = "updateWithUpstream", description = "UPDATE_TASK_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true, schema = @Schema(implementation = long.class)),
            @Parameter(name = "code", description = "TASK_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "1")),
            @Parameter(name = "taskDefinitionJsonObj", description = "TASK_DEFINITION_JSON", required = true, schema = @Schema(implementation = String.class)),
            @Parameter(name = "upstreamCodes", description = "UPSTREAM_CODES", required = false, schema = @Schema(implementation = String.class))
    })
    @PutMapping(value = "/{code}/with-upstream")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_TASK_DEFINITION_ERROR)
    @OperatorLog(auditType = AuditType.TASK_UPDATE)
    public Result updateTaskWithUpstream(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                         @PathVariable(value = "code") long code,
                                         @RequestParam(value = "taskDefinitionJsonObj", required = true) String taskDefinitionJsonObj,
                                         @RequestParam(value = "upstreamCodes", required = false) String upstreamCodes) {
        Map<String, Object> result = taskDefinitionService.updateTaskWithUpstream(loginUser, projectCode, code,
                taskDefinitionJsonObj, upstreamCodes);
        return returnDataList(result);
    }

    /**
     * query task definition version paging list info
     *
     * @param loginUser login user info
     * @param projectCode project code
     * @param code task definition code
     * @param pageNo the task definition version list current page number
     * @param pageSize the task definition version list page size
     * @param code the task definition code
     * @return the task definition version list
     */
    @Operation(summary = "queryVersions", description = "QUERY_TASK_DEFINITION_VERSIONS_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "TASK_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "1")),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "10"))
    })
    @GetMapping(value = "/{code}/versions")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_DEFINITION_VERSIONS_ERROR)
    public Result queryTaskDefinitionVersions(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                              @PathVariable(value = "code") long code,
                                              @RequestParam(value = "pageNo") int pageNo,
                                              @RequestParam(value = "pageSize") int pageSize) {
        checkPageParams(pageNo, pageSize);
        return taskDefinitionService.queryTaskDefinitionVersions(loginUser, projectCode, code, pageNo, pageSize);
    }

    /**
     * switch task definition version
     *
     * @param loginUser login user info
     * @param projectCode project code
     * @param code the task definition code
     * @param version the version user want to switch
     * @return switch version result code
     */
    @Operation(summary = "switchVersion", description = "SWITCH_TASK_DEFINITION_VERSION_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "TASK_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "1")),
            @Parameter(name = "version", description = "VERSION", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @GetMapping(value = "/{code}/versions/{version}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(SWITCH_TASK_DEFINITION_VERSION_ERROR)
    @OperatorLog(auditType = AuditType.TASK_SWITCH_VERSION)
    public Result switchTaskDefinitionVersion(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                              @PathVariable(value = "code") long code,
                                              @PathVariable(value = "version") int version) {
        Map<String, Object> result = taskDefinitionService.switchVersion(loginUser, projectCode, code, version);
        return returnDataList(result);
    }

    /**
     * delete the certain task definition version by version and code
     *
     * @param loginUser login user info
     * @param projectCode project code
     * @param code the task definition code
     * @param version the task definition version user want to delete
     * @return delete version result code
     */
    @Operation(summary = "deleteVersion", description = "DELETE_TASK_DEFINITION_VERSION_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "TASK_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "1")),
            @Parameter(name = "version", description = "VERSION", required = true, schema = @Schema(implementation = int.class, example = "100"))
    })
    @DeleteMapping(value = "/{code}/versions/{version}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_TASK_DEFINITION_VERSION_ERROR)
    @OperatorLog(auditType = AuditType.TASK_DELETE_VERSION)
    public Result deleteTaskDefinitionVersion(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                              @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                              @PathVariable(value = "code") long code,
                                              @PathVariable(value = "version") int version) {
        Map<String, Object> result =
                taskDefinitionService.deleteByCodeAndVersion(loginUser, projectCode, code, version);
        return returnDataList(result);
    }

    /**
     * delete task definition by code
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param code the task definition code
     * @return delete result code
     */
    @Operation(summary = "deleteTaskDefinition", description = "DELETE_TASK_DEFINITION_BY_CODE_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "TASK_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "1"))
    })
    @DeleteMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_TASK_DEFINE_BY_CODE_ERROR)
    @OperatorLog(auditType = AuditType.TASK_DELETE)
    public Result deleteTaskDefinitionByCode(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                             @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                             @PathVariable(value = "code") long code) {
        taskDefinitionService.deleteTaskDefinitionByCode(loginUser, code);
        return new Result(Status.SUCCESS);
    }

    /**
     * query detail of task definition by code
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param code the task definition code
     * @return task definition detail
     */
    @Operation(summary = "queryTaskDefinitionByCode", description = "QUERY_TASK_DEFINITION_DETAIL_NOTES")
    @Parameters({
            @Parameter(name = "code", description = "TASK_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "1"))
    })
    @GetMapping(value = "/{code}")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DETAIL_OF_TASK_DEFINITION_ERROR)
    public Result queryTaskDefinitionDetail(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                            @PathVariable(value = "code") long code) {
        Map<String, Object> result = taskDefinitionService.queryTaskDefinitionDetail(loginUser, projectCode, code);
        return returnDataList(result);
    }

    /**
     * query task definition list paging
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param searchWorkflowName searchWorkflowName
     * @param searchTaskName searchTaskName
     * @param taskType taskType
     * @param taskExecuteType taskExecuteType
     * @param pageNo page number
     * @param pageSize page size
     * @return task definition page
     */
    @Operation(summary = "queryTaskDefinitionListPaging", description = "QUERY_TASK_DEFINITION_LIST_PAGING_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROJECT_CODE", required = false, schema = @Schema(implementation = long.class)),
            @Parameter(name = "searchWorkflowName", description = "SEARCH_WORKFLOW_NAME", required = false, schema = @Schema(implementation = String.class)),
            @Parameter(name = "searchTaskName", description = "SEARCH_TASK_NAME", required = false, schema = @Schema(implementation = String.class)),
            @Parameter(name = "taskType", description = "TASK_TYPE", required = false, schema = @Schema(implementation = String.class, example = "SHELL")),
            @Parameter(name = "taskExecuteType", description = "TASK_EXECUTE_TYPE", required = false, schema = @Schema(implementation = TaskExecuteType.class, example = "STREAM")),
            @Parameter(name = "pageNo", description = "PAGE_NO", required = true, schema = @Schema(implementation = int.class, example = "1")),
            @Parameter(name = "pageSize", description = "PAGE_SIZE", required = true, schema = @Schema(implementation = int.class, example = "10"))
    })
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_TASK_DEFINITION_LIST_PAGING_ERROR)
    public Result queryTaskDefinitionListPaging(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                                @RequestParam(value = "searchTaskName", required = false) String searchTaskName,
                                                @RequestParam(value = "taskType", required = false) String taskType,
                                                @RequestParam(value = "taskExecuteType", required = false, defaultValue = "BATCH") TaskExecuteType taskExecuteType,
                                                @RequestParam("pageNo") Integer pageNo,
                                                @RequestParam("pageSize") Integer pageSize) {
        checkPageParams(pageNo, pageSize);
        searchTaskName = ParameterUtils.handleEscapes(searchTaskName);
        return taskDefinitionService.queryTaskDefinitionListPaging(loginUser, projectCode,
                searchTaskName, taskType, taskExecuteType, pageNo, pageSize);
    }

    /**
     * gen task code list
     *
     * @param loginUser login user
     * @param genNum gen num
     * @return task code list
     */
    @Operation(summary = "genTaskCodeList", description = "GEN_TASK_CODE_LIST_NOTES")
    @Parameters({
            @Parameter(name = "genNum", description = "GEN_NUM", required = true, schema = @Schema(implementation = int.class, example = "1"))
    })
    @GetMapping(value = "/gen-task-codes")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(LOGIN_USER_QUERY_PROJECT_LIST_PAGING_ERROR)
    public Result genTaskCodeList(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                  @RequestParam("genNum") Integer genNum) {
        Map<String, Object> result = taskDefinitionService.genTaskCodeList(genNum);
        return returnDataList(result);
    }

    /**
     * release task definition
     *
     * @param loginUser login user
     * @param projectCode project code
     * @param code task definition code
     * @param releaseState releaseState
     * @return update result code
     */
    @Operation(summary = "releaseTaskDefinition", description = "RELEASE_TASK_DEFINITION_NOTES")
    @Parameters({
            @Parameter(name = "projectCode", description = "PROCESS_DEFINITION_NAME", required = true, schema = @Schema(implementation = long.class)),
            @Parameter(name = "code", description = "TASK_DEFINITION_CODE", required = true, schema = @Schema(implementation = long.class, example = "123456789")),
            @Parameter(name = "releaseState", description = "RELEASE_PROCESS_DEFINITION_NOTES", required = true, schema = @Schema(implementation = ReleaseState.class))
    })
    @PostMapping(value = "/{code}/release")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RELEASE_TASK_DEFINITION_ERROR)
    @OperatorLog(auditType = AuditType.TASK_RELEASE)
    public Result releaseTaskDefinition(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @Parameter(name = "projectCode", description = "PROJECT_CODE", required = true) @PathVariable long projectCode,
                                        @PathVariable(value = "code", required = true) long code,
                                        @RequestParam(value = "releaseState", required = true, defaultValue = "OFFLINE") ReleaseState releaseState) {
        Map<String, Object> result =
                taskDefinitionService.releaseTaskDefinition(loginUser, projectCode, code, releaseState);
        return returnDataList(result);
    }
}
