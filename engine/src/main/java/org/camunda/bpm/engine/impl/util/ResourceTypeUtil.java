/*
 * Copyright © 2013-2019 camunda services GmbH and various authors (info@camunda.com)
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
package org.camunda.bpm.engine.impl.util;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.ProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.authorization.TaskPermissions;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;

public class ResourceTypeUtil {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  private static final Map<Integer, Class<? extends Enum<? extends Permission>>> PERMISSION_ENUMS = new HashMap<>();

  static {
    PERMISSION_ENUMS.put(Resources.BATCH.resourceType(), BatchPermissions.class);
    PERMISSION_ENUMS.put(Resources.PROCESS_DEFINITION.resourceType(), ProcessDefinitionPermissions.class);
    PERMISSION_ENUMS.put(Resources.PROCESS_INSTANCE.resourceType(), ProcessInstancePermissions.class);
    PERMISSION_ENUMS.put(Resources.TASK.resourceType(), TaskPermissions.class);

    // the rest
    for (Resource resource : Resources.values()) {
      int resourceType = resource.resourceType();
      if (!PERMISSION_ENUMS.containsKey(resourceType)) {
        PERMISSION_ENUMS.put(resourceType, Permissions.class);
      }
    }
  }

  public static boolean resourceIsContainedInArray(Integer resourceTypeId, Resource[] list) {
    for (Resource resource : list) {
      if (resourceTypeId == resource.resourceType()) {
        return true;
      }
    }
    return false;
  }

  public static Map<Integer, Class<? extends Enum<? extends Permission>>> getPermissionEnums() {
    return PERMISSION_ENUMS;
  }

  public static Permission[] getPermissionsByResourceType(int givenResourceType) {
    Class<? extends Enum<? extends Permission>> clazz = PERMISSION_ENUMS.get(givenResourceType);
    if (clazz == null) {
      return Permissions.values();
    }
    return ((Permission[]) clazz.getEnumConstants());
  }

  public static Permission getPermissionByNameAndResourceType(String permissionName, int resourceType) {
    for (Permission permission : getPermissionsByResourceType(resourceType)) {
      if (permission.getName().equals(permissionName)) {
        return permission;
      }
    }
    throw new BadUserRequestException(
        String.format("The permission '%s' is not valid for '%s' resource type.", permissionName, getResource(resourceType))
        );
  }

  public static Resource getResource(int resourceType) {
    for (Resource resource : Resources.values()) {
      if (resource.resourceType() == resourceType) {
        return resource;
      }
    }
    return null;
  }
}