package io.quarkiverse.arangodb.client.ext.deployment;

import com.arangodb.shaded.vertx.core.spi.resolver.ResolverProvider;

import io.quarkiverse.arangodb.client.ext.runtime.ArangodbClientProducer;
import io.quarkiverse.arangodb.client.ext.runtime.QuarkusJacksonArangodbSerdeProducer;
import io.quarkiverse.arangodb.client.ext.runtime.TruststoreArangodbSSLContextProviderProducer;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageConfigBuildItem;

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

    /**
     * The shaded version of the driver comes with vertx shaded inside it.
     * vertx needs a custom configuration for native configuration.
     * I've replicated some parts of the one present in the vertx extension here:
     * <a href=
     * "https://github.com/quarkusio/quarkus/blob/3.8.0/extensions/vertx/deployment/src/main/java/io/quarkus/vertx/core/deployment/VertxCoreProcessor.java#L91">vertx
     * native configuration</a>
     */
    @BuildStep
    NativeImageConfigBuildItem fixShadedVertxNativeBuildFromQuarkusVertxExtension() {
        return NativeImageConfigBuildItem.builder()
                .addRuntimeInitializedClass("com.arangodb.shaded.vertx.core.buffer.impl.VertxByteBufAllocator")
                .addRuntimeInitializedClass("com.arangodb.shaded.vertx.core.buffer.impl.PartialPooledByteBufAllocator")
                .addRuntimeInitializedClass("com.arangodb.shaded.vertx.core.http.impl.VertxHttp2ClientUpgradeCodec")
                .addRuntimeInitializedClass("com.arangodb.shaded.vertx.core.eventbus.impl.clustered.ClusteredEventBus")
                .addNativeImageSystemProperty(ResolverProvider.DISABLE_DNS_RESOLVER_PROP_NAME, "true").build();
    }
}
