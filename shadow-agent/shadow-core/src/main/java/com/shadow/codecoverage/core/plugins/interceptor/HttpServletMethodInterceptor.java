package com.shadow.codecoverage.core.plugins.interceptor;

import com.shadow.codecoverage.core.api.EventListener;
import com.shadow.codecoverage.core.api.event.BeforeEvent;
import com.shadow.codecoverage.core.api.event.Event;
import com.shadow.codecoverage.core.api.event.ReturnEvent;
import com.shadow.codecoverage.core.config.AgentConfig;
import com.shadow.codecoverage.core.context.ContextManager;
import com.shadow.codecoverage.core.context.trace.AbstractSpan;
import com.shadow.codecoverage.core.context.trace.EntrySpan;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;

/**
 * @Classname HttpServletMethodInterceptor
 * @Description TODO
 * @Date 2023/2/5 11:28
 * @Created by pepsi
 */
public class HttpServletMethodInterceptor implements EventListener {


    private final String HTTP_REQUEST_CLASS = "javax.servlet.http.HttpServletRequest";

    private String REQUEST_URI_METHOD = "getRequestURI";

    private String REQUEST_HEADER_METHOD = "getHeader";


    @Override
    public void onEvent(Event event) throws Throwable {
        Event.Type type = event.eventType;
        switch (type) {
            case BEFORE:
                beforeServletMethod((BeforeEvent) event);
                break;
            case RETURN:
                afterServletMethod((ReturnEvent) event);
                break;
            default:
                break;
        }

    }

    private void afterServletMethod(ReturnEvent event) {
        AbstractSpan span = ContextManager.getActiveSpan();
        if (span != null) {
            ContextManager.stopSpan();
        }
    }

    private void beforeServletMethod(BeforeEvent event) throws ClassNotFoundException {
        Object request = event.args[0];

        if (request == null || event.args.length < 2) {
            //
            return;
        }

        Object caseInf = null;

        String reqUri = "";

        Class<?> httpServletReqClazz = request.getClass().getClassLoader().loadClass(HTTP_REQUEST_CLASS);

        try {
            Method getUriMethod = httpServletReqClazz.getDeclaredMethod(REQUEST_URI_METHOD);
            getUriMethod.setAccessible(true);
            reqUri = (String) getUriMethod.invoke(request);

        } catch (Throwable t) {
            return;
        }

        try {
            Method getHeaderMethod = httpServletReqClazz.getMethod(REQUEST_HEADER_METHOD, String.class);
            for (String key : AgentConfig.AUTOMATIC_REQUEST_FLAG_KEYS) {
                caseInf = getHeaderMethod.invoke(request, key);
                if (StringUtils.isNotBlank(ObjectUtils.toString(caseInf))) {
                    break;
                }
            }
        } catch (Throwable t) {
            return;
        }
        EntrySpan span = (EntrySpan) ContextManager.createEntrySpan();
        span.setUri(reqUri);
        span.setCaseInf(ObjectUtils.toString(caseInf));
    }
}
