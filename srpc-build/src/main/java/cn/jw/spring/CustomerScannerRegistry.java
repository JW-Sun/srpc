package cn.jw.spring;

import cn.jw.annotation.RpcScan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.stereotype.Component;

@Slf4j
public class CustomerScannerRegistry implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private ResourceLoader resourceLoader;
    private static final String SPRING_BEAN_BASE_PATH = "cn.jw.spring";
    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获得attributes 并且 获得rpcScan的注解的扫描名称
        AnnotationAttributes rpcScanAnnotationAttributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(RpcScan.class.getName()));

        // 扫描的包的类
        String[] rpcScanPackages = null;
        if (rpcScanAnnotationAttributes != null) {
            rpcScanPackages = rpcScanAnnotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        } else {
            rpcScanPackages = new String[]{((StandardAnnotationMetadata) importingClassMetadata).getIntrospectedClass().getPackage().getName()};
        }

        // 扫描rpcService
        CustomerScanner rpcServiceScanner = new CustomerScanner(registry, RpcScan.class);
        CustomerScanner springServiceScanner = new CustomerScanner(registry, Component.class);

        if (resourceLoader != null) {
            rpcServiceScanner.setResourceLoader(resourceLoader);
            springServiceScanner.setResourceLoader(resourceLoader);
        }

        int springScanCount = rpcServiceScanner.scan(rpcScanPackages);
        int rpcServiceScanCount = rpcServiceScanner.scan(rpcScanPackages);
        log.info("spring扫描的个数是：[{}]", springScanCount);
        log.info("rpcService扫描的个数是：[{}]", rpcServiceScanCount);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
