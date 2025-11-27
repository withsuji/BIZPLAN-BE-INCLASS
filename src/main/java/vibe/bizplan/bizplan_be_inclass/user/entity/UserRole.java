package vibe.bizplan.bizplan_be_inclass.user.entity;

/**
 * User roles for RBAC.
 * REQ-NF-021: Owner/Member/Viewer 3-tier RBAC
 */
public enum UserRole {
    OWNER,   // Full access, can manage team settings
    MEMBER,  // Can create/edit own resources
    VIEWER   // Read-only access
}

