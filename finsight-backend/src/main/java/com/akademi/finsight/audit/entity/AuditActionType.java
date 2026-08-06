package com.akademi.finsight.audit.entity;

public enum AuditActionType {
    LOGIN_SUCCESS,
    LOGOUT,
    PASSWORD_CHANGED,
    PASSWORD_RESET_COMPLETED,
    USER_CREATED,
    USER_UPDATED,
    USER_ACTIVATED,
    USER_DEACTIVATED,
    USER_DELETED,
    VERIFICATION_RESENT
}
