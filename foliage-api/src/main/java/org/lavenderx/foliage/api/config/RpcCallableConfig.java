package org.lavenderx.foliage.api.config;

import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;
import org.lavenderx.foliage.nettyrpc.annotation.RpcService;
import org.lavenderx.foliage.nettyrpc.client.RpcClient;
import org.lavenderx.foliage.nettyrpc.registry.ServiceDiscovery;
import org.reflections.Reflections;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Configuration
@Slf4j
public class RpcCallableConfig implements BeanFactoryPostProcessor {

    private String registryAddress;

    private String serverAddress;

    public RpcCallableConfig() {
        loadRpcConfig();
    }

    @Bean
    public ServiceDiscovery serviceDiscovery() {
        return new ServiceDiscovery(registryAddress);
    }

    @Bean
    public RpcClient rpcClient() {
        return new RpcClient(serviceDiscovery());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
            throws BeansException {
        final String rpcServicePackages = "org.lavenderx.foliage.nettyrpc.rpcservice";
        final RpcClient rpcClient = rpcClient();

        log.info("Initializing RPC client stubs");
        Reflections reflections = new Reflections(rpcServicePackages);
        for (Class<?> clazz : reflections.getTypesAnnotatedWith(RpcService.class)) {
            String rpcServiceName = clazz.getSimpleName();
            log.info("Processing rpc service annotation {}", rpcServiceName);

            // Register rpc stub
            String rpcClientStub = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, clazz.getSimpleName());

            log.info("Registering rpc stub {}", rpcClientStub);
            Object rpcServiceBean = rpcClient.create(clazz);
            beanFactory.registerSingleton(rpcClientStub, clazz.cast(rpcServiceBean));
            beanFactory.initializeBean(clazz.cast(rpcServiceBean), rpcClientStub);

            log.info("Complete registering rpc service {}", rpcServiceName);
        }
    }

    private void loadRpcConfig() {
        try (InputStream inputStream =
                     getClass().getClassLoader().getResourceAsStream("rpc.properties")) {
            Properties props = new Properties();
            props.load(inputStream);

            this.registryAddress = props.getProperty("registry.address");
            this.serverAddress = props.getProperty("server.address");

        } catch (IOException ex) {
            throw new RuntimeException("Failed to read RPC config file");
        }
    }
}
