package cn.jw.annotation;

import cn.jw.spring.CustomerScannerRegistry;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target(value = {ElementType.FIELD, ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
@Import(value = CustomerScannerRegistry.class)
public @interface RpcScan {
    // 指定注解扫描的包名
    String[] basePackage();
}
