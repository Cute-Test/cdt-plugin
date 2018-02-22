package ch.hsr.ifs.mockator.plugin.linker;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.array;
import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.list;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;


// Implements Itanium C++ ABI name mangling according to
// http://sourcery.mentor.com/public/cxx-abi/abi.html#mangling
// that is used by the GNU Compiler Collection and others.
@SuppressWarnings("restriction")
public class ItaniumMangledNameGenerator {

   private final ICPPFunction        function;
   private final StringBuilder       mangledName;
   private final SubstitutionHistory history;

   public ItaniumMangledNameGenerator(final ICPPFunction function) {
      this.function = function;
      mangledName = new StringBuilder();
      history = new SubstitutionHistory();
   }

   public String createMangledName() {
      if (function.isExternC()) { return function.getName(); }

      mangledName();
      return mangledName.toString();
   }

   // <mangled-name> ::= _Z <encoding>
   private void mangledName() {
      mangledName.append("_Z");
      encoding();
   }

   // <encoding> ::= <function name> <bare-function-type>
   private void encoding() {
      functionName();
      bareFunctionType();
   }

   // <functionName> ::= <nested-name>
   // ::= <unscoped-name>
   // ::= <unscoped-template-name> <template-args>
   // ::= <local-name> # See Scope Encoding below
   private void functionName() {
      final String[] qualifiedName = getQualifiedFunctionName();

      if (qualifiedName.length > 1) {
         nestedName(qualifiedName);
      } else {
         unscopedName(qualifiedName[0]);
      }
   }

   // <bare-function-type> ::= <signature type>+
   private void bareFunctionType() {
      final ICPPParameter[] parameters = function.getParameters();

      if (parameters.length == 0) {
         type(getVoidType());
      } else {
         for (final ICPPParameter param : parameters) {
            type(param.getType());
         }
      }
   }

   private static ICPPBasicType getVoidType() {
      return new CPPBasicType(IBasicType.Kind.eVoid, 0);
   }

   // <name> ::= <nested-name>
   // ::= <unscoped-name>
   // ::= <unscoped-template-name> <template-args>
   // ::= <local-name> # See Scope Encoding below
   private void name(final IType type) {
      if (type instanceof ICPPTemplateInstance) {
         final ICPPTemplateInstance instance = (ICPPTemplateInstance) type;
         unscopedName(ASTUtil.getQfName(instance.getTemplateDefinition()));
         templateArgs(list(instance.getTemplateArguments()));
      } else if (type instanceof ICPPClassType) {
         final ICPPClassType classType = (ICPPClassType) type;
         final String[] qualifiedName = getQualifiedName(classType);

         if (qualifiedName.length == 1) {
            unscopedName(classType.getName());
         } else {
            nestedName(qualifiedName);
         }
      }
   }

   // <nested-name> ::= N [<CV-qualifiers>] <prefix> <unqualified-name> E
   // ::= N [<CV-qualifiers>] <template-prefix> <template-args> E
   private void nestedName(final String[] names) {
      mangledName.append("N");
      cvQualifiers(function.getType());
      boolean substituted = false;
      final String qfName = list(names).stream().collect(Collectors.joining("::"));

      if (isSubstitutionNecessary(qfName)) {
         substitution(qfName, null);
         substituted = true;
      }

      for (int i = 0; i < names.length; i++) {
         if (!substituted && isSubstitutionNecessary(names[i])) {
            substitution(names[i], null);
         } else {
            if (i < names.length - 1) {
               history.addToHistory(names[i]);
            }

            prefix(names[i]);
            unQualifiedName(names[i]);
         }
      }

      history.addToHistory(qfName);
      mangledName.append("E");
   }

   // <unscoped-name> ::= <unqualified-name>
   // ::= St <unqualified-name> # ::std::$
   private void unscopedName(String name) {
      if (name.startsWith("std::")) {
         mangledName.append("St");
         name = name.substring("std::".length());
      }

      unQualifiedName(name);
   }

   // <substitution> ::= S <seq-id> _
   // ::= S_
   // ::= St # ::std::
   // ::= Sa # ::std::allocator
   // ::= Sb # ::std::basic_string
   // ::= Ss # ::std::basic_string < char,
   // ::std::char_traits<char>,
   // ::std::allocator<char> >
   // ::= Si # ::std::basic_istream<char, std::char_traits<char> >
   // ::= So # ::std::basic_ostream<char, std::char_traits<char> >
   // ::= Sd # ::std::basic_iostream<char, std::char_traits<char> >
   private void substitution(final String typeStr, final IType type) {
      mangledName.append("S");

      if (typeStr.startsWith("std::basic_string<char") || typeStr.startsWith("std::allocator<char>") || typeStr.startsWith(
            "std::char_traits<char>")) {
         mangledName.append("s");
      } else if (typeStr.startsWith("std::allocator")) {
         mangledName.append("a");

         if (type instanceof ICPPTemplateInstance) {
            final ICPPTemplateInstance instance = (ICPPTemplateInstance) type;
            templateArgs(list(instance.getTemplateArguments()));
         }
      } else if (typeStr.equals("std::basic_string")) {
         mangledName.append("b");
      } else if (typeStr.startsWith("std::basic_istream<char")) {
         mangledName.append("i");
      } else if (typeStr.startsWith("std::basic_ostream<char")) {
         mangledName.append("o");
      } else if (typeStr.startsWith("std::basic_iostream<char")) {
         mangledName.append("d");
      } else if (typeStr.startsWith("std::")) {
         mangledName.append("t");
      } else {
         mangledName.append(history.getBase36Value(typeStr));
         mangledName.append("_");
      }
   }

   private boolean isSubstitutionNecessary(final String typeStr) {
      if (typeStr.startsWith("std::basic_string") || typeStr.startsWith("std::basic_ostream<char") || typeStr.startsWith("std::basic_istream<char") ||
          typeStr.startsWith("std::basic_iostream<char") || typeStr.startsWith("std::allocator")) { return true; }

      return history.alreadySeen(typeStr);
   }

   // <template-args> ::= I <template-arg>+ E
   private void templateArgs(final Collection<ICPPTemplateArgument> templateArgs) {
      ILTISException.Unless.isTrue(templateArgs.size() >= 1, "templateArgs should not be called with empty template arg list");
      mangledName.append("I");

      for (final ICPPTemplateArgument arg : templateArgs) {
         templateArg(arg);
      }

      mangledName.append("E");
   }

   // <template-arg> ::= <type> # type or template
   // ::= X <expression> E # expression
   // ::= <expr-primary> # simple expressions
   // ::= J <template-arg>* E # argument pack
   private void templateArg(final ICPPTemplateArgument templateArg) {
      if (templateArg.isTypeValue()) {
         final IType type = templateArg.getTypeValue();
         type(type);
      }
   }

   // <template-param> ::= T_ # first template parameter
   // ::= T <parameter-2 non-negative number> _
   private void templateParam(final ICPPTemplateParameter type) {
      // TODO
   }

   // <template-template-param> ::= <template-param>
   // ::= <substitution>
   private void templateTemplateParam(final ICPPTemplateParameter type) {
      templateParam(type);
   }

   // <prefix> ::= <prefix> <unqualified-name>
   // ::= <template-prefix> <template-args>
   // ::= <template-param>
   // ::= <decltype>
   // ::= # empty
   // ::= <substitution>
   // ::= <prefix> <data-member-prefix>
   private void prefix(final String type) {
      // TODO
   }

   // <unqualified-name> ::= <operator-name>
   // ::= <ctor-dtor-name>
   // ::= <source-name>
   // ::= <unnamed-type-name>
   private void unQualifiedName(final String name) {
      if (name.isEmpty()) { return; }

      if (isCtor() || isDtor()) {
         sourceName(name);
         ctorDtorName();
      } else if (isOperatorName(name)) {
         operatorName();
      } else {
         sourceName(name);
      }
   }

   private boolean isCtor() {
      return function instanceof ICPPConstructor;
   }

   private static boolean isOperatorName(final String name) {
      return name.startsWith("operator");
   }

   private boolean isDtor() {
      return function instanceof ICPPMethod && ((ICPPMethod) function).isDestructor();
   }

   // <ctor-dtor-name> ::= C1 # complete object constructor
   // ::= C2 # base object constructor
   // ::= C3 # complete object allocating constructor
   // ::= D0 # deleting destructor
   // ::= D1 # complete object destructor
   // ::= D2 # base object destructor
   private void ctorDtorName() {
      if (isCtor()) {
         mangledName.append("C1");
      } else if (isDtor()) {
         mangledName.append("D1");
      }
   }

   // <operator-name> ::= nw # new
   // ::= na # new[]
   // ::= dl # delete
   // ::= da # delete[]
   // ::= ps # + (unary)
   // ::= ng # - (unary)
   // ::= ad # & (unary)
   // ::= de # * (unary)
   // ::= co # ~
   // ::= pl # +
   // ::= mi # -
   // ::= ml # *
   // ::= dv # /
   // ::= rm # %
   // ::= an # &
   // ::= or # |
   // ::= eo # ^
   // ::= aS # =
   // ::= pL # +=
   // ::= mI # -=
   // ::= mL # *=
   // ::= dV # /=
   // ::= rM # %=
   // ::= aN # &=
   // ::= oR # |=
   // ::= eO # ^=
   // ::= ls # <<
   // ::= rs # >>
   // ::= lS # <<=
   // ::= rS # >>=
   // ::= eq # ==
   // ::= ne # !=
   // ::= lt # <
   // ::= gt # >
   // ::= le # <=
   // ::= ge # >=
   // ::= nt # !
   // ::= aa # &&
   // ::= oo # ||
   // ::= pp # ++ (postfix in <expression> context)
   // ::= mm # -- (postfix in <expression> context)
   // ::= cm # ,
   // ::= pm # ->*
   // ::= pt # ->
   // ::= cl # ()
   // ::= ix # []
   // ::= qu # ?
   // ::= st # sizeof (a type)
   // ::= sz # sizeof (an expression)
   // ::= at # alignof (a type)
   // ::= az # alignof (an expression)
   // ::= cv <type> # (cast)
   // ::= v <digit> <source-name> # vendor extended operator
   private void operatorName() {
      final String operatorName = function.getName().replaceAll("operator", "").trim();
      final ICPPParameter[] parameters = function.getParameters();

      if (operatorName.equals("new")) {
         mangledName.append("nw");
      } else if (operatorName.equals("new[]")) {
         mangledName.append("na");
      } else if (operatorName.equals("delete")) {
         mangledName.append("dl");
      } else if (operatorName.equals("delete[]")) {
         mangledName.append("da");
      } else if (operatorName.equals("+") && parameters.length == 0) {
         mangledName.append("ps");
      } else if (operatorName.equals("-") && parameters.length == 0) {
         mangledName.append("ng");
      } else if (operatorName.equals("&") && parameters.length == 0) {
         mangledName.append("ad");
      } else if (operatorName.equals("*") && parameters.length == 0) {
         mangledName.append("de");
      } else if (operatorName.equals("+")) {
         mangledName.append("pl");
      } else if (operatorName.equals("-")) {
         mangledName.append("mi");
      } else if (operatorName.equals("*")) {
         mangledName.append("ml");
      } else if (operatorName.equals("/")) {
         mangledName.append("dv");
      } else if (operatorName.equals("%")) {
         mangledName.append("rm");
      } else if (operatorName.equals("&")) {
         mangledName.append("an");
      } else if (operatorName.equals("|")) {
         mangledName.append("or");
      } else if (operatorName.equals("^")) {
         mangledName.append("eo");
      } else if (operatorName.equals("=")) {
         mangledName.append("aS");
      } else if (operatorName.equals("+=")) {
         mangledName.append("pL");
      } else if (operatorName.equals("-=")) {
         mangledName.append("mI");
      } else if (operatorName.equals("*=")) {
         mangledName.append("mL");
      } else if (operatorName.equals("/=")) {
         mangledName.append("dV");
      } else if (operatorName.equals("%=")) {
         mangledName.append("rM");
      } else if (operatorName.equals("&=")) {
         mangledName.append("aN");
      } else if (operatorName.equals("|=")) {
         mangledName.append("oR");
      } else if (operatorName.equals("^=")) {
         mangledName.append("eO");
      } else if (operatorName.equals("<<")) {
         mangledName.append("ls");
      } else if (operatorName.equals(">>")) {
         mangledName.append("rs");
      } else if (operatorName.equals("<<=")) {
         mangledName.append("lS");
      } else if (operatorName.equals(">>=")) {
         mangledName.append("rS");
      } else if (operatorName.equals("==")) {
         mangledName.append("eq");
      } else if (operatorName.equals("!=")) {
         mangledName.append("ne");
      } else if (operatorName.equals("<")) {
         mangledName.append("lt");
      } else if (operatorName.equals(">")) {
         mangledName.append("gt");
      } else if (operatorName.equals("<=")) {
         mangledName.append("le");
      } else if (operatorName.equals(">=")) {
         mangledName.append("ge");
      } else if (operatorName.equals("!")) {
         mangledName.append("nt");
      } else if (operatorName.equals("&&")) {
         mangledName.append("aa");
      } else if (operatorName.equals("||")) {
         mangledName.append("oo");
      } else if (operatorName.equals("++")) {
         mangledName.append("pp");
      } else if (operatorName.equals("--")) {
         mangledName.append("mm");
      } else if (operatorName.equals(",")) {
         mangledName.append("cm");
      } else if (operatorName.equals("->*")) {
         mangledName.append("pm");
      } else if (operatorName.equals("->")) {
         mangledName.append("pt");
      } else if (operatorName.equals("()")) {
         mangledName.append("cl");
      } else if (operatorName.equals("[]")) {
         mangledName.append("ix");
      } else if (operatorName.equals("?")) {
         mangledName.append("qu");
      } else if (operatorName.equals("sizeof")) {
         mangledName.append("st");
      } else if (operatorName.equals("[]")) {
         mangledName.append("ix");
      }
   }

   // <source-name> ::= <positive length number> <identifier>
   private void sourceName(final String name) {
      number(name.length(), mangledName);
      identifier(name);
   }

   // <number> ::= [n] <non-negative decimal integer>
   private static void number(final int n, final StringBuilder mangledName) {
      if (n < 0) {
         mangledName.append("n");
      }
      mangledName.append(n);
   }

   // <identifier> ::= <unqualified source code identifier>
   private void identifier(final String name) {
      mangledName.append(name);
   }

   // <CV-qualifiers> ::= [r] [V] [K] # restrict (C99), volatile, const
   private void cvQualifiers(final IType type) {
      boolean isVolatile = false;
      boolean isConst = false;

      if (type instanceof ICPPFunctionType) {
         isVolatile = ((ICPPFunctionType) type).isVolatile();
         isConst = ((ICPPFunctionType) type).isConst();
      } else if (type instanceof IQualifierType) {
         isVolatile = ((IQualifierType) type).isVolatile();
         isConst = ((IQualifierType) type).isConst();
      }

      if (isVolatile) {
         mangledName.append("V");
      }

      if (isConst) {
         mangledName.append("K");
      }
   }

   // <type> ::= P <type> # pointer-to
   // ::= R <type> # reference-to
   // ::= O <type> # rvalue reference-to (C++0x)
   // ::= C <type> # complex pair (C 2000)
   // ::= G <type> # imaginary (C 2000)
   // ::= U <source-name> <type> # vendor extended type qualifier
   // ::= Dp <type> # pack expansion (C++0x)
   // ::= <CV-qualifiers> <type>
   // ::= <builtin-type>
   // ::= <function-type>
   // ::= <class-enum-type>
   // ::= <array-type>
   // ::= <pointer-to-member-type>
   // ::= <template-param>
   // ::= <template-template-param> <template-args>
   // ::= <decltype>
   // ::= <substitution>
   private void type(IType type) {
      if (type instanceof ITypedef) {
         type = ((ITypedef) type).getType();
      }

      if (type instanceof ICPPParameterPackType) {
         mangledName.append("Dp");
         type(((ICPPParameterPackType) type).getType());
      } else if (type instanceof IPointerType) {
         mangledName.append("P");
         type(((IPointerType) type).getType());
      } else if (type instanceof ICPPReferenceType) {
         final ICPPReferenceType refType = (ICPPReferenceType) type;

         if (refType.isRValueReference()) {
            mangledName.append("O");
         } else {
            mangledName.append("R");
         }

         type(((ICPPReferenceType) type).getType());
      } else if (type instanceof IQualifierType) {
         final IQualifierType qualifiedType = (IQualifierType) type;
         cvQualifiers(qualifiedType);
         type(qualifiedType.getType());
      } else if (type instanceof IBasicType) {
         final IBasicType basicType = (IBasicType) type;

         if (basicType.isComplex()) {
            mangledName.append("C");
         } else if (basicType.isImaginary()) {
            mangledName.append("G");
         }

         builtinType(basicType);
      } else if (isClassOrEnumType(type)) {
         final String typeStr = ASTTypeUtil.getType(type);

         if (isSubstitutionNecessary(typeStr)) {
            substitution(typeStr, type);
         } else {
            if (!typeStr.contains("::")) {
               history.addToHistory(typeStr);
            }
            classEnumType(type);
         }
      } else if (type instanceof IArrayType) {
         arrayType((IArrayType) type);
      } else if (type instanceof ICPPPointerToMemberType) {
         pointerToMemberType(type);
      } else if (type instanceof ICPPTemplateTemplateParameter) {
         templateTemplateParam((ICPPTemplateTemplateParameter) type);
      } else if (type instanceof ICPPTemplateParameter) {
         templateParam((ICPPTemplateParameter) type);
      }
   }

   private static boolean isClassOrEnumType(final IType type) {
      return type instanceof IEnumeration || type instanceof ICompositeType;
   }

   // <class-enum-type> ::= <name>
   private void classEnumType(final IType type) {
      name(type);
   }

   // <array-type> ::= A <positive dimension number> _ <element type>
   // ::= A [<dimension expression>] _ <element type>
   private void arrayType(final IArrayType type) {
      mangledName.append("A");
      mangledName.append(type.getSize());
      type(type);
   }

   // <pointer-to-member-type> ::= M <class type> <member type>
   private void pointerToMemberType(final IType type) {}

   // <builtin-type> ::= v # void
   // ::= w # wchar_t
   // ::= b # bool
   // ::= c # char
   // ::= a # signed char
   // ::= h # unsigned char
   // ::= s # short
   // ::= t # unsigned short
   // ::= i # int
   // ::= j # unsigned int
   // ::= l # long
   // ::= m # unsigned long
   // ::= x # long long, __int64
   // ::= y # unsigned long long, __int64
   // ::= n # __int128
   // ::= o # unsigned __int128
   // ::= f # float
   // ::= d # double
   // ::= e # long double, __float80
   // ::= g # __float128
   // ::= z # ellipsis
   // ::= Dd # IEEE 754r decimal floating point (64 bits)
   // ::= De # IEEE 754r decimal floating point (128 bits)
   // ::= Df # IEEE 754r decimal floating point (32 bits)
   // ::= Dh # IEEE 754r half-precision floating point (16 bits)
   // ::= Di # char32_t
   // ::= Ds # char16_t
   // ::= Da # auto (in dependent new-expressions)
   // ::= Dn # std::nullptr_t (i.e., decltype(nullptr))
   // ::= u <source-name> # vendor extended type
   private void builtinType(final IBasicType type) {
      switch (type.getKind()) {
      case eVoid:
         mangledName.append("v");
         break;
      case eChar:
         if (type.isUnsigned()) {
            mangledName.append("h");
         } else if (type.isSigned()) {
            mangledName.append("a");
         } else {
            mangledName.append("c");
         }
         break;
      case eFloat:
         mangledName.append("f");
         break;
      case eDouble:
         mangledName.append("d");
         break;
      case eBoolean:
         mangledName.append("b");
         break;
      case eInt:
         if (type.isShort()) {
            if (type.isUnsigned()) {
               mangledName.append("t");
            } else {
               mangledName.append("s");
            }
         } else if (type.isLong()) {
            if (type.isUnsigned()) {
               mangledName.append("m");
            } else {
               mangledName.append("l");
            }
         } else if (type.isUnsigned()) {
            mangledName.append("j");
         } else if (type.isLongLong()) {
            mangledName.append("x");
         } else {
            mangledName.append("i");
         }
         break;
      case eChar16:
         mangledName.append("Ds");
         break;
      case eChar32:
         mangledName.append("Di");
         break;
      case eWChar:
         mangledName.append("w");
         break;
      case eUnspecified:
         break;
      default:
         throw new ILTISException("Unexpected type found").rethrowUnchecked();
      }
   }

   private String[] getQualifiedFunctionName() {
      final String[] qfName = getQualifiedName(function);

      if (isCtor() || isDtor()) { return array(qfName[0], ""); }

      return qfName;
   }

   private static class SubstitutionHistory {

      private final Map<String, Integer> history;
      private int                        seqId;

      SubstitutionHistory() {
         history = new HashMap<>();
      }

      void addToHistory(final String type) {
         if (!alreadySeen(type)) {
            history.put(type, seqId++);
         }
      }

      String getBase36Value(final String type) {
         final Integer seqId = history.get(type);
         ILTISException.Unless.notNull(seqId, "Type must have occurred once already");

         // <substitution> ::= S <seq-id> _
         // ::= S_
         switch (seqId) {
         case 0:
            return ""; // The first repetition yields an empty string after S_
         default:
            final int n = seqId - 1;
            ILTISException.Unless.isTrue(n < 36, "Value not in range for base36 conversion");
            return Integer.toString(n, 36).toUpperCase();
         }
      }

      boolean alreadySeen(final String type) {
         return history.containsKey(type);
      }
   }

   private static String[] getQualifiedName(final ICPPBinding binding) {
      try {
         return binding.getQualifiedName();
      } catch (final DOMException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }
}
