package io.quarkiverse.arangodb.client.ext.deployment;

import io.quarkiverse.arangodb.client.ext.runtime.ArangodbClientProducer;
import io.quarkiverse.arangodb.client.ext.runtime.QuarkusJacksonArangodbSerdeProducer;
import io.quarkiverse.arangodb.client.ext.runtime.TruststoreArangodbSSLContextProviderProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class ArangodbClientExtProcessor {

    private static final String FEATURE = "arangodb-client-ext";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void produceStateRepositoryResultBuildItem(final Capabilities capabilities,
            final BuildProducer<AdditionalBeanBuildItem> additionalBeanBuildItemProducer) {
        if (capabilities.isPresent(Capability.JACKSON)) {
            additionalBeanBuildItemProducer.produce(
                    AdditionalBeanBuildItem.builder()
                            .setUnremovable()
                            .addBeanClasses(QuarkusJacksonArangodbSerdeProducer.class)
                            .build());
        }
        additionalBeanBuildItemProducer.produce(
                AdditionalBeanBuildItem.builder()
                        .setUnremovable()
                        .addBeanClasses(TruststoreArangodbSSLContextProviderProducer.class)
                        .build());
        additionalBeanBuildItemProducer.produce(
                AdditionalBeanBuildItem.builder()
                        .setUnremovable()
                        .addBeanClasses(ArangodbClientProducer.class)
                        .build());
    }
}
