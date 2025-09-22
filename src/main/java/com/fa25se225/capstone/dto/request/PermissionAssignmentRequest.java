package com.fa25se225.capstone.dto.request;

import java.util.Set;

public record PermissionAssignmentRequest(Set<String> permissionNames) {
}
