package edu.lternet.pasta.common.eml;

/**
 * Model an EML annotation element
 */
public class Annotation {
    
    private String id = null;
    private String system = null;
    private String scope = null;
    private String propertyURI = null;
    private String propertyURILabel = null;
    private String valueURI = null;
    private String valueURILabel = null;
    
    public void setId(String id) {
        this.id = id;
    }
    
    public void setSystem(String system) {
        this.system = system;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public void setPropertyURI(String propertyURI) {
        this.propertyURI = propertyURI;
    }
    
    public void setPropertyURILabel(String propertyURILabel) {
        this.propertyURILabel = propertyURILabel;
    }
    
    public void setValueURI(String valueURI) {
        this.valueURI = valueURI;
    }
    
    public void setValueURILabel(String valueURILabel) {
        this.valueURILabel = valueURILabel;
    }
    
    public String getId() {
        return this.id;
    }
    
    public String getSystem() {
        return this.system;
    }
    
    public String getScope() {
        return this.scope;
    }
    
    public String getPropertyURI() {
        return this.propertyURI;
    }
    
    public String getPropertyURILabel() {
        return this.propertyURILabel;
    }
    
    public String getValueURI() {
        return this.valueURI;
    }
    
    public String getValueURILabel() {
        return this.valueURILabel;
    }
    
}
