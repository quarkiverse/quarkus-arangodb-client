package io.quarkiverse.arangodb.client.ext.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class ArangodbClientExtProcessor {

    private static final String FEATURE = "arangodb-client-ext";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }
}
