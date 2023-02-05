package com.shadow.codecoverage.core.context.trace;

/**
 * @Classname EntrySpan
 * @Description TODO
 * @Date 2023/2/3 23:16
 * @Created by pepsi
 */
public class EntrySpan extends AbstractSpan {

    /**
     * http uri,dubbo method
     */
    private String uri;

    private String method;

    private String caseInf;

    private String apiType;

    private int stackDepth;

    public EntrySpan(int spanId, int parentSpanId) {
        super(spanId, parentSpanId);
    }

    public EntrySpan start() {
        if (++stackDepth == 1) {
            super.start();
        }
        return this;
    }

    @Override
    public boolean isEntry() {
        return true;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getCaseInf() {
        return caseInf;
    }

    public void setCaseInf(String caseInf) {
        this.caseInf = caseInf;
    }

    public String getApiType() {
        return apiType;
    }

    public void setApiType(String apiType) {
        this.apiType = apiType;
    }
}
