package com.ahadporkar.facilityflow.domain;

/** Stable database/API enum values. Translate their labels at the UI boundary. */
public final class Enums {
    private Enums() {}
    public enum Role { ADMIN, MANAGER, TECHNICIAN, VIEWER }
    public enum AssetType { HVAC, ELECTRICAL, FIRE_SAFETY, PLUMBING, ELEVATOR, OTHER }
    public enum AssetStatus { ACTIVE, OUT_OF_SERVICE, RETIRED }
    public enum WorkStatus { OPEN, ASSIGNED, IN_PROGRESS, COMPLETED, APPROVED, CANCELLED }
    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }
    public enum InspectionStatus { PASS, FAIL }
    public enum DefectStatus { OPEN, RESOLVED }
}
