package com.populaire.projetguerrefroide.dao.json;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.JsonAttribute;

import java.util.List;
import java.util.Map;

@CompiledJson(onUnknown = CompiledJson.Behavior.IGNORE)
public record CountryHistoryJson(
    @JsonAttribute(mandatory = true) long capital,
    @JsonAttribute(mandatory = true) String government,
    @JsonAttribute(mandatory = true) String ideology,
    @JsonAttribute(name = "national_identity", mandatory = true) String nationalIdentity,
    @JsonAttribute(name = "national_attitude", mandatory = true) String nationalAttitude,
    @JsonAttribute(name = "head_of_state") String headOfState,
    @JsonAttribute(name = "head_of_government") String headOfGovernment,
    @JsonAttribute(mandatory = true) Map<String, String> laws,
    List<ArmyJson> armies,
    List<NavyJson> navies,
    @JsonAttribute(name = "air_wings") List<AirWingJson> airWings) {
}
