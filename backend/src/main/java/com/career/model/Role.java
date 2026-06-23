package com.career.model;

public enum Role {
    JOB_SEEKER,
    COMPANY,
    /**
     * Legacy value kept so older H2 demo data can still be read.
     */
    STUDENT,
    ADMIN
}
