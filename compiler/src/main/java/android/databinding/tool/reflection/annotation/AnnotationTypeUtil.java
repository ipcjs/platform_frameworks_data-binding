/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.databinding.tool.reflection.annotation;

import android.databinding.tool.reflection.ModelClass;
import android.databinding.tool.reflection.ModelMethod;
import android.databinding.tool.reflection.TypeUtil;

import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

public class AnnotationTypeUtil extends TypeUtil {
    javax.lang.model.util.Types mTypes;

    public AnnotationTypeUtil(
            AnnotationAnalyzer annotationAnalyzer) {
        mTypes = annotationAnalyzer.getTypeUtils();
    }

    public static AnnotationTypeUtil getInstance() {
        return (AnnotationTypeUtil) TypeUtil.getInstance();
    }

    @Override
    public String getDescription(ModelClass modelClass) {
        // TODO use interface
        return modelClass.getCanonicalName().replace('.', '/');
    }

    @Override
    public String getDescription(ModelMethod modelMethod) {
        // TODO use interface
        AnnotationMethod method = ((AnnotationMethod) modelMethod);
        return getExecutableDescription(
                method.mExecutableElement, method.mMethod);
    }

    private String getDescription(TypeMirror typeMirror) {
        if (typeMirror == null) {
            throw new UnsupportedOperationException();
        }
        switch (typeMirror.getKind()) {
            case BOOLEAN:
                return BOOLEAN;
            case BYTE:
                return BYTE;
            case SHORT:
                return SHORT;
            case INT:
                return INT;
            case LONG:
                return LONG;
            case CHAR:
                return CHAR;
            case FLOAT:
                return FLOAT;
            case DOUBLE:
                return DOUBLE;
            case DECLARED:
                return CLASS_PREFIX + toJava(mTypes.erasure(typeMirror)).replace('.', '/') +
                        CLASS_SUFFIX;
            case VOID:
                return VOID;
            case ARRAY:
                final ArrayType arrayType = (ArrayType) typeMirror;
                final String componentType = getDescription(arrayType.getComponentType());
                return ARRAY + componentType;
            case TYPEVAR:
                final TypeVariable typeVariable = (TypeVariable) typeMirror;
                final String name = toJava(typeVariable);
                return CLASS_PREFIX + name.replace('.', '/') + CLASS_SUFFIX;
            case EXECUTABLE:
                return getExecutableDescription((ExecutableElement) mTypes.asElement(typeMirror),
                        (ExecutableType) typeMirror);
            default:
                throw new UnsupportedOperationException("cannot understand type "
                        + typeMirror.toString() + ", kind:" + typeMirror.getKind().name());
        }
    }

    private String getExecutableDescription(ExecutableElement executableElement,
            ExecutableType executableType) {
        final String methodName = executableElement.getSimpleName().toString();
        final String args = executableType.getParameterTypes().stream()
                .map(arg -> getDescription(arg))
                .collect(Collectors.joining("", "(", ")"));
        // TODO detect constructor?
        return methodName + args + getDescription(
                executableType.getReturnType());
    }

    /**
     * Returns the java representation of a TypeMirror type. For example, this may return
     * "java.util.Set&lt;java.lang.String&gt;"
     */
    public String toJava(TypeMirror typeMirror) {
        switch (typeMirror.getKind()) {
            case BOOLEAN:
                return "boolean";
            case BYTE:
                return "byte";
            case SHORT:
                return "short";
            case INT:
                return "int";
            case LONG:
                return "long";
            case CHAR:
                return "char";
            case FLOAT:
                return "float";
            case DOUBLE:
                return "double";
            case VOID:
                return "void";
            case NULL:
                return "null";
            case ARRAY:
                return toJava((ArrayType) typeMirror);
            case DECLARED:
                return toJava((DeclaredType) typeMirror);
            case TYPEVAR:
                return toJava((TypeVariable) typeMirror);
            case WILDCARD:
                return toJava((WildcardType) typeMirror);
            case NONE:
            case PACKAGE:
                return toJava(mTypes.asElement(typeMirror));
            case EXECUTABLE:
                return toJava((ExecutableType) typeMirror);
            case UNION:
                return toJava((UnionType) typeMirror);
            case INTERSECTION:
                return toJava((IntersectionType) typeMirror);
            case ERROR:
                return mTypes.asElement(typeMirror).getSimpleName().toString();
        }
        throw new IllegalArgumentException("Unexpected TypeMirror kind " +
                typeMirror.getKind() + ": " + typeMirror);
    }

    private String toJava(ArrayType arrayType) {
        TypeMirror component = arrayType.getComponentType();
        return toJava(component) + "[]";
    }

    private String toJava(DeclaredType declaredType) {
        TypeMirror enclosingType = declaredType.getEnclosingType();
        StringBuilder sb = new StringBuilder();
        if (enclosingType.getKind() != TypeKind.NONE) {
            sb.append(toJava(enclosingType)).append(declaredType.asElement().getSimpleName());
        } else {
            sb.append(toJava(declaredType.asElement()));
        }
        List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
        if (typeArgs != null && !typeArgs.isEmpty()) {
            String typeArgsString = typeArgs.stream()
                    .map(typeArg -> toJava(typeArg))
                    .collect(Collectors.joining(",", "<", ">"));
            sb.append(typeArgsString);
        }
        return sb.toString();
    }

    private String toJava(WildcardType wildcardType) {
        StringBuilder sb = new StringBuilder("?");
        final TypeMirror extendsBound = wildcardType.getExtendsBound();
        if (extendsBound != null) {
            sb.append(" extends ").append(toJava(extendsBound));
        }
        final TypeMirror superBound = wildcardType.getSuperBound();
        if (superBound != null) {
            sb.append(" super ").append(toJava(superBound));
        }
        return sb.toString();
    }

    private String toJava(ExecutableType executableType) {
        return toJava((ExecutableElement) mTypes.asElement(executableType), executableType);
    }

    public String toJava(ExecutableElement executableElement, ExecutableType executableType) {
        StringBuilder sb = new StringBuilder();
        if (executableElement.getModifiers() != null) {
            sb.append(executableElement.getModifiers().stream()
                    .map(mod -> mod.toString())
                    .collect(Collectors.joining(" ", "", " ")));
        }
        List<? extends TypeVariable> typeVariables = executableType.getTypeVariables();
        if (typeVariables != null && !typeVariables.isEmpty()) {
            String typeVariablesString = typeVariables.stream()
                    .map(typeVar -> toJava(typeVar))
                    .collect(Collectors.joining(",", "<", "> "));
            sb.append(typeVariablesString);
        }

        ElementKind kind = executableElement.getKind();
        if (kind == ElementKind.METHOD) {
            sb.append(toJava(executableType.getReturnType()));
            sb.append(' ');
        }

        sb.append(executableElement.getSimpleName().toString());
        List<? extends TypeMirror> paramTypes = executableType.getParameterTypes();
        if (paramTypes == null) {
            sb.append("()");
        } else {
            String params = executableType.getParameterTypes().stream()
                    .map(paramType -> toJava(paramType))
                    .collect(Collectors.joining(",", "(", ")"));
            sb.append(params);
        }
        return sb.toString();
    }

    private String toJava(TypeVariable typeVariable) {
        StringBuilder sb = new StringBuilder(typeVariable.asElement().getSimpleName().toString());
        TypeMirror upperBound = typeVariable.getUpperBound();
        String upperBoundString = toJava(upperBound);
        if (!"java.lang.Object".equals(upperBoundString)) {
            sb.append(" extends ").append(upperBoundString);
        }
        TypeMirror lowerBound = typeVariable.getLowerBound();
        if (lowerBound.getKind() != TypeKind.NULL) {
            sb.append(" super ").append(toJava(lowerBound));
        }
        return sb.toString();
    }

    private String toJava(UnionType unionType) {
        return unionType.getAlternatives().stream()
                .map(alt -> toJava(alt))
                .collect(Collectors.joining(" | "));
    }

    private String toJava(IntersectionType intersectionType) {
        return intersectionType.getBounds().stream()
                .map(bounds -> toJava(bounds))
                .collect(Collectors.joining(" & "));
    }

    private String toJava(Element element) {
        Element enclosing = element.getEnclosingElement();
        if (enclosing != null) {
            switch (enclosing.getKind()) {
                case PACKAGE:
                    return ((PackageElement) enclosing).getQualifiedName().toString() + '.' +
                            element.getSimpleName().toString();
                case ENUM:
                case CLASS:
                case INTERFACE:
                case ENUM_CONSTANT:
                case FIELD:
                    return toJava(enclosing) + '.' + element.getSimpleName();
            }
        }
        return element.getSimpleName().toString();
    }
}
