package com.fa25se225.capstone.constant;

import lombok.Getter;

@Getter
public enum PredefinedSystemPermission {

    TEST_PERMISSION("This is test permission"),
    TEST2_PERMISSION("This is test 2 permission"),
    TEST_PERMISSION_FOR_STUDENT_ROLE( "This is test permission for student role"),

    ;


    private String permissionDescription;


    PredefinedSystemPermission(String permissionDescription) {
        this.permissionDescription = permissionDescription;
    }

}
