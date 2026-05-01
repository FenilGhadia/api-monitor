package com.apimonitor.modules.user.entity;

/**
 * Application roles for RBAC.
 *
 * ADMIN — full access: logs, analytics, user management
 * USER  — read-only access to their own profile
 */
public enum Role {
    ADMIN,
    USER
}