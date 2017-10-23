package com.wyj.core.util;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wyj on 17-10-23.
 */
public class ReflexUtils {

	private static final String CGLIB_RENAMED_METHOD_PREFIX = "CGLIB$";

	private static final Field[] NO_FIELDS = {};

	private static final Method[] NO_METHODS = {};

	private static final float DEFAULT_LOAD_FACTOR = 0.75f;

	private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

	private static final int DEFAULT_INITIAL_CAPACITY = 256;

	private static final Map<Class<?>, Field[]> declaredFieldsCache =
			new ConcurrentHashMap<>(DEFAULT_INITIAL_CAPACITY,DEFAULT_LOAD_FACTOR,
					DEFAULT_CONCURRENCY_LEVEL);

	private static final Map<Class<?>, Method[]> declaredMethodsCache =
			new ConcurrentHashMap<>(DEFAULT_INITIAL_CAPACITY,DEFAULT_LOAD_FACTOR,
					DEFAULT_CONCURRENCY_LEVEL);

	public static Field[] getAllField(Class<?> clazz) {
		Assert.notNull(clazz);
		List<Field> list = new ArrayList<>(32);
		Class<?> searchType = clazz;
		while (Object.class != searchType && searchType != null) {
			Field[] fields = getDeclaredFields(searchType);
			list.addAll(Arrays.asList(fields));
			searchType = searchType.getSuperclass();
		}
		return list.toArray(new Field[list.size()]);
	}

	public static Optional<Field> findField(Class<?> clazz, String name) {
		return findField(clazz, name, null);
	}

	/**
	 * 查询范围:当前类以及父类
	 * 字段的访问类型: private, protected, public, default access
	 * @param clazz 类
	 * @param name 字段名称
	 * @param type 字段类型
	 * @return 根据字段名称以及字段类型从类中查询匹配的字段,
	 * 假设字段名称和字段类型有一个为空,那么可能会匹配多个字段, 只会返回第一个匹配的字段
	 */
	public static Optional<Field> findField(Class<?> clazz, String name, Class<?> type) {
		Assert.notNull(clazz);
		Assert.isTrue(name != null || type != null,
				"Either name or type of the field must be specified");

		Class<?> searchType = clazz;
		while (Object.class != searchType && searchType != null) {
			Field[] fields = getDeclaredFields(searchType);
			for (Field field : fields) {
				if ((name == null || name.equals(field.getName())) &&
						(type == null || type.equals(field.getType()))) {
					return Optional.of(field);
				}
			}
			searchType = searchType.getSuperclass();
		}
		return Optional.empty();
	}

	private static Field[] getDeclaredFields(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		Field[] result = declaredFieldsCache.get(clazz);
		if (result == null) {
			result = clazz.getDeclaredFields();
			declaredFieldsCache.put(clazz, (result.length == 0 ? NO_FIELDS : result));
		}
		return result;
	}

	public static void setField(Field field, Object target, Object value) {
		Assert.notNull(field);
		Assert.notNull(target);
		try {
			field.set(target, value);
		} catch (IllegalAccessException ex) {
			handleReflectionException(ex);
			throw new IllegalStateException(
					"Unexpected reflection exception - " + ex.getClass().getName() + ": " + ex.getMessage());
		}
	}


	public static Object invokeMethod(Method method, Object target) {
		Assert.notNull(method);
		Assert.notNull(target);
		return invokeMethod(method, target, new Object[0]);
	}

	public static Object invokeMethod(Method method, Object target, Object... args) {
		Assert.notNull(method);
		Assert.notNull(target);
		if (args == null) {
			args = new Object[0];
		}
		try {
			return method.invoke(target, args);
		}
		catch (Exception ex) {
			handleReflectionException(ex);
		}
		throw new IllegalStateException("Should never get here");
	}

	public static Method[] getUniqueSetMethod(Class<?> clazz) {
		Assert.notNull(clazz);
		List<Method> list = new ArrayList<>(32);

		// 获取当前这个类中的所有方法
		Method[] uniqueDeclaredMethods = getUniqueDeclaredMethods(clazz);
		for (Method method : uniqueDeclaredMethods) {
			String methodName = method.getName();

			//判断是否是set方法
			if (methodName.length() > 3 &&
					methodName.startsWith("set") &&
					method.getReturnType().equals(Void.class) &&
					method.getParameterCount() == 1 &&
					Modifier.isPublic(method.getModifiers())) {

				String fieldName = getFieldNameByMethodName(methodName, 3);
				Optional<Field> fieldOptional = findField(clazz, fieldName);
				// 判断是否存在这个字段
				if (!fieldOptional.isPresent()) {
					continue;
				}
				Field field = fieldOptional.get();
				// 判断method的参数类型是否和字段的类型相同
				if (method.getParameterTypes()[0].equals(field.getType())) {
					list.add(method);
				}
			}
		}
		return list.toArray(new Method[list.size()]);
	}

	public static Method[] getUniqueGetMethod(Class<?> clazz) {
		Assert.notNull(clazz);
		List<Method> list = new ArrayList<>(32);

		// 获取当前这个类中的所有方法
		Method[] uniqueDeclaredMethods = getUniqueDeclaredMethods(clazz);
		for (Method method : uniqueDeclaredMethods) {
			String methodName = method.getName();

			//判断是否是get方法
			if (methodName.length() > 3 &&
					methodName.startsWith("get") &&
					method.getParameterCount() == 0 &&
					Modifier.isPublic(method.getModifiers())) {
				String fieldName = getFieldNameByMethodName(methodName, 3);
				Optional<Field> fieldOptional = findField(clazz, fieldName);
				// 判断是否存在这个字段
				if (!fieldOptional.isPresent()) {
					continue;
				}
				Field field = fieldOptional.get();
				// 判断method的返回类型是否和字段的类型相同,或者是字段类型的子类
				if (method.getReturnType().isAssignableFrom(field.getType())) {
					list.add(method);
				}
			}
		}
		return list.toArray(new Method[list.size()]);
	}

	protected static String getFieldNameByMethodName(String methodName, int beginIndex) {
		Assert.isTrue(methodName.length() > beginIndex, "开始下标越界");

		String fieldName = methodName.substring(beginIndex, beginIndex + 1).toLowerCase();
		if (methodName.length() > beginIndex + 1) {
			fieldName += methodName.substring(beginIndex + 1);
		}
		return fieldName;
	}

	public static Method[] getAllDeclaredMethods(Class<?> leafClass) {
		final List<Method> methods = new ArrayList<>(32);
		doWithMethods(leafClass, method -> methods.add(method));

		return methods.toArray(new Method[methods.size()]);
	}

	public static Method[] getUniqueDeclaredMethods(Class<?> leafClass) {
		final List<Method> methods = new ArrayList<>(32);
		doWithMethods(leafClass, method -> {

			boolean knownSignature = false;
			Method methodBeingOverriddenWithCovariantReturnType = null;

			for (Method existingMethod : methods) {

				if (method.getName().equals(existingMethod.getName()) &&
						Arrays.equals(method.getParameterTypes(), existingMethod.getParameterTypes())) {

					// Is this a covariant return type situation?
					if (existingMethod.getReturnType() != method.getReturnType() &&
							existingMethod.getReturnType().equals(method.getReturnType())) {
						methodBeingOverriddenWithCovariantReturnType = existingMethod;
					}
					else {
						knownSignature = true;
					}
					break;
				}
			}
			if (methodBeingOverriddenWithCovariantReturnType != null) {
				methods.remove(methodBeingOverriddenWithCovariantReturnType);
			}
			if (!knownSignature && !isCglibRenamedMethod(method)) {
				methods.add(method);
			}
		});
		return methods.toArray(new Method[methods.size()]);
	}

	private static void doWithMethods(Class<?> clazz, MethodCallback mc) {
		doWithMethods(clazz, mc, null);
	}

	private static void doWithMethods(Class<?> clazz, MethodCallback mc, MethodFilter mf) {
		// Keep backing up the inheritance hierarchy.
		Method[] methods = getDeclaredMethods(clazz);
		for (Method method : methods) {
			if (mf != null && !mf.matches(method)) {
				continue;
			}
			try {
				mc.doWith(method);
			}
			catch (IllegalAccessException ex) {
				throw new IllegalStateException("Not allowed to access method '" + method.getName() + "': " + ex);
			}
		}
		if (clazz.getSuperclass() != null) {
			doWithMethods(clazz.getSuperclass(), mc, mf);
		}
		else if (clazz.isInterface()) {
			for (Class<?> superIfc : clazz.getInterfaces()) {
				doWithMethods(superIfc, mc, mf);
			}
		}
	}

	private static boolean isCglibRenamedMethod(Method renamedMethod) {
		String name = renamedMethod.getName();
		if (name.startsWith(CGLIB_RENAMED_METHOD_PREFIX)) {
			int i = name.length() - 1;
			while (i >= 0 && Character.isDigit(name.charAt(i))) {
				i--;
			}
			return ((i > CGLIB_RENAMED_METHOD_PREFIX.length()) &&
					(i < name.length() - 1) && name.charAt(i) == '$');
		}
		return false;
	}

	private static Method[] getDeclaredMethods(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");

		Method[] result = declaredMethodsCache.get(clazz);

		if (result == null) {
			Method[] declaredMethods = clazz.getDeclaredMethods();

			List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);

			if (defaultMethods != null) {
				result = new Method[declaredMethods.length + defaultMethods.size()];
				System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
				int index = declaredMethods.length;
				for (Method defaultMethod : defaultMethods) {
					result[index++] = defaultMethod;
				}
			}
			else {
				result = declaredMethods;
			}
			declaredMethodsCache.put(clazz, (result.length == 0 ? NO_METHODS : result));
		}
		return result;
	}

	private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
		List<Method> result = null;
		for (Class<?> ifc : clazz.getInterfaces()) {
			for (Method ifcMethod : ifc.getMethods()) {
				if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
					if (result == null) {
						result = new LinkedList<>();
					}
					result.add(ifcMethod);
				}
			}
		}
		return result;
	}

	public interface MethodCallback {

		void doWith(Method method) throws IllegalArgumentException, IllegalAccessException;
	}

	public interface MethodFilter {

		boolean matches(Method method);
	}


	private static void handleReflectionException(Exception ex) {
		if (ex instanceof NoSuchMethodException) {
			throw new IllegalStateException("Method not found: " + ex.getMessage());
		}
		if (ex instanceof IllegalAccessException) {
			throw new IllegalStateException("Could not access method: " + ex.getMessage());
		}
		if (ex instanceof InvocationTargetException) {
			handleInvocationTargetException((InvocationTargetException) ex);
		}
		if (ex instanceof RuntimeException) {
			throw (RuntimeException) ex;
		}
		throw new UndeclaredThrowableException(ex);
	}

	private static void handleInvocationTargetException(InvocationTargetException ex) {
		rethrowRuntimeException(ex.getTargetException());
	}

	private static void rethrowRuntimeException(Throwable ex) {
		if (ex instanceof RuntimeException) {
			throw (RuntimeException) ex;
		}
		if (ex instanceof Error) {
			throw (Error) ex;
		}
		throw new UndeclaredThrowableException(ex);
	}
}
