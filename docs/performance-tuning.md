# Performance Tuning Guide

Server operators' guide to getting the best performance from Strata.

## Quick Wins

1. **Pre-generate chunks**: Run `/strata pregen myworld 5000 4` to pre-generate a 5000-block radius using 4 threads. This is the single biggest performance improvement for survival servers.

2. **Reduce noise cache in strata.toml** if memory is tight:
   ```toml
   [performance]
   noise-cache-size = 2048  # Down from default 4096
   ```

3. **Use Paper's async chunk generation**: Strata's pipeline is thread-safe. Paper handles this automatically on 1.18+.

## Tuning strata.toml

### Thread Count

```toml
[performance]
chunk-gen-threads = 0  # Auto-detect (recommended)
```

For dedicated servers with 8+ cores, you can set this to `(cores - 2)`. For shared hosting, leave at 0.

### Cache Sizes

| Setting | Low Memory (<4GB) | Standard (4-8GB) | High Memory (8GB+) |
|---------|-------------------|-------------------|---------------------|
| noise-cache-size | 1024 | 4096 | 16384 |
| region-cache-size | 256 | 1024 | 4096 |
| proto-chunk-pool-size | 2 | 4 | 8 |

### Hot-Reload

On production servers, consider disabling the file watcher:

```toml
[hot-reload]
enabled = false  # Use /strata reload manually instead
```

## Profile-Level Tuning

### Ore Scarcity

In `profile.toml`, the global scarcity multiplier controls all ore rates:

```toml
ore-scarcity-multiplier = 0.6  # 60% of vanilla (default)
```

Set to `1.0` for vanilla rates or `0.3` for very scarce survival.

### Cave Density

In `carvers/caves.toml`, reduce probability for fewer caves:

```toml
[cheese]
probability = 0.04  # Down from 0.08

[spaghetti]
probability = 0.015  # Down from 0.03
```

### Structure Frequency

In structure configs, increase spacing for fewer structures:

```toml
[placement]
spacing = 64  # Up from 32 (structures are 2x further apart)
```

## Monitoring

Use `/strata info` to see generation statistics. Key metrics:
- **Avg chunk gen time**: Should be under 50ms per chunk
- **Cache hit rate**: Should be above 80%. If lower, increase cache sizes
- **Active chunks**: Number of chunks currently being generated

## Pre-Generation Best Practices

1. Run pre-gen during off-hours (fewer players = more CPU for pre-gen)
2. Use `batch-size = 512` in `strata.toml` for faster pre-gen
3. Monitor memory: if the server runs low, reduce threads or batch size
4. Pre-gen saves automatically — safe to stop and resume later

## JVM Flags

Recommended JVM flags for Strata servers:

```bash
java -Xms4G -Xmx8G \
  -XX:+UseG1GC \
  -XX:+ParallelRefProcEnabled \
  -XX:MaxGCPauseMillis=200 \
  -jar paper.jar
```

G1GC works well with Strata's allocation patterns.
