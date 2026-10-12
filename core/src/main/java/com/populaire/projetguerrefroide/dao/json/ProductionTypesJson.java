package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.Map;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record ProductionTypesJson(
    @JsonAttribute(name = "types_workers", mandatory = true) Map<String, WorkerTypeJson> typesWorkers,
    @JsonAttribute(name = "types_buildings", mandatory = true) Map<String, ProductionTypeJson> typesBuildings,
    @JsonAttribute(name = "types_rgo", mandatory = true) Map<String, ProductionTypeJson> typesRgo) {
}
