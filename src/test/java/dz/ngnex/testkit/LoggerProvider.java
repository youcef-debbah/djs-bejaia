package dz.ngnex.testkit;

import dz.ngnex.util.InjectableByTests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

@InjectableByTests
public final class LoggerProvider {

  @Produces
  @Dependent
  public Logger getLogger(final InjectionPoint injectionPoint) {
    return LoggerFactory.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
  }
}