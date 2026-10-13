package com.ahadporkar.facilityflow.api;

import com.ahadporkar.facilityflow.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/team")
public class TeamController {
    private final TeamService service;
    public TeamController(TeamService service) { this.service=service; }
    @GetMapping public List<TeamService.UserView> list() { return service.team(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('ADMIN')")
    public TeamService.UserView create(@Valid @RequestBody Requests.UserCreate r) { return service.create(r); }
}
