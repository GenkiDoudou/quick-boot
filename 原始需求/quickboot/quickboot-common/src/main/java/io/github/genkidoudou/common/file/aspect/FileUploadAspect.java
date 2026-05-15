package io.github.genkidoudou.common.file.aspect;

import io.github.genkidoudou.common.file.FileProperties;
import io.github.genkidoudou.common.file.FileUploadHook;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 文件上传切面
 * 在执行 FileTemplate.upload 前后调用 FileUploadHook 钩子
 *
 * @author genkidoudou
 * @since 2026/03/11
 */
@Slf4j
@Aspect
public class FileUploadAspect {

    private final FileProperties fileProperties;
    private final List<FileUploadHook> hooks;

    public FileUploadAspect(FileProperties fileProperties,
                            @Autowired(required = false) List<FileUploadHook> hooks) {
        this.fileProperties = fileProperties;
        this.hooks = hooks != null ? hooks : Collections.emptyList();
    }

    @Around("execution(* io.github.genkidoudou.common.file.FileTemplate.upload(..))")
    public Object aroundUpload(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        List<FileUploadHook> sortedHooks = new ArrayList<>(hooks);
        AnnotationAwareOrderComparator.sort(sortedHooks);

        // MultipartFile + classify
        if (args.length == 2 && args[0] instanceof MultipartFile && args[1] instanceof String) {
            MultipartFile file = (MultipartFile) args[0];
            String classify = (String) args[1];
            for (FileUploadHook hook : sortedHooks) {
                if (!hook.beforeUpload(file, classify)) {
                    throw new IllegalStateException("上传被钩子中断: " + hook.getClass().getName());
                }
            }
            try {
                String result = (String) pjp.proceed();
                for (FileUploadHook hook : sortedHooks) {
                    hook.afterUpload(result, file);
                }
                return result;
            } catch (Throwable e) {
                for (FileUploadHook hook : sortedHooks) {
                    hook.onError(file, classify, e);
                }
                throw e;
            }
        }

        // byte[] + filename + classify
        if (args.length == 3 && args[0] instanceof byte[] && args[1] instanceof String && args[2] instanceof String) {
            byte[] bytes = (byte[]) args[0];
            String filename = (String) args[1];
            String classify = (String) args[2];
            for (FileUploadHook hook : sortedHooks) {
                if (!hook.beforeUpload(bytes, filename, classify)) {
                    throw new IllegalStateException("上传被钩子中断: " + hook.getClass().getName());
                }
            }
            try {
                String result = (String) pjp.proceed();
                for (FileUploadHook hook : sortedHooks) {
                    hook.afterUpload(result, null);
                }
                return result;
            } catch (Throwable e) {
                for (FileUploadHook hook : sortedHooks) {
                    hook.onError(bytes, filename, classify, e);
                }
                throw e;
            }
        }

        // upload(MultipartFile) 单参数，走默认分类
        if (args.length == 1 && args[0] instanceof MultipartFile) {
            MultipartFile file = (MultipartFile) args[0];
            String classify = fileProperties.getDefaultClassify();
            for (FileUploadHook hook : sortedHooks) {
                if (!hook.beforeUpload(file, classify)) {
                    throw new IllegalStateException("上传被钩子中断: " + hook.getClass().getName());
                }
            }
            try {
                String result = (String) pjp.proceed();
                for (FileUploadHook hook : sortedHooks) {
                    hook.afterUpload(result, file);
                }
                return result;
            } catch (Throwable e) {
                for (FileUploadHook hook : sortedHooks) {
                    hook.onError(file, classify, e);
                }
                throw e;
            }
        }

        return pjp.proceed();
    }
}
