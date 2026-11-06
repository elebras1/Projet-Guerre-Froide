package com.populaire.projetguerrefroide.simulation.request;

import com.populaire.projetguerrefroide.protocol.SimulationHeader;
import com.populaire.projetguerrefroide.protocol.catalog.Catalog;
import com.populaire.projetguerrefroide.protocol.request.Request;
import com.populaire.projetguerrefroide.protocol.request.RequestParams;
import com.populaire.projetguerrefroide.protocol.request.Response;
import com.populaire.projetguerrefroide.simulation.SimulationContext;
import com.populaire.projetguerrefroide.simulation.SimulationState;
import com.populaire.projetguerrefroide.util.EcsConstants;
import io.github.elebras1.flecs.World;

public abstract class RequestHandler<R extends Response> {
    protected final World ecsWorld;
    protected final EcsConstants ecsConstants;
    protected final SimulationState state;
    private final SimulationHeader header;
    protected Catalog catalog;

    protected RequestHandler(SimulationContext simulationContext, SimulationState state, SimulationHeader header) {
        this.ecsWorld = simulationContext.getEcsWorld();
        this.ecsConstants = simulationContext.getEcsConstants();
        this.state = state;
        this.header = header;
    }

    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }

    public final boolean serve(Request<R> request) {
        R response = request.filling();
        RequestParams params = request.pendingParams();
        boolean found = this.fill(params, response);
        response.stamp(params, this.header.revision(), this.header.tick(), this.header.ownershipVersion());
        if (!found) {
            response.notFound();
        }
        request.complete();
        return found;
    }

    protected abstract boolean fill(RequestParams params, R response);

    protected int countryIndex(long countryId) {
        return countryId == 0 ? -1 : this.catalog.countries().indexOf(countryId);
    }

    protected int colonizerIndex(long countryId) {
        if (countryId == 0) {
            return -1;
        }
        long colonizerId = this.ecsWorld.obtainEntityView(countryId).target(this.ecsConstants.isColonyOf());
        return this.countryIndex(colonizerId);
    }
}
