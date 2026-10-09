# Upstream test restoration

Upstream baseline: `20da91a3c21532d0d0cfa9c796b79068c61d16b1`.

The first NetworkManager batch restores 22 source files and passes 988 test invocations using Maven 3.10, JDK 21 and JUnit 5. Enum conversions, interface/Wi-Fi status, semantic versions and property values are tested without a NetworkManager/D-Bus daemon or physical devices.

JUnit 4 constructor/field parameterization becomes Jupiter `@ParameterizedTest` with `@MethodSource`. Enclosed suites become `@Nested` classes, and expected exceptions use explicit `assertThrows` assertions.

Two upstream-only APIs are not introduced during test restoration: the `KuraModemMode` facade and the reverse set-to-bitmask overload of `MMModemMode`. Existing inbound bitmask conversions remain covered. The local `UNMANAGED` IPv4 plus disabled IPv6 result is also retained; it predates this migration (`219f081d`) and differs from the upstream test expectation.

```sh
mvn -pl :org.eclipse.kura.nm -am test
```

The central source inventory in the Kura repository records the exact exclusions and test reports. Other networking unit and container scenarios remain under review; this batch does not establish a full networking or hardware acceptance result.
