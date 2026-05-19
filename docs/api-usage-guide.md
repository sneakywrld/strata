# Strata API — Getting Started

This guide shows how to integrate with Strata from a third-party plugin.

## Dependency

Add `strata-api` to your project. Never depend on internal modules directly.

**Maven:**
```xml
<dependency>
    <groupId>com.protectcord.strata</groupId>
    <artifactId>strata-api</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

**Gradle:**
```kotlin
compileOnly("com.protectcord.strata:strata-api:1.0.0-SNAPSHOT")
```

## Accessing the API

```java
import com.protectcord.strata.api.core.StrataAPI;
import com.protectcord.strata.api.core.StrataProvider;

StrataAPI api = StrataProvider.get();
```

Always check if Strata is loaded before accessing:

```java
if (Bukkit.getPluginManager().isPluginEnabled("Strata")) {
    StrataAPI api = StrataProvider.get();
    // Use the API
}
```

## Registering a Custom Biome

```java
import com.protectcord.strata.api.biome.*;
import com.protectcord.strata.api.core.NamespacedKey;

Biome myBiome = BiomeBuilder.create()
    .key(NamespacedKey.of("myplugin", "enchanted_forest"))
    .category(BiomeCategory.FOREST)
    .climate(new ClimateParameters(0.7, 0.8, 0.5, -0.3, 0.2))
    .build();

api.biomeRegistry().register(myBiome);
```

## Listening to Events

```java
import com.protectcord.strata.api.event.bus.Subscribe;
import com.protectcord.strata.api.event.bus.EventPriority;
import com.protectcord.strata.api.event.generation.ChunkCompleteEvent;

public class MyListener {

    @Subscribe(priority = EventPriority.NORMAL)
    public void onChunkComplete(ChunkCompleteEvent event) {
        // Chunk at (event.chunkX(), event.chunkZ()) has finished generating
    }
}

// Register the listener:
api.eventBus().register(new MyListener());
```

## Registering a Custom Noise Function

```java
import com.protectcord.strata.api.noise.NoiseFunction;
import com.protectcord.strata.api.core.NamespacedKey;

NoiseFunction myNoise = new NoiseFunction() {
    @Override
    public NamespacedKey key() {
        return NamespacedKey.of("myplugin", "custom_noise");
    }

    @Override
    public double sample(double x, double z) {
        return Math.sin(x * 0.01) * Math.cos(z * 0.01);
    }

    @Override
    public double sample(double x, double y, double z) {
        return sample(x, z); // 2D noise projected to 3D
    }

    @Override
    public double minValue() { return -1.0; }

    @Override
    public double maxValue() { return 1.0; }
};

api.noiseRegistry().register(myNoise);
```

## Querying World Data

```java
// Get the profile used by a world
api.worldManager().getEngine("myworld").ifPresent(engine -> {
    // Access engine data
});

// Get current biome info
String biomeName = api.biomeRegistry()
    .get(NamespacedKey.of("strata", "sanctuary_meadow"))
    .map(b -> b.key().toString())
    .orElse("unknown");
```

## Pipeline Modification

Advanced: replace or wrap generation stages:

```java
api.pipelineAccessor().wrapStage(
    GenerationStageType.FEATURE_DECORATION,
    (inner, chunk, context) -> {
        // Pre-processing
        inner.process(chunk, context);
        // Post-processing: add custom features
    }
);
```

## Best Practices

- Depend on `strata-api` only, never on internal modules.
- Check `StrataProvider.get()` is non-null before using.
- Register custom biomes/noise during your plugin's `onEnable()`.
- Use event priorities to control execution order.
- Keep event handlers fast — they run during chunk generation.
