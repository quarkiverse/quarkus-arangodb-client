package io.quarkiverse.arangodb.client.ext.deployment;

import io.quarkiverse.arangodb.client.ext.runtime.ArangodbClient;
import io.quarkiverse.arangodb.client.ext.runtime.DefaultArangodbSSLContextProvider;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CapabilityBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class ArangodbClientExtProcessor {

    private static final String FEATURE = "arangodb-client-ext";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void produceStateRepositoryResultBuildItem(final BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemProducer) {
        additionalBeanBuildItemProducer.produce(
                AdditionalBeanBuildItem.builder()
                        .setUnremovable()
                        .addBeanClasses(DefaultArangodbSSLContextProvider.class)
                        .build());
        additionalBeanBuildItemProducer.produce(
                AdditionalBeanBuildItem.builder()
                        .setUnremovable()
                        .addBeanClasses(ArangodbClient.class)
                        .build());
    }

    @BuildStep
    void capabilities(final BuildProducer<CapabilityBuildItem> capabilityProducer) {
        capabilityProducer.produce(new CapabilityBuildItem("io.quarkus.arangodb", "arangodb-client-ext"));
    }
}
