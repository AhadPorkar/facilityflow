package com.ahadporkar.facilityflow.api;

import com.ahadporkar.facilityflow.domain.Enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Versioned REST contracts: request fields never expose tenant IDs. */
public final class Requests {
    private Requests() {}
    public record Login(@NotBlank @Email String email, @NotBlank String password) {}
    public record SiteUpsert(@NotBlank String code, @NotBlank String name, String address,
                             String city, String postalCode, String contactName, @Email String contactEmail) {}
    public record AssetUpsert(@NotNull UUID siteId, @NotBlank String code, @NotBlank String name,
                              @NotNull AssetType type, String location, String manufacturer,
                              String serialNumber, AssetStatus status, LocalDate installedOn) {}
    public record PlanUpsert(@NotNull UUID assetId, @NotBlank String title,
                             @NotNull @Min(1) @Max(3650) Integer intervalDays,
                             @NotNull LocalDate nextDueDate, String instructions,
                             UUID assignedToUserId, Boolean active) {}
    public record WorkOrderCreate(@NotNull UUID siteId, UUID assetId, @NotBlank String title,
                                  String description, @NotNull Priority priority,
                                  LocalDate dueDate, UUID assignedToUserId) {}
    public record Assign(@NotNull UUID userId) {}
    public record Complete(@NotBlank String resolution) {}
    public record TemplateItem(@NotBlank String label, Boolean required) {}
    public record TemplateCreate(@NotBlank String name, String description, AssetType assetType,
                                 @NotEmpty List<@Valid TemplateItem> items) {}
    public record InspectionItem(@NotNull UUID itemId, @NotNull Boolean passed, String note) {}
    public record InspectionCreate(@NotNull UUID assetId, @NotNull UUID templateId,
                                   String notes, @NotEmpty List<@Valid InspectionItem> results) {}
    public record UserCreate(@NotBlank String displayName, @Email @NotBlank String email,
                             @Size(min=12, max=72) String password, @NotNull Role role) {}
}
