/** Copyright (c) 2011, Intersect, Australia
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Intersect, Intersect's partners, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package au.org.intersect.sydma.webapp.permission;

import au.org.intersect.sydma.webapp.domain.AccessLevel;

/**
 * Enumeration of the different permission actions
 *
 * @version $Rev: 29 $
 */
public enum PermissionType
{
    VIEW_GROUP("viewGroup", PermissionAppliedType.GROUP, AccessLevel.VIEWING_ACCESS),
    EDIT_GROUP("editGroup", PermissionAppliedType.GROUP, AccessLevel.EDITING_ACCESS),
    CREATE_PROJECT("createProject", PermissionAppliedType.GROUP, AccessLevel.FULL_ACCESS),
    ASSIGN_PERMISSION("assignGroupPermissions", PermissionAppliedType.GROUP, AccessLevel.EDITING_ACCESS),
    ASSIGN_PROJECT_PERMISSION("assignProjectPermissions", PermissionAppliedType.PROJECT, AccessLevel.EDITING_ACCESS),
    ASSIGN_DATASET_PERMISSION("assignDatasetPermissions", PermissionAppliedType.DATASET, AccessLevel.EDITING_ACCESS),
    ASSIGN_DIRECTORY_PERMISSION("assignDirectoryPermissions", PermissionAppliedType.DIRECTORY,
            AccessLevel.EDITING_ACCESS),
    VIEW_PERMISSION("viewGroupPermissions", PermissionAppliedType.GROUP_AFFECTED, AccessLevel.EDITING_ACCESS),
    DELETE_PERMISSION("deletePermission", PermissionAppliedType.GROUP_AFFECTED, AccessLevel.FULL_ACCESS),
    ACTIVITY_LOG("permissionActivityLog", PermissionAppliedType.GROUP_AFFECTED, AccessLevel.EDITING_ACCESS),
    VIEW_PROJECT("viewProject", PermissionAppliedType.PROJECT, AccessLevel.VIEWING_ACCESS),
    EDIT_PROJECT("editProject", PermissionAppliedType.PROJECT, AccessLevel.EDITING_ACCESS),
    CREATE_DATASET("createDataset", PermissionAppliedType.PROJECT, AccessLevel.FULL_ACCESS),
    EDIT_DATASET("editDataset", PermissionAppliedType.DATASET, AccessLevel.EDITING_ACCESS),
    VIEW_DATASET("viewDataset", PermissionAppliedType.DATASET, AccessLevel.VIEWING_ACCESS),
    PUBLISH_DATASET("publishDataset", PermissionAppliedType.DATASET, AccessLevel.FULL_ACCESS),
    REJECT_ADVERTISING_DATASET("rejectAdvertisingDataset", PermissionAppliedType.DATASET, AccessLevel.FULL_ACCESS),
    //db instance related
    CREATE_DATABASE_INSTANCE("createSchema", PermissionAppliedType.DATASET, AccessLevel.FULL_ACCESS),
    VIEW_DATABASE_INSTANCE("viewSchema", PermissionAppliedType.DATASET, AccessLevel.VIEWING_ACCESS),
    EDIT_DATABASE_INSTANCE("editSchema", PermissionAppliedType.DATASET, AccessLevel.EDITING_ACCESS),
    DELETE_DATABASE_INSTANCE("deleteSchema", PermissionAppliedType.DATASET, AccessLevel.FULL_ACCESS),
    CREATE_DATABASE_SQL("createDatabaseSql", PermissionAppliedType.DATASET, AccessLevel.FULL_ACCESS),
    EDIT_DATABASE_SQL("editDatabaseSql", PermissionAppliedType.DATASET, AccessLevel.EDITING_ACCESS),
    CHANGE_DATABASE_PASSWORD("changeDatabasePassword", PermissionAppliedType.DATASET, AccessLevel.FULL_ACCESS),
    VIEW_DATABASE_SQL("viewDatabaseSql", PermissionAppliedType.DATASET, AccessLevel.VIEWING_ACCESS),
    BACKUP_DATABASE_INSTANCE("manageDatabase", PermissionAppliedType.DATASET, AccessLevel.EDITING_ACCESS),
    REVERSE_DATABASE_SCHEMA("reverseSchema", PermissionAppliedType.DATASET, AccessLevel.FULL_ACCESS),
    //file management related
    UPLOAD("upload", PermissionAppliedType.DIRECTORY, AccessLevel.EDITING_ACCESS),
    DOWNLOAD("download", PermissionAppliedType.DIRECTORY, AccessLevel.VIEWING_ACCESS),
    CREATE_DIRECTORY("createDirectory", PermissionAppliedType.DIRECTORY, AccessLevel.EDITING_ACCESS),
    MOVE_DIRECTORY_FILE("moveDirectoryFile", PermissionAppliedType.DIRECTORY, AccessLevel.EDITING_ACCESS),
    DELETE_DIRECTORY("deleteDirectory", PermissionAppliedType.DIRECTORY, AccessLevel.EDITING_ACCESS),
    VIEW_DIRECTORY("viewDirectory", PermissionAppliedType.DIRECTORY, AccessLevel.VIEWING_ACCESS),
    DELETE_FILE("deleteFile", PermissionAppliedType.DIRECTORY, AccessLevel.EDITING_ACCESS),
    //annotation related
    CREATE_ANNOTATION("createAnnotation", PermissionAppliedType.DIRECTORY, AccessLevel.EDITING_ACCESS), 
    EDIT_ANNOTATION("editAnnotation", PermissionAppliedType.DIRECTORY, AccessLevel.EDITING_ACCESS),
    DELETE_ANNOTATION("deleteAnnotation", PermissionAppliedType.DIRECTORY, AccessLevel.EDITING_ACCESS),
    //vocabulary and keyword related
    MANAGE_VOCABULARY("manageVocabulary", PermissionAppliedType.GROUP, AccessLevel.FULL_ACCESS);
    
    private String permissionTypeName;
    
    private PermissionAppliedType appliedType;

    private AccessLevel requiredLevel;
    
    PermissionType(String permissionTypeName, PermissionAppliedType appliedType, AccessLevel requiredLevel)
    {
        this.permissionTypeName = permissionTypeName;
        this.appliedType = appliedType;
        this.requiredLevel = requiredLevel;
    }
    
    public String getPermissionTypeName()
    {
        return permissionTypeName;
    }

    public PermissionAppliedType getAppliedType()
    {
        return appliedType;
    }

    public AccessLevel getRequiredLevel()
    {
        return requiredLevel;
    }
}
