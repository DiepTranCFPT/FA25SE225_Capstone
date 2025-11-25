package com.fa25se225.capstone.constant;


import lombok.Getter;

import java.util.Set;

@Getter
public enum PredefinedSystemRole {
    ADMIN("Admin description role...",
            Set.of(PredefinedSystemPermission.TEST_PERMISSION.name(),
                    PredefinedSystemPermission.TEST2_PERMISSION.name())
            ),

    TEACHER( "Teacher description role...",
               Set.of(PredefinedSystemPermission.TEST_PERMISSION.name(),
                       PredefinedSystemPermission.TEST2_PERMISSION.name())
            ),

    PARENT( "Parent description role...",
            Set.of(PredefinedSystemPermission.TEST_PERMISSION.name(),
                    PredefinedSystemPermission.TEST2_PERMISSION.name())
    ),

    STUDENT( "Student description role...",
            Set.of(PredefinedSystemPermission.TEST_PERMISSION.name(),
                    PredefinedSystemPermission.TEST2_PERMISSION.name(),
                    PredefinedSystemPermission.TEST_PERMISSION_FOR_STUDENT_ROLE.name())
    ),


    ;


    private final String description;
    private final Set<String> permissions;

    PredefinedSystemRole(String description, Set<String> permissions) {
        this.description = description;
        this.permissions = permissions;
    }

}
