package ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.util;

import org.eclipse.cdt.core.dom.ast.ASTTypeMatcher;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * Information of a type to replace in the transformation process. A type
 * information contains the type, the name of the template type and the default
 * type if there is any. A type information is comparable with other type
 * informations to allow an ordering for the template definition when using
 * multiple types.
 * 
 * @author ythrier(at)hsr.ch
 */
public class TypeInformation implements Comparable<TypeInformation> {
    private static final IASTDeclSpecifier NULL_DEFAULT_TYPE = null;
    private static final String NULL_TEMPLATE_NAME = "";
    private boolean default_ = false;
    private int orderId_;
    private IType type_;
    private String templateName_;
    private IASTDeclSpecifier defaultType_;
    private IASTDeclSpecifier callSpecificDefaultType_;

    /**
     * Create the type information.
     * 
     * @param type
     *            Type of the information.
     * @param templateName
     *            Template name.
     * @param defaultType
     *            Default type (if there is any).
     */
    public TypeInformation(IType type, String templateName,
            IASTDeclSpecifier defaultType) {
        this.type_ = type;
        this.templateName_ = templateName;
        this.defaultType_ = defaultType;
    }

    /**
     * Create a type information only specifing the type itself.
     * 
     * @param type
     *            Type of the information.
     */
    public TypeInformation(IType type) {
        this(type, NULL_TEMPLATE_NAME, NULL_DEFAULT_TYPE);
    }

    /**
     * Return whether the type should be defaulted when adjusting a type.
     * 
     * @return True if the type should default, otherwise false.
     */
    public boolean shouldDefault() {
        return default_;
    }

    /**
     * Enable/disable template param defaulting.
     * 
     * @param on
     *            True to enable, false to disable.
     */
    public void setDefaulting(boolean on) {
        this.default_ = on;
    }

    /**
     * Return the order id of the type information.
     * 
     * @return Order id.
     */
    public int getOrderId() {
        return orderId_;
    }

    /**
     * Set the order id of the type information.
     * 
     * @param orderId
     *            Order id.
     */
    public void setOrderId(int orderId) {
        this.orderId_ = orderId;
    }

    /**
     * Return the type of the type information.
     * 
     * @return Type.
     */
    public IType getType() {
        return type_;
    }

    /**
     * Return the name of the type.
     * 
     * @return Name.
     */
    public String getTypeName() {
        return type_.toString();
    }

    /**
     * Return the name of the template type.
     * 
     * @return Template type name.
     */
    public String getTemplateName() {
        return templateName_;
    }

    /**
     * Set the name of the template type.
     * 
     * @param templateName
     *            Template type name.
     */
    public void setTemplateName(String templateName) {
        this.templateName_ = templateName;
    }

    /**
     * Return the default type of the type information or <code>null</code> if
     * there is no default type at all.
     * 
     * @return Default type or <code>null</code> if there is no default type.
     */
    public IASTDeclSpecifier getDefaultType() {
        return defaultType_;
    }

    /**
     * Returns the call specific default type. This default type was determined
     * based on the call (e.g. a function call) and is adjusted for each call.
     * 
     * @return Default type.
     */
    public IASTDeclSpecifier getCallSpecificDefaultType() {
        return callSpecificDefaultType_;
    }

    /**
     * Set the default type of the type information. This will also set the call
     * specific default type, since if it is possible to find a "global" default
     * type, there is no call specific default type at all.
     * 
     * @param defaultType
     *            Default type.
     */
    public void setDefaultType(IASTDeclSpecifier defaultType) {
        this.defaultType_ = defaultType;
        this.callSpecificDefaultType_ = defaultType;
    }

    /**
     * Set the call specific default type (different for each call).
     * 
     * @param defaultType
     *            Default type.
     */
    public void setCallSpecificDefaultType(IASTDeclSpecifier defaultType) {
        this.callSpecificDefaultType_ = defaultType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof TypeInformation) {
            TypeInformation rhs = (TypeInformation) o;
            return (hasSameTemplateName(rhs) && hasSameType(rhs));
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (type_.toString().hashCode() + templateName_.hashCode());
    }

    /**
     * Check if this type information and the parameter type information has the
     * same type.
     * 
     * @param rhs
     *            Other type information.
     * @return True if this type information and the parameter type information
     *         has the same type, otherwise false.
     */
    private boolean hasSameType(TypeInformation rhs) {
        return new ASTTypeMatcher().isEquivalent(getType(), rhs.getType());
    }

    /**
     * Check if this type information and the parameter type information has the
     * same template name.
     * 
     * @param rhs
     *            Other type information.
     * @return True if this type information and the parameter type information
     *         has the same template name, otherwise false.
     */
    private boolean hasSameTemplateName(TypeInformation rhs) {
        return getTemplateName().equals(rhs.getTemplateName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(TypeInformation rhs) {
        if (orderId_ < rhs.orderId_)
            return -1;
        if (orderId_ > rhs.orderId_)
            return 1;
        return 0;
    }

    /**
     * Check if the type info has a default type.
     * 
     * @return True if a default type was set, otherwise false.
     */
    public boolean hasDefaultType() {
        return (defaultType_ != null);
    }
}
