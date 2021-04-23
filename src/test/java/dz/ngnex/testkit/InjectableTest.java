package dz.ngnex.testkit;

import dz.ngnex.util.InjectableByTests;
import dz.ngnex.util.TestWithTransaction;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.junit4.WeldInitiator;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.reflections.Reflections;

import javax.ejb.Stateless;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.*;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

public class InjectableTest extends SimpleTest {

  private static final String DZ_NGNEX = "dz.ngnex";

  public static final Set<Class<?>> INJECTABLE_BEANS = new Reflections(DZ_NGNEX)
      .getTypesAnnotatedWith(InjectableByTests.class, true);

  public static final Set<Class<?>> TRANSACTIONAL_BEANS = new Reflections(DZ_NGNEX)
      .getTypesAnnotatedWith(TestWithTransaction.class, true);

  public static final Set<Class<?>> STATELESS_EJB = new Reflections(DZ_NGNEX)
      .getTypesAnnotatedWith(Stateless.class, true);

  @Rule
  public WeldInitiator weld = WeldInitiator.from(managedTestBeans())
      .setEjbFactory(new SimpleEjbFactory())
      .inject(this)
      .activate(RequestScoped.class, SessionScoped.class)
      .build();

  private static Weld managedTestBeans() {
    Weld weld = WeldInitiator.createWeld();
    for (Class<?> bean : INJECTABLE_BEANS)
      weld.addBeanClass(bean);

    for (Class<?> bean : TRANSACTIONAL_BEANS)
      weld.addBeanClass(bean);

    return weld;
  }

  private final class SimpleEjbFactory implements Function<InjectionPoint, Object> {

    private final Deque<Class<?>> typesBeingCreated = new LinkedList<>();
    private final Map<Class<?>, Deque<Field>> deferredInjections = new HashMap<>();
    private final Map<Class<?>, Deque<Object>> cache = new HashMap<>();

    @Override
    public Object apply(InjectionPoint injectionPoint) {
      Member member = injectionPoint.getMember();
      Class<?> ejbClass = findStatelessEjbClass(getBaseClass(member));
      if (typesBeingCreated.contains(ejbClass)) {
        addToDeferredInjections(ejbClass, injectionPoint);
        return null;
      } else {
        Object ejbInstance;

        if (typesBeingCreated.isEmpty())
          reset();

        typesBeingCreated.push(ejbClass);
        try {
          ejbInstance = produceEjbInstance(ejbClass);
        } finally {
          typesBeingCreated.pop();
        }

        addToCache(ejbClass, ejbInstance);

        if (typesBeingCreated.isEmpty())
          handleDeferredInjections();

        return ejbInstance;
      }
    }

    private void reset() {
      deferredInjections.clear();
      cache.clear();
    }

    private void addToDeferredInjections(Class<?> ejbClass, InjectionPoint injectionPoint) {
      Member member = injectionPoint.getMember();
      if (member instanceof Field) {
        Field field = (Field) member;
        Collection<Field> injectionPoints = deferredInjections.get(ejbClass);
        if (injectionPoints != null)
          injectionPoints.add(field);
        else {
          Deque<Field> newInjectionPoints = new LinkedList<>();
          newInjectionPoints.add(field);
          deferredInjections.put(ejbClass, newInjectionPoints);
        }
      } else
        throw new AssertionError("circular dependency can not be solved because " +
            "deferred EJB injection is only supported for fields ("
            + member.getDeclaringClass().getSimpleName() + "." + member.getName() + ")");
    }

    private void addToCache(Class<?> ejbClass, Object ejbInstance) {
      Collection<Object> cachedInstances = cache.get(ejbClass);
      if (cachedInstances != null) {
        cachedInstances.add(ejbInstance);
      } else {
        LinkedList<Object> newCachedInstances = new LinkedList<>();
        newCachedInstances.add(ejbInstance);
        cache.put(ejbClass, newCachedInstances);
      }
    }

    private void handleDeferredInjections() {
      for (Entry<Class<?>, Deque<Field>> injectionPoints : deferredInjections.entrySet()) {
        Class<?> ejbClass = injectionPoints.getKey();
        for (Field injectionPoint : injectionPoints.getValue()) {
          for (Object injectionTarget : getInjectionTargets(injectionPoint))
            inject(injectionTarget, injectionPoint, getEjbInstance(ejbClass), ejbClass);
        }
      }
    }

    private Object getEjbInstance(Class<?> ejbClass) {
      Deque<Object> instances = cache.get(ejbClass);
      if (instances == null || instances.isEmpty())
        throw new RuntimeException("no EJB could be retrieved for deferred injection of type: "
            + ejbClass.getName() + " all available EJBs are: " + cache);

      return instances.peekFirst();
    }

    @NotNull
    private Collection<Object> getInjectionTargets(Field member) {
      Class<?> targetClass = member.getDeclaringClass();
      String memberName = targetClass.getSimpleName() + "." + member.getName();

      Collection<Object> injectionTargets = cache.get(targetClass);

      if (injectionTargets == null || injectionTargets.isEmpty())
        throw new RuntimeException("no injection target found for deferred injection: " + memberName);
      return injectionTargets;
    }

    private void inject(Object injectionTarget, Field member, Object ejbInstance, Class<?> ejbClass) {
      String memberName = member.getDeclaringClass().getSimpleName() + "." + member.getName();
      try {
        member.setAccessible(true);
        member.set(injectionTarget, ejbInstance);
      } catch (RuntimeException | IllegalAccessException e) {
        throw new RuntimeException("could not inject field: " + memberName + " on target: " + injectionTarget
            + " with EJB instance: " + ejbInstance + " (" + ejbClass.getName() + ")", e);
      }
    }

    private <T> T produceEjbInstance(Class<T> ejbClass) {
      try {
        BeanManager beanManager = weld.getBeanManager();

        AnnotatedType<T> declaringType = beanManager.createAnnotatedType(ejbClass);
        InjectionTargetFactory<T> injectionTargetFactory = beanManager.getInjectionTargetFactory(declaringType);
        InjectionTarget<T> injectionTarget = injectionTargetFactory.createInjectionTarget(null);

        CreationalContext<T> context = beanManager.createCreationalContext(null);

        T instance = injectionTarget.produce(context);
        injectionTarget.inject(instance, context);
        injectionTarget.postConstruct(instance);

        return instance;
      } catch (RuntimeException e) {
        throw new RuntimeException("could not produce EJB of class: " + ejbClass.getName(), e);
      }
    }

    @NotNull
    private Class<?> findStatelessEjbClass(Class<?> type) {
      if (!type.isInterface())
        return type;

      Class<?> implementation = null;

      for (Class<?> ejbType : STATELESS_EJB)
        if (type.isAssignableFrom(ejbType))
          if (implementation == null)
            implementation = ejbType;
          else
            throw new RuntimeException("multiple EJB implementation found for interface: " + type.getName()
                + " (" + implementation.getName() + " and " + ejbType.getName() + ")");

      if (implementation != null)
        return implementation;
      else
        throw new RuntimeException("no Stateless EJB implementation found for interface: " + type.getName());
    }

    private Class<?> getBaseClass(Member member) {
      if (member instanceof Field)
        return ((Field) member).getType();
      else
        throw new RuntimeException("injection member not supported: " + member);
    }
  }
}
