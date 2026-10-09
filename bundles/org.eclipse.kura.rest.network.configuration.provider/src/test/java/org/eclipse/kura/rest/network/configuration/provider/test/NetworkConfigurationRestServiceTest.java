/*******************************************************************************
 * Copyright (c) 2021, 2026 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Eurotech
 ******************************************************************************/
package org.eclipse.kura.rest.network.configuration.provider.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloudconnection.message.KuraMessage;
import org.eclipse.kura.cloudconnection.request.RequestHandlerMessageConstants;
import org.eclipse.kura.configuration.ComponentConfiguration;
import org.eclipse.kura.configuration.ConfigurationService;
import org.eclipse.kura.core.configuration.ComponentConfigurationImpl;
import org.eclipse.kura.core.configuration.metatype.ObjectFactory;
import org.eclipse.kura.crypto.CryptoService;
import org.eclipse.kura.internal.rest.network.configuration.NetworkConfigurationRestService;
import org.eclipse.kura.message.KuraPayload;
import org.eclipse.kura.message.KuraResponsePayload;
import org.eclipse.kura.request.handler.jaxrs.DefaultExceptionHandler;
import org.eclipse.kura.request.handler.jaxrs.JaxRsRequestHandlerProxy;
import org.eclipse.kura.rest.network.configuration.provider.test.responses.MockComponentConfiguration;
import org.eclipse.kura.rest.network.configuration.provider.test.responses.RestNetworkConfigurationJson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

/** Endpoint/DTO scenarios through the actual in-process proxy; transport and authentication require runtime acceptance. */
public class NetworkConfigurationRestServiceTest {

    private static final String METHOD_SPEC_GET = "GET";
    private static final String METHOD_SPEC_POST = "POST";
    private static final String METHOD_SPEC_PUT = "PUT";
    private static final String METHOD_SPEC_DELETE = "DELETE";

    private static final String NETWORK_CONF_SERVICE_PID = "org.eclipse.kura.net.admin.NetworkConfigurationService";
    private static final String IP4_FIREWALL_CONF_SERVICE_PID =
            "org.eclipse.kura.net.admin.FirewallConfigurationService";
    private static final String IP6_FIREWALL_CONF_SERVICE_PID =
            "org.eclipse.kura.net.admin.ipv6.FirewallConfigurationServiceIPv6";

    private final Map<String, Map<String, Object>> receivedConfigsByPid = new HashMap<>();

    private final ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);

    public NetworkConfigurationRestServiceTest() {
        var endpoint = new NetworkConfigurationRestService();
        endpoint.setConfigurationService(this.configurationService);
        endpoint.setCryptoService(Mockito.mock(CryptoService.class));
        this.proxy = new JaxRsRequestHandlerProxy(endpoint);
    }

    @Test
    public void shouldReturnNotFoundIfNoServiceIsRegistered() {
        whenRequestIsPerformed(new MethodSpec(METHOD_SPEC_GET), "/nothing");

        thenResponseCodeIs(404);
    }

    @Test
    public void shouldReturnListOfNetworkConfigurationPids() {
        givenMockGetNetworkConfigurationPids();

        whenRequestIsPerformed(new MethodSpec(METHOD_SPEC_GET), "/configurableComponents");

        thenRequestSucceeds();
        var actualPids = JsonParser.parseString(new String(this.response.getPayload().getBody(),
                StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("pids");
        Set<String> names = new HashSet<>();
        actualPids.forEach(pid -> names.add(pid.getAsString()));
        assertEquals(3, actualPids.size());
        assertEquals(Set.of(NETWORK_CONF_SERVICE_PID, IP4_FIREWALL_CONF_SERVICE_PID,
                IP6_FIREWALL_CONF_SERVICE_PID), names);
    }

    @Test
    public void shouldReturnMockedConfigurationList() throws KuraException {
        givenMockGetNetworkConfigurationsList();

        whenRequestIsPerformed(new MethodSpec(METHOD_SPEC_GET), "/configurableComponents/configurations");

        thenRequestSucceeds();
        thenResponseBodyEqualsJson(RestNetworkConfigurationJson.ALL_CONFIGURATIONS_RESPONSE);
    }

    @Test
    public void shouldReturnAMockedNetworkConfiguration() throws KuraException {
        givenMockGetNetworkConfiguration();

        whenRequestIsPerformed(
                new MethodSpec(METHOD_SPEC_POST),
                "/configurableComponents/configurations/byPid",
                RestNetworkConfigurationJson.FIREWALL_IP6_BYPID_REQUEST);

        thenRequestSucceeds();
        thenResponseBodyEqualsJson(RestNetworkConfigurationJson.SINGLE_CONFIG_RESPONSE);
    }

    @Test
    public void shouldReturnAMockedDefaultNetworkConfiguration() throws KuraException {
        givenMockGetDefaultNetworkConfiguration();

        whenRequestIsPerformed(
                new MethodSpec(METHOD_SPEC_POST),
                "/configurableComponents/configurations/byPid/_default",
                RestNetworkConfigurationJson.FIREWALL_IP6_BYPID_REQUEST);

        thenRequestSucceeds();
        thenResponseBodyEqualsJson(RestNetworkConfigurationJson.SINGLE_CONFIG_RESPONSE);
    }

    @Test
    public void shouldUpdateWithoutErrors() throws KuraException {
        givenMockUpdateConfiguration();

        whenRequestIsPerformed(
                new MethodSpec(METHOD_SPEC_PUT),
                "/configurableComponents/configurations/_update",
                RestNetworkConfigurationJson.FIREWALL_IP6_UPDATE_REQUEST);

        thenRequestSucceeds();
        thenValueIsUpdated(
                IP6_FIREWALL_CONF_SERVICE_PID, "firewall.ipv6.open.ports", "1234,tcp,0:0:0:0:0:0:0:0/0,,,,,#");
    }

    @Test
    public void shouldUpdateNetInterfacesProperty() throws KuraException {
        givenMockUpdateConfiguration();
        givenMockNetworkConfigurationService();

        whenRequestIsPerformed(
                new MethodSpec(METHOD_SPEC_PUT),
                "/configurableComponents/configurations/_update",
                RestNetworkConfigurationJson.NETWORK_CONFIGURATION_UPDATE_REQUEST);

        thenRequestSucceeds();
        thenValueIsUpdated(NETWORK_CONF_SERVICE_PID, "net.interface.wlan0.config.ip4.status", "netIPv4StatusDisabled");
        thenValueIsUpdated(NETWORK_CONF_SERVICE_PID, "net.interfaces", "eth0,wlan0");
    }

    @Test
    public void shouldReturnEmptyArray() throws KuraException {
        givenMockUpdateConfiguration();

        whenRequestIsPerformed(new MethodSpec(METHOD_SPEC_GET), "/factoryComponents");

        thenRequestSucceeds();
        thenResponseBodyEqualsJson(RestNetworkConfigurationJson.EMPTY_PIDS_RESPONSE);
    }

    @Test
    public void shouldReturnNothing() throws KuraException {
        givenMockUpdateConfiguration();

        whenRequestIsPerformed(
                new MethodSpec(METHOD_SPEC_POST),
                "/factoryComponents",
                RestNetworkConfigurationJson.EMPTY_CONFIGS_REQUEST);

        thenResponseCodeIs(200);
    }

    @Test
    public void shouldReturnInvalidPidResponse() throws KuraException {
        givenMockUpdateConfiguration();

        whenRequestIsPerformed(
                new MethodSpec(METHOD_SPEC_DELETE, "DEL"),
                "/factoryComponents/byPid",
                RestNetworkConfigurationJson.INVALID_PID_DELETE_REQUEST);

        thenResponseCodeIs(500);
        thenResponseBodyEqualsJson(RestNetworkConfigurationJson.INVALID_PID_DELETE_RESPONSE);
    }

    @Test
    public void shouldReturnEmptyConfigsOnGet() throws KuraException {
        givenMockUpdateConfiguration();

        whenRequestIsPerformed(new MethodSpec(METHOD_SPEC_GET), "/factoryComponents/ocd");

        thenRequestSucceeds();
        thenResponseBodyEqualsJson(RestNetworkConfigurationJson.EMPTY_CONFIGS_RESPONSE);
    }

    @Test
    public void shouldReturnEmptyConfigsOnPost() throws KuraException {
        givenMockUpdateConfiguration();

        whenRequestIsPerformed(
                new MethodSpec(METHOD_SPEC_POST),
                "/factoryComponents/ocd/byFactoryPid",
                RestNetworkConfigurationJson.EMPTY_PIDS_REQUEST);

        thenRequestSucceeds();
        thenResponseBodyEqualsJson(RestNetworkConfigurationJson.EMPTY_CONFIGS_RESPONSE);
    }

    /*
     * Given
     */

    private void givenMockGetNetworkConfigurationPids() {
        Set<String> pids = new HashSet<>();
        pids.add(IP6_FIREWALL_CONF_SERVICE_PID);
        pids.add(NETWORK_CONF_SERVICE_PID);
        pids.add(IP4_FIREWALL_CONF_SERVICE_PID);
        pids.add("org.example.non.network.Component");
        when(configurationService.getConfigurableComponentPids()).thenReturn(pids);
    }

    private void givenMockGetNetworkConfigurationsList() throws KuraException {
        MockComponentConfiguration mockNetworkConfig = new MockComponentConfiguration(0);
        MockComponentConfiguration mockIp4Config = new MockComponentConfiguration(1);
        MockComponentConfiguration mockIp6Config = new MockComponentConfiguration(2);
        when(configurationService.getComponentConfiguration(NETWORK_CONF_SERVICE_PID))
                .thenReturn(mockNetworkConfig.getComponentConfiguration());
        when(configurationService.getComponentConfiguration(IP4_FIREWALL_CONF_SERVICE_PID))
                .thenReturn(mockIp4Config.getComponentConfiguration());
        when(configurationService.getComponentConfiguration(IP6_FIREWALL_CONF_SERVICE_PID))
                .thenReturn(mockIp6Config.getComponentConfiguration());
    }

    private void givenMockGetNetworkConfiguration() throws KuraException {
        MockComponentConfiguration mockIp6Config = new MockComponentConfiguration(0);
        when(configurationService.getComponentConfiguration(IP6_FIREWALL_CONF_SERVICE_PID))
                .thenReturn(mockIp6Config.getComponentConfiguration());
    }

    private void givenMockGetDefaultNetworkConfiguration() throws KuraException {
        MockComponentConfiguration mockIp6Config = new MockComponentConfiguration(0);
        when(configurationService.getDefaultComponentConfiguration(IP6_FIREWALL_CONF_SERVICE_PID))
                .thenReturn(mockIp6Config.getComponentConfiguration());
    }

    @SuppressWarnings("unchecked")
    private void givenMockUpdateConfiguration() throws KuraException {
        final Answer<?> configurationUpdateAnswer = i -> {
            this.receivedConfigsByPid.put(i.getArgument(0, String.class), i.getArgument(1, Map.class));
            return (Void) null;
        };
        Mockito.doAnswer(configurationUpdateAnswer)
                .when(configurationService)
                .updateConfiguration(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyBoolean());
    }

    private void givenMockNetworkConfigurationService() throws KuraException {
        Map<String, Object> properties = new HashMap<>();
        properties.put("net.interfaces", "eth0");
        ComponentConfiguration componentConfig =
                new ComponentConfigurationImpl(NETWORK_CONF_SERVICE_PID, new ObjectFactory().createTocd(), properties);
        when(configurationService.getComponentConfiguration(NETWORK_CONF_SERVICE_PID))
                .thenReturn(componentConfig);
    }

    /*
     * Then
     */

    private void thenValueIsUpdated(final String pid, final String expectedKey, final Object expectedValue) {
        assertEquals(expectedValue, this.receivedConfigsByPid.get(pid).get(expectedKey));
    }

    /*
     * Utils
     */

    private JaxRsRequestHandlerProxy proxy;
    private KuraMessage response;

    private record MethodSpec(String method, String... aliases) { }

    private void whenRequestIsPerformed(MethodSpec method, String path) {
        whenRequestIsPerformed(method, path, null);
    }

    private void whenRequestIsPerformed(MethodSpec method, String path, String body) {
        var payload = new KuraPayload();
        if (body != null) {
            payload.setBody(body.getBytes(StandardCharsets.UTF_8));
        }
        var request = new KuraMessage(payload);
        request.getProperties().put(
                RequestHandlerMessageConstants.ARGS_KEY.value(),
                Arrays.asList(path.substring(1).split("/")));
        try {
            this.response = switch (method.method()) {
            case "GET" -> this.proxy.doGet(null, request);
            case "POST" -> this.proxy.doPost(null, request);
            case "PUT" -> this.proxy.doPut(null, request);
            case "DELETE" -> this.proxy.doDel(null, request);
            default -> throw new AssertionError(method);
            };
        } catch (KuraException e) {
            this.response = DefaultExceptionHandler.toKuraMessage(
                    DefaultExceptionHandler.toWebApplicationException(e),
                    Optional.empty());
        } catch (Exception e) {
            throw new AssertionError("Request failed", e);
        }
    }

    private void thenRequestSucceeds() {
        thenResponseCodeIs(200);
    }

    private void thenResponseCodeIs(int expected) {
        Assertions.assertNotNull(this.response);
        var payload = new KuraResponsePayload(this.response.getPayload());
        Assertions.assertEquals(expected, payload.getResponseCode(), payload::getExceptionStack);
    }

    private void thenResponseBodyEqualsJson(String expected) {
        Assertions.assertEquals(JsonParser.parseString(expected),
                JsonParser.parseString(new String(this.response.getPayload().getBody(),
                        StandardCharsets.UTF_8)));
    }
}
